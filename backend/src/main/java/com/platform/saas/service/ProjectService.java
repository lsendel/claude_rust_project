package com.platform.saas.service;

import com.platform.saas.exception.QuotaExceededException;
import com.platform.saas.exception.TenantNotFoundException;
import com.platform.saas.model.Priority;
import com.platform.saas.model.Project;
import com.platform.saas.model.ProjectStatus;
import com.platform.saas.model.Tenant;
import com.platform.saas.repository.ProjectRepository;
import com.platform.saas.repository.TenantRepository;
import com.platform.saas.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing projects with quota enforcement and multi-tenant isolation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TenantRepository tenantRepository;
    private final EventPublisher eventPublisher;

    /**
     * Create a new project with quota enforcement.
     * @param project The project to create
     * @return The created project
     * @throws QuotaExceededException if tenant quota is exceeded
     * @throws TenantNotFoundException if tenant not found
     */
    public Project createProject(Project project) {
        UUID tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        log.info("Creating project for tenant: {}", tenantId);

        // Ensure project belongs to current tenant
        project.setTenantId(tenantId);

        // Check quota before creating
        checkQuota(tenantId);

        // Save project
        Project savedProject = projectRepository.save(project);
        log.info("Project created: id={}, name={}, tenant={}",
                savedProject.getId(), savedProject.getName(), tenantId);

        // Publish event
        publishProjectCreatedEvent(savedProject);

        return savedProject;
    }

    /**
     * Get a project by ID with tenant isolation.
     * @param projectId The project ID
     * @return The project
     */
    @Transactional(readOnly = true)
    public Project getProject(UUID projectId) {
        UUID tenantId = TenantContext.getTenantId();

        return projectRepository.findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));
    }

    /**
     * Get all projects for the current tenant.
     * @return List of projects
     */
    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
        UUID tenantId = TenantContext.getTenantId();
        return projectRepository.findByTenantId(tenantId);
    }

    /**
     * Get projects by status for the current tenant.
     * @param status The project status
     * @return List of projects with the given status
     */
    @Transactional(readOnly = true)
    public List<Project> getProjectsByStatus(ProjectStatus status) {
        UUID tenantId = TenantContext.getTenantId();
        return projectRepository.findByTenantIdAndStatus(tenantId, status);
    }

    /**
     * Get projects by priority for the current tenant.
     * @param priority The project priority
     * @return List of projects with the given priority
     */
    @Transactional(readOnly = true)
    public List<Project> getProjectsByPriority(Priority priority) {
        UUID tenantId = TenantContext.getTenantId();
        return projectRepository.findByTenantIdAndPriority(tenantId, priority);
    }

    /**
     * Get projects by owner for the current tenant.
     * @param ownerId The owner user ID
     * @return List of projects owned by the user
     */
    @Transactional(readOnly = true)
    public List<Project> getProjectsByOwner(UUID ownerId) {
        UUID tenantId = TenantContext.getTenantId();
        return projectRepository.findByTenantIdAndOwnerId(tenantId, ownerId);
    }

    /**
     * Get overdue projects for the current tenant.
     * @return List of overdue projects
     */
    @Transactional(readOnly = true)
    public List<Project> getOverdueProjects() {
        UUID tenantId = TenantContext.getTenantId();
        return projectRepository.findOverdueProjects(tenantId, LocalDate.now());
    }

    /**
     * Get active projects for the current tenant.
     * @return List of active projects
     */
    @Transactional(readOnly = true)
    public List<Project> getActiveProjects() {
        UUID tenantId = TenantContext.getTenantId();
        return projectRepository.findActiveProjects(tenantId);
    }

    /**
     * Update an existing project.
     * @param projectId The project ID
     * @param updatedProject The updated project data
     * @return The updated project
     */
    public Project updateProject(UUID projectId, Project updatedProject) {
        UUID tenantId = TenantContext.getTenantId();

        Project existing = projectRepository.findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));

        // Track what changed
        Map<String, Object> changes = new HashMap<>();

        // Update fields
        if (updatedProject.getName() != null && !updatedProject.getName().equals(existing.getName())) {
            changes.put("name", Map.of("old", existing.getName(), "new", updatedProject.getName()));
            existing.setName(updatedProject.getName());
        }
        if (updatedProject.getDescription() != null && !updatedProject.getDescription().equals(existing.getDescription())) {
            changes.put("description", Map.of("old", existing.getDescription(), "new", updatedProject.getDescription()));
            existing.setDescription(updatedProject.getDescription());
        }
        if (updatedProject.getStatus() != null && !updatedProject.getStatus().equals(existing.getStatus())) {
            changes.put("status", Map.of("old", existing.getStatus().toString(), "new", updatedProject.getStatus().toString()));
            existing.setStatus(updatedProject.getStatus());
        }
        if (updatedProject.getDueDate() != null && !updatedProject.getDueDate().equals(existing.getDueDate())) {
            changes.put("dueDate", Map.of("old", existing.getDueDate(), "new", updatedProject.getDueDate()));
            existing.setDueDate(updatedProject.getDueDate());
        }
        if (updatedProject.getOwnerId() != null && !updatedProject.getOwnerId().equals(existing.getOwnerId())) {
            changes.put("ownerId", Map.of("old", existing.getOwnerId(), "new", updatedProject.getOwnerId()));
            existing.setOwnerId(updatedProject.getOwnerId());
        }
        if (updatedProject.getProgressPercentage() != null && !updatedProject.getProgressPercentage().equals(existing.getProgressPercentage())) {
            changes.put("progressPercentage", Map.of("old", existing.getProgressPercentage(), "new", updatedProject.getProgressPercentage()));
            existing.setProgressPercentage(updatedProject.getProgressPercentage());
        }
        if (updatedProject.getPriority() != null && !updatedProject.getPriority().equals(existing.getPriority())) {
            changes.put("priority", Map.of("old", existing.getPriority().toString(), "new", updatedProject.getPriority().toString()));
            existing.setPriority(updatedProject.getPriority());
        }

        Project saved = projectRepository.save(existing);
        log.info("Project updated: id={}, tenant={}", projectId, tenantId);

        // Publish event if there were changes
        if (!changes.isEmpty()) {
            publishProjectUpdatedEvent(saved, changes);
        }

        return saved;
    }

    /**
     * Delete a project.
     * @param projectId The project ID
     */
    public void deleteProject(UUID projectId) {
        UUID tenantId = TenantContext.getTenantId();

        Project project = projectRepository.findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));

        // Publish event before deleting
        publishProjectDeletedEvent(project);

        projectRepository.delete(project);
        log.info("Project deleted: id={}, tenant={}", projectId, tenantId);
    }

    /**
     * Count projects for the current tenant.
     * @return The project count
     */
    @Transactional(readOnly = true)
    public long countProjects() {
        UUID tenantId = TenantContext.getTenantId();
        return projectRepository.countByTenantId(tenantId);
    }

    /**
     * Count active projects for the current tenant.
     * @return The active project count
     */
    @Transactional(readOnly = true)
    public long countActiveProjects() {
        UUID tenantId = TenantContext.getTenantId();
        return projectRepository.countActiveProjects(tenantId);
    }

    /**
     * Check if tenant has exceeded their quota for projects.
     * @param tenantId The tenant ID
     * @throws QuotaExceededException if quota is exceeded
     * @throws TenantNotFoundException if tenant not found
     */
    private void checkQuota(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        Integer quotaLimit = tenant.getQuotaLimit();

        // Enterprise tier has unlimited quota (null quota limit)
        if (quotaLimit == null) {
            log.debug("Enterprise tier - no quota limit for tenant: {}", tenantId);
            return;
        }

        long currentCount = projectRepository.countByTenantId(tenantId);

        if (currentCount >= quotaLimit) {
            log.warn("Quota exceeded for tenant: {} - current: {}, limit: {}",
                    tenantId, currentCount, quotaLimit);
            throw new QuotaExceededException(tenantId, "projects", currentCount, quotaLimit);
        }

        log.debug("Quota check passed for tenant: {} - current: {}, limit: {}",
                tenantId, currentCount, quotaLimit);
    }

    /**
     * Publish project.created event.
     */
    private void publishProjectCreatedEvent(Project project) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("projectId", project.getId().toString());
        payload.put("name", project.getName());
        payload.put("status", project.getStatus().toString());
        payload.put("priority", project.getPriority().toString());
        payload.put("ownerId", project.getOwnerId().toString());

        eventPublisher.publishEvent(
                project.getTenantId(),
                "project.created",
                project.getId(),
                "project",
                payload
        );
    }

    /**
     * Publish project.updated event.
     */
    private void publishProjectUpdatedEvent(Project project, Map<String, Object> changes) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("projectId", project.getId().toString());
        payload.put("name", project.getName());
        payload.put("status", project.getStatus().toString());
        payload.put("changes", changes);

        eventPublisher.publishEvent(
                project.getTenantId(),
                "project.updated",
                project.getId(),
                "project",
                payload
        );
    }

    /**
     * Publish project.deleted event.
     */
    private void publishProjectDeletedEvent(Project project) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("projectId", project.getId().toString());
        payload.put("name", project.getName());
        payload.put("status", project.getStatus().toString());

        eventPublisher.publishEvent(
                project.getTenantId(),
                "project.deleted",
                project.getId(),
                "project",
                payload
        );
    }
}
