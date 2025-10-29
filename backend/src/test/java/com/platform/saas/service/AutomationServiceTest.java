package com.platform.saas.service;

import com.platform.saas.model.AutomationRule;
import com.platform.saas.model.EventLog;
import com.platform.saas.repository.AutomationRuleRepository;
import com.platform.saas.repository.EventLogRepository;
import com.platform.saas.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AutomationService.
 *
 * Sprint 4 - Service Layer Testing
 * Target: 385 missed instructions → ~308 covered (80% of class)
 *
 * Test Categories:
 * 1. createRule() - tenant isolation, defaults initialization
 * 2. getRule() - tenant isolation
 * 3. getAllRules(), getActiveRules(), getRulesByEventType()
 * 4. getTopExecutedRules()
 * 5. updateRule() - field updates, tenant isolation
 * 6. deleteRule() - tenant isolation
 * 7. toggleRuleStatus()
 * 8. countRules()
 * 9. getRecentLogs(), getLogsForRule(), getFailedLogs()
 * 10. getLogsByDateRange(), countLogsByStatus(), getAverageExecutionDuration()
 * 11. Error handling - null tenant context
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AutomationService Tests")
class AutomationServiceTest {

    @Mock
    private AutomationRuleRepository automationRuleRepository;

    @Mock
    private EventLogRepository eventLogRepository;

    @InjectMocks
    private AutomationService automationService;

    private MockedStatic<TenantContext> tenantContextMock;

    private UUID tenantId;
    private UUID ruleId;
    private AutomationRule testRule;

    @BeforeEach
    void setUp() {
        tenantContextMock = mockStatic(TenantContext.class);
        tenantId = UUID.randomUUID();
        ruleId = UUID.randomUUID();

        testRule = new AutomationRule();
        testRule.setId(ruleId);
        testRule.setTenantId(tenantId);
        testRule.setName("Test Automation Rule");
        testRule.setEventType("task.status.changed");
        testRule.setActionType("send_notification");
        testRule.setIsActive(true);
        testRule.setExecutionCount(0L);
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    // ==================== createRule() Tests ====================

    @Test
    @DisplayName("Should create automation rule with tenant ID from context")
    void createRule_ValidRule_SetsTenantId() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);

        AutomationRule newRule = new AutomationRule();
        newRule.setName("New Rule");
        newRule.setEventType("project.created");
        newRule.setActionType("send_email");

        when(automationRuleRepository.save(any(AutomationRule.class)))
                .thenAnswer(invocation -> {
                    AutomationRule saved = invocation.getArgument(0);
                    saved.setId(UUID.randomUUID());
                    return saved;
                });

        // When
        AutomationRule result = automationService.createRule(newRule);

        // Then
        assertThat(result.getTenantId()).isEqualTo(tenantId);
        assertThat(result.getName()).isEqualTo("New Rule");
        assertThat(result.getEventType()).isEqualTo("project.created");
        assertThat(result.getActionType()).isEqualTo("send_email");

