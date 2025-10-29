import React from 'react';

type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

interface PriorityBadgeProps {
  priority: Priority;
}

/**
 * Priority badge component for displaying task priority with appropriate colors.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const PriorityBadge: React.FC<PriorityBadgeProps> = ({ priority }) => {
  const getPriorityStyle = (): React.CSSProperties => {
    const baseStyle: React.CSSProperties = {
      display: 'inline-block',
      padding: '4px 12px',
      borderRadius: '12px',
      fontSize: '12px',
      fontWeight: '500',
    };

    const colorMap: Record<Priority, { bg: string; color: string }> = {
      LOW: { bg: '#dbeafe', color: '#1e40af' },
      MEDIUM: { bg: '#fef3c7', color: '#92400e' },
      HIGH: { bg: '#fed7aa', color: '#9a3412' },
      CRITICAL: { bg: '#fecaca', color: '#991b1b' },
    };

    const colors = colorMap[priority] || colorMap.MEDIUM;

    return {
      ...baseStyle,
      backgroundColor: colors.bg,
      color: colors.color,
    };
  };

  return <span style={getPriorityStyle()}>{priority}</span>;
};

export default PriorityBadge;
