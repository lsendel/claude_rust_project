package com.platform.saas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * EventLog entity records automation rule executions for audit trail.
 * Tracks success/failure, execution duration, and any error details.
 */
@Entity
@Table(name = "event_logs", indexes = {
    @Index(name = "idx_event_log_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_event_log_rule_id", columnList = "automation_rule_id"),
    @Index(name = "idx_event_log_event_type", columnList = "event_type"),
    @Index(name = "idx_event_log_created_at", columnList = "created_at"),
    @Index(name = "idx_event_log_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Tenant ID cannot be null")
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    /**
     * The automation rule that was executed.
     * May be null if the event was published but no rules matched.
     */
    @Column(name = "automation_rule_id")
    private UUID automationRuleId;

    /**
     * Type of event that was published.
     * Examples: "project.created", "task.status.changed"
     */
    @NotBlank(message = "Event type cannot be blank")
    @Column(name = "event_type", nullable = false)
    private String eventType;

    /**
     * Action that was executed.
     * Examples: "send_email", "call_webhook", "create_task"
     */
    @Column(name = "action_type")
    private String actionType;

    /**
     * Execution status.
     */
    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExecutionStatus status;

    /**
     * Event payload (stored as JSON).
     * Contains the data that triggered the automation.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_payload", columnDefinition = "jsonb")
    private Map<String, Object> eventPayload;

    /**
     * Action result (stored as JSON).
     * Contains the response from the action execution.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_result", columnDefinition = "jsonb")
    private Map<String, Object> actionResult;

    /**
     * Error message if execution failed.
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Stack trace if execution failed.
     */
    @Column(name = "error_stack_trace", columnDefinition = "TEXT")
    private String errorStackTrace;

    /**
     * Execution duration in milliseconds.
     */
    @Column(name = "execution_duration_ms")
    private Long executionDurationMs;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Resource ID that triggered the event (e.g., project ID, task ID).
     */
    @Column(name = "resource_id")
    private UUID resourceId;

    /**
     * Resource type (e.g., "project", "task").
     */
    @Column(name = "resource_type")
    private String resourceType;

    /**
     * Execution status enum.
     */
    public enum ExecutionStatus {
        /**
         * Automation executed successfully.
         */
        SUCCESS,

        /**
         * Automation failed with an error.
         */
        FAILED,

        /**
         * Automation was skipped (e.g., conditions not met).
         */
        SKIPPED,

        /**
         * Event was published but no rules matched.
         */
        NO_RULES_MATCHED
    }

    @Override
    public String toString() {
        return "EventLog{" +
                "id=" + id +
                ", tenantId=" + tenantId +
                ", automationRuleId=" + automationRuleId +
                ", eventType='" + eventType + '\'' +
                ", actionType='" + actionType + '\'' +
                ", status=" + status +
                ", executionDurationMs=" + executionDurationMs +
                ", createdAt=" + createdAt +
                '}';
    }
}
