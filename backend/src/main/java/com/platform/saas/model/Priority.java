package com.platform.saas.model;

/**
 * Priority level enumeration.
 * Used by both Projects and Tasks to indicate importance/urgency.
 */
public enum Priority {
    /**
     * Low priority - can be done later
     */
    LOW,

    /**
     * Medium priority - normal importance
     */
    MEDIUM,

    /**
     * High priority - should be done soon
     */
    HIGH,

    /**
     * Critical priority - must be done immediately
     */
    CRITICAL
}
