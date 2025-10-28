package com.platform.saas.model;

/**
 * Task status enumeration.
 * Represents the current state of a task.
 */
public enum TaskStatus {
    /**
     * Task has not been started
     */
    TODO,

    /**
     * Task is currently being worked on
     */
    IN_PROGRESS,

    /**
     * Task is blocked by dependencies or issues
     */
    BLOCKED,

    /**
     * Task has been completed
     */
    COMPLETED
}
