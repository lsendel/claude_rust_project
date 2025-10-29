import React from 'react';

interface ProgressBarProps {
  progress: number;
  showPercentage?: boolean;
}

/**
 * Progress bar component for displaying task completion progress.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const ProgressBar: React.FC<ProgressBarProps> = ({ progress, showPercentage = true }) => {
  const containerStyle: React.CSSProperties = {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
  };

  const barContainerStyle: React.CSSProperties = {
    width: '120px',
    height: '8px',
    backgroundColor: '#e5e7eb',
    borderRadius: '4px',
    overflow: 'hidden',
  };

  const barFillStyle: React.CSSProperties = {
    height: '100%',
    width: `${progress}%`,
    backgroundColor: progress === 100 ? '#10b981' : '#3b82f6',
    transition: 'width 0.3s ease',
  };

  const percentageStyle: React.CSSProperties = {
    fontSize: '13px',
    color: '#6b7280',
    minWidth: '35px',
  };

  return (
    <div style={containerStyle}>
      <div style={barContainerStyle}>
        <div style={barFillStyle} />
      </div>
      {showPercentage && <span style={percentageStyle}>{progress}%</span>}
    </div>
  );
};

export default ProgressBar;
