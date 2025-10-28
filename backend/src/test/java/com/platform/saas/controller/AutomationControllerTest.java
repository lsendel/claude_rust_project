package com.platform.saas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.saas.model.AutomationRule;
import com.platform.saas.model.EventLog;
import com.platform.saas.service.AutomationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for AutomationController.
 * Tests automation rule and event log management endpoints.
 * PMAT: Test code Cyc≤10, Cog≤15
 */
@WebMvcTest(controllers = AutomationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AutomationController Tests")
class AutomationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AutomationService automationService;

    @MockBean
    private com.platform.saas.repository.TenantRepository tenantRepository;

    private AutomationRule testRule;
    private EventLog testLog;
    private UUID testRuleId;
    private UUID testTenantId;

    @BeforeEach
    void setUp() {
        testRuleId = UUID.randomUUID();
        testTenantId = UUID.randomUUID();

        testRule = new AutomationRule();
        testRule.setId(testRuleId);
        testRule.setTenantId(testTenantId);
        testRule.setName("Test Rule");
        testRule.setDescription("Test automation rule");
        testRule.setEventType("task.status.changed");
        testRule.setActionType("send_email");
        testRule.setConditions(Collections.singletonMap("status", "completed"));
        testRule.setActionConfig(Collections.singletonMap("type", "send_email"));
        testRule.setIsActive(true);
        testRule.setExecutionCount(0L);
        testRule.setCreatedAt(LocalDateTime.now());
        testRule.setUpdatedAt(LocalDateTime.now());

        testLog = new EventLog();
        testLog.setId(UUID.randomUUID());
        testLog.setTenantId(testTenantId);
        testLog.setAutomationRuleId(testRuleId);
        testLog.setEventType("task.status.changed");
        testLog.setActionType("send_email");
        testLog.setStatus(EventLog.ExecutionStatus.SUCCESS);
        testLog.setExecutionDurationMs(150L);
        testLog.setCreatedAt(LocalDateTime.now());
    }

    // ========== CREATE RULE TEST ==========

    @Test
    @WithMockUser(authorities = {"ADMINISTRATOR"})
    @DisplayName("Should create automation rule successfully")
    void createRule_AsAdministrator_Success() throws Exception {
        // Given
        when(automationService.createRule(any(AutomationRule.class))).thenReturn(testRule);

        // When & Then
        mockMvc.perform(post("/api/automations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRule)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testRuleId.toString()))
                .andExpect(jsonPath("$.name").value("Test Rule"))
                .andExpect(jsonPath("$.eventType").value("task.status.changed"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(automationService, times(1)).createRule(any(AutomationRule.class));
    }

    // ========== GET ALL RULES TESTS ==========

    @Test
    @WithMockUser
    @DisplayName("Should get all automation rules")
    void getAllRules_WithoutFilter_Success() throws Exception {
        // Given
        List<AutomationRule> rules = Arrays.asList(testRule);
        when(automationService.getAllRules()).thenReturn(rules);

        // When & Then
        mockMvc.perform(get("/api/automations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Test Rule"));

        verify(automationService, times(1)).getAllRules();
        verify(automationService, never()).getActiveRules();
    }

    @Test
    @WithMockUser
    @DisplayName("Should get only active automation rules")
    void getAllRules_ActiveOnly_Success() throws Exception {
        // Given
        List<AutomationRule> rules = Arrays.asList(testRule);
        when(automationService.getActiveRules()).thenReturn(rules);

        // When & Then
        mockMvc.perform(get("/api/automations")
                        .param("activeOnly", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(automationService, times(1)).getActiveRules();
        verify(automationService, never()).getAllRules();
    }

    // ========== GET RULE BY ID TEST ==========

    @Test
    @WithMockUser
    @DisplayName("Should get automation rule by ID successfully")
    void getRule_ById_Success() throws Exception {
        // Given
        when(automationService.getRule(testRuleId)).thenReturn(testRule);

        // When & Then
        mockMvc.perform(get("/api/automations/{id}", testRuleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testRuleId.toString()))
                .andExpect(jsonPath("$.name").value("Test Rule"));

        verify(automationService, times(1)).getRule(testRuleId);
    }

    // ========== GET RULES BY EVENT TYPE TEST ==========

    @Test
    @WithMockUser
    @DisplayName("Should get rules by event type")
    void getRulesByEventType_Success() throws Exception {
        // Given
        List<AutomationRule> rules = Arrays.asList(testRule);
        when(automationService.getRulesByEventType("task.status.changed")).thenReturn(rules);

        // When & Then
        mockMvc.perform(get("/api/automations/by-event-type")
                        .param("eventType", "task.status.changed")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].eventType").value("task.status.changed"));

        verify(automationService, times(1)).getRulesByEventType("task.status.changed");
    }

    // ========== GET TOP EXECUTED RULES TEST ==========

    @Test
    @WithMockUser
    @DisplayName("Should get top executed rules")
    void getTopExecutedRules_Success() throws Exception {
        // Given
        testRule.setExecutionCount(100L);
        List<AutomationRule> rules = Arrays.asList(testRule);
        when(automationService.getTopExecutedRules(10)).thenReturn(rules);

        // When & Then
        mockMvc.perform(get("/api/automations/top-executed")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(automationService, times(1)).getTopExecutedRules(10);
    }

    // ========== UPDATE RULE TEST ==========

    @Test
    @WithMockUser(authorities = {"ADMINISTRATOR"})
    @DisplayName("Should update automation rule successfully")
    void updateRule_AsAdministrator_Success() throws Exception {
        // Given
        AutomationRule updatedRule = new AutomationRule();
        updatedRule.setId(testRuleId);
        updatedRule.setTenantId(testTenantId);  // Required field
        updatedRule.setName("Updated Rule");
        updatedRule.setEventType("task.status.changed");
        updatedRule.setActionType("send_email");
        updatedRule.setIsActive(false);

        when(automationService.updateRule(eq(testRuleId), any(AutomationRule.class)))
                .thenReturn(updatedRule);

        // When & Then
        mockMvc.perform(put("/api/automations/{id}", testRuleId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRule)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testRuleId.toString()))
                .andExpect(jsonPath("$.name").value("Updated Rule"))
                .andExpect(jsonPath("$.isActive").value(false));

        verify(automationService, times(1)).updateRule(eq(testRuleId), any(AutomationRule.class));
    }

    // ========== TOGGLE RULE STATUS TEST ==========

    @Test
    @WithMockUser(authorities = {"ADMINISTRATOR"})
    @DisplayName("Should toggle rule status successfully")
    void toggleRuleStatus_AsAdministrator_Success() throws Exception {
        // Given
        testRule.setIsActive(false);
        when(automationService.toggleRuleStatus(testRuleId, false)).thenReturn(testRule);

        Map<String, Boolean> request = new HashMap<>();
        request.put("isActive", false);

        // When & Then
        mockMvc.perform(patch("/api/automations/{id}/toggle", testRuleId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        verify(automationService, times(1)).toggleRuleStatus(testRuleId, false);
    }

    // ========== DELETE RULE TEST ==========

    @Test
    @WithMockUser(authorities = {"ADMINISTRATOR"})
    @DisplayName("Should delete automation rule successfully")
    void deleteRule_AsAdministrator_Success() throws Exception {
        // Given
        doNothing().when(automationService).deleteRule(testRuleId);

        // When & Then
        mockMvc.perform(delete("/api/automations/{id}", testRuleId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(automationService, times(1)).deleteRule(testRuleId);
    }

    // ========== GET RULE COUNT TEST ==========

    @Test
    @WithMockUser
    @DisplayName("Should get rule count successfully")
    void getRuleCount_Success() throws Exception {
        // Given
        when(automationService.countRules()).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/automations/count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));

        verify(automationService, times(1)).countRules();
    }

    // ========== GET RECENT LOGS TEST ==========

    @Test
    @WithMockUser
    @DisplayName("Should get recent event logs")
    void getRecentLogs_Success() throws Exception {
        // Given
        List<EventLog> logs = Arrays.asList(testLog);
        when(automationService.getRecentLogs(50)).thenReturn(logs);

        // When & Then
        mockMvc.perform(get("/api/automations/logs")
                        .param("limit", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].eventType").value("task.status.changed"))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));

        verify(automationService, times(1)).getRecentLogs(50);
    }

    // ========== GET LOGS FOR RULE TEST ==========

    @Test
    @WithMockUser
    @DisplayName("Should get logs for specific rule")
    void getLogsForRule_Success() throws Exception {
        // Given
        List<EventLog> logs = Arrays.asList(testLog);
        when(automationService.getLogsForRule(testRuleId)).thenReturn(logs);

        // When & Then
        mockMvc.perform(get("/api/automations/{id}/logs", testRuleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].automationRuleId").value(testRuleId.toString()));

        verify(automationService, times(1)).getLogsForRule(testRuleId);
    }

    // ========== GET FAILED LOGS TEST ==========

    @Test
    @WithMockUser
    @DisplayName("Should get failed event logs")
    void getFailedLogs_Success() throws Exception {
        // Given
        testLog.setStatus(EventLog.ExecutionStatus.FAILED);
        List<EventLog> logs = Arrays.asList(testLog);
        when(automationService.getFailedLogs()).thenReturn(logs);

        // When & Then
        mockMvc.perform(get("/api/automations/logs/failed")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("FAILED"));

        verify(automationService, times(1)).getFailedLogs();
    }

    // ========== GET LOGS BY DATE RANGE TEST ==========

    @Test
    @WithMockUser
    @DisplayName("Should get logs by date range")
    void getLogsByDateRange_Success() throws Exception {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<EventLog> logs = Arrays.asList(testLog);
        when(automationService.getLogsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(logs);

        // When & Then
        mockMvc.perform(get("/api/automations/logs/date-range")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(automationService, times(1)).getLogsByDateRange(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }

    // ========== GET AUTOMATION STATS TEST ==========

    @Test
    @WithMockUser
    @DisplayName("Should get automation statistics")
    void getAutomationStats_Success() throws Exception {
        // Given
        when(automationService.countRules()).thenReturn(10L);
        when(automationService.countLogsByStatus(EventLog.ExecutionStatus.SUCCESS)).thenReturn(100L);
        when(automationService.countLogsByStatus(EventLog.ExecutionStatus.FAILED)).thenReturn(5L);
        when(automationService.countLogsByStatus(EventLog.ExecutionStatus.SKIPPED)).thenReturn(2L);
        when(automationService.countLogsByStatus(EventLog.ExecutionStatus.NO_RULES_MATCHED)).thenReturn(1L);
        when(automationService.getAverageExecutionDuration()).thenReturn(150.0);

        // When & Then
        mockMvc.perform(get("/api/automations/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRules").value(10))
                .andExpect(jsonPath("$.successfulExecutions").value(100))
                .andExpect(jsonPath("$.failedExecutions").value(5))
                .andExpect(jsonPath("$.skippedExecutions").value(2))
                .andExpect(jsonPath("$.noRulesMatched").value(1))
                .andExpect(jsonPath("$.averageExecutionDurationMs").value(150.0));

        verify(automationService, times(1)).countRules();
        verify(automationService, times(4)).countLogsByStatus(any());
        verify(automationService, times(1)).getAverageExecutionDuration();
    }
}
