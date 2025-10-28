package com.platform.saas.repository;

import com.platform.saas.model.AutomationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for AutomationRule entity operations.
 * Provides queries for finding automation rules by tenant, event type, and status.
 */
@Repository
public interface AutomationRuleRepository extends JpaRepository<AutomationRule, UUID> {

    /**
     * Find all automation rules for a tenant.
     * @param tenantId The tenant's ID
     * @return List of automation rules
     */
    List<AutomationRule> findByTenantId(UUID tenantId);

    /**
     * Find all active automation rules for a tenant.
     * @param tenantId The tenant's ID
     * @param isActive Whether the rule is active
     * @return List of active automation rules
     */
    List<AutomationRule> findByTenantIdAndIsActive(UUID tenantId, Boolean isActive);

    /**
     * Find all automation rules for a specific event type.
     * Used by the event publisher to determine which rules to trigger.
     * @param eventType The event type (e.g., "task.status.changed")
     * @param isActive Whether the rule is active
     * @return List of automation rules matching the event type
     */
    List<AutomationRule> findByEventTypeAndIsActive(String eventType, Boolean isActive);

    /**
     * Find all active automation rules for a tenant and event type.
     * @param tenantId The tenant's ID
     * @param eventType The event type
     * @param isActive Whether the rule is active
     * @return List of automation rules
     */
    List<AutomationRule> findByTenantIdAndEventTypeAndIsActive(
            UUID tenantId,
            String eventType,
            Boolean isActive
    );

    /**
     * Count automation rules for a tenant.
     * @param tenantId The tenant's ID
     * @return Number of automation rules
     */
    long countByTenantId(UUID tenantId);

    /**
     * Count active automation rules for a tenant.
     * @param tenantId The tenant's ID
     * @param isActive Whether the rule is active
     * @return Number of active automation rules
     */
    long countByTenantIdAndIsActive(UUID tenantId, Boolean isActive);

    /**
     * Find automation rules created by a specific user.
     * @param createdBy The user's ID
     * @return List of automation rules
     */
    List<AutomationRule> findByCreatedBy(UUID createdBy);

    /**
     * Find automation rules by action type for a tenant.
     * Useful for reporting and analytics.
     * @param tenantId The tenant's ID
     * @param actionType The action type (e.g., "send_email")
     * @return List of automation rules
     */
    List<AutomationRule> findByTenantIdAndActionType(UUID tenantId, String actionType);

    /**
     * Get most frequently executed rules for a tenant.
     * @param tenantId The tenant's ID
     * @param limit Maximum number of results
     * @return List of automation rules ordered by execution count
     */
    @Query("SELECT ar FROM AutomationRule ar WHERE ar.tenantId = :tenantId " +
           "ORDER BY ar.executionCount DESC LIMIT :limit")
    List<AutomationRule> findTopExecutedRules(@Param("tenantId") UUID tenantId, @Param("limit") int limit);
}
