package com.platform.saas.dto;

import com.platform.saas.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for user information including tenant-specific role.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID userId;
    private String email;
    private String name;
    private UserRole role;
    private UUID invitedBy;
    private LocalDateTime joinedAt;
    private LocalDateTime lastLoginAt;
}
