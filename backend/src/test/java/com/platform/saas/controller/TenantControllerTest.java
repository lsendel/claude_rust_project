package com.platform.saas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.saas.dto.TenantRegistrationRequest;
import com.platform.saas.dto.TenantResponse;
import com.platform.saas.dto.TenantUsageResponse;
import com.platform.saas.model.SubscriptionTier;
import com.platform.saas.service.ProjectService;
import com.platform.saas.service.TaskService;
import com.platform.saas.service.TenantService;
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
import java.util.UUID;

import static org.hamcrest.Matchers.closeTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for TenantController.
 * Tests REST endpoints, validation, and service integration.
 * PMAT: Test code Cyc≤10, Cog≤15
 */
@WebMvcTest(controllers = TenantController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TenantController Tests")
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TenantService tenantService;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private TaskService taskService;

    @MockBean
    private com.platform.saas.repository.TenantRepository tenantRepository;

    private TenantRegistrationRequest registrationRequest;
    private TenantResponse tenantResponse;
    private UUID testTenantId;

    @BeforeEach
    void setUp() {
        testTenantId = UUID.randomUUID();

        registrationRequest = new TenantRegistrationRequest();
        registrationRequest.setSubdomain("test-company");
        registrationRequest.setName("Test Company");
        registrationRequest.setDescription("Test company description");
        registrationRequest.setOwnerEmail("owner@test.com");
        registrationRequest.setOwnerName("Test Owner");
        registrationRequest.setSubscriptionTier(SubscriptionTier.FREE);

        tenantResponse = TenantResponse.builder()
                .id(testTenantId)
                .subdomain("test-company")
                .name("Test Company")
                .description("Test company description")
                .subscriptionTier(SubscriptionTier.FREE)
                .quotaLimit(50)
                .currentUsage(0L)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ========== REGISTER TENANT TEST ==========

    @Test
    @WithMockUser
    @DisplayName("Should register new tenant successfully")
    void registerTenant_Success() throws Exception {
        // Given
        when(tenantService.registerTenant(any(TenantRegistrationRequest.class)))
                .thenReturn(tenantResponse);

        // When & Then
        mockMvc.perform(post("/api/tenants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testTenantId.toString()))
                .andExpect(jsonPath("$.subdomain").value("test-company"))
                .andExpect(jsonPath("$.name").value("Test Company"))
                .andExpect(jsonPath("$.subscriptionTier").value("FREE"))
                .andExpect(jsonPath("$.quotaLimit").value(50))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(tenantService, times(1)).registerTenant(any(TenantRegistrationRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should register tenant with PRO tier")
    void registerTenant_ProTier_Success() throws Exception {
        // Given
        registrationRequest.setSubscriptionTier(SubscriptionTier.PRO);
        tenantResponse.setSubscriptionTier(SubscriptionTier.PRO);
        tenantResponse.setQuotaLimit(1000);

        when(tenantService.registerTenant(any(TenantRegistrationRequest.class)))
                .thenReturn(tenantResponse);

        // When & Then
        mockMvc.perform(post("/api/tenants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subscriptionTier").value("PRO"))
                .andExpect(jsonPath("$.quotaLimit").value(1000));

        verify(tenantService, times(1)).registerTenant(any(TenantRegistrationRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should register tenant with ENTERPRISE tier")
    void registerTenant_EnterpriseTier_Success() throws Exception {
        // Given
        registrationRequest.setSubscriptionTier(SubscriptionTier.ENTERPRISE);
        tenantResponse.setSubscriptionTier(SubscriptionTier.ENTERPRISE);
        tenantResponse.setQuotaLimit(null); // Unlimited

        when(tenantService.registerTenant(any(TenantRegistrationRequest.class)))
                .thenReturn(tenantResponse);

        // When & Then
        mockMvc.perform(post("/api/tenants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subscriptionTier").value("ENTERPRISE"))
                .andExpect(jsonPath("$.quotaLimit").doesNotExist());

        verify(tenantService, times(1)).registerTenant(any(TenantRegistrationRequest.class));
    }

    // ========== GET TENANT BY ID TEST ==========

    @Test
    @WithMockUser
    @DisplayName("Should get tenant by ID successfully")
    void getTenantById_Success() throws Exception {
        // Given
        when(tenantService.getTenantById(testTenantId)).thenReturn(tenantResponse);

        // When & Then
        mockMvc.perform(get("/api/tenants/{id}", testTenantId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTenantId.toString()))
                .andExpect(jsonPath("$.subdomain").value("test-company"))
                .andExpect(jsonPath("$.name").value("Test Company"));

        verify(tenantService, times(1)).getTenantById(testTenantId);
    }

    // ========== GET TENANT BY SUBDOMAIN TEST ==========

    @Test
    @WithMockUser
    @DisplayName("Should get tenant by subdomain successfully")
    void getTenantBySubdomain_Success() throws Exception {
        // Given
        when(tenantService.getTenantBySubdomain("test-company")).thenReturn(tenantResponse);

        // When & Then
        mockMvc.perform(get("/api/tenants/subdomain/{subdomain}", "test-company")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTenantId.toString()))
                .andExpect(jsonPath("$.subdomain").value("test-company"))
                .andExpect(jsonPath("$.name").value("Test Company"));

        verify(tenantService, times(1)).getTenantBySubdomain("test-company");
    }

    // ========== VALIDATE SUBDOMAIN TESTS ==========

    @Test
    @WithMockUser
    @DisplayName("Should validate subdomain successfully")
    void validateSubdomain_Valid_Success() throws Exception {
        // Given
        doNothing().when(tenantService).validateSubdomain("valid-subdomain");

        // When & Then
        mockMvc.perform(get("/api/tenants/validate-subdomain")
                        .param("subdomain", "valid-subdomain")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(tenantService, times(1)).validateSubdomain("valid-subdomain");
    }

    // ========== GET TENANT USAGE TESTS ==========

    @Test
    @WithMockUser
    @DisplayName("Should get tenant usage successfully")
    void getTenantUsage_Success() throws Exception {
        // Given
        when(tenantService.getTenantById(testTenantId)).thenReturn(tenantResponse);
        when(projectService.countProjects()).thenReturn(10L);
        when(taskService.countTasks()).thenReturn(15L);

        // When & Then
        mockMvc.perform(get("/api/tenants/{id}/usage", testTenantId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectCount").value(10))
                .andExpect(jsonPath("$.taskCount").value(15))
                .andExpect(jsonPath("$.totalUsage").value(25))
                .andExpect(jsonPath("$.quotaLimit").value(50))
                .andExpect(jsonPath("$.subscriptionTier").value("FREE"))
                .andExpect(jsonPath("$.usagePercentage").value(50.0))
                .andExpect(jsonPath("$.quotaExceeded").value(false))
                .andExpect(jsonPath("$.nearingQuota").value(false));

        verify(tenantService, times(1)).getTenantById(testTenantId);
        verify(projectService, times(1)).countProjects();
        verify(taskService, times(1)).countTasks();
    }

    @Test
    @WithMockUser
    @DisplayName("Should show nearing quota when usage >= 80%")
    void getTenantUsage_NearingQuota_Success() throws Exception {
        // Given
        when(tenantService.getTenantById(testTenantId)).thenReturn(tenantResponse);
        when(projectService.countProjects()).thenReturn(20L);
        when(taskService.countTasks()).thenReturn(21L); // 41/50 = 82%

        // When & Then
        mockMvc.perform(get("/api/tenants/{id}/usage", testTenantId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsage").value(41))
                .andExpect(jsonPath("$.quotaLimit").value(50))
                .andExpect(jsonPath("$.usagePercentage").value(82.0))
                .andExpect(jsonPath("$.quotaExceeded").value(false))
                .andExpect(jsonPath("$.nearingQuota").value(true));

        verify(tenantService, times(1)).getTenantById(testTenantId);
    }

    @Test
    @WithMockUser
    @DisplayName("Should show quota exceeded when usage >= limit")
    void getTenantUsage_QuotaExceeded_Success() throws Exception {
        // Given
        when(tenantService.getTenantById(testTenantId)).thenReturn(tenantResponse);
        when(projectService.countProjects()).thenReturn(30L);
        when(taskService.countTasks()).thenReturn(25L); // 55/50 = 110%

        // When & Then
        mockMvc.perform(get("/api/tenants/{id}/usage", testTenantId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsage").value(55))
                .andExpect(jsonPath("$.quotaLimit").value(50))
                .andExpect(jsonPath("$.usagePercentage").value(closeTo(110.0, 0.01)))
                .andExpect(jsonPath("$.quotaExceeded").value(true))
                .andExpect(jsonPath("$.nearingQuota").value(true));

        verify(tenantService, times(1)).getTenantById(testTenantId);
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle unlimited quota (ENTERPRISE tier)")
    void getTenantUsage_UnlimitedQuota_Success() throws Exception {
        // Given
        tenantResponse.setSubscriptionTier(SubscriptionTier.ENTERPRISE);
        tenantResponse.setQuotaLimit(null);

        when(tenantService.getTenantById(testTenantId)).thenReturn(tenantResponse);
        when(projectService.countProjects()).thenReturn(500L);
        when(taskService.countTasks()).thenReturn(1000L);

        // When & Then
        mockMvc.perform(get("/api/tenants/{id}/usage", testTenantId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectCount").value(500))
                .andExpect(jsonPath("$.taskCount").value(1000))
                .andExpect(jsonPath("$.totalUsage").value(1500))
                .andExpect(jsonPath("$.quotaLimit").doesNotExist())
                .andExpect(jsonPath("$.subscriptionTier").value("ENTERPRISE"))
                .andExpect(jsonPath("$.usagePercentage").value(0.0))
                .andExpect(jsonPath("$.quotaExceeded").value(false))
                .andExpect(jsonPath("$.nearingQuota").value(false));

        verify(tenantService, times(1)).getTenantById(testTenantId);
    }

    // ========== ERROR CASE TESTS ==========

    @Test
    @WithMockUser
    @DisplayName("Should return 404 when tenant not found by ID")
    void getTenantById_NotFound_Returns404() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(tenantService.getTenantById(nonExistentId))
                .thenThrow(new com.platform.saas.exception.TenantNotFoundException("Tenant not found with id: " + nonExistentId));

        // When & Then
        mockMvc.perform(get("/api/tenants/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(tenantService, times(1)).getTenantById(nonExistentId);
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 404 when tenant not found by subdomain")
    void getTenantBySubdomain_NotFound_Returns404() throws Exception {
        // Given
        when(tenantService.getTenantBySubdomain("nonexistent"))
                .thenThrow(new com.platform.saas.exception.TenantNotFoundException("Tenant not found with subdomain: nonexistent"));

        // When & Then
        mockMvc.perform(get("/api/tenants/subdomain/{subdomain}", "nonexistent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(tenantService, times(1)).getTenantBySubdomain("nonexistent");
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 409 when subdomain already exists")
    void validateSubdomain_AlreadyExists_Returns409() throws Exception {
        // Given
        doThrow(new com.platform.saas.exception.SubdomainAlreadyExistsException("Subdomain already taken"))
                .when(tenantService).validateSubdomain("existing-company");

        // When & Then
        mockMvc.perform(get("/api/tenants/validate-subdomain")
                        .param("subdomain", "existing-company")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        verify(tenantService, times(1)).validateSubdomain("existing-company");
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 when subdomain format is invalid")
    void validateSubdomain_InvalidFormat_Returns400() throws Exception {
        // Given
        doThrow(new com.platform.saas.exception.InvalidSubdomainException("Invalid subdomain format"))
                .when(tenantService).validateSubdomain("Invalid-Subdomain!");

        // When & Then
        mockMvc.perform(get("/api/tenants/validate-subdomain")
                        .param("subdomain", "Invalid-Subdomain!")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(tenantService, times(1)).validateSubdomain("Invalid-Subdomain!");
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 when registering tenant with invalid data")
    void registerTenant_InvalidData_Returns400() throws Exception {
        // Given - registration with empty subdomain (violates @NotBlank)
        registrationRequest.setSubdomain("");  // Invalid: empty subdomain

        // When & Then
        mockMvc.perform(post("/api/tenants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isBadRequest());

        verify(tenantService, never()).registerTenant(any(com.platform.saas.dto.TenantRegistrationRequest.class));
    }
}
