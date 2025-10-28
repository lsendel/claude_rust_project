package com.platform.saas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * AutomationRule entity represents a configured automation triggered by domain events.
 * Rules are tenant-scoped and can trigger various actions like sending emails or webhooks.
 */
@Entity
@Table(name = "automation_rules", indexes = {
    @Index(name = "idx_automation_rule_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_automation_rule_event_type", columnList = "event_type"),
    @Index(name = "idx_automation_rule_active", columnList = "is_active")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutomationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Tenant ID cannot be null")
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @NotBlank(message = "Name cannot be blank")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    /**
     * Event type that triggers this rule.
     * Examples: "project.created", "project.updated", "task.status.changed", "task.completed"
     */
    @NotBlank(message = "Event type cannot be blank")
    @Column(name = "event_type", nullable = false)
    private String eventType;

    /**
     * Action to execute when the rule is triggered.
     * Examples: "send_email", "call_webhook", "create_task", "send_notification"
     */
    @NotBlank(message = "Action type cannot be blank")
    @Column(name = "action_type", nullable = false)
    private String actionType;

    /**
     * Conditions for the rule to trigger (stored as JSON).
     * Example: {"field": "status", "operator": "equals", "value": "COMPLETED"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conditions", columnDefinition = "jsonb")
    private Map<String, Object> conditions;

    /**
     * Configuration for the action (stored as JSON).
     * Example for send_email: {"to": "user@example.com", "subject": "Task completed", "template": "task_completed"}
     * Example for call_webhook: {"url": "https://api.example.com/webhook", "method": "POST"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_config", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> actionConfig;

    /**
     * Whether this rule is currently active.
     */
    @NotNull(message = "Active status cannot be null")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * User who created this rule.
     */
    @Column(name = "created_by")
    private UUID createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Number of times this rule has been triggered.
     */
    @Column(name = "execution_count")
    private Long executionCount = 0L;

    /**
     * Last time this rule was triggered.
     */
    @Column(name = "last_executed_at")
    private LocalDateTime lastExecutedAt;

    /**
     * Increment execution count and update last executed timestamp.
     */
    public void recordExecution() {
        this.executionCount = (this.executionCount == null ? 0 : this.executionCount) + 1;
        this.lastExecutedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "AutomationRule{" +
                "id=" + id +
                ", tenantId=" + tenantId +
                ", name='" + name + '\'' +
                ", eventType='" + eventType + '\'' +
                ", actionType='" + actionType + '\'' +
                ", isActive=" + isActive +
                ", executionCount=" + executionCount +
                '}';
    }
}
