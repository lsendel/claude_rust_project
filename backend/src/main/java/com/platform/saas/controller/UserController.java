package com.platform.saas.controller;

import com.platform.saas.dto.InviteUserRequest;
import com.platform.saas.dto.InviteUserResponse;
import com.platform.saas.dto.UserResponse;
import com.platform.saas.model.User;
import com.platform.saas.model.UserTenant;
import com.platform.saas.repository.UserRepository;
import com.platform.saas.repository.UserTenantRepository;
import com.platform.saas.security.TenantContext;
import com.platform.saas.service.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for user management within tenants.
 * Handles user invitations, listing, and removal.
 */
@RestController
@RequestMapping("/api/tenants/{tenantId}/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final InvitationService invitationService;
    private final UserTenantRepository userTenantRepository;
    private final UserRepository userRepository;

    /**
     * Invite a user to join a tenant.
     * Only administrators can invite users.
     *
     * POST /api/tenants/{tenantId}/users/invite
     *
     * @param tenantId Tenant ID
     * @param request Invitation request with email and role
     * @return InviteUserResponse with invitation details
     */
    @PostMapping("/invite")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<InviteUserResponse> inviteUser(
            @PathVariable UUID tenantId,
            @Valid @RequestBody InviteUserRequest request) {
        log.info("Inviting user {} to tenant {} with role {}", request.getEmail(), tenantId, request.getRole());

        // Get current user ID from security context (inviter)
        UUID invitedByUserId = getCurrentUserId();

        InviteUserResponse response = invitationService.inviteUser(tenantId, request, invitedByUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all users belonging to a tenant.
     * All authenticated users can view the member list.
     *
     * GET /api/tenants/{tenantId}/users
     *
     * @param tenantId Tenant ID
     * @return List of users with their roles
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getTenantUsers(@PathVariable UUID tenantId) {
        log.info("Fetching users for tenant {}", tenantId);

        List<UserTenant> userTenants = userTenantRepository.findByTenantId(tenantId);

        List<UserResponse> userResponses = userTenants.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(userResponses);
    }

    /**
     * Remove a user from a tenant.
     * Only administrators can remove users.
     * Cannot remove the last administrator.
     *
     * DELETE /api/tenants/{tenantId}/users/{userId}
     *
     * @param tenantId Tenant ID
     * @param userId User ID to remove
     * @return 204 No Content on success
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<Void> removeUser(
            @PathVariable UUID tenantId,
            @PathVariable UUID userId) {
        log.info("Removing user {} from tenant {}", userId, tenantId);

        invitationService.removeUserFromTenant(tenantId, userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Map UserTenant and User entities to UserResponse DTO.
     */
    private UserResponse mapToUserResponse(UserTenant userTenant) {
        User user = userRepository.findById(userTenant.getUserId())
                .orElse(null);

        if (user == null) {
            // User has been deleted but UserTenant still exists (data inconsistency)
            return UserResponse.builder()
                    .userId(userTenant.getUserId())
                    .email("unknown@example.com")
                    .name("Unknown User")
                    .role(userTenant.getRole())
                    .invitedBy(userTenant.getInvitedBy())
                    .joinedAt(userTenant.getJoinedAt())
                    .build();
        }

        return UserResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(userTenant.getRole())
                .invitedBy(userTenant.getInvitedBy())
                .joinedAt(userTenant.getJoinedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    /**
     * Get the current user's ID from the security context.
     * This should be extracted from the JWT token in a real implementation.
     * For now, returns a placeholder UUID.
     */
    private UUID getCurrentUserId() {
        // TODO: Extract from SecurityContext/JWT token in actual implementation
        // For now, return the first administrator in the tenant as a placeholder
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            return userTenantRepository.findByTenantId(tenantId).stream()
                    .filter(ut -> ut.getRole() == com.platform.saas.model.UserRole.ADMINISTRATOR)
                    .findFirst()
                    .map(UserTenant::getUserId)
                    .orElse(UUID.randomUUID());
        }
        return UUID.randomUUID();
    }
}
