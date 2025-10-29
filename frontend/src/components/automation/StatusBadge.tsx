import React from 'react';
import { ExecutionStatus } from '../../services/automationService';

interface StatusBadgeProps {
  status: ExecutionStatus;
}

/**
 * Status badge component for displaying execution status with appropriate colors.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  const getStatusStyle = (): React.CSSProperties => {
    const baseStyle: React.CSSProperties = {
      padding: '4px 12px',
      borderRadius: '12px',
      fontSize: '12px',
      fontWeight: '500',
      display: 'inline-block',
    };

    const colorMap: Record<ExecutionStatus, { bg: string; color: string }> = {
      SUCCESS: { bg: '#d1fae5', color: '#065f46' },
      FAILED: { bg: '#fee2e2', color: '#991b1b' },
      SKIPPED: { bg: '#fef3c7', color: '#92400e' },
      NO_RULES_MATCHED: { bg: '#f3f4f6', color: '#4b5563' },
    };

    const colors = colorMap[status] || colorMap.NO_RULES_MATCHED;
    return { ...baseStyle, backgroundColor: colors.bg, color: colors.color };
  };

  return <span style={getStatusStyle()}>{status}</span>;
};

export default StatusBadge;
