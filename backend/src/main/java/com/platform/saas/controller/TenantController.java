package com.platform.saas.controller;

import com.platform.saas.dto.TenantRegistrationRequest;
import com.platform.saas.dto.TenantResponse;
import com.platform.saas.dto.TenantUsageResponse;
import com.platform.saas.service.ProjectService;
import com.platform.saas.service.TaskService;
import com.platform.saas.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST API controller for tenant management operations.
 * Handles tenant registration, retrieval, and subdomain validation.
 */
@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Slf4j
public class TenantController {

    private final TenantService tenantService;
    private final ProjectService projectService;
    private final TaskService taskService;

    /**
     * Register a new tenant.
     * Creates both the tenant and associates the owner as an administrator.
     *
     * POST /api/tenants
     *
     * @param request The tenant registration request
     * @return The created tenant response
     */
    @PostMapping
    public ResponseEntity<TenantResponse> registerTenant(
            @Valid @RequestBody TenantRegistrationRequest request) {
        log.info("Received tenant registration request for subdomain: {}", request.getSubdomain());

        TenantResponse response = tenantService.registerTenant(request);

        log.info("Tenant registered successfully with ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get tenant by ID.
     *
     * GET /api/tenants/{id}
     *
     * @param id The tenant ID
     * @return The tenant response
     */
    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable UUID id) {
        log.debug("Getting tenant by ID: {}", id);

        TenantResponse response = tenantService.getTenantById(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Get tenant by subdomain.
     *
     * GET /api/tenants/subdomain/{subdomain}
     *
     * @param subdomain The tenant subdomain
     * @return The tenant response
     */
    @GetMapping("/subdomain/{subdomain}")
    public ResponseEntity<TenantResponse> getTenantBySubdomain(@PathVariable String subdomain) {
        log.debug("Getting tenant by subdomain: {}", subdomain);

        TenantResponse response = tenantService.getTenantBySubdomain(subdomain);

        return ResponseEntity.ok(response);
    }

    /**
     * Validate subdomain availability.
     *
     * GET /api/tenants/validate-subdomain?subdomain={subdomain}
     *
     * @param subdomain The subdomain to validate
     * @return 200 OK if subdomain is valid and available, 4xx error otherwise
     */
    @GetMapping("/validate-subdomain")
    public ResponseEntity<Void> validateSubdomain(@RequestParam String subdomain) {
        log.debug("Validating subdomain: {}", subdomain);

        // This will throw exceptions if validation fails, which will be handled by GlobalExceptionHandler
        tenantService.validateSubdomain(subdomain);

        return ResponseEntity.ok().build();
    }

    /**
     * Get tenant quota usage information.
     *
     * GET /api/tenants/{id}/usage
     *
     * @param id The tenant ID
     * @return The tenant usage information
     */
    @GetMapping("/{id}/usage")
    public ResponseEntity<TenantUsageResponse> getTenantUsage(@PathVariable UUID id) {
        log.debug("Getting usage for tenant: {}", id);

        // Get tenant details
        TenantResponse tenant = tenantService.getTenantById(id);

        // Count projects and tasks
        long projectCount = projectService.countProjects();
        long taskCount = taskService.countTasks();
        long totalUsage = projectCount + taskCount;

        // Build response
        TenantUsageResponse usage = TenantUsageResponse.builder()
                .projectCount(projectCount)
                .taskCount(taskCount)
                .totalUsage(totalUsage)
                .quotaLimit(tenant.getQuotaLimit())
                .subscriptionTier(tenant.getSubscriptionTier())
                .build();

        usage.calculateUsagePercentage();

        return ResponseEntity.ok(usage);
    }
}
