package com.platform.saas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.saas.dto.InviteUserRequest;
import com.platform.saas.dto.InviteUserResponse;
import com.platform.saas.dto.UserResponse;
import com.platform.saas.model.User;
import com.platform.saas.model.UserRole;
import com.platform.saas.model.UserTenant;
import com.platform.saas.repository.UserRepository;
import com.platform.saas.repository.UserTenantRepository;
import com.platform.saas.service.InvitationService;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for UserController.
 * Tests user management endpoints within tenants.
 * PMAT: Test code Cyc≤10, Cog≤15
 */
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InvitationService invitationService;

    @MockBean
    private UserTenantRepository userTenantRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private com.platform.saas.repository.TenantRepository tenantRepository;

    private UUID testTenantId;
    private UUID testUserId;
    private UUID inviterId;
    private InviteUserRequest inviteRequest;
    private InviteUserResponse inviteResponse;
    private User testUser;
    private UserTenant testUserTenant;

    @BeforeEach
    void setUp() {
        testTenantId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        inviterId = UUID.randomUUID();

        inviteRequest = new InviteUserRequest();
        inviteRequest.setEmail("newuser@example.com");
        inviteRequest.setRole(UserRole.EDITOR);

        inviteResponse = InviteUserResponse.builder()
                .email("newuser@example.com")
                .role(UserRole.EDITOR)
                .emailSent(true)
                .build();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("user@example.com");
        testUser.setName("Test User");
        testUser.setCreatedAt(LocalDateTime.now());

        testUserTenant = new UserTenant();
        testUserTenant.setTenantId(testTenantId);
        testUserTenant.setUserId(testUserId);
        testUserTenant.setRole(UserRole.EDITOR);
        testUserTenant.setInvitedBy(inviterId);
        testUserTenant.setJoinedAt(LocalDateTime.now());
    }

    // ========== INVITE USER TEST ==========

    @Test
    @WithMockUser(authorities = {"ADMINISTRATOR"})
    @DisplayName("Should invite user successfully as ADMINISTRATOR")
    void inviteUser_AsAdministrator_Success() throws Exception {
        // Given
        when(invitationService.inviteUser(eq(testTenantId), any(InviteUserRequest.class), any(UUID.class)))
                .thenReturn(inviteResponse);

        // When & Then
        mockMvc.perform(post("/api/tenants/{tenantId}/users/invite", testTenantId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.role").value("EDITOR"))
                .andExpect(jsonPath("$.emailSent").value(true));

        verify(invitationService, times(1)).inviteUser(
                eq(testTenantId),
                any(InviteUserRequest.class),
                any(UUID.class)
        );
    }

    // ========== GET TENANT USERS TESTS ==========

    @Test
    @WithMockUser
    @DisplayName("Should get all tenant users successfully")
    void getTenantUsers_Success() throws Exception {
        // Given
        List<UserTenant> userTenants = Arrays.asList(testUserTenant);
        when(userTenantRepository.findByTenantId(testTenantId)).thenReturn(userTenants);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/tenants/{tenantId}/users", testTenantId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(testUserId.toString()))
                .andExpect(jsonPath("$[0].email").value("user@example.com"))
                .andExpect(jsonPath("$[0].name").value("Test User"))
                .andExpect(jsonPath("$[0].role").value("EDITOR"));

        verify(userTenantRepository, times(1)).findByTenantId(testTenantId);
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle deleted users gracefully")
    void getTenantUsers_WithDeletedUser_Success() throws Exception {
        // Given - user has been deleted but UserTenant still exists
        List<UserTenant> userTenants = Arrays.asList(testUserTenant);
        when(userTenantRepository.findByTenantId(testTenantId)).thenReturn(userTenants);
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/tenants/{tenantId}/users", testTenantId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(testUserId.toString()))
                .andExpect(jsonPath("$[0].email").value("unknown@example.com"))
                .andExpect(jsonPath("$[0].name").value("Unknown User"))
                .andExpect(jsonPath("$[0].role").value("EDITOR"));

        verify(userTenantRepository, times(1)).findByTenantId(testTenantId);
        verify(userRepository, times(1)).findById(testUserId);
    }

    // ========== REMOVE USER TEST ==========

    @Test
    @WithMockUser(authorities = {"ADMINISTRATOR"})
    @DisplayName("Should remove user successfully as ADMINISTRATOR")
    void removeUser_AsAdministrator_Success() throws Exception {
        // Given
        doNothing().when(invitationService).removeUserFromTenant(testTenantId, testUserId);

        // When & Then
        mockMvc.perform(delete("/api/tenants/{tenantId}/users/{userId}", testTenantId, testUserId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(invitationService, times(1)).removeUserFromTenant(testTenantId, testUserId);
    }
}
