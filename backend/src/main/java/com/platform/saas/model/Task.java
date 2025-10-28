package com.platform.saas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Task entity represents an actionable work item within a project.
 * All tasks are isolated per tenant and belong to a specific project.
 */
@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_task_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_task_tenant_project", columnList = "tenant_id, project_id"),
    @Index(name = "idx_task_tenant_status", columnList = "tenant_id, status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Tenant ID cannot be null")
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @NotNull(message = "Project ID cannot be null")
    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @NotBlank(message = "Task name cannot be blank")
    @Size(min = 1, max = 255, message = "Task name must be between 1 and 255 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status = TaskStatus.TODO;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Min(value = 0, message = "Progress percentage cannot be negative")
    @Max(value = 100, message = "Progress percentage cannot exceed 100")
    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;

    @NotNull(message = "Priority cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority = Priority.MEDIUM;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Check if task is overdue.
     */
    public boolean isOverdue() {
        if (this.dueDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(this.dueDate) &&
               this.status != TaskStatus.COMPLETED;
    }

    /**
     * Check if task is in progress.
     */
    public boolean isInProgress() {
        return this.status == TaskStatus.IN_PROGRESS;
    }

    /**
     * Check if task is blocked.
     */
    public boolean isBlocked() {
        return this.status == TaskStatus.BLOCKED;
    }

    /**
     * Check if task is completed.
     */
    public boolean isCompleted() {
        return this.status == TaskStatus.COMPLETED;
    }

    /**
     * Mark task as completed.
     */
    public void complete() {
        this.status = TaskStatus.COMPLETED;
        this.progressPercentage = 100;
    }

    /**
     * Start working on the task.
     */
    public void startWork() {
        if (this.status == TaskStatus.TODO || this.status == TaskStatus.BLOCKED) {
            this.status = TaskStatus.IN_PROGRESS;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", tenantId=" + tenantId +
                ", projectId=" + projectId +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                '}';
    }
}
