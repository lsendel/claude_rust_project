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
 * Project entity represents a project management unit within a tenant.
 * All projects are isolated per tenant for multi-tenancy.
 */
@Entity
@Table(name = "projects", indexes = {
    @Index(name = "idx_project_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_project_tenant_status", columnList = "tenant_id, status"),
    @Index(name = "idx_project_tenant_due_date", columnList = "tenant_id, due_date")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Tenant ID cannot be null")
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @NotBlank(message = "Project name cannot be blank")
    @Size(min = 1, max = 255, message = "Project name must be between 1 and 255 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProjectStatus status = ProjectStatus.PLANNING;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @NotNull(message = "Owner ID cannot be null")
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

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
     * Check if project is overdue.
     */
    public boolean isOverdue() {
        if (this.dueDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(this.dueDate) &&
               this.status != ProjectStatus.COMPLETED &&
               this.status != ProjectStatus.ARCHIVED;
    }

    /**
     * Check if project is active (not completed or archived).
     */
    public boolean isActive() {
        return this.status != ProjectStatus.COMPLETED &&
               this.status != ProjectStatus.ARCHIVED;
    }

    /**
     * Check if project can be edited (not completed or archived).
     */
    public boolean canBeEdited() {
        return this.status != ProjectStatus.COMPLETED &&
               this.status != ProjectStatus.ARCHIVED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", tenantId=" + tenantId +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", progressPercentage=" + progressPercentage +
                '}';
    }
}
