package com.platform.saas.service;

import com.platform.saas.dto.InviteUserRequest;
import com.platform.saas.dto.InviteUserResponse;
import com.platform.saas.exception.TenantNotFoundException;
import com.platform.saas.exception.UserNotFoundException;
import com.platform.saas.model.Tenant;
import com.platform.saas.model.User;
import com.platform.saas.model.UserRole;
import com.platform.saas.model.UserTenant;
import com.platform.saas.repository.TenantRepository;
import com.platform.saas.repository.UserRepository;
import com.platform.saas.repository.UserTenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for InvitationService.
 * Tests user invitation, email sending, and tenant membership management.
 *
 * CRITICAL SERVICE COMPONENT - High coverage required
 * PMAT: Test code Cyc≤10, Cog≤15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InvitationService Unit Tests - Critical Service Component")
class InvitationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserTenantRepository userTenantRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private InvitationService invitationService;

    private Tenant testTenant;
    private User testUser;
    private User inviterUser;
    private InviteUserRequest inviteRequest;
    private UUID tenantId;
    private UUID userId;
    private UUID inviterId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        inviterId = UUID.randomUUID();

        testTenant = new Tenant();
        testTenant.setId(tenantId);
        testTenant.setSubdomain("acme");
        testTenant.setName("Acme Corp");

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("user@example.com");
        testUser.setName("Test User");
        testUser.setCognitoUserId("cognito-123");

        inviterUser = new User();
        inviterUser.setId(inviterId);
        inviterUser.setName("Inviter User");
        inviterUser.setEmail("inviter@example.com");

        inviteRequest = InviteUserRequest.builder()
                .email("newuser@example.com")
                .role(UserRole.EDITOR)
                .message("Welcome to the team!")
                .build();
    }

    // ========== INVITE USER - NEW USER TESTS ==========

    @Test
    @DisplayName("Should invite new user and create placeholder account")
    void inviteUser_NewUser_CreatesPlaceholder() {
        // Given
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(userRepository.findByEmail(inviteRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(userTenantRepository.save(any(UserTenant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(inviterId)).thenReturn(Optional.of(inviterUser));
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // When
        InviteUserResponse response = invitationService.inviteUser(tenantId, inviteRequest, inviterId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(inviteRequest.getEmail());
        assertThat(response.getRole()).isEqualTo(UserRole.EDITOR);
        assertThat(response.getTenantId()).isEqualTo(tenantId);
        assertThat(response.getInvitedBy()).isEqualTo(inviterId);
        assertThat(response.isExistingUser()).isFalse();
        assertThat(response.isEmailSent()).isTrue();

        // Verify placeholder user was created
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getEmail()).isEqualTo(inviteRequest.getEmail());
        assertThat(capturedUser.getCognitoUserId()).startsWith("PENDING_");

        // Verify user-tenant association was created
        verify(userTenantRepository).save(any(UserTenant.class));

        // Verify email was sent
        verify(emailService).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should extract name from email for placeholder user")
    void inviteUser_NewUser_ExtractsNameFromEmail() {
        // Given
        InviteUserRequest request = InviteUserRequest.builder()
                .email("john.doe@example.com")
                .role(UserRole.EDITOR)
                .build();

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(userTenantRepository.save(any(UserTenant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(inviterId)).thenReturn(Optional.of(inviterUser));
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // When
        invitationService.inviteUser(tenantId, request, inviterId);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getName()).isEqualTo("John Doe");
    }

    // ========== INVITE USER - EXISTING USER TESTS ==========

    @Test
    @DisplayName("Should invite existing user without creating new account")
    void inviteUser_ExistingUser_NoNewAccount() {
        // Given
        inviteRequest.setEmail(testUser.getEmail());

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(userTenantRepository.existsByUserIdAndTenantId(testUser.getId(), tenantId)).thenReturn(false);
        when(userTenantRepository.save(any(UserTenant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(inviterId)).thenReturn(Optional.of(inviterUser));
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // When
        InviteUserResponse response = invitationService.inviteUser(tenantId, inviteRequest, inviterId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(testUser.getId());
        assertThat(response.isExistingUser()).isTrue();

        // Verify NO new user was created
        verify(userRepository, never()).save(any(User.class));

        // Verify user-tenant association was created
        verify(userTenantRepository).save(any(UserTenant.class));
    }

    @Test
    @DisplayName("Should throw exception when user already member of tenant")
    void inviteUser_UserAlreadyMember_ThrowsException() {
        // Given
        inviteRequest.setEmail(testUser.getEmail());

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(userTenantRepository.existsByUserIdAndTenantId(testUser.getId(), tenantId)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> invitationService.inviteUser(tenantId, inviteRequest, inviterId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already a member");

        // Verify no association or email was created/sent
        verify(userTenantRepository, never()).save(any(UserTenant.class));
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    // ========== INVITE USER - TENANT VALIDATION TESTS ==========

    @Test
    @DisplayName("Should throw exception when tenant not found")
    void inviteUser_TenantNotFound_ThrowsException() {
        // Given
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> invitationService.inviteUser(tenantId, inviteRequest, inviterId))
                .isInstanceOf(TenantNotFoundException.class);

        // Verify no user or association was created
        verify(userRepository, never()).save(any(User.class));
        verify(userTenantRepository, never()).save(any(UserTenant.class));
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    // ========== INVITE USER - EMAIL SENDING TESTS ==========

    @Test
    @DisplayName("Should send invitation email with correct details")
    void inviteUser_EmailSent_WithCorrectDetails() {
        // Given
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(userRepository.findByEmail(inviteRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(userTenantRepository.save(any(UserTenant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(inviterId)).thenReturn(Optional.of(inviterUser));
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // When
        InviteUserResponse response = invitationService.inviteUser(tenantId, inviteRequest, inviterId);

        // Then
        assertThat(response.isEmailSent()).isTrue();

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailService).sendEmail(emailCaptor.capture(), subjectCaptor.capture(), bodyCaptor.capture());

        assertThat(emailCaptor.getValue()).isEqualTo(inviteRequest.getEmail());
        assertThat(subjectCaptor.getValue()).contains("Acme Corp");
        assertThat(bodyCaptor.getValue()).contains("Inviter User");
        assertThat(bodyCaptor.getValue()).contains("Acme Corp");
        assertThat(bodyCaptor.getValue()).contains("EDITOR");
        assertThat(bodyCaptor.getValue()).contains("Welcome to the team!");
    }

    @Test
    @DisplayName("Should handle email sending failure gracefully")
    void inviteUser_EmailFails_ContinuesWithoutError() {
        // Given
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(userRepository.findByEmail(inviteRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(userTenantRepository.save(any(UserTenant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(inviterId)).thenReturn(Optional.of(inviterUser));
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailService).sendEmail(anyString(), anyString(), anyString());

        // When
        InviteUserResponse response = invitationService.inviteUser(tenantId, inviteRequest, inviterId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isEmailSent()).isFalse(); // Email failed but invitation still created

        // Verify user and association were still created
        verify(userRepository).save(any(User.class));
        verify(userTenantRepository).save(any(UserTenant.class));
    }

    @Test
    @DisplayName("Should include custom message in email when provided")
    void inviteUser_WithCustomMessage_IncludesInEmail() {
        // Given
        String customMessage = "Looking forward to working with you!";
        inviteRequest.setMessage(customMessage);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(userRepository.findByEmail(inviteRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(userTenantRepository.save(any(UserTenant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(inviterId)).thenReturn(Optional.of(inviterUser));
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // When
        invitationService.inviteUser(tenantId, inviteRequest, inviterId);

        // Then
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(anyString(), anyString(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).contains(customMessage);
    }

    @Test
    @DisplayName("Should handle missing inviter name gracefully")
    void inviteUser_InviterNotFound_UsesDefaultName() {
        // Given
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(userRepository.findByEmail(inviteRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(userTenantRepository.save(any(UserTenant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(inviterId)).thenReturn(Optional.empty());
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // When
        InviteUserResponse response = invitationService.inviteUser(tenantId, inviteRequest, inviterId);

        // Then
        assertThat(response.isEmailSent()).isTrue();

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(anyString(), anyString(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).contains("Someone");
    }

    // ========== INVITE USER - ROLE TESTS ==========

    @Test
    @DisplayName("Should invite user with ADMINISTRATOR role")
    void inviteUser_AdministratorRole_Success() {
        // Given
        inviteRequest.setRole(UserRole.ADMINISTRATOR);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(userRepository.findByEmail(inviteRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(userTenantRepository.save(any(UserTenant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(inviterId)).thenReturn(Optional.of(inviterUser));
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // When
        InviteUserResponse response = invitationService.inviteUser(tenantId, inviteRequest, inviterId);

        // Then
        assertThat(response.getRole()).isEqualTo(UserRole.ADMINISTRATOR);

        ArgumentCaptor<UserTenant> utCaptor = ArgumentCaptor.forClass(UserTenant.class);
        verify(userTenantRepository).save(utCaptor.capture());
        assertThat(utCaptor.getValue().getRole()).isEqualTo(UserRole.ADMINISTRATOR);
    }

    @Test
    @DisplayName("Should invite user with VIEWER role")
    void inviteUser_ViewerRole_Success() {
        // Given
        inviteRequest.setRole(UserRole.VIEWER);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(userRepository.findByEmail(inviteRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(userTenantRepository.save(any(UserTenant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(inviterId)).thenReturn(Optional.of(inviterUser));
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // When
        InviteUserResponse response = invitationService.inviteUser(tenantId, inviteRequest, inviterId);

        // Then
        assertThat(response.getRole()).isEqualTo(UserRole.VIEWER);
    }

    // ========== REMOVE USER FROM TENANT TESTS ==========

    @Test
    @DisplayName("Should remove user from tenant successfully")
    void removeUserFromTenant_Success() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(userId);
        userTenant.setTenantId(tenantId);
        userTenant.setRole(UserRole.EDITOR);

        when(tenantRepository.existsById(tenantId)).thenReturn(true);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userTenantRepository.findByUserIdAndTenantId(userId, tenantId))
                .thenReturn(Optional.of(userTenant));
        doNothing().when(userTenantRepository).delete(userTenant);

        // When
        invitationService.removeUserFromTenant(tenantId, userId);

        // Then
        verify(tenantRepository).existsById(tenantId);
        verify(userRepository).existsById(userId);
        verify(userTenantRepository).findByUserIdAndTenantId(userId, tenantId);
        verify(userTenantRepository).delete(userTenant);
    }

    @Test
    @DisplayName("Should throw exception when removing user from non-existent tenant")
    void removeUserFromTenant_TenantNotFound_ThrowsException() {
        // Given
        when(tenantRepository.existsById(tenantId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> invitationService.removeUserFromTenant(tenantId, userId))
                .isInstanceOf(TenantNotFoundException.class);

        verify(userRepository, never()).existsById(any());
        verify(userTenantRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when removing non-existent user")
    void removeUserFromTenant_UserNotFound_ThrowsException() {
        // Given
        when(tenantRepository.existsById(tenantId)).thenReturn(true);
        when(userRepository.existsById(userId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> invitationService.removeUserFromTenant(tenantId, userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userTenantRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when user not member of tenant")
    void removeUserFromTenant_UserNotMember_ThrowsException() {
        // Given
        when(tenantRepository.existsById(tenantId)).thenReturn(true);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userTenantRepository.findByUserIdAndTenantId(userId, tenantId))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> invitationService.removeUserFromTenant(tenantId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a member");

        verify(userTenantRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should prevent removing last administrator from tenant")
    void removeUserFromTenant_LastAdmin_ThrowsException() {
        // Given
        UserTenant adminUserTenant = new UserTenant();
        adminUserTenant.setUserId(userId);
        adminUserTenant.setTenantId(tenantId);
        adminUserTenant.setRole(UserRole.ADMINISTRATOR);

        when(tenantRepository.existsById(tenantId)).thenReturn(true);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userTenantRepository.findByUserIdAndTenantId(userId, tenantId))
                .thenReturn(Optional.of(adminUserTenant));
        when(userTenantRepository.countAdministratorsByTenantId(tenantId)).thenReturn(1L);

        // When/Then
        assertThatThrownBy(() -> invitationService.removeUserFromTenant(tenantId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("last administrator");

        verify(userTenantRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should allow removing administrator when others exist")
    void removeUserFromTenant_NotLastAdmin_Success() {
        // Given
        UserTenant adminUserTenant = new UserTenant();
        adminUserTenant.setUserId(userId);
        adminUserTenant.setTenantId(tenantId);
        adminUserTenant.setRole(UserRole.ADMINISTRATOR);

        when(tenantRepository.existsById(tenantId)).thenReturn(true);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userTenantRepository.findByUserIdAndTenantId(userId, tenantId))
                .thenReturn(Optional.of(adminUserTenant));
        when(userTenantRepository.countAdministratorsByTenantId(tenantId)).thenReturn(2L);
        doNothing().when(userTenantRepository).delete(adminUserTenant);

        // When
        invitationService.removeUserFromTenant(tenantId, userId);

        // Then
        verify(userTenantRepository).delete(adminUserTenant);
    }

    @Test
    @DisplayName("Should remove non-admin user without admin count check")
    void removeUserFromTenant_NonAdmin_NoAdminCheck() {
        // Given
        UserTenant editorUserTenant = new UserTenant();
        editorUserTenant.setUserId(userId);
        editorUserTenant.setTenantId(tenantId);
        editorUserTenant.setRole(UserRole.EDITOR);

        when(tenantRepository.existsById(tenantId)).thenReturn(true);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userTenantRepository.findByUserIdAndTenantId(userId, tenantId))
                .thenReturn(Optional.of(editorUserTenant));
        doNothing().when(userTenantRepository).delete(editorUserTenant);

        // When
        invitationService.removeUserFromTenant(tenantId, userId);

        // Then
        verify(userTenantRepository).delete(editorUserTenant);
        verify(userTenantRepository, never()).countAdministratorsByTenantId(any());
    }
}
