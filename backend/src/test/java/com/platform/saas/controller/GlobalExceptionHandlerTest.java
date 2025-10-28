package com.platform.saas.controller;

import com.platform.saas.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for GlobalExceptionHandler to ensure proper error responses.
 * Tests all exception handlers for consistent error formatting.
 * PMAT: Test code Cyc≤10, Cog≤15
 */
@WebMvcTest(controllers = {ProjectController.class, TenantController.class, TaskController.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.platform.saas.service.ProjectService projectService;

    @MockBean
    private com.platform.saas.service.TenantService tenantService;

    @MockBean
    private com.platform.saas.service.TaskService taskService;

    @MockBean
    private com.platform.saas.repository.TenantRepository tenantRepository;

    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
    }

    // ========== TENANT NOT FOUND EXCEPTION ==========

    @Test
    @WithMockUser
    @DisplayName("Should handle TenantNotFoundException with 404")
    void handleTenantNotFoundException_Returns404() throws Exception {
        // Given - use UUID constructor
        when(tenantService.getTenantById(testId))
                .thenThrow(new TenantNotFoundException(testId));

        // When & Then
        mockMvc.perform(get("/api/tenants/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Tenant not found with ID: " + testId))
                .andExpect(jsonPath("$.path").value("/api/tenants/" + testId));

        verify(tenantService, times(1)).getTenantById(testId);
    }

    // ========== USER NOT FOUND EXCEPTION ==========

    @Test
    @WithMockUser
    @DisplayName("Should handle UserNotFoundException with 404")
    void handleUserNotFoundException_Returns404() throws Exception {
        // Given - simulate user not found in project retrieval
        UUID userId = UUID.randomUUID();
        when(projectService.getProject(testId))
                .thenThrow(new UserNotFoundException(userId));

        // When & Then
        mockMvc.perform(get("/api/projects/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found with ID: " + userId))
                .andExpect(jsonPath("$.path").value("/api/projects/" + testId));
    }

    // ========== SUBDOMAIN ALREADY EXISTS EXCEPTION ==========

    @Test
    @WithMockUser
    @DisplayName("Should handle SubdomainAlreadyExistsException with 409")
    void handleSubdomainAlreadyExists_Returns409() throws Exception {
        // Given - pass just the subdomain, not the full message
        doThrow(new SubdomainAlreadyExistsException("test-company"))
                .when(tenantService).validateSubdomain("test-company");

        // When & Then
        mockMvc.perform(get("/api/tenants/validate-subdomain")
                        .param("subdomain", "test-company")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Subdomain 'test-company' is already registered"))
                .andExpect(jsonPath("$.path").value("/api/tenants/validate-subdomain"));
    }

    // ========== INVALID SUBDOMAIN EXCEPTION ==========

    @Test
    @WithMockUser
    @DisplayName("Should handle InvalidSubdomainException with 400")
    void handleInvalidSubdomain_Returns400() throws Exception {
        // Given
        doThrow(new InvalidSubdomainException("Subdomain must be lowercase alphanumeric"))
                .when(tenantService).validateSubdomain("Test-Company");

        // When & Then
        mockMvc.perform(get("/api/tenants/validate-subdomain")
                        .param("subdomain", "Test-Company")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Subdomain must be lowercase alphanumeric"))
                .andExpect(jsonPath("$.path").value("/api/tenants/validate-subdomain"));
    }

    // ========== QUOTA EXCEEDED EXCEPTION ==========

    @Test
    @WithMockUser
    @DisplayName("Should handle QuotaExceededException with 402")
    void handleQuotaExceeded_Returns402() throws Exception {
        // Given - simulate quota exceeded when getting project
        when(projectService.getProject(testId))
                .thenThrow(new QuotaExceededException("Project quota limit reached"));

        // When & Then
        mockMvc.perform(get("/api/projects/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.status").value(402))
                .andExpect(jsonPath("$.error").value("Quota Exceeded"))
                .andExpect(jsonPath("$.message").value("Project quota limit reached. Please upgrade your subscription to continue."))
                .andExpect(jsonPath("$.path").value("/api/projects/" + testId));
    }

    // ========== VALIDATION EXCEPTION ==========

    @Test
    @WithMockUser
    @DisplayName("Should handle MethodArgumentNotValidException with 400")
    void handleValidationErrors_Returns400() throws Exception {
        // Given - invalid project with missing required fields
        String invalidProject = "{\"name\":\"\",\"description\":\"test\"}";

        // When & Then
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidProject))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("One or more fields failed validation"))
                .andExpect(jsonPath("$.path").value("/api/projects"))
                .andExpect(jsonPath("$.details").isArray());
    }

    // ========== ACCESS DENIED EXCEPTION ==========

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should handle AccessDeniedException with 403")
    void handleAccessDenied_Returns403() throws Exception {
        // Given - viewer trying to delete project (requires ADMINISTRATOR/EDITOR)
        doThrow(new AccessDeniedException("Access denied"))
                .when(projectService).deleteProject(testId);

        // When & Then
        mockMvc.perform(delete("/api/projects/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Access Denied"))
                .andExpect(jsonPath("$.message").value("You do not have permission to perform this action. Only users with ADMINISTRATOR or EDITOR roles can modify resources."))
                .andExpect(jsonPath("$.path").value("/api/projects/" + testId));
    }

    // ========== ILLEGAL ARGUMENT EXCEPTION ==========

    @Test
    @WithMockUser
    @DisplayName("Should handle IllegalArgumentException with 400")
    void handleIllegalArgument_Returns400() throws Exception {
        // Given
        when(taskService.getTask(testId))
                .thenThrow(new IllegalArgumentException("Invalid task ID format"));

        // When & Then
        mockMvc.perform(get("/api/tasks/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid task ID format"))
                .andExpect(jsonPath("$.path").value("/api/tasks/" + testId));
    }

    // ========== GENERIC EXCEPTION ==========

    @Test
    @WithMockUser
    @DisplayName("Should handle generic Exception with 500")
    void handleGenericException_Returns500() throws Exception {
        // Given - simulate unexpected runtime exception
        when(projectService.getProject(testId))
                .thenThrow(new RuntimeException("Unexpected database error"));

        // When & Then
        mockMvc.perform(get("/api/projects/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please try again later."))
                .andExpect(jsonPath("$.path").value("/api/projects/" + testId));
    }
}
