package com.platform.saas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.saas.model.User;
import com.platform.saas.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for InternalApiController.
 * Tests internal API endpoints secured with API secret.
 * PMAT: Test code Cyc≤10, Cog≤15
 */
@WebMvcTest(controllers = InternalApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {"app.api.internal-secret=test-secret-123"})
@DisplayName("InternalApiController Tests")
class InternalApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private com.platform.saas.repository.TenantRepository tenantRepository;

    private InternalApiController.CognitoUserRequest cognitoRequest;
    private User testUser;
    private static final String VALID_API_SECRET = "test-secret-123";
    private static final String INVALID_API_SECRET = "wrong-secret";

    @BeforeEach
    void setUp() {
        cognitoRequest = new InternalApiController.CognitoUserRequest();
        cognitoRequest.setCognitoUserId("cognito-user-123");
        cognitoRequest.setEmail("test@example.com");
        cognitoRequest.setName("Test User");

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setCognitoUserId("cognito-user-123");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setCreatedAt(LocalDateTime.now());
    }

    // ========== CREATE USER FROM COGNITO TESTS ==========

    @Test
    @DisplayName("Should create user from Cognito with valid API secret")
    void createUserFromCognito_ValidSecret_Success() throws Exception {
        // Given
        when(userService.createUserFromCognito(
                eq("cognito-user-123"),
                eq("test@example.com"),
                eq("Test User")
        )).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/api/internal/users/from-cognito")
                        .header("X-API-Secret", VALID_API_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cognitoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cognitoUserId").value("cognito-user-123"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));

        verify(userService, times(1)).createUserFromCognito(
                "cognito-user-123",
                "test@example.com",
                "Test User"
        );
    }

    @Test
    @DisplayName("Should reject request with invalid API secret")
    void createUserFromCognito_InvalidSecret_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/internal/users/from-cognito")
                        .header("X-API-Secret", INVALID_API_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cognitoRequest)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).createUserFromCognito(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should reject request with missing API secret")
    void createUserFromCognito_MissingSecret_ServerError() throws Exception {
        // When & Then - missing required header causes 500 (MissingRequestHeaderException)
        mockMvc.perform(post("/api/internal/users/from-cognito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cognitoRequest)))
                .andExpect(status().isInternalServerError());

        verify(userService, never()).createUserFromCognito(anyString(), anyString(), anyString());
    }

    // ========== HEALTH CHECK TEST ==========

    @Test
    @DisplayName("Should return OK for health check")
    void health_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/internal/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
