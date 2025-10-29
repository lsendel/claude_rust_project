import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { authService } from '../services/authService';
import { getErrorMessage } from '../services/api';

/**
 * OAuth callback page.
 * Handles the redirect from AWS Cognito after authentication.
 * Exchanges authorization code for access token and redirects to dashboard.
 */
const OAuthCallbackPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const handleCallback = async () => {
      try {
        // Get authorization code from URL parameters
        const code = searchParams.get('code');
        const errorParam = searchParams.get('error');
        const errorDescription = searchParams.get('error_description');

        // Check for OAuth errors
        if (errorParam) {
          setError(errorDescription || 'Authentication failed');
          return;
        }

        // Validate code exists
        if (!code) {
          setError('No authorization code received');
          return;
        }

        // Exchange code for access token
        const accessToken = await authService.exchangeCodeForToken(code);

        // Store token in localStorage
        localStorage.setItem('accessToken', accessToken);

        // Redirect to dashboard
        navigate('/dashboard', { replace: true });
      } catch (err) {
        const errorMessage = getErrorMessage(err);
        console.error('OAuth callback error:', errorMessage);
        setError(errorMessage);
      }
    };

    handleCallback();
  }, [searchParams, navigate]);

  if (error) {
    return (
      <div style={styles.container}>
        <div style={styles.card}>
          <div style={styles.errorIcon}>⚠️</div>
          <h1 style={styles.errorTitle}>Authentication Failed</h1>
          <p style={styles.errorMessage}>{error}</p>
          <button
            onClick={() => navigate('/login')}
            style={styles.button}
          >
            Back to Login
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <div style={styles.spinner} />
        <h1 style={styles.loadingTitle}>Signing you in...</h1>
        <p style={styles.loadingMessage}>Please wait while we complete your authentication</p>
      </div>
    </div>
  );
};

/**
 * Inline styles for the component.
 */
const styles = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#f3f4f6',
    padding: '20px',
  },
  card: {
    backgroundColor: 'white',
    borderRadius: '8px',
    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
    padding: '40px',
    maxWidth: '400px',
    width: '100%',
    textAlign: 'center' as const,
  },
  spinner: {
    width: '40px',
    height: '40px',
    border: '4px solid #e5e7eb',
    borderTop: '4px solid #3b82f6',
    borderRadius: '50%',
    animation: 'spin 1s linear infinite',
    margin: '0 auto 24px',
  },
  loadingTitle: {
    fontSize: '24px',
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: '8px',
  },
  loadingMessage: {
    fontSize: '14px',
    color: '#6b7280',
  },
  errorIcon: {
    fontSize: '48px',
    marginBottom: '16px',
  },
  errorTitle: {
    fontSize: '24px',
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: '8px',
  },
  errorMessage: {
    fontSize: '14px',
    color: '#ef4444',
    marginBottom: '24px',
  },
  button: {
    padding: '12px 24px',
    backgroundColor: '#3b82f6',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    fontSize: '14px',
    fontWeight: '500',
    cursor: 'pointer',
  },
};

// Add keyframes for spinner animation
const styleSheet = document.createElement('style');
styleSheet.textContent = `
  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
`;
document.head.appendChild(styleSheet);

export default OAuthCallbackPage;
