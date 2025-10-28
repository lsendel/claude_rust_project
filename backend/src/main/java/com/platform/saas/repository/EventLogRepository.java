package com.platform.saas.repository;

import com.platform.saas.model.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for EventLog entity operations.
 * Provides queries for audit trail and automation execution history.
 */
@Repository
public interface EventLogRepository extends JpaRepository<EventLog, UUID> {

    /**
     * Find all event logs for a tenant.
     * @param tenantId The tenant's ID
     * @return List of event logs
     */
    List<EventLog> findByTenantId(UUID tenantId);

    /**
     * Find event logs for a specific automation rule.
     * @param automationRuleId The automation rule's ID
     * @return List of event logs
     */
    List<EventLog> findByAutomationRuleId(UUID automationRuleId);

    /**
     * Find event logs for a tenant ordered by created date descending.
     * @param tenantId The tenant's ID
     * @return List of event logs
     */
    List<EventLog> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    /**
     * Find recent event logs for a tenant with limit.
     * @param tenantId The tenant's ID
     * @param limit Maximum number of results
     * @return List of recent event logs
     */
    @Query("SELECT el FROM EventLog el WHERE el.tenantId = :tenantId " +
           "ORDER BY el.createdAt DESC LIMIT :limit")
    List<EventLog> findRecentLogs(@Param("tenantId") UUID tenantId, @Param("limit") int limit);

    /**
     * Find event logs by status for a tenant.
     * @param tenantId The tenant's ID
     * @param status The execution status
     * @return List of event logs
     */
    List<EventLog> findByTenantIdAndStatus(UUID tenantId, EventLog.ExecutionStatus status);

    /**
     * Find failed event logs for a tenant.
     * Useful for monitoring and alerting.
     * @param tenantId The tenant's ID
     * @param status The execution status (FAILED)
     * @return List of failed event logs
     */
    List<EventLog> findByTenantIdAndStatusOrderByCreatedAtDesc(
            UUID tenantId,
            EventLog.ExecutionStatus status
    );

    /**
     * Find event logs within a date range.
     * @param tenantId The tenant's ID
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return List of event logs
     */
    @Query("SELECT el FROM EventLog el WHERE el.tenantId = :tenantId " +
           "AND el.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY el.createdAt DESC")
    List<EventLog> findByTenantIdAndDateRange(
            @Param("tenantId") UUID tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Count event logs by status for a tenant.
     * @param tenantId The tenant's ID
     * @param status The execution status
     * @return Count of event logs
     */
    long countByTenantIdAndStatus(UUID tenantId, EventLog.ExecutionStatus status);

    /**
     * Find event logs for a specific resource (e.g., a task or project).
     * @param resourceId The resource's ID
     * @param resourceType The resource type (e.g., "task", "project")
     * @return List of event logs
     */
    List<EventLog> findByResourceIdAndResourceType(UUID resourceId, String resourceType);

    /**
     * Delete old event logs for cleanup.
     * @param before Delete logs created before this date
     */
    void deleteByCreatedAtBefore(LocalDateTime before);

    /**
     * Get average execution duration for a tenant.
     * @param tenantId The tenant's ID
     * @return Average execution duration in milliseconds
     */
    @Query("SELECT AVG(el.executionDurationMs) FROM EventLog el " +
           "WHERE el.tenantId = :tenantId AND el.executionDurationMs IS NOT NULL")
    Double getAverageExecutionDuration(@Param("tenantId") UUID tenantId);
}
