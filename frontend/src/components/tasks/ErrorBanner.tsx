import React from 'react';

interface ErrorBannerProps {
  message: string;
  onClose: () => void;
}

/**
 * Error banner component for displaying error messages.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const ErrorBanner: React.FC<ErrorBannerProps> = ({ message, onClose }) => {
  const bannerStyle: React.CSSProperties = {
    padding: '12px 16px',
    backgroundColor: '#fee2e2',
    border: '1px solid #fecaca',
    borderRadius: '6px',
    marginBottom: '16px',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    color: '#991b1b',
  };

  const closeButtonStyle: React.CSSProperties = {
    background: 'none',
    border: 'none',
    color: '#991b1b',
    cursor: 'pointer',
    fontSize: '18px',
    fontWeight: 'bold',
  };

  return (
    <div style={bannerStyle}>
      <span>{message}</span>
      <button onClick={onClose} style={closeButtonStyle}>
        ×
      </button>
    </div>
  );
};

export default ErrorBanner;
