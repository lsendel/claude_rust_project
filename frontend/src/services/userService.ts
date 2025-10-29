import { apiClient } from './api';

/**
 * User role within a tenant.
 */
export type UserRole = 'ADMINISTRATOR' | 'EDITOR' | 'VIEWER';

/**
 * User model with tenant-specific role information.
 */
export interface User {
  userId: string;
  email: string;
  name: string;
  role: UserRole;
  invitedBy?: string;
  joinedAt: string;
  lastLoginAt?: string;
}

/**
 * Request for inviting a user to a tenant.
 */
export interface InviteUserRequest {
  email: string;
  role: UserRole;
  message?: string;
}

/**
 * Response after inviting a user.
 */
export interface InviteUserResponse {
  userId: string;
  tenantId: string;
  email: string;
  role: UserRole;
  invitedBy: string;
  invitedAt: string;
  existingUser: boolean;
  emailSent: boolean;
}

/**
 * User service for managing tenant members.
 */
export const userService = {
  /**
   * Invite a user to join the tenant.
   */
  async inviteUser(tenantId: string, request: InviteUserRequest): Promise<InviteUserResponse> {
    const response = await apiClient.post<InviteUserResponse>(
      `/tenants/${tenantId}/users/invite`,
      request
    );
    return response.data;
  },

  /**
   * Get all users in a tenant.
   */
  async listUsers(tenantId: string): Promise<User[]> {
    const response = await apiClient.get<User[]>(`/tenants/${tenantId}/users`);
    return response.data;
  },

  /**
   * Remove a user from a tenant.
   */
  async removeUser(tenantId: string, userId: string): Promise<void> {
    await apiClient.delete(`/tenants/${tenantId}/users/${userId}`);
  },
};
