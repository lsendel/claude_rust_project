import React from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useTenant } from '../contexts/TenantContext';

/**
 * Login page with OAuth2 provider buttons.
 * Redirects to AWS Cognito for authentication.
 */
const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const { tenant, subdomain } = useTenant();

  /**
   * Handle OAuth provider login.
   */
  const handleLogin = (provider: 'google' | 'facebook' | 'github') => {
    login(provider);
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <div style={styles.header}>
          {tenant && (
            <div style={styles.tenantInfo}>
              <h2 style={styles.tenantName}>{tenant.name}</h2>
              <p style={styles.tenantSubdomain}>@{subdomain}</p>
            </div>
          )}
          <h1 style={styles.title}>
            {tenant ? 'Sign in to your account' : 'Sign in'}
          </h1>
          <p style={styles.subtitle}>
            Choose your preferred sign-in method
          </p>
        </div>

        <div style={styles.providers}>
          {/* Google Login */}
          <button
            onClick={() => handleLogin('google')}
            style={styles.providerButton}
          >
            <svg style={styles.providerIcon} viewBox="0 0 24 24">
              <path
                fill="#4285F4"
                d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
              />
              <path
                fill="#34A853"
                d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
              />
              <path
                fill="#FBBC05"
                d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
              />
              <path
                fill="#EA4335"
                d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
              />
            </svg>
            <span>Continue with Google</span>
          </button>

          {/* Facebook Login */}
          <button
            onClick={() => handleLogin('facebook')}
            style={styles.providerButton}
          >
            <svg style={styles.providerIcon} viewBox="0 0 24 24">
              <path
                fill="#1877F2"
                d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"
              />
            </svg>
            <span>Continue with Facebook</span>
          </button>

          {/* GitHub Login */}
          <button
            onClick={() => handleLogin('github')}
            style={styles.providerButton}
          >
            <svg style={styles.providerIcon} viewBox="0 0 24 24">
              <path
                fill="#181717"
                d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"
              />
            </svg>
            <span>Continue with GitHub</span>
          </button>
        </div>

        <div style={styles.footer}>
          <p style={styles.footerText}>
            Don't have an organization?{' '}
            <a href="/signup" style={styles.link}>
              Create one
            </a>
          </p>
          <p style={styles.footerSmall}>
            By signing in, you agree to our Terms of Service and Privacy Policy
          </p>
        </div>
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
  },
  header: {
    textAlign: 'center' as const,
    marginBottom: '32px',
  },
  tenantInfo: {
    marginBottom: '24px',
  },
  tenantName: {
    fontSize: '18px',
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: '4px',
  },
  tenantSubdomain: {
    fontSize: '14px',
    color: '#6b7280',
  },
  title: {
    fontSize: '24px',
    fontWeight: 'bold',
    marginBottom: '8px',
    color: '#111827',
  },
  subtitle: {
    fontSize: '14px',
    color: '#6b7280',
  },
  providers: {
    display: 'flex',
    flexDirection: 'column' as const,
    gap: '12px',
  },
  providerButton: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '12px',
    padding: '12px 16px',
    backgroundColor: 'white',
    border: '1px solid #d1d5db',
    borderRadius: '6px',
    fontSize: '14px',
    fontWeight: '500',
    color: '#374151',
    cursor: 'pointer',
    transition: 'all 0.2s',
  },
  providerIcon: {
    width: '20px',
    height: '20px',
  },
  footer: {
    marginTop: '32px',
    textAlign: 'center' as const,
  },
  footerText: {
    fontSize: '14px',
    color: '#6b7280',
    marginBottom: '8px',
  },
  footerSmall: {
    fontSize: '12px',
    color: '#9ca3af',
  },
  link: {
    color: '#3b82f6',
    textDecoration: 'none',
    fontWeight: '500',
  },
};

export default LoginPage;
