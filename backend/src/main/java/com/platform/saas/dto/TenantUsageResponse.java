package com.platform.saas.dto;

import com.platform.saas.model.SubscriptionTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for tenant quota usage information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantUsageResponse {

    private Long projectCount;
    private Long taskCount;
    private Long totalUsage;
    private Integer quotaLimit;
    private SubscriptionTier subscriptionTier;
    private Double usagePercentage;
    private Boolean quotaExceeded;
    private Boolean nearingQuota; // true if >= 80% of quota

    /**
     * Calculate usage percentage.
     */
    public void calculateUsagePercentage() {
        if (quotaLimit == null || quotaLimit == 0) {
            this.usagePercentage = 0.0;
            this.quotaExceeded = false;
            this.nearingQuota = false;
        } else {
            this.usagePercentage = (totalUsage.doubleValue() / quotaLimit) * 100;
            this.quotaExceeded = totalUsage >= quotaLimit;
            this.nearingQuota = usagePercentage >= 80.0;
        }
    }
}
