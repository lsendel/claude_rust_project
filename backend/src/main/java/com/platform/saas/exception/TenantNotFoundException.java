package com.platform.saas.exception;

import java.util.UUID;

/**
 * Exception thrown when a tenant cannot be found.
 */
public class TenantNotFoundException extends RuntimeException {

    public TenantNotFoundException(UUID tenantId) {
        super("Tenant not found with ID: " + tenantId);
    }

    public TenantNotFoundException(String subdomain) {
        super("Tenant not found with subdomain: " + subdomain);
    }
}
