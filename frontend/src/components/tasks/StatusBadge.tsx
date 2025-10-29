import React from 'react';

type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'BLOCKED' | 'COMPLETED';

interface StatusBadgeProps {
  status: TaskStatus;
}

/**
 * Status badge component for displaying task status with appropriate colors.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  const getStatusStyle = (): React.CSSProperties => {
    const baseStyle: React.CSSProperties = {
      display: 'inline-block',
      padding: '4px 12px',
      borderRadius: '12px',
      fontSize: '12px',
      fontWeight: '500',
    };

    const colorMap: Record<TaskStatus, { bg: string; color: string }> = {
      TODO: { bg: '#e5e7eb', color: '#374151' },
      IN_PROGRESS: { bg: '#dbeafe', color: '#1e40af' },
      BLOCKED: { bg: '#fecaca', color: '#991b1b' },
      COMPLETED: { bg: '#d1fae5', color: '#065f46' },
    };

    const colors = colorMap[status] || colorMap.TODO;

    return {
      ...baseStyle,
      backgroundColor: colors.bg,
      color: colors.color,
    };
  };

  const displayText = status.replace('_', ' ');

  return <span style={getStatusStyle()}>{displayText}</span>;
};

export default StatusBadge;
