package com.platform.saas.dto;

import com.platform.saas.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for inviting a user to a tenant.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteUserRequest {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    private String email;

    @NotNull(message = "Role cannot be null")
    private UserRole role;

    private String message; // Optional custom message to include in invitation email
}
