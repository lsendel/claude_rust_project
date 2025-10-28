package com.platform.saas.service;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for UserService.
 * Tests user management, Cognito integration, and tenant membership operations.
 *
 * CRITICAL SERVICE COMPONENT - High coverage required
 * PMAT: Test code Cyc≤10, Cog≤15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests - Critical Service Component")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserTenantRepository userTenantRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Tenant testTenant;
    private UserTenant testUserTenant;
    private UUID userId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tenantId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setCognitoUserId("cognito-user-123");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testTenant = new Tenant();
        testTenant.setId(tenantId);
        testTenant.setSubdomain("acme");
        testTenant.setName("Acme Corp");

        testUserTenant = new UserTenant();
        testUserTenant.setUserId(userId);
        testUserTenant.setTenantId(tenantId);
        testUserTenant.setRole(UserRole.EDITOR);
    }

    // ========== CREATE USER FROM COGNITO TESTS ==========

    @Test
    @DisplayName("Should create new user from Cognito authentication")
    void createUserFromCognito_NewUser_Success() {
        // Given
        String cognitoId = "cognito-new-user";
        String email = "newuser@example.com";
        String name = "New User";

        when(userRepository.existsByCognitoUserId(cognitoId)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        // When
        User result = userService.createUserFromCognito(cognitoId, email, name);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCognitoUserId()).isEqualTo(cognitoId);
        assertThat(result.getEmail()).isEqualTo(email.toLowerCase());
        assertThat(result.getName()).isEqualTo(name);

        verify(userRepository).existsByCognitoUserId(cognitoId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should normalize email to lowercase when creating user from Cognito")
    void createUserFromCognito_NormalizeEmail_Success() {
        // Given
        String cognitoId = "cognito-user";
        String email = "User@EXAMPLE.COM";
        String name = "User";

        when(userRepository.existsByCognitoUserId(cognitoId)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.createUserFromCognito(cognitoId, email, name);

        // Then
        assertThat(result.getEmail()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("Should return existing user when Cognito ID already exists")
    void createUserFromCognito_ExistingUser_ReturnsExisting() {
        // Given
        String cognitoId = testUser.getCognitoUserId();
        String email = "different@example.com";
        String name = "Different Name";

        when(userRepository.existsByCognitoUserId(cognitoId)).thenReturn(true);
        when(userRepository.findByCognitoUserId(cognitoId)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.createUserFromCognito(cognitoId, email, name);

        // Then
        assertThat(result).isEqualTo(testUser);
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail()); // Original email retained

        verify(userRepository).existsByCognitoUserId(cognitoId);
        verify(userRepository).findByCognitoUserId(cognitoId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when existing user lookup fails")
    void createUserFromCognito_ExistingUserNotFound_ThrowsException() {
        // Given
        String cognitoId = "cognito-user";

        when(userRepository.existsByCognitoUserId(cognitoId)).thenReturn(true);
        when(userRepository.findByCognitoUserId(cognitoId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.createUserFromCognito(cognitoId, "email@test.com", "Name"))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ========== FIND OR CREATE USER BY EMAIL TESTS ==========

    @Test
    @DisplayName("Should return existing user when found by email")
    void findOrCreateUserByEmail_ExistingUser_ReturnsExisting() {
        // Given
        String email = testUser.getEmail();
        String name = "Different Name";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.findOrCreateUserByEmail(email, name);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should create placeholder user when not found by email")
    void findOrCreateUserByEmail_NewUser_CreatesPlaceholder() {
        // Given
        String email = "newuser@example.com";
        String name = "New User";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        // When
        User result = userService.findOrCreateUserByEmail(email, name);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getCognitoUserId()).startsWith("pending-");

        verify(userRepository).findByEmail(email);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should normalize email to lowercase when finding or creating user")
    void findOrCreateUserByEmail_NormalizeEmail_Success() {
        // Given
        String email = "USER@EXAMPLE.COM";

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));

        // When
        User result = userService.findOrCreateUserByEmail(email, "Name");

        // Then
        verify(userRepository).findByEmail("user@example.com");
    }

    // ========== GET USER BY ID TESTS ==========

    @Test
    @DisplayName("Should return user when found by ID")
    void getUserById_Found_Success() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserById(userId);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void getUserById_NotFound_ThrowsException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(nonExistentId);
    }

    // ========== GET USER BY COGNITO ID TESTS ==========

    @Test
    @DisplayName("Should return user when found by Cognito ID")
    void getUserByCognitoId_Found_Success() {
        // Given
        String cognitoId = testUser.getCognitoUserId();
        when(userRepository.findByCognitoUserId(cognitoId)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserByCognitoId(cognitoId);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByCognitoUserId(cognitoId);
    }

    @Test
    @DisplayName("Should throw exception when user not found by Cognito ID")
    void getUserByCognitoId_NotFound_ThrowsException() {
        // Given
        String cognitoId = "non-existent-cognito-id";
        when(userRepository.findByCognitoUserId(cognitoId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserByCognitoId(cognitoId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findByCognitoUserId(cognitoId);
    }

    // ========== GET USER BY EMAIL TESTS ==========

    @Test
    @DisplayName("Should return user when found by email")
    void getUserByEmail_Found_Success() {
        // Given
        String email = testUser.getEmail();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserByEmail(email);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should normalize email to lowercase when finding by email")
    void getUserByEmail_NormalizeEmail_Success() {
        // Given
        String email = "USER@EXAMPLE.COM";
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserByEmail(email);

        // Then
        verify(userRepository).findByEmail("user@example.com");
    }

    @Test
    @DisplayName("Should throw exception when user not found by email")
    void getUserByEmail_NotFound_ThrowsException() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findByEmail(email);
    }

    // ========== ADD USER TO TENANT TESTS ==========

    @Test
    @DisplayName("Should add user to tenant with specified role")
    void addUserToTenant_Success() {
        // Given
        UUID invitedBy = UUID.randomUUID();
        UserRole role = UserRole.ADMINISTRATOR;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(userTenantRepository.existsByUserIdAndTenantId(userId, tenantId)).thenReturn(false);
        when(userTenantRepository.save(any(UserTenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserTenant result = userService.addUserToTenant(userId, tenantId, role, invitedBy);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getTenantId()).isEqualTo(tenantId);
        assertThat(result.getRole()).isEqualTo(role);
        assertThat(result.getInvitedBy()).isEqualTo(invitedBy);

        verify(userRepository).findById(userId);
        verify(tenantRepository).findById(tenantId);
        verify(userTenantRepository).existsByUserIdAndTenantId(userId, tenantId);
        verify(userTenantRepository).save(any(UserTenant.class));
    }

    @Test
    @DisplayName("Should handle null invitedBy when adding user to tenant")
    void addUserToTenant_NullInvitedBy_Success() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(userTenantRepository.existsByUserIdAndTenantId(userId, tenantId)).thenReturn(false);
        when(userTenantRepository.save(any(UserTenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserTenant result = userService.addUserToTenant(userId, tenantId, UserRole.EDITOR, null);

        // Then
        assertThat(result.getInvitedBy()).isNull();
    }

    @Test
    @DisplayName("Should return existing relationship when user already in tenant")
    void addUserToTenant_AlreadyExists_ReturnsExisting() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(userTenantRepository.existsByUserIdAndTenantId(userId, tenantId)).thenReturn(true);
        when(userTenantRepository.findByUserIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(testUserTenant));

        // When
        UserTenant result = userService.addUserToTenant(userId, tenantId, UserRole.ADMINISTRATOR, null);

        // Then
        assertThat(result).isEqualTo(testUserTenant);
        verify(userTenantRepository, never()).save(any(UserTenant.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found when adding to tenant")
    void addUserToTenant_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.addUserToTenant(userId, tenantId, UserRole.EDITOR, null))
                .isInstanceOf(UserNotFoundException.class);

        verify(tenantRepository, never()).findById(any());
        verify(userTenantRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when tenant not found when adding user")
    void addUserToTenant_TenantNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.addUserToTenant(userId, tenantId, UserRole.EDITOR, null))
                .isInstanceOf(TenantNotFoundException.class);

        verify(userTenantRepository, never()).save(any());
    }

    // ========== GET USER TENANTS TESTS ==========

    @Test
    @DisplayName("Should return list of user's tenants")
    void getUserTenants_Success() {
        // Given
        UserTenant ut1 = new UserTenant();
        ut1.setUserId(userId);
        ut1.setTenantId(UUID.randomUUID());

        UserTenant ut2 = new UserTenant();
        ut2.setUserId(userId);
        ut2.setTenantId(UUID.randomUUID());

        List<UserTenant> userTenants = Arrays.asList(ut1, ut2);
        when(userTenantRepository.findByUserId(userId)).thenReturn(userTenants);

        // When
        List<UserTenant> result = userService.getUserTenants(userId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(ut1, ut2);
        verify(userTenantRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Should return empty list when user has no tenants")
    void getUserTenants_NoTenants_ReturnsEmptyList() {
        // Given
        when(userTenantRepository.findByUserId(userId)).thenReturn(List.of());

        // When
        List<UserTenant> result = userService.getUserTenants(userId);

        // Then
        assertThat(result).isEmpty();
        verify(userTenantRepository).findByUserId(userId);
    }

    // ========== GET TENANT USERS TESTS ==========

    @Test
    @DisplayName("Should return list of tenant's users")
    void getTenantUsers_Success() {
        // Given
        UserTenant ut1 = new UserTenant();
        ut1.setUserId(UUID.randomUUID());
        ut1.setTenantId(tenantId);

        UserTenant ut2 = new UserTenant();
        ut2.setUserId(UUID.randomUUID());
        ut2.setTenantId(tenantId);

        List<UserTenant> tenantUsers = Arrays.asList(ut1, ut2);
        when(userTenantRepository.findByTenantId(tenantId)).thenReturn(tenantUsers);

        // When
        List<UserTenant> result = userService.getTenantUsers(tenantId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(ut1, ut2);
        verify(userTenantRepository).findByTenantId(tenantId);
    }

    @Test
    @DisplayName("Should return empty list when tenant has no users")
    void getTenantUsers_NoUsers_ReturnsEmptyList() {
        // Given
        when(userTenantRepository.findByTenantId(tenantId)).thenReturn(List.of());

        // When
        List<UserTenant> result = userService.getTenantUsers(tenantId);

        // Then
        assertThat(result).isEmpty();
        verify(userTenantRepository).findByTenantId(tenantId);
    }

    // ========== GET USER TENANT RELATIONSHIP TESTS ==========

    @Test
    @DisplayName("Should return user-tenant relationship")
    void getUserTenantRelationship_Found_Success() {
        // Given
        when(userTenantRepository.findByUserIdAndTenantId(userId, tenantId))
                .thenReturn(Optional.of(testUserTenant));

        // When
        UserTenant result = userService.getUserTenantRelationship(userId, tenantId);

        // Then
        assertThat(result).isEqualTo(testUserTenant);
        verify(userTenantRepository).findByUserIdAndTenantId(userId, tenantId);
    }

    @Test
    @DisplayName("Should throw exception when relationship not found")
    void getUserTenantRelationship_NotFound_ThrowsException() {
        // Given
        when(userTenantRepository.findByUserIdAndTenantId(userId, tenantId))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserTenantRelationship(userId, tenantId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userTenantRepository).findByUserIdAndTenantId(userId, tenantId);
    }

    // ========== UPDATE LAST LOGIN TESTS ==========

    @Test
    @DisplayName("Should update last login timestamp")
    void updateLastLogin_Success() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        userService.updateLastLogin(userId);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception when user not found for last login update")
    void updateLastLogin_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.updateLastLogin(userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    // ========== HAS ROLE TESTS ==========

    @Test
    @DisplayName("Should return true when user has specified role")
    void hasRole_UserHasRole_ReturnsTrue() {
        // Given
        testUserTenant.setRole(UserRole.ADMINISTRATOR);
        when(userTenantRepository.findByUserIdAndTenantId(userId, tenantId))
                .thenReturn(Optional.of(testUserTenant));

        // When
        boolean result = userService.hasRole(userId, tenantId, UserRole.ADMINISTRATOR);

        // Then
        assertThat(result).isTrue();
        verify(userTenantRepository).findByUserIdAndTenantId(userId, tenantId);
    }

    @Test
    @DisplayName("Should return false when user has different role")
    void hasRole_UserHasDifferentRole_ReturnsFalse() {
        // Given
        testUserTenant.setRole(UserRole.EDITOR);
        when(userTenantRepository.findByUserIdAndTenantId(userId, tenantId))
                .thenReturn(Optional.of(testUserTenant));

        // When
        boolean result = userService.hasRole(userId, tenantId, UserRole.ADMINISTRATOR);

        // Then
        assertThat(result).isFalse();
        verify(userTenantRepository).findByUserIdAndTenantId(userId, tenantId);
    }

    @Test
    @DisplayName("Should return false when user not in tenant")
    void hasRole_UserNotInTenant_ReturnsFalse() {
        // Given
        when(userTenantRepository.findByUserIdAndTenantId(userId, tenantId))
                .thenReturn(Optional.empty());

        // When
        boolean result = userService.hasRole(userId, tenantId, UserRole.EDITOR);

        // Then
        assertThat(result).isFalse();
        verify(userTenantRepository).findByUserIdAndTenantId(userId, tenantId);
    }

    // ========== CAN EDIT TESTS ==========

    @Test
    @DisplayName("Should return true when user can edit")
    void canEdit_UserCanEdit_ReturnsTrue() {
        // Given
        testUserTenant.setRole(UserRole.ADMINISTRATOR);
        when(userTenantRepository.findByUserIdAndTenantId(userId, tenantId))
                .thenReturn(Optional.of(testUserTenant));

        // When
        boolean result = userService.canEdit(userId, tenantId);

        // Then
        assertThat(result).isTrue();
        verify(userTenantRepository).findByUserIdAndTenantId(userId, tenantId);
    }

    @Test
    @DisplayName("Should return false when user cannot edit")
    void canEdit_UserCannotEdit_ReturnsFalse() {
        // Given
        testUserTenant.setRole(UserRole.VIEWER);
        when(userTenantRepository.findByUserIdAndTenantId(userId, tenantId))
                .thenReturn(Optional.of(testUserTenant));

        // When
        boolean result = userService.canEdit(userId, tenantId);

        // Then
        assertThat(result).isFalse();
        verify(userTenantRepository).findByUserIdAndTenantId(userId, tenantId);
    }

    @Test
    @DisplayName("Should return false when user not in tenant for canEdit")
    void canEdit_UserNotInTenant_ReturnsFalse() {
        // Given
        when(userTenantRepository.findByUserIdAndTenantId(userId, tenantId))
                .thenReturn(Optional.empty());

        // When
        boolean result = userService.canEdit(userId, tenantId);

        // Then
        assertThat(result).isFalse();
        verify(userTenantRepository).findByUserIdAndTenantId(userId, tenantId);
    }

    // ========== IS ADMINISTRATOR TESTS ==========

    @Test
    @DisplayName("Should return true when user is administrator")
    void isAdministrator_UserIsAdmin_ReturnsTrue() {
        // Given
        testUserTenant.setRole(UserRole.ADMINISTRATOR);
        when(userTenantRepository.findByUserIdAndTenantId(userId, tenantId))
                .thenReturn(Optional.of(testUserTenant));

        // When
        boolean result = userService.isAdministrator(userId, tenantId);

        // Then
        assertThat(result).isTrue();
        verify(userTenantRepository).findByUserIdAndTenantId(userId, tenantId);
    }

    @Test
    @DisplayName("Should return false when user is not administrator")
    void isAdministrator_UserIsNotAdmin_ReturnsFalse() {
        // Given
        testUserTenant.setRole(UserRole.EDITOR);
        when(userTenantRepository.findByUserIdAndTenantId(userId, tenantId))
                .thenReturn(Optional.of(testUserTenant));

        // When
        boolean result = userService.isAdministrator(userId, tenantId);

        // Then
        assertThat(result).isFalse();
        verify(userTenantRepository).findByUserIdAndTenantId(userId, tenantId);
    }

    @Test
    @DisplayName("Should return false when user not in tenant for isAdministrator")
    void isAdministrator_UserNotInTenant_ReturnsFalse() {
        // Given
        when(userTenantRepository.findByUserIdAndTenantId(userId, tenantId))
                .thenReturn(Optional.empty());

        // When
        boolean result = userService.isAdministrator(userId, tenantId);

        // Then
        assertThat(result).isFalse();
        verify(userTenantRepository).findByUserIdAndTenantId(userId, tenantId);
    }
}
