import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';

/**
 * User information from JWT token.
 */
export interface User {
  id: string; // User ID (same as userId)
  userId: string;
  cognitoUserId: string;
  email: string;
  name: string;
  emailVerified: boolean;
}

/**
 * Authentication context state.
 */
interface AuthContextType {
  user: User | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (provider: 'google' | 'facebook' | 'github') => void;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

/**
 * Authentication context for managing user session state.
 */
const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

/**
 * Authentication provider component.
 * Manages user authentication state and provides login/logout methods.
 */
export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  /**
   * Initialize auth state from localStorage on mount.
   */
  useEffect(() => {
    const initializeAuth = async () => {
      try {
        const storedToken = localStorage.getItem('accessToken');
        if (storedToken) {
          setAccessToken(storedToken);
          await fetchUserInfo(storedToken);
        }
      } catch (error) {
        console.error('Failed to initialize auth:', error);
        // Clear invalid token
        localStorage.removeItem('accessToken');
      } finally {
        setIsLoading(false);
      }
    };

    initializeAuth();
  }, []);

  /**
   * Fetch user information from backend using access token.
   */
  const fetchUserInfo = async (token: string): Promise<void> => {
    try {
      const response = await fetch('/api/auth/me', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Failed to fetch user info');
      }

      const userData = await response.json();
      // Ensure id field is populated from userId
      if (userData && !userData.id && userData.userId) {
        userData.id = userData.userId;
      }
      setUser(userData);
    } catch (error) {
      console.error('Failed to fetch user info:', error);
      throw error;
    }
  };

  /**
   * Redirect to Cognito login page for OAuth2 provider.
   */
  const login = (provider: 'google' | 'facebook' | 'github') => {
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
  };

  /**
   * Logout user and clear session.
   */
  const logout = () => {
    setUser(null);
    setAccessToken(null);
    localStorage.removeItem('accessToken');

    // Redirect to Cognito logout
    const cognitoUrl = import.meta.env.VITE_COGNITO_DOMAIN;
    const clientId = import.meta.env.VITE_COGNITO_CLIENT_ID;
    const logoutUri = import.meta.env.VITE_APP_URL;

    const logoutUrl = `${cognitoUrl}/logout?` +
      `client_id=${clientId}&` +
      `logout_uri=${encodeURIComponent(logoutUri)}`;

    window.location.href = logoutUrl;
  };

  /**
   * Refresh user information from backend.
   */
  const refreshUser = async (): Promise<void> => {
    if (accessToken) {
      await fetchUserInfo(accessToken);
    }
  };

  const value: AuthContextType = {
    user,
    accessToken,
    isAuthenticated: !!user && !!accessToken,
    isLoading,
    login,
    logout,
    refreshUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

/**
 * Hook to use authentication context.
 * Must be used within AuthProvider.
 */
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};
