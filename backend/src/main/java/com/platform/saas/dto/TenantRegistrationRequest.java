package com.platform.saas.dto;

import com.platform.saas.model.SubscriptionTier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for tenant registration requests.
 * Contains all necessary information to create a new tenant and associate the owner.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantRegistrationRequest {

    @NotBlank(message = "Subdomain is required")
    @Size(min = 3, max = 63, message = "Subdomain must be between 3 and 63 characters")
    @Pattern(regexp = "^[a-z0-9-]{3,63}$", message = "Subdomain must contain only lowercase letters, numbers, and hyphens")
    private String subdomain;

    @NotBlank(message = "Organization name is required")
    @Size(min = 1, max = 255, message = "Organization name must be between 1 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "Owner email is required")
    @Email(message = "Owner email must be valid")
    private String ownerEmail;

    @NotBlank(message = "Owner name is required")
    @Size(min = 1, max = 255, message = "Owner name must be between 1 and 255 characters")
    private String ownerName;

    private SubscriptionTier subscriptionTier = SubscriptionTier.FREE;
}
