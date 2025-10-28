package com.platform.saas.security;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Thread-local storage for tenant context.
 * Stores the current tenant ID for the duration of a request.
 *
 * This class enables multi-tenant data isolation by making the tenant ID
 * available throughout the request lifecycle without passing it explicitly
 * through every method call.
 *
 * Usage:
 * - Set tenant ID at the start of request processing (via TenantContextFilter)
 * - Access tenant ID anywhere in the application via getTenantId()
 * - Clear tenant ID at the end of request processing (cleanup)
 */
@Slf4j
public class TenantContext {

    private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> TENANT_SUBDOMAIN = new ThreadLocal<>();

    /**
     * Set the current tenant ID for this thread.
     *
     * @param tenantId The tenant ID to set
     */
    public static void setTenantId(UUID tenantId) {
        if (tenantId == null) {
            log.warn("Attempting to set null tenant ID");
            return;
        }

        log.debug("Setting tenant ID: {}", tenantId);
        TENANT_ID.set(tenantId);
    }

    /**
     * Get the current tenant ID for this thread.
     *
     * @return The current tenant ID, or null if not set
     */
    public static UUID getTenantId() {
        UUID tenantId = TENANT_ID.get();

        if (tenantId == null) {
            log.warn("Tenant ID not set in context - this may indicate a security issue");
        }

        return tenantId;
    }

    /**
     * Set the current tenant subdomain for this thread.
     *
     * @param subdomain The tenant subdomain to set
     */
    public static void setTenantSubdomain(String subdomain) {
        if (subdomain == null || subdomain.trim().isEmpty()) {
            log.warn("Attempting to set null or empty tenant subdomain");
            return;
        }

        log.debug("Setting tenant subdomain: {}", subdomain);
        TENANT_SUBDOMAIN.set(subdomain);
    }

    /**
     * Get the current tenant subdomain for this thread.
     *
     * @return The current tenant subdomain, or null if not set
     */
    public static String getTenantSubdomain() {
        return TENANT_SUBDOMAIN.get();
    }

    /**
     * Check if tenant context is set.
     *
     * @return true if tenant ID is set, false otherwise
     */
    public static boolean isSet() {
        return TENANT_ID.get() != null;
    }

    /**
     * Clear the tenant context for this thread.
     * IMPORTANT: Must be called at the end of request processing to prevent
     * memory leaks and context pollution across requests.
     */
    public static void clear() {
        UUID tenantId = TENANT_ID.get();

        if (tenantId != null) {
            log.debug("Clearing tenant context for tenant ID: {}", tenantId);
        }

        TENANT_ID.remove();
        TENANT_SUBDOMAIN.remove();
    }

    /**
     * Execute a block of code with a specific tenant context.
     * Automatically clears context after execution.
     *
     * @param tenantId The tenant ID to use
     * @param action The action to execute
     */
    public static void executeWithTenantId(UUID tenantId, Runnable action) {
        try {
            setTenantId(tenantId);
            action.run();
        } finally {
            clear();
        }
    }

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private TenantContext() {
        throw new UnsupportedOperationException("Cannot instantiate utility class");
    }
}
