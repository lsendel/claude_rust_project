package com.platform.saas.dto;

import com.platform.saas.model.SubscriptionTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for tenant information responses.
 * Contains tenant details suitable for external API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {

    private UUID id;
    private String subdomain;
    private String name;
    private String description;
    private SubscriptionTier subscriptionTier;
    private Integer quotaLimit;
    private Long currentUsage;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
