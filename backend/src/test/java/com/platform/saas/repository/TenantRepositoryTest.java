package com.platform.saas.repository;

import com.platform.saas.model.SubscriptionTier;
import com.platform.saas.model.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TenantRepository.
 *
 * Sprint 3 - Repository Layer Testing
 * Uses @DataJpaTest for in-memory H2 database testing
 * Excludes Flyway and uses JPA schema generation
 *
 * Test Categories:
 * 1. findBySubdomain (3 tests)
 * 2. existsBySubdomain (2 tests)
 * 3. CRUD Operations (3 tests)
 */
@DataJpaTest(excludeAutoConfiguration = FlywayAutoConfiguration.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("TenantRepository Integration Tests")
class TenantRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant testTenant;

    @BeforeEach
    void setUp() {
        testTenant = new Tenant();
        testTenant.setSubdomain("test-tenant");
        testTenant.setName("Test Tenant");
        testTenant.setSubscriptionTier(SubscriptionTier.FREE);
        testTenant.setCreatedAt(LocalDateTime.now());
        testTenant.setUpdatedAt(LocalDateTime.now());
    }

    // ==================== findBySubdomain Tests ====================

    @Test
    @DisplayName("Should find tenant by subdomain when exists")
    void findBySubdomain_ExistingTenant_Found() {
        // Given
        entityManager.persist(testTenant);
        entityManager.flush();

        // When
        Optional<Tenant> found = tenantRepository.findBySubdomain("test-tenant");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getSubdomain()).isEqualTo("test-tenant");
        assertThat(found.get().getName()).isEqualTo("Test Tenant");
    }

    @Test
    @DisplayName("Should return empty when tenant not found by subdomain")
    void findBySubdomain_NonExistingTenant_Empty() {
        // When
        Optional<Tenant> found = tenantRepository.findBySubdomain("nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find correct tenant among multiple tenants by subdomain")
    void findBySubdomain_MultipleTenants_FindsCorrect() {
        // Given
        Tenant tenant1 = new Tenant();
        tenant1.setSubdomain("tenant-one");
        tenant1.setName("Tenant One");
        tenant1.setSubscriptionTier(SubscriptionTier.FREE);
        tenant1.setCreatedAt(LocalDateTime.now());
        tenant1.setUpdatedAt(LocalDateTime.now());

        Tenant tenant2 = new Tenant();
        tenant2.setSubdomain("tenant-two");
        tenant2.setName("Tenant Two");
        tenant2.setSubscriptionTier(SubscriptionTier.PRO);
        tenant2.setCreatedAt(LocalDateTime.now());
        tenant2.setUpdatedAt(LocalDateTime.now());

        entityManager.persist(tenant1);
        entityManager.persist(tenant2);
        entityManager.flush();

        // When
        Optional<Tenant> found = tenantRepository.findBySubdomain("tenant-two");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getSubdomain()).isEqualTo("tenant-two");
        assertThat(found.get().getName()).isEqualTo("Tenant Two");
        assertThat(found.get().getSubscriptionTier()).isEqualTo(SubscriptionTier.PRO);
    }

    // ==================== existsBySubdomain Tests ====================

    @Test
    @DisplayName("Should return true when subdomain exists")
    void existsBySubdomain_ExistingSubdomain_ReturnsTrue() {
        // Given
        entityManager.persist(testTenant);
        entityManager.flush();

        // When
        boolean exists = tenantRepository.existsBySubdomain("test-tenant");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when subdomain does not exist")
    void existsBySubdomain_NonExistingSubdomain_ReturnsFalse() {
        // When
        boolean exists = tenantRepository.existsBySubdomain("nonexistent");

        // Then
        assertThat(exists).isFalse();
    }

    // ==================== CRUD Operations Tests ====================

    @Test
    @DisplayName("Should save and retrieve tenant")
    void save_ValidTenant_Success() {
        // When
        Tenant saved = tenantRepository.save(testTenant);
        Tenant retrieved = tenantRepository.findById(saved.getId()).orElse(null);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getSubdomain()).isEqualTo("test-tenant");
        assertThat(retrieved.getName()).isEqualTo("Test Tenant");
    }

    @Test
    @DisplayName("Should delete tenant")
    void delete_ExistingTenant_Success() {
        // Given
        Tenant saved = tenantRepository.save(testTenant);

        // When
        tenantRepository.delete(saved);
        Optional<Tenant> found = tenantRepository.findById(saved.getId());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should count all tenants")
    void count_MultipleTenants_ReturnsCorrectCount() {
        // Given
        Tenant tenant1 = new Tenant();
        tenant1.setSubdomain("tenant-one");
        tenant1.setName("Tenant One");
        tenant1.setSubscriptionTier(SubscriptionTier.FREE);
        tenant1.setCreatedAt(LocalDateTime.now());
        tenant1.setUpdatedAt(LocalDateTime.now());

        Tenant tenant2 = new Tenant();
        tenant2.setSubdomain("tenant-two");
        tenant2.setName("Tenant Two");
        tenant2.setSubscriptionTier(SubscriptionTier.PRO);
        tenant2.setCreatedAt(LocalDateTime.now());
        tenant2.setUpdatedAt(LocalDateTime.now());

        tenantRepository.save(tenant1);
        tenantRepository.save(tenant2);

        // When
        long count = tenantRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }
}
