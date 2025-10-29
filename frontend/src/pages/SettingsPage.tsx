import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useTenant } from '../contexts/TenantContext';
import { userService, User, UserRole, InviteUserRequest } from '../services/userService';
import MessageBanner from '../components/settings/MessageBanner';
import InvitationForm from '../components/settings/InvitationForm';
import UserTable from '../components/settings/UserTable';

/**
 * Settings page component - Team management and user invitations.
 * PMAT Thresholds: Cyc≤15, Cog≤30, LOC≤400
 */
const SettingsPage: React.FC = () => {
  const { user: currentUser } = useAuth();
  const { tenant: currentTenant } = useTenant();

  // State
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Invitation form state
  const [inviteEmail, setInviteEmail] = useState('');
  const [inviteRole, setInviteRole] = useState<UserRole>('VIEWER');
  const [inviteMessage, setInviteMessage] = useState('');
  const [inviting, setInviting] = useState(false);

  // Load users
  useEffect(() => {
    if (currentTenant?.id) {
      loadUsers();
    }
  }, [currentTenant?.id]);

  const loadUsers = async () => {
    if (!currentTenant?.id) return;

    try {
      setLoading(true);
      setError(null);
      const data = await userService.listUsers(currentTenant.id);
      setUsers(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load team members');
    } finally {
      setLoading(false);
    }
  };

  const handleInvite = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentTenant?.id) return;

    setInviting(true);
    setError(null);
    setSuccess(null);

    try {
      const request: InviteUserRequest = {
        email: inviteEmail,
        role: inviteRole,
        message: inviteMessage || undefined,
      };

      const response = await userService.inviteUser(currentTenant.id, request);

      setSuccess(
        response.emailSent
          ? `Invitation sent to ${inviteEmail}`
          : `User ${inviteEmail} added to team (email not sent)`
      );

      // Reset form
      setInviteEmail('');
      setInviteRole('VIEWER');
      setInviteMessage('');

      // Reload users
      loadUsers();
    } catch (err: any) {
      if (err.response?.status === 403) {
        setError('You do not have permission to invite users. Only administrators can invite users.');
      } else {
        setError(err.response?.data?.message || 'Failed to send invitation');
      }
    } finally {
      setInviting(false);
    }
  };

  const handleRemove = async (userId: string, userEmail: string) => {
    if (!currentTenant?.id) return;
    if (!window.confirm(`Are you sure you want to remove ${userEmail} from the team?`)) return;

    setError(null);
    setSuccess(null);

    try {
      await userService.removeUser(currentTenant.id, userId);
      setSuccess(`${userEmail} has been removed from the team`);
      loadUsers();
    } catch (err: any) {
      if (err.response?.status === 403) {
        setError('You do not have permission to remove users. Only administrators can remove users.');
      } else {
        setError(err.response?.data?.message || 'Failed to remove user');
      }
    }
  };

  const containerStyle: React.CSSProperties = {
    padding: '24px',
    maxWidth: '1200px',
    margin: '0 auto',
  };

  const headerStyle: React.CSSProperties = {
    fontSize: '28px',
    fontWeight: 'bold',
    color: '#1f2937',
    marginBottom: '24px',
  };

  const sectionStyle: React.CSSProperties = {
    backgroundColor: 'white',
    borderRadius: '8px',
    padding: '24px',
    boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
    marginBottom: '24px',
  };

  const sectionTitleStyle: React.CSSProperties = {
    fontSize: '20px',
    fontWeight: '600',
    color: '#1f2937',
    marginBottom: '16px',
  };

  return (
    <div style={containerStyle}>
      <h1 style={headerStyle}>Settings</h1>

      {error && <MessageBanner message={error} type="error" onClose={() => setError(null)} />}

      {success && <MessageBanner message={success} type="success" onClose={() => setSuccess(null)} />}

      {/* Invite User Section */}
      <div style={sectionStyle}>
        <h2 style={sectionTitleStyle}>Invite Team Member</h2>
        <InvitationForm
          email={inviteEmail}
          role={inviteRole}
          message={inviteMessage}
          inviting={inviting}
          onSubmit={handleInvite}
          onEmailChange={setInviteEmail}
          onRoleChange={setInviteRole}
          onMessageChange={setInviteMessage}
        />
      </div>

      {/* Team Members Section */}
      <div style={sectionStyle}>
        <h2 style={sectionTitleStyle}>Team Members</h2>
        <UserTable
          users={users}
          loading={loading}
          currentUserId={currentUser?.id}
          onRemove={handleRemove}
        />
      </div>
    </div>
  );
};

export default SettingsPage;