        verify(automationRuleRepository).save(any(AutomationRule.class));
    }

    @Test
    @DisplayName("Should initialize default values when creating rule")
    void createRule_NoDefaults_InitializesDefaults() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);

        AutomationRule newRule = new AutomationRule();
        newRule.setName("Rule without defaults");
        newRule.setEventType("task.created");
        newRule.setActionType("webhook");
        // No isActive or executionCount set

        when(automationRuleRepository.save(any(AutomationRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AutomationRule result = automationService.createRule(newRule);

        // Then
        assertThat(result.getIsActive()).isTrue(); // Default is true
        assertThat(result.getExecutionCount()).isEqualTo(0L); // Default is 0

        verify(automationRuleRepository).save(any(AutomationRule.class));
    }

    @Test
    @DisplayName("Should not override isActive if already set")
    void createRule_IsActiveSet_DoesNotOverride() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);

        AutomationRule newRule = new AutomationRule();
        newRule.setName("Inactive Rule");
        newRule.setEventType("task.deleted");
        newRule.setActionType("log");
        newRule.setIsActive(false); // Explicitly set to false
        newRule.setExecutionCount(10L); // Explicitly set

        when(automationRuleRepository.save(any(AutomationRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AutomationRule result = automationService.createRule(newRule);

        // Then
        assertThat(result.getIsActive()).isFalse(); // Should remain false
        assertThat(result.getExecutionCount()).isEqualTo(10L); // Should remain 10

        verify(automationRuleRepository).save(any(AutomationRule.class));
    }

    @Test
    @DisplayName("Should throw exception when tenant context not set")
    void createRule_NoTenantContext_ThrowsException() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(null);

        AutomationRule newRule = new AutomationRule();
        newRule.setName("Test Rule");

        // When / Then
        assertThatThrownBy(() -> automationService.createRule(newRule))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Tenant context not set");

        verify(automationRuleRepository, never()).save(any(AutomationRule.class));
    }

    // ==================== getRule() Tests ====================

    @Test
    @DisplayName("Should get rule by ID when belongs to tenant")
    void getRule_ExistsAndBelongsToTenant_ReturnsRule() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);
        when(automationRuleRepository.findById(ruleId)).thenReturn(Optional.of(testRule));

        // When
        AutomationRule result = automationService.getRule(ruleId);

        // Then
        assertThat(result).isEqualTo(testRule);
        assertThat(result.getTenantId()).isEqualTo(tenantId);

        verify(automationRuleRepository).findById(ruleId);
    }

    @Test
    @DisplayName("Should throw exception when rule belongs to different tenant")
    void getRule_BelongsToDifferentTenant_ThrowsException() {
        // Given
        UUID differentTenantId = UUID.randomUUID();
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(differentTenantId);
        when(automationRuleRepository.findById(ruleId)).thenReturn(Optional.of(testRule));

        // When / Then
        assertThatThrownBy(() -> automationService.getRule(ruleId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Automation rule not found");

        verify(automationRuleRepository).findById(ruleId);
    }

    @Test
    @DisplayName("Should throw exception when rule not found")
    void getRule_NotFound_ThrowsException() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);
        when(automationRuleRepository.findById(ruleId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> automationService.getRule(ruleId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Automation rule not found");

        verify(automationRuleRepository).findById(ruleId);
    }

    // ==================== getAllRules() Tests ====================

    @Test
    @DisplayName("Should get all rules for tenant")
    void getAllRules_MultipleRules_ReturnsAllForTenant() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);

        List<AutomationRule> rules = Arrays.asList(testRule, createRule("Rule 2"), createRule("Rule 3"));
        when(automationRuleRepository.findByTenantId(tenantId)).thenReturn(rules);

        // When
        List<AutomationRule> result = automationService.getAllRules();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyElementsOf(rules);

        verify(automationRuleRepository).findByTenantId(tenantId);
    }

    @Test
    @DisplayName("Should return empty list when no rules exist")
    void getAllRules_NoRules_ReturnsEmptyList() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);
        when(automationRuleRepository.findByTenantId(tenantId)).thenReturn(Collections.emptyList());

        // When
        List<AutomationRule> result = automationService.getAllRules();

        // Then
        assertThat(result).isEmpty();

        verify(automationRuleRepository).findByTenantId(tenantId);
    }

    // ==================== getActiveRules() Tests ====================

    @Test
    @DisplayName("Should get only active rules for tenant")
    void getActiveRules_MixedRules_ReturnsOnlyActive() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);

        List<AutomationRule> activeRules = Arrays.asList(testRule, createRule("Active Rule 2"));
        when(automationRuleRepository.findByTenantIdAndIsActive(tenantId, true)).thenReturn(activeRules);

        // When
        List<AutomationRule> result = automationService.getActiveRules();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(AutomationRule::getIsActive);

        verify(automationRuleRepository).findByTenantIdAndIsActive(tenantId, true);
    }

    // ==================== getRulesByEventType() Tests ====================

    @Test
    @DisplayName("Should get rules by event type")
    void getRulesByEventType_MatchingRules_ReturnsFiltered() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);

        String eventType = "task.status.changed";
        List<AutomationRule> rules = Arrays.asList(testRule, createRule("Another Rule"));
        when(automationRuleRepository.findByTenantIdAndEventTypeAndIsActive(tenantId, eventType, true))
                .thenReturn(rules);

        // When
        List<AutomationRule> result = automationService.getRulesByEventType(eventType);

        // Then
        assertThat(result).hasSize(2);

        verify(automationRuleRepository).findByTenantIdAndEventTypeAndIsActive(tenantId, eventType, true);
    }

    // ==================== getTopExecutedRules() Tests ====================

    @Test
    @DisplayName("Should get top executed rules")
    void getTopExecutedRules_WithLimit_ReturnsTopRules() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);

        int limit = 10;
        List<AutomationRule> topRules = Arrays.asList(testRule, createRule("Top Rule 2"));
        when(automationRuleRepository.findTopExecutedRules(tenantId, limit)).thenReturn(topRules);

        // When
        List<AutomationRule> result = automationService.getTopExecutedRules(limit);

        // Then
        assertThat(result).hasSize(2);

        verify(automationRuleRepository).findTopExecutedRules(tenantId, limit);
    }

    // ==================== updateRule() Tests ====================

    @Test
    @DisplayName("Should update rule fields")
    void updateRule_ValidUpdate_UpdatesFields() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);
        when(automationRuleRepository.findById(ruleId)).thenReturn(Optional.of(testRule));

        AutomationRule updates = new AutomationRule();
        updates.setName("Updated Name");
        updates.setEventType("task.updated");
        updates.setActionType("webhook");
        updates.setIsActive(false);

        Map<String, Object> conditions = new HashMap<>();
        conditions.put("status", "COMPLETED");
        updates.setConditions(conditions);

        Map<String, Object> actionConfig = new HashMap<>();
        actionConfig.put("url", "https://example.com/webhook");
        updates.setActionConfig(actionConfig);

        when(automationRuleRepository.save(any(AutomationRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AutomationRule result = automationService.updateRule(ruleId, updates);

        // Then
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEventType()).isEqualTo("task.updated");
        assertThat(result.getActionType()).isEqualTo("webhook");
        assertThat(result.getIsActive()).isFalse();
        assertThat(result.getConditions()).isEqualTo(conditions);
        assertThat(result.getActionConfig()).isEqualTo(actionConfig);

        verify(automationRuleRepository).save(testRule);
    }

    @Test
    @DisplayName("Should not update null fields")
    void updateRule_NullFields_DoesNotUpdate() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);
        when(automationRuleRepository.findById(ruleId)).thenReturn(Optional.of(testRule));

        String originalName = testRule.getName();
        String originalEventType = testRule.getEventType();

        AutomationRule updates = new AutomationRule();
        updates.setActionType("new_action"); // Only update actionType

        when(automationRuleRepository.save(any(AutomationRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AutomationRule result = automationService.updateRule(ruleId, updates);

        // Then
        assertThat(result.getName()).isEqualTo(originalName); // Unchanged
        assertThat(result.getEventType()).isEqualTo(originalEventType); // Unchanged
        assertThat(result.getActionType()).isEqualTo("new_action"); // Changed

        verify(automationRuleRepository).save(testRule);
    }

    @Test
    @DisplayName("Should enforce tenant isolation when updating")
    void updateRule_DifferentTenant_ThrowsException() {
        // Given
        UUID differentTenantId = UUID.randomUUID();
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(differentTenantId);
        when(automationRuleRepository.findById(ruleId)).thenReturn(Optional.of(testRule));

        AutomationRule updates = new AutomationRule();
        updates.setName("Updated Name");

        // When / Then
        assertThatThrownBy(() -> automationService.updateRule(ruleId, updates))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Automation rule not found");

        verify(automationRuleRepository, never()).save(any(AutomationRule.class));
    }

    // ==================== deleteRule() Tests ====================

    @Test
    @DisplayName("Should delete rule when belongs to tenant")
    void deleteRule_BelongsToTenant_DeletesRule() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);
        when(automationRuleRepository.findById(ruleId)).thenReturn(Optional.of(testRule));

        // When
        automationService.deleteRule(ruleId);

        // Then
        verify(automationRuleRepository).delete(testRule);
    }

    @Test
    @DisplayName("Should enforce tenant isolation when deleting")
    void deleteRule_DifferentTenant_ThrowsException() {
        // Given
        UUID differentTenantId = UUID.randomUUID();
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(differentTenantId);
        when(automationRuleRepository.findById(ruleId)).thenReturn(Optional.of(testRule));

        // When / Then
        assertThatThrownBy(() -> automationService.deleteRule(ruleId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Automation rule not found");

        verify(automationRuleRepository, never()).delete(any(AutomationRule.class));
    }

    // ==================== toggleRuleStatus() Tests ====================

    @Test
    @DisplayName("Should toggle rule status to inactive")
    void toggleRuleStatus_ToInactive_UpdatesStatus() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);
        when(automationRuleRepository.findById(ruleId)).thenReturn(Optional.of(testRule));
        when(automationRuleRepository.save(any(AutomationRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AutomationRule result = automationService.toggleRuleStatus(ruleId, false);

        // Then
        assertThat(result.getIsActive()).isFalse();

        verify(automationRuleRepository).save(testRule);
    }

    @Test
    @DisplayName("Should toggle rule status to active")
    void toggleRuleStatus_ToActive_UpdatesStatus() {
        // Given
        testRule.setIsActive(false); // Start as inactive
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);
        when(automationRuleRepository.findById(ruleId)).thenReturn(Optional.of(testRule));
        when(automationRuleRepository.save(any(AutomationRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AutomationRule result = automationService.toggleRuleStatus(ruleId, true);

        // Then
        assertThat(result.getIsActive()).isTrue();

        verify(automationRuleRepository).save(testRule);
    }

    // ==================== countRules() Tests ====================

    @Test
    @DisplayName("Should count rules for tenant")
    void countRules_MultipleRules_ReturnsCount() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);
        when(automationRuleRepository.countByTenantId(tenantId)).thenReturn(5L);

        // When
        long result = automationService.countRules();

        // Then
        assertThat(result).isEqualTo(5L);

        verify(automationRuleRepository).countByTenantId(tenantId);
    }

    // ==================== Event Log Tests ====================

    @Test
    @DisplayName("Should get recent logs for tenant")
    void getRecentLogs_WithLimit_ReturnsLogs() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);

        int limit = 10;
        List<EventLog> logs = Arrays.asList(createEventLog(), createEventLog());
        when(eventLogRepository.findRecentLogs(tenantId, limit)).thenReturn(logs);

        // When
        List<EventLog> result = automationService.getRecentLogs(limit);

        // Then
        assertThat(result).hasSize(2);

        verify(eventLogRepository).findRecentLogs(tenantId, limit);
    }

    @Test
    @DisplayName("Should get logs for specific rule")
    void getLogsForRule_ValidRule_ReturnsLogs() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);
        when(automationRuleRepository.findById(ruleId)).thenReturn(Optional.of(testRule));

        List<EventLog> logs = Arrays.asList(createEventLog(), createEventLog());
        when(eventLogRepository.findByAutomationRuleId(ruleId)).thenReturn(logs);

        // When
        List<EventLog> result = automationService.getLogsForRule(ruleId);

        // Then
        assertThat(result).hasSize(2);

        verify(automationRuleRepository).findById(ruleId);
        verify(eventLogRepository).findByAutomationRuleId(ruleId);
    }

    @Test
    @DisplayName("Should throw exception when getting logs for rule from different tenant")
    void getLogsForRule_DifferentTenant_ThrowsException() {
        // Given
        UUID differentTenantId = UUID.randomUUID();
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(differentTenantId);
        when(automationRuleRepository.findById(ruleId)).thenReturn(Optional.of(testRule));

        // When / Then
        assertThatThrownBy(() -> automationService.getLogsForRule(ruleId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Automation rule not found");

        verify(eventLogRepository, never()).findByAutomationRuleId(any());
    }

    @Test
    @DisplayName("Should get failed logs for tenant")
    void getFailedLogs_HasFailedLogs_ReturnsLogs() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);

        List<EventLog> failedLogs = Arrays.asList(createEventLog(), createEventLog());
        when(eventLogRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(
                tenantId, EventLog.ExecutionStatus.FAILED))
                .thenReturn(failedLogs);

        // When
        List<EventLog> result = automationService.getFailedLogs();

        // Then
        assertThat(result).hasSize(2);

        verify(eventLogRepository).findByTenantIdAndStatusOrderByCreatedAtDesc(
                tenantId, EventLog.ExecutionStatus.FAILED);
    }

    @Test
    @DisplayName("Should get logs by date range")
    void getLogsByDateRange_ValidRange_ReturnsLogs() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);

        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        List<EventLog> logs = Arrays.asList(createEventLog(), createEventLog());
        when(eventLogRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate))
                .thenReturn(logs);

        // When
        List<EventLog> result = automationService.getLogsByDateRange(startDate, endDate);

        // Then
        assertThat(result).hasSize(2);

        verify(eventLogRepository).findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    @Test
    @DisplayName("Should count logs by status")
    void countLogsByStatus_SpecificStatus_ReturnsCount() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);

        EventLog.ExecutionStatus status = EventLog.ExecutionStatus.SUCCESS;
        when(eventLogRepository.countByTenantIdAndStatus(tenantId, status)).thenReturn(15L);

        // When
        long result = automationService.countLogsByStatus(status);

        // Then
        assertThat(result).isEqualTo(15L);

        verify(eventLogRepository).countByTenantIdAndStatus(tenantId, status);
    }

    @Test
    @DisplayName("Should get average execution duration")
    void getAverageExecutionDuration_HasExecutions_ReturnsAverage() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);
        when(eventLogRepository.getAverageExecutionDuration(tenantId)).thenReturn(125.5);

        // When
        Double result = automationService.getAverageExecutionDuration();

        // Then
        assertThat(result).isEqualTo(125.5);

        verify(eventLogRepository).getAverageExecutionDuration(tenantId);
    }

    @Test
    @DisplayName("Should return 0.0 when average execution duration is null")
    void getAverageExecutionDuration_NoExecutions_ReturnsZero() {
        // Given
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(tenantId);
        when(eventLogRepository.getAverageExecutionDuration(tenantId)).thenReturn(null);

        // When
        Double result = automationService.getAverageExecutionDuration();

        // Then
        assertThat(result).isEqualTo(0.0);

        verify(eventLogRepository).getAverageExecutionDuration(tenantId);
    }

    // ==================== Helper Methods ====================

    private AutomationRule createRule(String name) {
        AutomationRule rule = new AutomationRule();
        rule.setId(UUID.randomUUID());
        rule.setTenantId(tenantId);
        rule.setName(name);
        rule.setEventType("test.event");
        rule.setActionType("test_action");
        rule.setIsActive(true);
        rule.setExecutionCount(0L);
        return rule;
    }

    private EventLog createEventLog() {
        EventLog log = new EventLog();
        log.setId(UUID.randomUUID());
        log.setTenantId(tenantId);
        log.setEventType("test.event");
        log.setStatus(EventLog.ExecutionStatus.SUCCESS);
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }
}
