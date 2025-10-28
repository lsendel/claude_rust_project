package com.platform.saas.controller;

import com.platform.saas.model.Priority;
import com.platform.saas.model.Task;
import com.platform.saas.model.TaskStatus;
import com.platform.saas.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for task management operations.
 * All operations are tenant-scoped via TenantContext.
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    /**
     * Create a new task.
     * POST /api/tasks
     * Requires ADMINISTRATOR or EDITOR role.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR', 'EDITOR')")
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task) {
        log.info("Creating task: name={}, project={}", task.getName(), task.getProjectId());
        Task created = taskService.createTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get all tasks for the current tenant.
     * GET /api/tasks
     * PMAT: Cyc≤10, Cog≤15 - Filter logic extracted to helper method
     */
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false, defaultValue = "false") boolean overdueOnly) {

        List<Task> tasks = fetchTasksByFilters(projectId, status, priority, overdueOnly);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Helper method to fetch tasks based on provided filters.
     * Extracted to maintain PMAT compliance (Cyc≤10, Cog≤15).
     * PMAT: Cyc=8, Cog=10
     */
    private List<Task> fetchTasksByFilters(UUID projectId, TaskStatus status,
                                           Priority priority, boolean overdueOnly) {
        if (projectId != null && status != null) {
            return taskService.getTasksByProjectAndStatus(projectId, status);
        }
        if (projectId != null) {
            return overdueOnly ? taskService.getOverdueTasksForProject(projectId)
                               : taskService.getTasksByProject(projectId);
        }
        if (status != null) {
            return taskService.getTasksByStatus(status);
        }
        if (priority != null) {
            return taskService.getTasksByPriority(priority);
        }
        if (overdueOnly) {
            return taskService.getOverdueTasks();
        }
        return taskService.getAllTasks();
    }

    /**
     * Get a specific task by ID.
     * GET /api/tasks/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable UUID id) {
        log.info("Fetching task: id={}", id);
        Task task = taskService.getTask(id);
        return ResponseEntity.ok(task);
    }

    /**
     * Update an existing task.
     * PUT /api/tasks/{id}
     * Requires ADMINISTRATOR or EDITOR role.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR', 'EDITOR')")
    public ResponseEntity<Task> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody Task task) {
        log.info("Updating task: id={}", id);
        Task updated = taskService.updateTask(id, task);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a task.
     * DELETE /api/tasks/{id}
     * Requires ADMINISTRATOR or EDITOR role.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR', 'EDITOR')")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        log.info("Deleting task: id={}", id);
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get task count for the current tenant or a specific project.
     * GET /api/tasks/count
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countTasks(@RequestParam(required = false) UUID projectId) {
        long count = projectId != null
                ? taskService.countTasksByProject(projectId)
                : taskService.countTasks();
        return ResponseEntity.ok(count);
    }

    /**
     * Get average progress for a project's tasks.
     * GET /api/tasks/progress/average
     */
    @GetMapping("/progress/average")
    public ResponseEntity<Double> getAverageProgress(@RequestParam UUID projectId) {
        Double avgProgress = taskService.calculateAverageProgress(projectId);
        return ResponseEntity.ok(avgProgress);
    }

    // TODO: Add assignee management endpoints
    // POST /api/tasks/{id}/assignees - Assign user to task
    // DELETE /api/tasks/{id}/assignees/{userId} - Remove assignee
    // GET /api/tasks/{id}/assignees - List task assignees

    // TODO: Add dependency management endpoints
    // POST /api/tasks/{id}/dependencies - Add task dependency
    // DELETE /api/tasks/{id}/dependencies/{blockingTaskId} - Remove dependency
    // GET /api/tasks/{id}/dependencies - List task dependencies
}
