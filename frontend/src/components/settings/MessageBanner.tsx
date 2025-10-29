import React from 'react';

interface MessageBannerProps {
  message: string;
  type: 'error' | 'success';
  onClose: () => void;
}

/**
 * Message banner component for displaying success/error messages.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const MessageBanner: React.FC<MessageBannerProps> = ({ message, type, onClose }) => {
  const getBannerStyle = (): React.CSSProperties => {
    const baseStyle: React.CSSProperties = {
      padding: '12px 16px',
      border: '1px solid',
      borderRadius: '6px',
      marginBottom: '16px',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
    };

    if (type === 'success') {
      return {
        ...baseStyle,
        backgroundColor: '#d1fae5',
        borderColor: '#a7f3d0',
        color: '#065f46',
      };
    }

    return {
      ...baseStyle,
      backgroundColor: '#fee2e2',
      borderColor: '#fecaca',
      color: '#991b1b',
    };
  };

  const closeButtonStyle: React.CSSProperties = {
    background: 'none',
    border: 'none',
    color: 'inherit',
    cursor: 'pointer',
    fontSize: '18px',
    fontWeight: 'bold',
  };

  return (
    <div style={getBannerStyle()}>
      <span>{message}</span>
      <button onClick={onClose} style={closeButtonStyle}>
        ×
      </button>
    </div>
  );
};

export default MessageBanner;
