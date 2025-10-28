package com.platform.saas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UserTenant join entity maps users to tenants with tenant-specific roles.
 * A user can belong to multiple tenants with different roles in each.
 */
@Entity
@Table(name = "user_tenants", indexes = {
    @Index(name = "idx_user_tenant_tenant_id", columnList = "tenant_id")
})
@EntityListeners(AuditingEntityListener.class)
@IdClass(UserTenantId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTenant {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Id
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", insertable = false, updatable = false)
    private Tenant tenant;

    @NotNull(message = "Role cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "invited_by")
    private UUID invitedBy;

    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    /**
     * Check if user has administrative privileges in this tenant.
     */
    public boolean isAdministrator() {
        return this.role == UserRole.ADMINISTRATOR;
    }

    /**
     * Check if user can edit resources in this tenant.
     */
    public boolean canEdit() {
        return this.role == UserRole.ADMINISTRATOR || this.role == UserRole.EDITOR;
    }

    /**
     * Check if user can only view resources in this tenant.
     */
    public boolean isViewerOnly() {
        return this.role == UserRole.VIEWER;
    }

    @Override
    public String toString() {
        return "UserTenant{" +
                "userId=" + userId +
                ", tenantId=" + tenantId +
                ", role=" + role +
                ", joinedAt=" + joinedAt +
                '}';
    }
}
