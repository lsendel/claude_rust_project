package com.platform.saas.repository;

import com.platform.saas.model.Priority;
import com.platform.saas.model.Task;
import com.platform.saas.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Task entity operations.
 * All queries are tenant-scoped to ensure multi-tenant data isolation.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    /**
     * Find a task by ID within a specific tenant.
     * Ensures tenant isolation for task access.
     * @param id The task ID
     * @param tenantId The tenant ID
     * @return Optional containing the task if found within the tenant
     */
    Optional<Task> findByIdAndTenantId(UUID id, UUID tenantId);

    /**
     * Find all tasks belonging to a tenant.
     * @param tenantId The tenant ID
     * @return List of tasks for the tenant
     */
    List<Task> findByTenantId(UUID tenantId);

    /**
     * Find all tasks for a specific project.
     * @param tenantId The tenant ID
     * @param projectId The project ID
     * @return List of tasks for the project
     */
    List<Task> findByTenantIdAndProjectId(UUID tenantId, UUID projectId);

    /**
     * Find tasks by tenant and status.
     * @param tenantId The tenant ID
     * @param status The task status
     * @return List of tasks matching the criteria
     */
    List<Task> findByTenantIdAndStatus(UUID tenantId, TaskStatus status);

    /**
     * Find tasks by project and status.
     * @param tenantId The tenant ID
     * @param projectId The project ID
     * @param status The task status
     * @return List of tasks matching the criteria
     */
    List<Task> findByTenantIdAndProjectIdAndStatus(UUID tenantId, UUID projectId, TaskStatus status);

    /**
     * Find tasks by tenant and priority.
     * @param tenantId The tenant ID
     * @param priority The task priority
     * @return List of tasks matching the criteria
     */
    List<Task> findByTenantIdAndPriority(UUID tenantId, Priority priority);

    /**
     * Find overdue tasks for a tenant.
     * @param tenantId The tenant ID
     * @param currentDate The current date
     * @return List of overdue tasks
     */
    @Query("SELECT t FROM Task t WHERE t.tenantId = :tenantId AND t.dueDate < :currentDate " +
           "AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks(@Param("tenantId") UUID tenantId, @Param("currentDate") LocalDate currentDate);

    /**
     * Find overdue tasks for a specific project.
     * @param tenantId The tenant ID
     * @param projectId The project ID
     * @param currentDate The current date
     * @return List of overdue tasks
     */
    @Query("SELECT t FROM Task t WHERE t.tenantId = :tenantId AND t.projectId = :projectId " +
           "AND t.dueDate < :currentDate AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasksForProject(@Param("tenantId") UUID tenantId,
                                          @Param("projectId") UUID projectId,
                                          @Param("currentDate") LocalDate currentDate);

    /**
     * Count tasks for a tenant.
     * Used for quota enforcement.
     * @param tenantId The tenant ID
     * @return The count of tasks
     */
    long countByTenantId(UUID tenantId);

    /**
     * Count tasks for a specific project.
     * @param tenantId The tenant ID
     * @param projectId The project ID
     * @return The count of tasks
     */
    long countByTenantIdAndProjectId(UUID tenantId, UUID projectId);

    /**
     * Calculate average progress for a project's tasks.
     * @param tenantId The tenant ID
     * @param projectId The project ID
     * @return The average progress percentage
     */
    @Query("SELECT AVG(t.progressPercentage) FROM Task t " +
           "WHERE t.tenantId = :tenantId AND t.projectId = :projectId")
    Double calculateAverageProgress(@Param("tenantId") UUID tenantId, @Param("projectId") UUID projectId);
}
