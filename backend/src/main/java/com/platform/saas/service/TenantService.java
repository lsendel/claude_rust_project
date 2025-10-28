package com.platform.saas.service;

import com.platform.saas.dto.TenantRegistrationRequest;
import com.platform.saas.dto.TenantResponse;
import com.platform.saas.exception.InvalidSubdomainException;
import com.platform.saas.exception.QuotaExceededException;
import com.platform.saas.exception.SubdomainAlreadyExistsException;
import com.platform.saas.exception.TenantNotFoundException;
import com.platform.saas.model.SubscriptionTier;
import com.platform.saas.model.Tenant;
import com.platform.saas.model.User;
import com.platform.saas.model.UserRole;
import com.platform.saas.repository.ProjectRepository;
import com.platform.saas.repository.TaskRepository;
import com.platform.saas.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service for tenant management operations.
 * Handles tenant creation, validation, quota enforcement, and lifecycle management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;

    /**
     * Reserved subdomains that cannot be used for tenant registration.
     */
    private static final Set<String> RESERVED_SUBDOMAINS = Set.of(
            "www", "api", "admin", "app", "mail", "email", "support",
            "help", "docs", "blog", "status", "cdn", "assets", "static",
            "ftp", "smtp", "pop", "imap", "webmail", "portal", "dashboard"
    );

    /**
     * Register a new tenant with an owner user.
     * Creates both the tenant and associates the owner as an administrator.
     *
     * @param request The tenant registration request
     * @return The created tenant response
     * @throws SubdomainAlreadyExistsException if subdomain is already taken
     * @throws InvalidSubdomainException if subdomain fails validation
     */
    @Transactional
    public TenantResponse registerTenant(TenantRegistrationRequest request) {
        log.info("Registering new tenant with subdomain: {}", request.getSubdomain());

        // Validate subdomain
        validateSubdomain(request.getSubdomain());

        // Check if subdomain already exists
        if (tenantRepository.existsBySubdomain(request.getSubdomain())) {
            log.warn("Subdomain already exists: {}", request.getSubdomain());
            throw new SubdomainAlreadyExistsException(request.getSubdomain());
        }

        // Create tenant
        Tenant tenant = new Tenant();
        tenant.setSubdomain(request.getSubdomain().toLowerCase());
        tenant.setName(request.getName());
        tenant.setDescription(request.getDescription());
        tenant.setSubscriptionTier(request.getSubscriptionTier());
        tenant.setQuotaLimit(getQuotaForTier(request.getSubscriptionTier()));
        tenant.setActive(true);

        tenant = tenantRepository.save(tenant);
        log.info("Tenant created with ID: {}", tenant.getId());

        // Create owner user and associate with tenant
        User owner = userService.findOrCreateUserByEmail(
                request.getOwnerEmail(),
                request.getOwnerName()
        );
        userService.addUserToTenant(owner.getId(), tenant.getId(), UserRole.ADMINISTRATOR, null);

        log.info("Tenant registration completed for subdomain: {}", request.getSubdomain());
        return toTenantResponse(tenant, 0L);
    }

    /**
     * Get tenant by ID.
     *
     * @param tenantId The tenant ID
     * @return The tenant response
     * @throws TenantNotFoundException if tenant not found
     */
    @Transactional(readOnly = true)
    public TenantResponse getTenantById(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        long currentUsage = calculateCurrentUsage(tenantId);
        return toTenantResponse(tenant, currentUsage);
    }

    /**
     * Get tenant by subdomain.
     *
     * @param subdomain The tenant subdomain
     * @return The tenant response
     * @throws TenantNotFoundException if tenant not found
     */
    @Transactional(readOnly = true)
    public TenantResponse getTenantBySubdomain(String subdomain) {
        Tenant tenant = tenantRepository.findBySubdomain(subdomain.toLowerCase())
                .orElseThrow(() -> new TenantNotFoundException(subdomain));

        long currentUsage = calculateCurrentUsage(tenant.getId());
        return toTenantResponse(tenant, currentUsage);
    }

    /**
     * Validate subdomain against business rules.
     *
     * @param subdomain The subdomain to validate
     * @throws InvalidSubdomainException if validation fails
     */
    public void validateSubdomain(String subdomain) {
        if (subdomain == null || subdomain.isBlank()) {
            throw new InvalidSubdomainException("Subdomain cannot be empty");
        }

        String normalizedSubdomain = subdomain.toLowerCase().trim();

        // Check length
        if (normalizedSubdomain.length() < 3 || normalizedSubdomain.length() > 63) {
            throw new InvalidSubdomainException(subdomain, "must be between 3 and 63 characters");
        }

        // Check format (lowercase, numbers, hyphens only)
        if (!normalizedSubdomain.matches("^[a-z0-9-]{3,63}$")) {
            throw new InvalidSubdomainException(subdomain, "must contain only lowercase letters, numbers, and hyphens");
        }

        // Cannot start or end with hyphen
        if (normalizedSubdomain.startsWith("-") || normalizedSubdomain.endsWith("-")) {
            throw new InvalidSubdomainException(subdomain, "cannot start or end with a hyphen");
        }

        // Check reserved words
        if (RESERVED_SUBDOMAINS.contains(normalizedSubdomain)) {
            throw new InvalidSubdomainException(subdomain, "is reserved and cannot be used");
        }

        log.debug("Subdomain validation passed: {}", subdomain);
    }

    /**
     * Check if tenant has exceeded their quota.
     *
     * @param tenantId The tenant ID
     * @throws QuotaExceededException if quota is exceeded
     */
    public void enforceQuota(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        if (tenant.getQuotaLimit() == null) {
            // No quota limit (unlimited tier)
            return;
        }

        long currentUsage = calculateCurrentUsage(tenantId);

        if (tenant.isQuotaExceeded(currentUsage)) {
            log.warn("Quota exceeded for tenant {}: {}/{}", tenantId, currentUsage, tenant.getQuotaLimit());
            throw new QuotaExceededException(tenantId, "projects and tasks", currentUsage, tenant.getQuotaLimit());
        }
    }

    /**
     * Calculate current usage (projects + tasks) for a tenant.
     *
     * @param tenantId The tenant ID
     * @return The total count of projects and tasks
     */
    private long calculateCurrentUsage(UUID tenantId) {
        long projectCount = projectRepository.countByTenantId(tenantId);
        long taskCount = taskRepository.countByTenantId(tenantId);
        return projectCount + taskCount;
    }

    /**
     * Get quota limit for a subscription tier.
     *
     * @param tier The subscription tier
     * @return The quota limit (null for unlimited)
     */
    private Integer getQuotaForTier(SubscriptionTier tier) {
        return switch (tier) {
            case FREE -> 50;
            case PRO -> 1000;
            case ENTERPRISE -> null; // Unlimited
        };
    }

    /**
     * Convert Tenant entity to TenantResponse DTO.
     *
     * @param tenant The tenant entity
     * @param currentUsage The current usage count
     * @return The tenant response DTO
     */
    private TenantResponse toTenantResponse(Tenant tenant, Long currentUsage) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .subdomain(tenant.getSubdomain())
                .name(tenant.getName())
                .description(tenant.getDescription())
                .subscriptionTier(tenant.getSubscriptionTier())
                .quotaLimit(tenant.getQuotaLimit())
                .currentUsage(currentUsage)
                .isActive(tenant.isActive())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }
}
