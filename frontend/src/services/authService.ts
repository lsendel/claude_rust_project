import { apiClient } from './api';
import { User } from '../contexts/AuthContext';

/**
 * Authentication service for OAuth2 operations.
 */
export const authService = {
  /**
   * Exchange authorization code for access token.
   * Called from OAuth callback page.
   */
  async exchangeCodeForToken(code: string): Promise<string> {
    // In a real implementation, this would call your backend
    // which would exchange the code with Cognito
    const cognitoUrl = import.meta.env.VITE_COGNITO_DOMAIN;
    const clientId = import.meta.env.VITE_COGNITO_CLIENT_ID;
    const redirectUri = import.meta.env.VITE_APP_URL + '/oauth/callback';

    const tokenResponse = await fetch(`${cognitoUrl}/oauth2/token`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({
        grant_type: 'authorization_code',
        client_id: clientId,
        code: code,
        redirect_uri: redirectUri,
      }),
    });

    if (!tokenResponse.ok) {
      throw new Error('Failed to exchange code for token');
    }

    const tokenData = await tokenResponse.json();
    return tokenData.access_token;
  },

  /**
   * Get current user information.
   */
  async getCurrentUser(): Promise<User> {
    const response = await apiClient.get<User>('/auth/me');
    return response.data;
  },

  /**
   * Check authentication status.
   */
  async checkAuthStatus(): Promise<boolean> {
    try {
      await apiClient.get('/auth/status');
      return true;
    } catch (error) {
      return false;
    }
  },

  /**
   * Redirect to Cognito login page.
   */
  redirectToCognitoLogin(provider: 'google' | 'facebook' | 'github'): void {
    const cognitoUrl = import.meta.env.VITE_COGNITO_DOMAIN;
    const clientId = import.meta.env.VITE_COGNITO_CLIENT_ID;
    const redirectUri = import.meta.env.VITE_APP_URL + '/oauth/callback';

    const authUrl = `${cognitoUrl}/oauth2/authorize?` +
      `client_id=${clientId}&` +
      `response_type=code&` +
      `scope=openid+email+profile&` +
      `redirect_uri=${encodeURIComponent(redirectUri)}&` +
      `identity_provider=${provider.charAt(0).toUpperCase() + provider.slice(1)}`;

    window.location.href = authUrl;
  },
};
