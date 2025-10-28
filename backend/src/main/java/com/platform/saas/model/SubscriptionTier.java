package com.platform.saas.model;

/**
 * Subscription tier for tenant accounts.
 * Defines quota limits and feature access levels.
 */
public enum SubscriptionTier {
    /**
     * Free tier - Limited to 50 projects/tasks
     */
    FREE,

    /**
     * Pro tier - Limited to 1,000 projects/tasks
     */
    PRO,

    /**
     * Enterprise tier - Unlimited projects/tasks
     */
    ENTERPRISE
}
