package com.platform.saas.model;

/**
 * Project status enumeration.
 * Represents the current lifecycle stage of a project.
 */
public enum ProjectStatus {
    /**
     * Project is in planning phase
     */
    PLANNING,

    /**
     * Project is actively being worked on
     */
    ACTIVE,

    /**
     * Project is temporarily on hold
     */
    ON_HOLD,

    /**
     * Project has been completed
     */
    COMPLETED,

    /**
     * Project has been archived
     */
    ARCHIVED
}
