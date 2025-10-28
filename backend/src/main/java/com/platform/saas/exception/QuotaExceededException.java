package com.platform.saas.exception;

import java.util.UUID;

/**
 * Exception thrown when a tenant exceeds their subscription quota.
 */
public class QuotaExceededException extends RuntimeException {

    public QuotaExceededException(UUID tenantId, String resourceType, long currentUsage, long limit) {
        super(String.format("Tenant %s has exceeded quota for %s: %d/%d",
                tenantId, resourceType, currentUsage, limit));
    }

    public QuotaExceededException(String message) {
        super(message);
    }
}
