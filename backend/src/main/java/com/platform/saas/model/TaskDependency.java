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
 * TaskDependency represents a dependency relationship between two tasks.
 * The blocking task must be completed before the blocked task can start.
 */
@Entity
@Table(name = "task_dependencies", indexes = {
    @Index(name = "idx_task_dependency_blocked", columnList = "blocked_task_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(TaskDependencyId.class)
public class TaskDependency implements Serializable {

    @Id
    @NotNull(message = "Blocking task ID cannot be null")
    @Column(name = "blocking_task_id", nullable = false)
    private UUID blockingTaskId;

    @Id
    @NotNull(message = "Blocked task ID cannot be null")
    @Column(name = "blocked_task_id", nullable = false)
    private UUID blockedTaskId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Convenience constructor for creating task dependencies.
     */
    public TaskDependency(UUID blockingTaskId, UUID blockedTaskId) {
        this.blockingTaskId = blockingTaskId;
        this.blockedTaskId = blockedTaskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskDependency that = (TaskDependency) o;
        return Objects.equals(blockingTaskId, that.blockingTaskId) &&
               Objects.equals(blockedTaskId, that.blockedTaskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockingTaskId, blockedTaskId);
    }

    @Override
    public String toString() {
        return "TaskDependency{" +
                "blockingTaskId=" + blockingTaskId +
                ", blockedTaskId=" + blockedTaskId +
                ", createdAt=" + createdAt +
                '}';
    }
}
