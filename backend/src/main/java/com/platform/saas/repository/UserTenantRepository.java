package com.platform.saas.repository;

import com.platform.saas.model.UserRole;
import com.platform.saas.model.UserTenant;
import com.platform.saas.model.UserTenantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserTenant join entity operations.
 * Manages the many-to-many relationship between users and tenants.
 */
@Repository
public interface UserTenantRepository extends JpaRepository<UserTenant, UserTenantId> {

    /**
     * Find all tenants a user belongs to.
     * @param userId The user's ID
     * @return List of UserTenant associations for this user
     */
    List<UserTenant> findByUserId(UUID userId);

    /**
     * Find all users belonging to a tenant.
     * @param tenantId The tenant's ID
     * @return List of UserTenant associations for this tenant
     */
    List<UserTenant> findByTenantId(UUID tenantId);

    /**
     * Find a specific user-tenant relationship.
     * @param userId The user's ID
     * @param tenantId The tenant's ID
     * @return Optional containing the UserTenant if found
     */
    Optional<UserTenant> findByUserIdAndTenantId(UUID userId, UUID tenantId);

    /**
     * Check if a user belongs to a tenant.
     * @param userId The user's ID
     * @param tenantId The tenant's ID
     * @return true if the relationship exists, false otherwise
     */
    boolean existsByUserIdAndTenantId(UUID userId, UUID tenantId);

    /**
     * Find all users with a specific role in a tenant.
     * @param tenantId The tenant's ID
     * @param role The role to filter by
     * @return List of UserTenant associations matching the criteria
     */
    List<UserTenant> findByTenantIdAndRole(UUID tenantId, UserRole role);

    /**
     * Count the number of users in a tenant.
     * @param tenantId The tenant's ID
     * @return The count of users
     */
    @Query("SELECT COUNT(ut) FROM UserTenant ut WHERE ut.tenantId = :tenantId")
    long countUsersByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Count the number of administrators in a tenant.
     * @param tenantId The tenant's ID
     * @return The count of administrators
     */
    @Query("SELECT COUNT(ut) FROM UserTenant ut WHERE ut.tenantId = :tenantId AND ut.role = 'ADMINISTRATOR'")
    long countAdministratorsByTenantId(@Param("tenantId") UUID tenantId);
}
