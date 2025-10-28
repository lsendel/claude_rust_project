package com.platform.saas.controller;

import com.platform.saas.model.Priority;
import com.platform.saas.model.Project;
import com.platform.saas.model.ProjectStatus;
import com.platform.saas.service.ProjectService;
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
 * REST controller for project management operations.
 * All operations are tenant-scoped via TenantContext.
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Create a new project.
     * POST /api/projects
     * Requires ADMINISTRATOR or EDITOR role.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR', 'EDITOR')")
    public ResponseEntity<Project> createProject(@Valid @RequestBody Project project) {
        log.info("Creating project: name={}", project.getName());
        Project created = projectService.createProject(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get all projects for the current tenant.
     * GET /api/projects
     * PMAT: Cyc≤12, Cog≤20
     */
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) UUID ownerId,
            @RequestParam(required = false, defaultValue = "false") boolean overdueOnly,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {

        List<Project> projects = fetchProjectsByFilters(status, priority, ownerId, overdueOnly, activeOnly);
        return ResponseEntity.ok(projects);
    }

    /**
     * Fetch projects based on provided filters.
     * Extracted to reduce cyclomatic complexity of getAllProjects.
     * PMAT: Helper method for filter logic
     */
    private List<Project> fetchProjectsByFilters(
            ProjectStatus status,
            Priority priority,
            UUID ownerId,
            boolean overdueOnly,
            boolean activeOnly) {

        if (overdueOnly) {
            return projectService.getOverdueProjects();
        }
        if (activeOnly) {
            return projectService.getActiveProjects();
        }
        if (status != null) {
            return projectService.getProjectsByStatus(status);
        }
        if (priority != null) {
            return projectService.getProjectsByPriority(priority);
        }
        if (ownerId != null) {
            return projectService.getProjectsByOwner(ownerId);
        }
        return projectService.getAllProjects();
    }

    /**
     * Get a specific project by ID.
     * GET /api/projects/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable UUID id) {
        log.info("Fetching project: id={}", id);
        Project project = projectService.getProject(id);
        return ResponseEntity.ok(project);
    }

    /**
     * Update an existing project.
     * PUT /api/projects/{id}
     * Requires ADMINISTRATOR or EDITOR role.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR', 'EDITOR')")
    public ResponseEntity<Project> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody Project project) {
        log.info("Updating project: id={}", id);
        Project updated = projectService.updateProject(id, project);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a project.
     * DELETE /api/projects/{id}
     * Requires ADMINISTRATOR or EDITOR role.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR', 'EDITOR')")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        log.info("Deleting project: id={}", id);
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get project count for the current tenant.
     * GET /api/projects/count
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countProjects(@RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        long count = activeOnly ? projectService.countActiveProjects() : projectService.countProjects();
        return ResponseEntity.ok(count);
    }
}
