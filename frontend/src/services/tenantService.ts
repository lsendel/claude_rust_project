import { apiClient } from './api';
import { Tenant } from '../contexts/TenantContext';

/**
 * Tenant registration request.
 */
export interface TenantRegistrationRequest {
  subdomain: string;
  name: string;
  description?: string;
  ownerEmail: string;
  ownerName: string;
  subscriptionTier?: 'FREE' | 'PRO' | 'ENTERPRISE';
}

/**
 * Tenant usage response showing quota consumption.
 */
export interface TenantUsageResponse {
  projectCount: number;
  taskCount: number;
  totalUsage: number;
  quotaLimit: number | null;
  subscriptionTier: 'FREE' | 'PRO' | 'ENTERPRISE';
  usagePercentage: number | null;
  quotaExceeded: boolean;
  nearingQuota: boolean;
}

/**
 * Tenant service for managing tenant operations.
 */
export const tenantService = {
  /**
   * Register a new tenant.
   */
  async registerTenant(request: TenantRegistrationRequest): Promise<Tenant> {
    const response = await apiClient.post<Tenant>('/tenants', request);
    return response.data;
  },

  /**
   * Get tenant by ID.
   */
  async getTenantById(tenantId: string): Promise<Tenant> {
    const response = await apiClient.get<Tenant>(`/tenants/${tenantId}`);
    return response.data;
  },

  /**
   * Get tenant by subdomain.
   */
  async getTenantBySubdomain(subdomain: string): Promise<Tenant> {
    const response = await apiClient.get<Tenant>(`/tenants/subdomain/${subdomain}`);
    return response.data;
  },

  /**
   * Validate subdomain availability.
   */
  async validateSubdomain(subdomain: string): Promise<boolean> {
    try {
      await apiClient.get(`/tenants/validate-subdomain`, {
        params: { subdomain },
      });
      return true;
    } catch (error) {
      return false;
    }
  },

  /**
   * Get tenant usage statistics including quota consumption.
   */
  async getTenantUsage(tenantId: string): Promise<TenantUsageResponse> {
    const response = await apiClient.get<TenantUsageResponse>(`/tenants/${tenantId}/usage`);
    return response.data;
  },
};
