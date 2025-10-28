package com.platform.saas.controller;

import com.platform.saas.model.AutomationRule;
import com.platform.saas.model.EventLog;
import com.platform.saas.service.AutomationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for automation rules and event logs.
 * All endpoints require ADMINISTRATOR role except for read operations.
 */
@RestController
@RequestMapping("/api/automations")
@RequiredArgsConstructor
@Slf4j
public class AutomationController {

    private final AutomationService automationService;

    /**
     * Create a new automation rule.
     * @param rule The automation rule to create
     * @return The created automation rule
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<AutomationRule> createRule(@Valid @RequestBody AutomationRule rule) {
        log.info("Creating automation rule: {}", rule.getName());
        AutomationRule created = automationService.createRule(rule);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get all automation rules for the current tenant.
     * @param activeOnly Optional filter to get only active rules
     * @return List of automation rules
     */
    @GetMapping
    public ResponseEntity<List<AutomationRule>> getAllRules(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        log.debug("Getting automation rules, activeOnly={}", activeOnly);

        List<AutomationRule> rules = activeOnly
                ? automationService.getActiveRules()
                : automationService.getAllRules();

        return ResponseEntity.ok(rules);
    }

    /**
     * Get a specific automation rule by ID.
     * @param id The rule ID
     * @return The automation rule
     */
    @GetMapping("/{id}")
    public ResponseEntity<AutomationRule> getRule(@PathVariable UUID id) {
        log.debug("Getting automation rule: {}", id);
        AutomationRule rule = automationService.getRule(id);
        return ResponseEntity.ok(rule);
    }

    /**
     * Get automation rules by event type.
     * @param eventType The event type (e.g., "task.status.changed")
     * @return List of automation rules for the event type
     */
    @GetMapping("/by-event-type")
    public ResponseEntity<List<AutomationRule>> getRulesByEventType(
            @RequestParam String eventType) {
        log.debug("Getting automation rules for event type: {}", eventType);
        List<AutomationRule> rules = automationService.getRulesByEventType(eventType);
        return ResponseEntity.ok(rules);
    }

    /**
     * Get top executed automation rules.
     * @param limit Maximum number of rules to return (default: 10)
     * @return List of top executed rules
     */
    @GetMapping("/top-executed")
    public ResponseEntity<List<AutomationRule>> getTopExecutedRules(
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("Getting top {} executed automation rules", limit);
        List<AutomationRule> rules = automationService.getTopExecutedRules(limit);
        return ResponseEntity.ok(rules);
    }

    /**
     * Update an existing automation rule.
     * @param id The rule ID
     * @param rule The updated rule data
     * @return The updated automation rule
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<AutomationRule> updateRule(
            @PathVariable UUID id,
            @Valid @RequestBody AutomationRule rule) {
        log.info("Updating automation rule: {}", id);
        AutomationRule updated = automationService.updateRule(id, rule);
        return ResponseEntity.ok(updated);
    }

    /**
     * Toggle automation rule active status.
     * @param id The rule ID
     * @param request Request body containing the new active status
     * @return The updated automation rule
     */
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<AutomationRule> toggleRuleStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, Boolean> request) {
        log.info("Toggling automation rule status: {}", id);

        Boolean isActive = request.get("isActive");
        if (isActive == null) {
            throw new IllegalArgumentException("isActive field is required");
        }

        AutomationRule updated = automationService.toggleRuleStatus(id, isActive);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete an automation rule.
     * @param id The rule ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID id) {
        log.info("Deleting automation rule: {}", id);
        automationService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get automation rule count.
     * @return Count of automation rules
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getRuleCount() {
        long count = automationService.countRules();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    /**
     * Get recent event logs.
     * @param limit Maximum number of logs to return (default: 50)
     * @return List of recent event logs
     */
    @GetMapping("/logs")
    public ResponseEntity<List<EventLog>> getRecentLogs(
            @RequestParam(defaultValue = "50") int limit) {
        log.debug("Getting recent {} event logs", limit);
        List<EventLog> logs = automationService.getRecentLogs(limit);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get event logs for a specific automation rule.
     * @param id The rule ID
     * @return List of event logs for the rule
     */
    @GetMapping("/{id}/logs")
    public ResponseEntity<List<EventLog>> getLogsForRule(@PathVariable UUID id) {
        log.debug("Getting event logs for automation rule: {}", id);
        List<EventLog> logs = automationService.getLogsForRule(id);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get failed event logs.
     * @return List of failed event logs
     */
    @GetMapping("/logs/failed")
    public ResponseEntity<List<EventLog>> getFailedLogs() {
        log.debug("Getting failed event logs");
        List<EventLog> logs = automationService.getFailedLogs();
        return ResponseEntity.ok(logs);
    }

    /**
     * Get event logs within a date range.
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return List of event logs within the date range
     */
    @GetMapping("/logs/date-range")
    public ResponseEntity<List<EventLog>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.debug("Getting event logs from {} to {}", startDate, endDate);
        List<EventLog> logs = automationService.getLogsByDateRange(startDate, endDate);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get automation statistics.
     * @return Statistics about automation execution
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAutomationStats() {
        log.debug("Getting automation statistics");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRules", automationService.countRules());
        stats.put("successfulExecutions", automationService.countLogsByStatus(EventLog.ExecutionStatus.SUCCESS));
        stats.put("failedExecutions", automationService.countLogsByStatus(EventLog.ExecutionStatus.FAILED));
        stats.put("skippedExecutions", automationService.countLogsByStatus(EventLog.ExecutionStatus.SKIPPED));
        stats.put("noRulesMatched", automationService.countLogsByStatus(EventLog.ExecutionStatus.NO_RULES_MATCHED));
        stats.put("averageExecutionDurationMs", automationService.getAverageExecutionDuration());

        return ResponseEntity.ok(stats);
    }
}
