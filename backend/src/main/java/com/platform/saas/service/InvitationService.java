package com.platform.saas.service;

import com.platform.saas.dto.InviteUserRequest;
import com.platform.saas.dto.InviteUserResponse;
import com.platform.saas.exception.TenantNotFoundException;
import com.platform.saas.exception.UserNotFoundException;
import com.platform.saas.model.*;
import com.platform.saas.repository.TenantRepository;
import com.platform.saas.repository.UserRepository;
import com.platform.saas.repository.UserTenantRepository;
import com.platform.saas.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing user invitations to tenants.
 * Handles invitation email sending and user-tenant association creation.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InvitationService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final UserTenantRepository userTenantRepository;
    private final EmailService emailService; // Will be implemented in T064

    /**
     * Invite a user to join a tenant.
     * If the user doesn't exist, creates a placeholder user record.
     * Sends an invitation email with registration/login link.
     *
     * @param tenantId Tenant to invite user to
     * @param request Invitation request containing email and role
     * @param invitedByUserId ID of the user sending the invitation
     * @return InviteUserResponse with invitation details
     * @throws TenantNotFoundException if tenant doesn't exist
     * @throws IllegalArgumentException if user is already a member
     */
    public InviteUserResponse inviteUser(UUID tenantId, InviteUserRequest request, UUID invitedByUserId) {
        log.info("Inviting user {} to tenant {} with role {}", request.getEmail(), tenantId, request.getRole());

        // Validate tenant exists
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        User user;
        boolean isExistingUser = existingUser.isPresent();

        if (isExistingUser) {
            user = existingUser.get();
            log.info("User {} already exists with ID {}", request.getEmail(), user.getId());

            // Check if user is already a member of this tenant
            if (userTenantRepository.existsByUserIdAndTenantId(user.getId(), tenantId)) {
                throw new IllegalArgumentException(
                        String.format("User %s is already a member of tenant %s",
                                request.getEmail(), tenant.getName())
                );
            }
        } else {
            // Create placeholder user (will be completed when they sign up)
            user = new User();
            user.setEmail(request.getEmail());
            user.setName(extractNameFromEmail(request.getEmail()));
            // Cognito user ID will be set when they complete signup
            user.setCognitoUserId("PENDING_" + UUID.randomUUID().toString());
            user = userRepository.save(user);
            log.info("Created placeholder user with ID {} for email {}", user.getId(), request.getEmail());
        }

        // Create user-tenant association
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(user.getId());
        userTenant.setTenantId(tenantId);
        userTenant.setRole(request.getRole());
        userTenant.setInvitedBy(invitedByUserId);
        userTenant.setJoinedAt(LocalDateTime.now());
        userTenantRepository.save(userTenant);

        log.info("Created UserTenant association for user {} in tenant {} with role {}",
                user.getId(), tenantId, request.getRole());

        // Send invitation email
        boolean emailSent = sendInvitationEmail(user, tenant, request, invitedByUserId);

        return InviteUserResponse.builder()
                .userId(user.getId())
                .tenantId(tenantId)
                .email(user.getEmail())
                .role(request.getRole())
                .invitedBy(invitedByUserId)
                .invitedAt(LocalDateTime.now())
                .existingUser(isExistingUser)
                .emailSent(emailSent)
                .build();
    }

    /**
     * Remove a user from a tenant.
     * Prevents removal of the last administrator.
     *
     * @param tenantId Tenant to remove user from
     * @param userId User to remove
     * @throws TenantNotFoundException if tenant doesn't exist
     * @throws UserNotFoundException if user doesn't exist
     * @throws IllegalArgumentException if attempting to remove last admin
     */
    public void removeUserFromTenant(UUID tenantId, UUID userId) {
        log.info("Removing user {} from tenant {}", userId, tenantId);

        // Validate tenant and user exist
        if (!tenantRepository.existsById(tenantId)) {
            throw new TenantNotFoundException(tenantId);
        }

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        // Check if user is in the tenant
        UserTenant userTenant = userTenantRepository.findByUserIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("User %s is not a member of tenant %s", userId, tenantId)
                ));

        // Prevent removal of last administrator
        if (userTenant.getRole() == UserRole.ADMINISTRATOR) {
            long adminCount = userTenantRepository.countAdministratorsByTenantId(tenantId);
            if (adminCount <= 1) {
                throw new IllegalArgumentException(
                        "Cannot remove the last administrator from the tenant"
                );
            }
        }

        userTenantRepository.delete(userTenant);
        log.info("Successfully removed user {} from tenant {}", userId, tenantId);
    }

    /**
     * Send invitation email to user.
     * Placeholder implementation - AWS SES integration in T064.
     *
     * @param user User being invited
     * @param tenant Tenant they're being invited to
     * @param request Invitation request
     * @param invitedByUserId ID of user sending invitation
     * @return true if email sent successfully, false otherwise
     */
    private boolean sendInvitationEmail(User user, Tenant tenant, InviteUserRequest request, UUID invitedByUserId) {
        try {
            // Get inviter's name
            String inviterName = userRepository.findById(invitedByUserId)
                    .map(User::getName)
                    .orElse("Someone");

            // Build invitation link
            String invitationLink = buildInvitationLink(tenant.getSubdomain(), user.getEmail());

            // Email subject and body
            String subject = String.format("You're invited to join %s", tenant.getName());
            String body = buildInvitationEmailBody(inviterName, tenant.getName(),
                    request.getRole().toString(), invitationLink, request.getMessage());

            // Send email via EmailService (will be implemented in T064)
            emailService.sendEmail(user.getEmail(), subject, body);

            log.info("Invitation email sent to {} for tenant {}", user.getEmail(), tenant.getName());
            return true;
        } catch (Exception e) {
            log.error("Failed to send invitation email to {}: {}", user.getEmail(), e.getMessage());
            return false;
        }
    }

    /**
     * Build invitation link based on tenant subdomain.
     */
    private String buildInvitationLink(String subdomain, String email) {
        // TODO: Get base URL from configuration
        String baseUrl = "https://app.example.com"; // Placeholder
        return String.format("%s/signup?subdomain=%s&email=%s", baseUrl, subdomain, email);
    }

    /**
     * Build invitation email body with HTML formatting.
     */
    private String buildInvitationEmailBody(String inviterName, String tenantName,
                                           String role, String invitationLink, String customMessage) {
        StringBuilder body = new StringBuilder();
        body.append(String.format("<h2>%s invited you to join %s</h2>", inviterName, tenantName));
        body.append(String.format("<p>You've been invited to join <strong>%s</strong> as a <strong>%s</strong>.</p>",
                tenantName, role));

        if (customMessage != null && !customMessage.isBlank()) {
            body.append(String.format("<p><em>%s</em></p>", customMessage));
        }

        body.append("<p>Click the link below to get started:</p>");
        body.append(String.format("<p><a href=\"%s\">Accept Invitation</a></p>", invitationLink));
        body.append("<p>If you didn't expect this invitation, you can safely ignore this email.</p>");

        return body.toString();
    }

    /**
     * Extract a name from an email address.
     * e.g., "john.doe@example.com" -> "John Doe"
     */
    private String extractNameFromEmail(String email) {
        String localPart = email.split("@")[0];
        String[] parts = localPart.split("[._-]");
        StringBuilder name = new StringBuilder();

        for (String part : parts) {
            if (name.length() > 0) {
                name.append(" ");
            }
            name.append(part.substring(0, 1).toUpperCase());
            if (part.length() > 1) {
                name.append(part.substring(1).toLowerCase());
            }
        }

        return name.toString();
    }
}
