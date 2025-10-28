package com.platform.saas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Tenant entity represents an organization or customer account.
 * Each tenant has complete data isolation and subscription-based quotas.
 */
@Entity
@Table(name = "tenants", indexes = {
    @Index(name = "idx_tenant_subdomain", columnList = "subdomain", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Subdomain cannot be blank")
    @Size(min = 3, max = 63, message = "Subdomain must be between 3 and 63 characters")
    @Pattern(regexp = "^[a-z0-9-]{3,63}$", message = "Subdomain must be lowercase alphanumeric with hyphens only")
    @Column(name = "subdomain", unique = true, nullable = false, length = 63)
    private String subdomain;

    @NotBlank(message = "Tenant name cannot be blank")
    @Size(max = 255, message = "Tenant name must not exceed 255 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @NotNull(message = "Subscription tier cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_tier", nullable = false)
    private SubscriptionTier subscriptionTier = SubscriptionTier.FREE;

    @Column(name = "quota_limit")
    private Integer quotaLimit;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Pre-persist hook to set quota limit based on subscription tier.
     */
    @PrePersist
    public void prePersist() {
        if (this.quotaLimit == null) {
            this.quotaLimit = getDefaultQuotaForTier(this.subscriptionTier);
        }
        validateSubdomain();
    }

    /**
     * Pre-update hook to validate subdomain and quota.
     */
    @PreUpdate
    public void preUpdate() {
        validateSubdomain();
    }

    /**
     * Get default quota limit for a subscription tier.
     */
    private Integer getDefaultQuotaForTier(SubscriptionTier tier) {
        return switch (tier) {
            case FREE -> 50;
            case PRO -> 1000;
            case ENTERPRISE -> null; // unlimited
        };
    }

    /**
     * Validate that subdomain is not a reserved word.
     */
    private void validateSubdomain() {
        String[] reservedSubdomains = {"www", "api", "admin", "app", "platform"};
        for (String reserved : reservedSubdomains) {
            if (reserved.equalsIgnoreCase(this.subdomain)) {
                throw new IllegalArgumentException("Subdomain '" + reserved + "' is reserved and cannot be used");
            }
        }
    }

    /**
     * Check if tenant has reached its quota limit.
     */
    public boolean isQuotaExceeded(long currentUsage) {
        if (this.quotaLimit == null) {
            return false; // Unlimited (Enterprise tier)
        }
        return currentUsage >= this.quotaLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tenant tenant = (Tenant) o;
        return Objects.equals(id, tenant.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Tenant{" +
                "id=" + id +
                ", subdomain='" + subdomain + '\'' +
                ", name='" + name + '\'' +
                ", subscriptionTier=" + subscriptionTier +
                ", quotaLimit=" + quotaLimit +
                '}';
    }
}
