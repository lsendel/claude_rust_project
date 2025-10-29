import React from 'react';
import { User } from '../../services/userService';
import RoleBadge from './RoleBadge';

interface UserRowProps {
  user: User;
  currentUserId?: string;
  onRemove: (userId: string, userEmail: string) => void;
}

/**
 * User row component for displaying individual team member in table.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const UserRow: React.FC<UserRowProps> = ({ user, currentUserId, onRemove }) => {
  const tdStyle: React.CSSProperties = {
    padding: '16px 12px',
    borderBottom: '1px solid #f3f4f6',
  };

  const removeButtonStyle: React.CSSProperties = {
    padding: '6px 12px',
    border: '1px solid #dc2626',
    borderRadius: '4px',
    backgroundColor: 'white',
    color: '#dc2626',
    cursor: 'pointer',
    fontSize: '13px',
  };

  return (
    <tr>
      <td style={tdStyle}>
        <span style={{ fontWeight: '500', color: '#1f2937' }}>{user.name}</span>
      </td>
      <td style={tdStyle}>
        <span style={{ color: '#4b5563' }}>{user.email}</span>
      </td>
      <td style={tdStyle}>
        <RoleBadge role={user.role} />
      </td>
      <td style={tdStyle}>
        <span style={{ fontSize: '14px', color: '#6b7280' }}>
          {new Date(user.joinedAt).toLocaleDateString()}
        </span>
      </td>
      <td style={tdStyle}>
        {user.userId !== currentUserId && (
          <button
            style={removeButtonStyle}
            onClick={() => onRemove(user.userId, user.email)}
            onMouseOver={(e) => (e.currentTarget.style.backgroundColor = '#fef2f2')}
            onMouseOut={(e) => (e.currentTarget.style.backgroundColor = 'white')}
          >
            Remove
          </button>
        )}
      </td>
    </tr>
  );
};

export default UserRow;
