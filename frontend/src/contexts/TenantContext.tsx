import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';

/**
 * Tenant information.
 */
export interface Tenant {
  id: string;
  subdomain: string;
  name: string;
  description?: string;
  subscriptionTier: 'FREE' | 'PRO' | 'ENTERPRISE';
  quotaLimit: number | null;
  currentUsage: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * Tenant context state.
 */
interface TenantContextType {
  tenant: Tenant | null;
  subdomain: string | null;
  isLoading: boolean;
  error: string | null;
  refreshTenant: () => Promise<void>;
}

/**
 * Tenant context for managing current tenant information.
 */
const TenantContext = createContext<TenantContextType | undefined>(undefined);

interface TenantProviderProps {
  children: ReactNode;
}

/**
 * Extract subdomain from current hostname.
 * For multi-tenant architecture where each tenant has a subdomain.
 *
 * Examples:
 * - acme.platform.com -> "acme"
 * - localhost:3000 -> null (development)
 */
const extractSubdomain = (): string | null => {
  const hostname = window.location.hostname;

  // Development mode (localhost)
  if (hostname === 'localhost' || hostname === '127.0.0.1') {
    // Check for subdomain in query parameter for testing
    const params = new URLSearchParams(window.location.search);
    return params.get('subdomain');
  }

  // Production mode - extract subdomain
  const parts = hostname.split('.');

  // Need at least 3 parts for subdomain (subdomain.domain.tld)
  if (parts.length >= 3) {
    return parts[0];
  }

  return null;
};

/**
 * Tenant provider component.
 * Extracts subdomain from URL and fetches tenant information.
 */
export const TenantProvider: React.FC<TenantProviderProps> = ({ children }) => {
  const [tenant, setTenant] = useState<Tenant | null>(null);
  const [subdomain, setSubdomain] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /**
   * Fetch tenant information from backend.
   */
  const fetchTenant = async (subdomainParam: string): Promise<void> => {
    try {
      setIsLoading(true);
      setError(null);

      const response = await fetch(`/api/tenants/subdomain/${subdomainParam}`);

      if (!response.ok) {
        if (response.status === 404) {
          throw new Error('Tenant not found');
        }
        throw new Error('Failed to fetch tenant information');
      }

      const tenantData: Tenant = await response.json();
      setTenant(tenantData);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error';
      console.error('Failed to fetch tenant:', errorMessage);
      setError(errorMessage);
      setTenant(null);
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Initialize tenant on mount by extracting subdomain.
   */
  useEffect(() => {
    const currentSubdomain = extractSubdomain();
    setSubdomain(currentSubdomain);

    if (currentSubdomain) {
      fetchTenant(currentSubdomain);
    } else {
      setIsLoading(false);
    }
  }, []);

  /**
   * Refresh tenant information from backend.
   */
  const refreshTenant = async (): Promise<void> => {
    if (subdomain) {
      await fetchTenant(subdomain);
    }
  };

  const value: TenantContextType = {
    tenant,
    subdomain,
    isLoading,
    error,
    refreshTenant,
  };

  return <TenantContext.Provider value={value}>{children}</TenantContext.Provider>;
};

/**
 * Hook to use tenant context.
 * Must be used within TenantProvider.
 */
export const useTenant = (): TenantContextType => {
  const context = useContext(TenantContext);
  if (!context) {
    throw new Error('useTenant must be used within TenantProvider');
  }
  return context;
};
