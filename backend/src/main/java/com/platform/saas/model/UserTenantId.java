package com.platform.saas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite primary key for UserTenant entity.
 * Represents the many-to-many relationship between User and Tenant.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTenantId implements Serializable {

    private UUID userId;
    private UUID tenantId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserTenantId that = (UserTenantId) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(tenantId, that.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, tenantId);
    }
}
