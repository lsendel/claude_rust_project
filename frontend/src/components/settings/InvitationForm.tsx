import React from 'react';
import { UserRole } from '../../services/userService';

interface InvitationFormProps {
  email: string;
  role: UserRole;
  message: string;
  inviting: boolean;
  onSubmit: (e: React.FormEvent) => void;
  onEmailChange: (value: string) => void;
  onRoleChange: (value: UserRole) => void;
  onMessageChange: (value: string) => void;
}

/**
 * Invitation form component for inviting new team members.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const InvitationForm: React.FC<InvitationFormProps> = ({
  email,
  role,
  message,
  inviting,
  onSubmit,
  onEmailChange,
  onRoleChange,
  onMessageChange,
}) => {
  const formGroupStyle: React.CSSProperties = {
    marginBottom: '16px',
  };

  const labelStyle: React.CSSProperties = {
    display: 'block',
    marginBottom: '6px',
    fontSize: '14px',
    fontWeight: '500',
    color: '#374151',
  };

  const inputStyle: React.CSSProperties = {
    width: '100%',
    padding: '8px 12px',
    border: '1px solid #d1d5db',
    borderRadius: '6px',
    fontSize: '14px',
  };

  const textareaStyle: React.CSSProperties = {
    ...inputStyle,
    minHeight: '80px',
    resize: 'vertical' as const,
  };

  const buttonStyle: React.CSSProperties = {
    padding: '10px 20px',
    backgroundColor: '#2563eb',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: '500',
  };

  return (
    <form onSubmit={onSubmit}>
      <div style={formGroupStyle}>
        <label style={labelStyle} htmlFor="inviteEmail">
          Email Address *
        </label>
        <input
          id="inviteEmail"
          type="email"
          style={inputStyle}
          value={email}
          onChange={(e) => onEmailChange(e.target.value)}
          required
          placeholder="user@example.com"
        />
      </div>

      <div style={formGroupStyle}>
        <label style={labelStyle} htmlFor="inviteRole">
          Role *
        </label>
        <select
          id="inviteRole"
          style={inputStyle}
          value={role}
          onChange={(e) => onRoleChange(e.target.value as UserRole)}
        >
          <option value="VIEWER">Viewer - Read-only access</option>
          <option value="EDITOR">Editor - Can create and modify</option>
          <option value="ADMINISTRATOR">Administrator - Full access</option>
        </select>
      </div>

      <div style={formGroupStyle}>
        <label style={labelStyle} htmlFor="inviteMessage">
          Custom Message (Optional)
        </label>
        <textarea
          id="inviteMessage"
          style={textareaStyle}
          value={message}
          onChange={(e) => onMessageChange(e.target.value)}
          placeholder="Add a personal message to the invitation email..."
        />
      </div>

      <button
        type="submit"
        style={buttonStyle}
        disabled={inviting}
        onMouseOver={(e) => !inviting && (e.currentTarget.style.backgroundColor = '#1d4ed8')}
        onMouseOut={(e) => !inviting && (e.currentTarget.style.backgroundColor = '#2563eb')}
      >
        {inviting ? 'Sending Invitation...' : 'Send Invitation'}
      </button>
    </form>
  );
};

export default InvitationForm;
