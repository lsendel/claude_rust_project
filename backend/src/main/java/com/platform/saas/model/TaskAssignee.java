package com.platform.saas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * TaskAssignee join entity maps tasks to assigned users.
 * Represents the many-to-many relationship between tasks and users.
 */
@Entity
@Table(name = "task_assignees", indexes = {
    @Index(name = "idx_task_assignee_user_id", columnList = "user_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(TaskAssigneeId.class)
public class TaskAssignee implements Serializable {

    @Id
    @NotNull(message = "Task ID cannot be null")
    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Id
    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @CreatedDate
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    /**
     * Convenience constructor for creating task assignments.
     */
    public TaskAssignee(UUID taskId, UUID userId) {
        this.taskId = taskId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskAssignee that = (TaskAssignee) o;
        return Objects.equals(taskId, that.taskId) &&
               Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, userId);
    }

    @Override
    public String toString() {
        return "TaskAssignee{" +
                "taskId=" + taskId +
                ", userId=" + userId +
                ", assignedAt=" + assignedAt +
                '}';
    }
}
