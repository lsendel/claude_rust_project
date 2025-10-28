package com.platform.saas.service;

import com.platform.saas.model.AutomationRule;
import com.platform.saas.model.EventLog;
import com.platform.saas.repository.AutomationRuleRepository;
import com.platform.saas.repository.EventLogRepository;
import com.platform.saas.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing automation rules.
 * Handles CRUD operations and provides access to event logs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AutomationService {

    private final AutomationRuleRepository automationRuleRepository;
    private final EventLogRepository eventLogRepository;

    /**
     * Create a new automation rule.
     * @param rule The automation rule to create
     * @return The created automation rule
     */
    public AutomationRule createRule(AutomationRule rule) {
        UUID tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        log.info("Creating automation rule for tenant: {}", tenantId);

        // Ensure rule belongs to current tenant
        rule.setTenantId(tenantId);

        // Initialize defaults
        if (rule.getIsActive() == null) {
            rule.setIsActive(true);
        }
        if (rule.getExecutionCount() == null) {
            rule.setExecutionCount(0L);
        }

        AutomationRule savedRule = automationRuleRepository.save(rule);
        log.info("Automation rule created: id={}, name={}, event={}, action={}, tenant={}",
                savedRule.getId(), savedRule.getName(), savedRule.getEventType(),
                savedRule.getActionType(), tenantId);

        return savedRule;
    }

    /**
     * Get an automation rule by ID with tenant isolation.
     * @param ruleId The rule ID
     * @return The automation rule
     */
    @Transactional(readOnly = true)
    public AutomationRule getRule(UUID ruleId) {
        UUID tenantId = TenantContext.getTenantId();

        return automationRuleRepository.findById(ruleId)
                .filter(rule -> rule.getTenantId().equals(tenantId))
                .orElseThrow(() -> new RuntimeException("Automation rule not found: " + ruleId));
    }

    /**
     * Get all automation rules for the current tenant.
     * @return List of automation rules
     */
    @Transactional(readOnly = true)
    public List<AutomationRule> getAllRules() {
        UUID tenantId = TenantContext.getTenantId();
        return automationRuleRepository.findByTenantId(tenantId);
    }

    /**
     * Get active automation rules for the current tenant.
     * @return List of active automation rules
     */
    @Transactional(readOnly = true)
    public List<AutomationRule> getActiveRules() {
        UUID tenantId = TenantContext.getTenantId();
        return automationRuleRepository.findByTenantIdAndIsActive(tenantId, true);
    }

    /**
     * Get automation rules by event type.
     * @param eventType The event type (e.g., "task.status.changed")
     * @return List of automation rules for the event type
     */
    @Transactional(readOnly = true)
    public List<AutomationRule> getRulesByEventType(String eventType) {
        UUID tenantId = TenantContext.getTenantId();
        return automationRuleRepository.findByTenantIdAndEventTypeAndIsActive(tenantId, eventType, true);
    }

    /**
     * Get top executed automation rules for the current tenant.
     * @param limit Maximum number of rules to return
     * @return List of top executed rules
     */
    @Transactional(readOnly = true)
    public List<AutomationRule> getTopExecutedRules(int limit) {
        UUID tenantId = TenantContext.getTenantId();
        return automationRuleRepository.findTopExecutedRules(tenantId, limit);
    }

    /**
     * Update an existing automation rule.
     * @param ruleId The rule ID
     * @param updatedRule The updated rule data
     * @return The updated automation rule
     */
    public AutomationRule updateRule(UUID ruleId, AutomationRule updatedRule) {
        UUID tenantId = TenantContext.getTenantId();

        AutomationRule existing = automationRuleRepository.findById(ruleId)
                .filter(rule -> rule.getTenantId().equals(tenantId))
                .orElseThrow(() -> new RuntimeException("Automation rule not found: " + ruleId));

        // Update fields
        if (updatedRule.getName() != null) {
            existing.setName(updatedRule.getName());
        }
        if (updatedRule.getEventType() != null) {
            existing.setEventType(updatedRule.getEventType());
        }
        if (updatedRule.getActionType() != null) {
            existing.setActionType(updatedRule.getActionType());
        }
        if (updatedRule.getConditions() != null) {
            existing.setConditions(updatedRule.getConditions());
        }
        if (updatedRule.getActionConfig() != null) {
            existing.setActionConfig(updatedRule.getActionConfig());
        }
        if (updatedRule.getIsActive() != null) {
            existing.setIsActive(updatedRule.getIsActive());
        }

        AutomationRule saved = automationRuleRepository.save(existing);
        log.info("Automation rule updated: id={}, tenant={}", ruleId, tenantId);

        return saved;
    }

    /**
     * Delete an automation rule.
     * @param ruleId The rule ID
     */
    public void deleteRule(UUID ruleId) {
        UUID tenantId = TenantContext.getTenantId();

        AutomationRule rule = automationRuleRepository.findById(ruleId)
                .filter(r -> r.getTenantId().equals(tenantId))
                .orElseThrow(() -> new RuntimeException("Automation rule not found: " + ruleId));

        automationRuleRepository.delete(rule);
        log.info("Automation rule deleted: id={}, tenant={}", ruleId, tenantId);
    }

    /**
     * Toggle automation rule active status.
     * @param ruleId The rule ID
     * @param isActive The new active status
     * @return The updated automation rule
     */
    public AutomationRule toggleRuleStatus(UUID ruleId, boolean isActive) {
        UUID tenantId = TenantContext.getTenantId();

        AutomationRule rule = automationRuleRepository.findById(ruleId)
                .filter(r -> r.getTenantId().equals(tenantId))
                .orElseThrow(() -> new RuntimeException("Automation rule not found: " + ruleId));

        rule.setIsActive(isActive);
        AutomationRule saved = automationRuleRepository.save(rule);

        log.info("Automation rule status changed: id={}, active={}, tenant={}",
                ruleId, isActive, tenantId);

        return saved;
    }

    /**
     * Count automation rules for the current tenant.
     * @return The rule count
     */
    @Transactional(readOnly = true)
    public long countRules() {
        UUID tenantId = TenantContext.getTenantId();
        return automationRuleRepository.countByTenantId(tenantId);
    }

    /**
     * Get recent event logs for the current tenant.
     * @param limit Maximum number of logs to return
     * @return List of recent event logs
     */
    @Transactional(readOnly = true)
    public List<EventLog> getRecentLogs(int limit) {
        UUID tenantId = TenantContext.getTenantId();
        return eventLogRepository.findRecentLogs(tenantId, limit);
    }

    /**
     * Get event logs for a specific automation rule.
     * @param ruleId The rule ID
     * @return List of event logs for the rule
     */
    @Transactional(readOnly = true)
    public List<EventLog> getLogsForRule(UUID ruleId) {
        UUID tenantId = TenantContext.getTenantId();

        // Verify rule belongs to tenant
        automationRuleRepository.findById(ruleId)
                .filter(rule -> rule.getTenantId().equals(tenantId))
                .orElseThrow(() -> new RuntimeException("Automation rule not found: " + ruleId));

        return eventLogRepository.findByAutomationRuleId(ruleId);
    }

    /**
     * Get failed event logs for the current tenant.
     * @return List of failed event logs
     */
    @Transactional(readOnly = true)
    public List<EventLog> getFailedLogs() {
        UUID tenantId = TenantContext.getTenantId();
        return eventLogRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(
                tenantId,
                EventLog.ExecutionStatus.FAILED
        );
    }

    /**
     * Get event logs within a date range.
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return List of event logs within the date range
     */
    @Transactional(readOnly = true)
    public List<EventLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        UUID tenantId = TenantContext.getTenantId();
        return eventLogRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    /**
     * Get count of event logs by status.
     * @param status The execution status
     * @return Count of event logs with the given status
     */
    @Transactional(readOnly = true)
    public long countLogsByStatus(EventLog.ExecutionStatus status) {
        UUID tenantId = TenantContext.getTenantId();
        return eventLogRepository.countByTenantIdAndStatus(tenantId, status);
    }

    /**
     * Get average execution duration for automations.
     * @return Average execution duration in milliseconds
     */
    @Transactional(readOnly = true)
    public Double getAverageExecutionDuration() {
        UUID tenantId = TenantContext.getTenantId();
        Double avg = eventLogRepository.getAverageExecutionDuration(tenantId);
        return avg != null ? avg : 0.0;
    }
}
