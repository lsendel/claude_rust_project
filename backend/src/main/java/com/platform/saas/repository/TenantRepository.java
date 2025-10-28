package com.platform.saas.repository;

import com.platform.saas.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Tenant entity operations.
 * Provides CRUD operations and custom queries for tenant management.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    /**
     * Find a tenant by its subdomain.
     * @param subdomain The unique subdomain identifier
     * @return Optional containing the tenant if found
     */
    Optional<Tenant> findBySubdomain(String subdomain);

    /**
     * Check if a subdomain already exists.
     * @param subdomain The subdomain to check
     * @return true if subdomain exists, false otherwise
     */
    boolean existsBySubdomain(String subdomain);
}
