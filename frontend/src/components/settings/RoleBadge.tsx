import React from 'react';
import { UserRole } from '../../services/userService';

interface RoleBadgeProps {
  role: UserRole;
}

/**
 * Role badge component for displaying user roles with appropriate colors.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const RoleBadge: React.FC<RoleBadgeProps> = ({ role }) => {
  const getRoleBadgeStyle = (): React.CSSProperties => {
    const baseStyle: React.CSSProperties = {
      padding: '4px 12px',
      borderRadius: '12px',
      fontSize: '12px',
      fontWeight: '500',
      display: 'inline-block',
    };

    switch (role) {
      case 'ADMINISTRATOR':
        return { ...baseStyle, backgroundColor: '#fee2e2', color: '#991b1b' };
      case 'EDITOR':
        return { ...baseStyle, backgroundColor: '#dbeafe', color: '#1e40af' };
      case 'VIEWER':
        return { ...baseStyle, backgroundColor: '#f3f4f6', color: '#4b5563' };
      default:
        return baseStyle;
    }
  };

  return <span style={getRoleBadgeStyle()}>{role}</span>;
};

export default RoleBadge;
