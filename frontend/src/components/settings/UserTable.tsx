import React from 'react';
import { User } from '../../services/userService';
import UserRow from './UserRow';

interface UserTableProps {
  users: User[];
  loading: boolean;
  currentUserId?: string;
  onRemove: (userId: string, userEmail: string) => void;
}

/**
 * User table component for displaying team members list.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const UserTable: React.FC<UserTableProps> = ({ users, loading, currentUserId, onRemove }) => {
  const tableStyle: React.CSSProperties = {
    width: '100%',
    borderCollapse: 'collapse',
  };

  const thStyle: React.CSSProperties = {
    textAlign: 'left' as const,
    padding: '12px',
    borderBottom: '2px solid #e5e7eb',
    fontSize: '12px',
    fontWeight: '600',
    color: '#6b7280',
    textTransform: 'uppercase' as const,
  };

  const emptyStateStyle: React.CSSProperties = {
    textAlign: 'center',
    padding: '24px',
    color: '#6b7280',
  };

  if (loading) {
    return <div style={emptyStateStyle}>Loading team members...</div>;
  }

  if (users.length === 0) {
    return <div style={emptyStateStyle}>No team members yet. Invite someone to get started!</div>;
  }

  return (
    <table style={tableStyle}>
      <thead>
        <tr>
          <th style={thStyle}>Name</th>
          <th style={thStyle}>Email</th>
          <th style={thStyle}>Role</th>
          <th style={thStyle}>Joined</th>
          <th style={thStyle}>Actions</th>
        </tr>
      </thead>
      <tbody>
        {users.map((user) => (
          <UserRow key={user.userId} user={user} currentUserId={currentUserId} onRemove={onRemove} />
        ))}
      </tbody>
    </table>
  );
};

export default UserTable;
