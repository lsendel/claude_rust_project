package com.platform.saas.controller;

import com.platform.saas.security.JwtUserInfo;
import com.platform.saas.security.JwtUserInfoExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for AuthController.
 * Tests authentication endpoints and user info retrieval.
 * PMAT: Test code Cyc≤10, Cog≤15
 */
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUserInfoExtractor jwtUserInfoExtractor;

    @MockBean
    private com.platform.saas.repository.TenantRepository tenantRepository;

    private JwtUserInfo testUserInfo;

    @BeforeEach
    void setUp() {
        testUserInfo = JwtUserInfo.builder()
                .cognitoUserId("user-123")
                .email("test@example.com")
                .name("Test User")
                .emailVerified(true)
                .build();
    }

    // ========== GET CURRENT USER TEST ==========

    @Test
    @WithMockUser
    @DisplayName("Should get current user info successfully")
    void getCurrentUser_Success() throws Exception {
        // Given - accept null JWT since @WithMockUser doesn't provide JWT token
        when(jwtUserInfoExtractor.extractUserInfo(any())).thenReturn(testUserInfo);

        // When & Then
        mockMvc.perform(get("/api/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cognitoUserId").value("user-123"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.emailVerified").value(true));

        verify(jwtUserInfoExtractor, times(1)).extractUserInfo(any());
    }

    // ========== AUTH STATUS CHECK TEST ==========

    @Test
    @WithMockUser
    @DisplayName("Should check auth status successfully")
    void checkAuthStatus_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
