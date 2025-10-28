package com.platform.saas.service;

import com.platform.saas.exception.QuotaExceededException;
import com.platform.saas.exception.TenantNotFoundException;
import com.platform.saas.model.*;
import com.platform.saas.repository.ProjectRepository;
import com.platform.saas.repository.TaskRepository;
import com.platform.saas.repository.TenantRepository;
import com.platform.saas.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Service for managing tasks with quota enforcement, dependency validation, and multi-tenant isolation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TenantRepository tenantRepository;
    private final EventPublisher eventPublisher;

    /**
     * Create a new task with quota enforcement.
     * @param task The task to create
     * @return The created task
     * @throws QuotaExceededException if tenant quota is exceeded
     * @throws TenantNotFoundException if tenant not found
     */
    public Task createTask(Task task) {
        UUID tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        log.info("Creating task for tenant: {}, project: {}", tenantId, task.getProjectId());

        // Ensure task belongs to current tenant
        task.setTenantId(tenantId);

        // Validate project exists and belongs to tenant
        validateProject(task.getProjectId(), tenantId);

        // Check quota before creating
        checkQuota(tenantId);

        // Save task
        Task savedTask = taskRepository.save(task);
        log.info("Task created: id={}, name={}, tenant={}, project={}",
                savedTask.getId(), savedTask.getName(), tenantId, savedTask.getProjectId());

        // Publish event
        publishTaskCreatedEvent(savedTask);

        return savedTask;
    }

    /**
     * Get a task by ID with tenant isolation.
     * @param taskId The task ID
     * @return The task
     */
    @Transactional(readOnly = true)
    public Task getTask(UUID taskId) {
        UUID tenantId = TenantContext.getTenantId();

        return taskRepository.findByIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
    }

    /**
     * Get all tasks for the current tenant.
     * @return List of tasks
     */
    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        UUID tenantId = TenantContext.getTenantId();
        return taskRepository.findByTenantId(tenantId);
    }

    /**
     * Get all tasks for a specific project.
     * @param projectId The project ID
     * @return List of tasks
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksByProject(UUID projectId) {
        UUID tenantId = TenantContext.getTenantId();

        // Validate project belongs to tenant
        validateProject(projectId, tenantId);

        return taskRepository.findByTenantIdAndProjectId(tenantId, projectId);
    }

    /**
     * Get tasks by status for the current tenant.
     * @param status The task status
     * @return List of tasks with the given status
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksByStatus(TaskStatus status) {
        UUID tenantId = TenantContext.getTenantId();
        return taskRepository.findByTenantIdAndStatus(tenantId, status);
    }

    /**
     * Get tasks by project and status.
     * @param projectId The project ID
     * @param status The task status
     * @return List of tasks matching the criteria
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksByProjectAndStatus(UUID projectId, TaskStatus status) {
        UUID tenantId = TenantContext.getTenantId();

        // Validate project belongs to tenant
        validateProject(projectId, tenantId);

        return taskRepository.findByTenantIdAndProjectIdAndStatus(tenantId, projectId, status);
    }

    /**
     * Get tasks by priority for the current tenant.
     * @param priority The task priority
     * @return List of tasks with the given priority
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksByPriority(Priority priority) {
        UUID tenantId = TenantContext.getTenantId();
        return taskRepository.findByTenantIdAndPriority(tenantId, priority);
    }

    /**
     * Get overdue tasks for the current tenant.
     * @return List of overdue tasks
     */
    @Transactional(readOnly = true)
    public List<Task> getOverdueTasks() {
        UUID tenantId = TenantContext.getTenantId();
        return taskRepository.findOverdueTasks(tenantId, LocalDate.now());
    }

    /**
     * Get overdue tasks for a specific project.
     * @param projectId The project ID
     * @return List of overdue tasks
     */
    @Transactional(readOnly = true)
    public List<Task> getOverdueTasksForProject(UUID projectId) {
        UUID tenantId = TenantContext.getTenantId();

        // Validate project belongs to tenant
        validateProject(projectId, tenantId);

        return taskRepository.findOverdueTasksForProject(tenantId, projectId, LocalDate.now());
    }

    /**
     * Update an existing task.
     * PMAT: Cyc≤10, Cog≤15
     * @param taskId The task ID
     * @param updatedTask The updated task data
     * @return The updated task
     */
    public Task updateTask(UUID taskId, Task updatedTask) {
        UUID tenantId = TenantContext.getTenantId();

        Task existing = taskRepository.findByIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        // Track what changed
        Map<String, Object> changes = new HashMap<>();
        TaskStatus oldStatus = existing.getStatus();

        // Apply field updates
        boolean statusChanged = applyTaskFieldUpdates(existing, updatedTask, changes);

        Task saved = taskRepository.save(existing);
        log.info("Task updated: id={}, tenant={}", taskId, tenantId);

        // Publish events if there were changes
        publishUpdateEventsIfNeeded(saved, changes, statusChanged, oldStatus);

        return saved;
    }

    /**
     * Apply field updates from updatedTask to existing task.
     * Extracted to reduce cyclomatic complexity of updateTask.
     * PMAT: Helper method for field updates
     * @return true if status changed, false otherwise
     */
    private boolean applyTaskFieldUpdates(Task existing, Task updatedTask, Map<String, Object> changes) {
        boolean statusChanged = false;

        if (updatedTask.getName() != null && !updatedTask.getName().equals(existing.getName())) {
            changes.put("name", Map.of("old", existing.getName(), "new", updatedTask.getName()));
            existing.setName(updatedTask.getName());
        }
        if (updatedTask.getDescription() != null && !updatedTask.getDescription().equals(existing.getDescription())) {
            changes.put("description", Map.of("old", existing.getDescription(), "new", updatedTask.getDescription()));
            existing.setDescription(updatedTask.getDescription());
        }
        if (updatedTask.getStatus() != null && !updatedTask.getStatus().equals(existing.getStatus())) {
            changes.put("status", Map.of("old", existing.getStatus().toString(), "new", updatedTask.getStatus().toString()));
            existing.setStatus(updatedTask.getStatus());
            statusChanged = true;
        }
        if (updatedTask.getDueDate() != null && !updatedTask.getDueDate().equals(existing.getDueDate())) {
            changes.put("dueDate", Map.of("old", existing.getDueDate(), "new", updatedTask.getDueDate()));
            existing.setDueDate(updatedTask.getDueDate());
        }
        if (updatedTask.getProgressPercentage() != null && !updatedTask.getProgressPercentage().equals(existing.getProgressPercentage())) {
            changes.put("progressPercentage", Map.of("old", existing.getProgressPercentage(), "new", updatedTask.getProgressPercentage()));
            existing.setProgressPercentage(updatedTask.getProgressPercentage());
        }
        if (updatedTask.getPriority() != null && !updatedTask.getPriority().equals(existing.getPriority())) {
            changes.put("priority", Map.of("old", existing.getPriority().toString(), "new", updatedTask.getPriority().toString()));
            existing.setPriority(updatedTask.getPriority());
        }

        return statusChanged;
    }

    /**
     * Publish update events if there were changes.
     * Extracted to reduce cyclomatic complexity of updateTask.
     * PMAT: Helper method for event publishing
     */
    private void publishUpdateEventsIfNeeded(Task task, Map<String, Object> changes,
                                            boolean statusChanged, TaskStatus oldStatus) {
        if (!changes.isEmpty()) {
            publishTaskUpdatedEvent(task, changes);

            if (statusChanged) {
                publishTaskStatusChangedEvent(task, oldStatus, task.getStatus());
            }
        }
    }

    /**
     * Delete a task.
     * @param taskId The task ID
     */
    public void deleteTask(UUID taskId) {
        UUID tenantId = TenantContext.getTenantId();

        Task task = taskRepository.findByIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        // Publish event before deleting
        publishTaskDeletedEvent(task);

        taskRepository.delete(task);
        log.info("Task deleted: id={}, tenant={}", taskId, tenantId);
    }

    /**
     * Count tasks for the current tenant.
     * @return The task count
     */
    @Transactional(readOnly = true)
    public long countTasks() {
        UUID tenantId = TenantContext.getTenantId();
        return taskRepository.countByTenantId(tenantId);
    }

    /**
     * Count tasks for a specific project.
     * @param projectId The project ID
     * @return The task count
     */
    @Transactional(readOnly = true)
    public long countTasksByProject(UUID projectId) {
        UUID tenantId = TenantContext.getTenantId();

        // Validate project belongs to tenant
        validateProject(projectId, tenantId);

        return taskRepository.countByTenantIdAndProjectId(tenantId, projectId);
    }

    /**
     * Calculate average progress for a project's tasks.
     * @param projectId The project ID
     * @return The average progress percentage
     */
    @Transactional(readOnly = true)
    public Double calculateAverageProgress(UUID projectId) {
        UUID tenantId = TenantContext.getTenantId();

        // Validate project belongs to tenant
        validateProject(projectId, tenantId);

        Double avgProgress = taskRepository.calculateAverageProgress(tenantId, projectId);
        return avgProgress != null ? avgProgress : 0.0;
    }

    /**
     * Validate that a dependency relationship doesn't create a circular dependency.
     * @param blockingTaskId The task that blocks
     * @param blockedTaskId The task that is blocked
     * @throws IllegalArgumentException if circular dependency detected
     */
    public void validateDependency(UUID blockingTaskId, UUID blockedTaskId) {
        if (blockingTaskId.equals(blockedTaskId)) {
            throw new IllegalArgumentException("Task cannot depend on itself");
        }

        // TODO: Implement graph traversal to detect circular dependencies
        // This would require a TaskDependencyRepository to query existing dependencies
        log.warn("Circular dependency check not yet implemented for tasks {} -> {}",
                blockingTaskId, blockedTaskId);
    }

    /**
     * Validate that a project exists and belongs to the current tenant.
     * @param projectId The project ID
     * @param tenantId The tenant ID
     * @throws RuntimeException if project not found or doesn't belong to tenant
     */
    private void validateProject(UUID projectId, UUID tenantId) {
        projectRepository.findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new RuntimeException("Project not found or access denied: " + projectId));
    }

    /**
     * Check if tenant has exceeded their quota for tasks.
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

        // Quota applies to combined projects + tasks
        long projectCount = projectRepository.countByTenantId(tenantId);
        long taskCount = taskRepository.countByTenantId(tenantId);
        long totalCount = projectCount + taskCount;

        if (totalCount >= quotaLimit) {
            log.warn("Quota exceeded for tenant: {} - current: {} (projects: {}, tasks: {}), limit: {}",
                    tenantId, totalCount, projectCount, taskCount, quotaLimit);
            throw new QuotaExceededException(tenantId, "projects+tasks", totalCount, quotaLimit);
        }

        log.debug("Quota check passed for tenant: {} - current: {}, limit: {}",
                tenantId, totalCount, quotaLimit);
    }

    /**
     * Publish task.created event.
     */
    private void publishTaskCreatedEvent(Task task) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", task.getId().toString());
        payload.put("projectId", task.getProjectId().toString());
        payload.put("name", task.getName());
        payload.put("status", task.getStatus().toString());
        payload.put("priority", task.getPriority().toString());

        eventPublisher.publishEvent(
                task.getTenantId(),
                "task.created",
                task.getId(),
                "task",
                payload
        );
    }

    /**
     * Publish task.updated event.
     */
    private void publishTaskUpdatedEvent(Task task, Map<String, Object> changes) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", task.getId().toString());
        payload.put("projectId", task.getProjectId().toString());
        payload.put("name", task.getName());
        payload.put("status", task.getStatus().toString());
        payload.put("changes", changes);

        eventPublisher.publishEvent(
                task.getTenantId(),
                "task.updated",
                task.getId(),
                "task",
                payload
        );
    }

    /**
     * Publish task.status.changed event (specific event for status changes).
     */
    private void publishTaskStatusChangedEvent(Task task, TaskStatus oldStatus, TaskStatus newStatus) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", task.getId().toString());
        payload.put("projectId", task.getProjectId().toString());
        payload.put("name", task.getName());
        payload.put("oldStatus", oldStatus.toString());
        payload.put("newStatus", newStatus.toString());
        payload.put("priority", task.getPriority().toString());

        eventPublisher.publishEvent(
                task.getTenantId(),
                "task.status.changed",
                task.getId(),
                "task",
                payload
        );
    }

    /**
     * Publish task.deleted event.
     */
    private void publishTaskDeletedEvent(Task task) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", task.getId().toString());
        payload.put("projectId", task.getProjectId().toString());
        payload.put("name", task.getName());
        payload.put("status", task.getStatus().toString());

        eventPublisher.publishEvent(
                task.getTenantId(),
                "task.deleted",
                task.getId(),
                "task",
                payload
        );
    }
}
