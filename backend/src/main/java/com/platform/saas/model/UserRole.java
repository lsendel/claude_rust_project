package com.platform.saas.model;

/**
 * User roles within a tenant.
 * Defines permission levels for tenant-specific access control.
 */
public enum UserRole {
    /**
     * Full administrative access - can manage users, settings, and all resources
     */
    ADMINISTRATOR,

    /**
     * Can create and modify projects/tasks but cannot manage users or settings
     */
    EDITOR,

    /**
     * Read-only access - can view projects/tasks but cannot modify
     */
    VIEWER
}
