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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for user management and user-tenant relationship operations.
 * Handles user creation, Cognito integration, and tenant membership management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final UserTenantRepository userTenantRepository;

    /**
     * Create a new user from AWS Cognito authentication.
     * This is typically called after successful OAuth2 authentication.
     *
     * @param cognitoUserId The Cognito user identifier
     * @param email The user's email address
     * @param name The user's display name
     * @return The created user
     */
    @Transactional
    public User createUserFromCognito(String cognitoUserId, String email, String name) {
        log.info("Creating user from Cognito: {}", email);

        // Check if user already exists
        if (userRepository.existsByCognitoUserId(cognitoUserId)) {
            log.info("User already exists with Cognito ID: {}", cognitoUserId);
            return userRepository.findByCognitoUserId(cognitoUserId)
                    .orElseThrow(() -> new UserNotFoundException(cognitoUserId, "cognitoUserId"));
        }

        User user = new User();
        user.setCognitoUserId(cognitoUserId);
        user.setEmail(email.toLowerCase());
        user.setName(name);

        user = userRepository.save(user);
        log.info("User created with ID: {}", user.getId());

        return user;
    }

    /**
     * Find or create a user by email.
     * Used during tenant registration when owner might not have authenticated yet.
     *
     * @param email The user's email address
     * @param name The user's display name
     * @return The found or created user
     */
    @Transactional
    public User findOrCreateUserByEmail(String email, String name) {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseGet(() -> {
                    log.info("Creating placeholder user for email: {}", email);
                    User user = new User();
                    user.setCognitoUserId("pending-" + UUID.randomUUID()); // Temporary placeholder
                    user.setEmail(email.toLowerCase());
                    user.setName(name);
                    return userRepository.save(user);
                });
    }

    /**
     * Get user by ID.
     *
     * @param userId The user ID
     * @return The user
     * @throws UserNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * Get user by Cognito user ID.
     *
     * @param cognitoUserId The Cognito user identifier
     * @return The user
     * @throws UserNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public User getUserByCognitoId(String cognitoUserId) {
        return userRepository.findByCognitoUserId(cognitoUserId)
                .orElseThrow(() -> new UserNotFoundException(cognitoUserId, "cognitoUserId"));
    }

    /**
     * Get user by email.
     *
     * @param email The user's email address
     * @return The user
     * @throws UserNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UserNotFoundException(email, "email"));
    }

    /**
     * Add a user to a tenant with a specific role.
     *
     * @param userId The user ID
     * @param tenantId The tenant ID
     * @param role The role to assign
     * @param invitedBy The user ID who invited this user (null for self-registration)
     * @return The created UserTenant relationship
     * @throws UserNotFoundException if user not found
     * @throws TenantNotFoundException if tenant not found
     */
    @Transactional
    public UserTenant addUserToTenant(UUID userId, UUID tenantId, UserRole role, UUID invitedBy) {
        log.info("Adding user {} to tenant {} with role {}", userId, tenantId, role);

        // Verify user and tenant exist
        User user = getUserById(userId);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        // Check if relationship already exists
        if (userTenantRepository.existsByUserIdAndTenantId(userId, tenantId)) {
            log.info("User {} already belongs to tenant {}", userId, tenantId);
            return userTenantRepository.findByUserIdAndTenantId(userId, tenantId)
                    .orElseThrow();
        }

        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(userId);
        userTenant.setTenantId(tenantId);
        userTenant.setRole(role);
        userTenant.setInvitedBy(invitedBy);

        userTenant = userTenantRepository.save(userTenant);
        log.info("User {} added to tenant {} successfully", userId, tenantId);

        return userTenant;
    }

    /**
     * Get all tenants a user belongs to.
     *
     * @param userId The user ID
     * @return List of UserTenant relationships
     */
    @Transactional(readOnly = true)
    public List<UserTenant> getUserTenants(UUID userId) {
        return userTenantRepository.findByUserId(userId);
    }

    /**
     * Get all users in a tenant.
     *
     * @param tenantId The tenant ID
     * @return List of UserTenant relationships
     */
    @Transactional(readOnly = true)
    public List<UserTenant> getTenantUsers(UUID tenantId) {
        return userTenantRepository.findByTenantId(tenantId);
    }

    /**
     * Get a user's role in a specific tenant.
     *
     * @param userId The user ID
     * @param tenantId The tenant ID
     * @return The UserTenant relationship
     * @throws UserNotFoundException if relationship not found
     */
    @Transactional(readOnly = true)
    public UserTenant getUserTenantRelationship(UUID userId, UUID tenantId) {
        return userTenantRepository.findByUserIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * Update user's last login timestamp.
     *
     * @param userId The user ID
     */
    @Transactional
    public void updateLastLogin(UUID userId) {
        User user = getUserById(userId);
        user.updateLastLogin();
        userRepository.save(user);
        log.debug("Updated last login for user: {}", userId);
    }

    /**
     * Check if a user has a specific role in a tenant.
     *
     * @param userId The user ID
     * @param tenantId The tenant ID
     * @param role The role to check
     * @return true if user has the role, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean hasRole(UUID userId, UUID tenantId, UserRole role) {
        return userTenantRepository.findByUserIdAndTenantId(userId, tenantId)
                .map(ut -> ut.getRole() == role)
                .orElse(false);
    }

    /**
     * Check if a user can edit resources in a tenant.
     *
     * @param userId The user ID
     * @param tenantId The tenant ID
     * @return true if user can edit, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean canEdit(UUID userId, UUID tenantId) {
        return userTenantRepository.findByUserIdAndTenantId(userId, tenantId)
                .map(UserTenant::canEdit)
                .orElse(false);
    }

    /**
     * Check if a user is an administrator in a tenant.
     *
     * @param userId The user ID
     * @param tenantId The tenant ID
     * @return true if user is administrator, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isAdministrator(UUID userId, UUID tenantId) {
        return userTenantRepository.findByUserIdAndTenantId(userId, tenantId)
                .map(UserTenant::isAdministrator)
                .orElse(false);
    }
}
