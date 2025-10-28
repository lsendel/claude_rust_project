package com.platform.saas.repository;

import com.platform.saas.model.Priority;
import com.platform.saas.model.Project;
import com.platform.saas.model.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Project entity operations.
 * All queries are tenant-scoped to ensure multi-tenant data isolation.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    /**
     * Find a project by ID within a specific tenant.
     * Ensures tenant isolation for project access.
     * @param id The project ID
     * @param tenantId The tenant ID
     * @return Optional containing the project if found within the tenant
     */
    Optional<Project> findByIdAndTenantId(UUID id, UUID tenantId);

    /**
     * Find all projects belonging to a tenant.
     * @param tenantId The tenant ID
     * @return List of projects for the tenant
     */
    List<Project> findByTenantId(UUID tenantId);

    /**
     * Find projects by tenant and status.
     * @param tenantId The tenant ID
     * @param status The project status
     * @return List of projects matching the criteria
     */
    List<Project> findByTenantIdAndStatus(UUID tenantId, ProjectStatus status);

    /**
     * Find projects by tenant and priority.
     * @param tenantId The tenant ID
     * @param priority The project priority
     * @return List of projects matching the criteria
     */
    List<Project> findByTenantIdAndPriority(UUID tenantId, Priority priority);

    /**
     * Find projects by tenant and owner.
     * @param tenantId The tenant ID
     * @param ownerId The owner user ID
     * @return List of projects matching the criteria
     */
    List<Project> findByTenantIdAndOwnerId(UUID tenantId, UUID ownerId);

    /**
     * Find overdue projects for a tenant.
     * @param tenantId The tenant ID
     * @param currentDate The current date
     * @return List of overdue projects
     */
    @Query("SELECT p FROM Project p WHERE p.tenantId = :tenantId AND p.dueDate < :currentDate " +
           "AND p.status NOT IN ('COMPLETED', 'ARCHIVED')")
    List<Project> findOverdueProjects(@Param("tenantId") UUID tenantId, @Param("currentDate") LocalDate currentDate);

    /**
     * Find active projects for a tenant (not completed or archived).
     * @param tenantId The tenant ID
     * @return List of active projects
     */
    @Query("SELECT p FROM Project p WHERE p.tenantId = :tenantId " +
           "AND p.status NOT IN ('COMPLETED', 'ARCHIVED')")
    List<Project> findActiveProjects(@Param("tenantId") UUID tenantId);

    /**
     * Count projects for a tenant.
     * Used for quota enforcement.
     * @param tenantId The tenant ID
     * @return The count of projects
     */
    long countByTenantId(UUID tenantId);

    /**
     * Count active projects for a tenant.
     * @param tenantId The tenant ID
     * @return The count of active projects
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.tenantId = :tenantId " +
           "AND p.status NOT IN ('COMPLETED', 'ARCHIVED')")
    long countActiveProjects(@Param("tenantId") UUID tenantId);
}
