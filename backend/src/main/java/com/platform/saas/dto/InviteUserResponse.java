package com.platform.saas.dto;

import com.platform.saas.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for user invitation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteUserResponse {

    private UUID userId;
    private UUID tenantId;
    private String email;
    private UserRole role;
    private UUID invitedBy;
    private LocalDateTime invitedAt;
    private boolean existingUser; // true if user already had an account
    private boolean emailSent; // true if invitation email was sent successfully
}
