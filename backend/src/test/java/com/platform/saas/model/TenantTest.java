package com.platform.saas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive test suite for Tenant entity.
 *
 * Sprint 2 - Model Layer Testing
 * Target: Validate all constraints, business logic, and entity behavior
 *
 * Test Categories:
 * 1. Entity Creation (4 tests)
 * 2. Subdomain Validation (10 tests)
 * 3. Name Validation (3 tests)
 * 4. Description Validation (2 tests)
 * 5. Active Status (2 tests)
 * 6. Subscription Tier (4 tests)
 * 7. Quota Management (6 tests)
 * 8. Lifecycle Hooks (5 tests)
 * 9. Business Methods (4 tests)
 * 10. Equals/HashCode (5 tests)
 * 11. ToString (2 tests)
 * 12. Entity Metadata (3 tests)
 */
@DisplayName("Tenant Entity Tests - Core Multi-Tenant Model")
class TenantTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==================== Entity Creation Tests ====================

    @Test
    @DisplayName("Should create valid tenant with all required fields")
    void create_ValidTenant_Success() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("acme-corp");
        tenant.setName("Acme Corporation");

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isEmpty();
        assertThat(tenant.getSubdomain()).isEqualTo("acme-corp");
        assertThat(tenant.getName()).isEqualTo("Acme Corporation");
        assertThat(tenant.isActive()).isTrue(); // Default value
        assertThat(tenant.getSubscriptionTier()).isEqualTo(SubscriptionTier.FREE); // Default value
    }

    @Test
    @DisplayName("Should set default values on creation")
    void create_Tenant_DefaultValues() {
        // Given
        Tenant tenant = new Tenant();

        // Then
        assertThat(tenant.isActive()).isTrue();
        assertThat(tenant.getSubscriptionTier()).isEqualTo(SubscriptionTier.FREE);
    }

    @Test
    @DisplayName("Should create tenant with optional description")
    void create_TenantWithDescription_Success() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("acme-corp");
        tenant.setName("Acme Corporation");
        tenant.setDescription("Global logistics and supply chain management");

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isEmpty();
        assertThat(tenant.getDescription()).isEqualTo("Global logistics and supply chain management");
    }

    @Test
    @DisplayName("Should fail validation when required fields are missing")
    void validate_MissingRequiredFields_Fails() {
        // Given
        Tenant tenant = new Tenant();
        // No subdomain or name set

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
    }

    // ==================== Subdomain Validation Tests ====================

    @Test
    @DisplayName("Should fail validation when subdomain is blank")
    void validate_SubdomainBlank_Fails() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("");
        tenant.setName("Test Company");

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("subdomain") &&
            v.getMessage().contains("cannot be blank")
        );
    }

    @Test
    @DisplayName("Should fail validation when subdomain is too short")
    void validate_SubdomainTooShort_Fails() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("ab"); // Less than 3 characters
        tenant.setName("Test Company");

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("subdomain")
        );
    }

    @Test
    @DisplayName("Should pass validation when subdomain is exactly 3 characters")
    void validate_SubdomainMinLength_Success() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("abc");
        tenant.setName("Test Company");

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should pass validation when subdomain is exactly 63 characters")
    void validate_SubdomainMaxLength_Success() {
        // Given
        Tenant tenant = new Tenant();
        String subdomain63 = "a".repeat(63);
        tenant.setSubdomain(subdomain63);
        tenant.setName("Test Company");

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isEmpty();
        assertThat(tenant.getSubdomain()).hasSize(63);
    }

    @Test
    @DisplayName("Should fail validation when subdomain exceeds 63 characters")
    void validate_SubdomainTooLong_Fails() {
        // Given
        Tenant tenant = new Tenant();
        String subdomain64 = "a".repeat(64);
        tenant.setSubdomain(subdomain64);
        tenant.setName("Test Company");

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("subdomain")
        );
    }

    @Test
    @DisplayName("Should pass validation with valid lowercase alphanumeric subdomain")
    void validate_SubdomainValidPattern_Success() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("acme-corp-2024");
        tenant.setName("Test Company");

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when subdomain contains uppercase letters")
    void validate_SubdomainUppercase_Fails() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("AcmeCorp");
        tenant.setName("Test Company");

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("subdomain") &&
            v.getMessage().contains("lowercase alphanumeric")
        );
    }

    @Test
    @DisplayName("Should fail validation when subdomain contains invalid characters")
    void validate_SubdomainInvalidCharacters_Fails() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("acme_corp");
        tenant.setName("Test Company");

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("subdomain") &&
            v.getMessage().contains("lowercase alphanumeric")
        );
    }

    @Test
    @DisplayName("Should reject reserved subdomain 'www' in prePersist")
    void prePersist_ReservedSubdomainWWW_ThrowsException() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("www");
        tenant.setName("Test Company");

        // When/Then
        assertThatThrownBy(tenant::prePersist)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("reserved");
    }

    @Test
    @DisplayName("Should reject reserved subdomain 'api' in prePersist")
    void prePersist_ReservedSubdomainAPI_ThrowsException() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("api");
        tenant.setName("Test Company");

        // When/Then
        assertThatThrownBy(tenant::prePersist)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("reserved");
    }

    // ==================== Name Validation Tests ====================

    @Test
    @DisplayName("Should fail validation when name is blank")
    void validate_NameBlank_Fails() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("acme-corp");
        tenant.setName("");

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("name")
        );
    }

    @Test
    @DisplayName("Should pass validation with valid name")
    void validate_NameValid_Success() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("acme-corp");
        tenant.setName("Acme Corporation LLC");

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when name exceeds 255 characters")
    void validate_NameTooLong_Fails() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("acme-corp");
        tenant.setName("A".repeat(256));

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("name")
        );
    }

    // ==================== Description Validation Tests ====================

    @Test
    @DisplayName("Should allow null description")
    void validate_DescriptionNull_Success() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("acme-corp");
        tenant.setName("Acme Corporation");
        tenant.setDescription(null);

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should allow non-empty description")
    void validate_DescriptionProvided_Success() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("acme-corp");
        tenant.setName("Acme Corporation");
        tenant.setDescription("A leading provider of innovative solutions");

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isEmpty();
        assertThat(tenant.getDescription()).isEqualTo("A leading provider of innovative solutions");
    }

    // ==================== Active Status Tests ====================

    @Test
    @DisplayName("Should default to active status true")
    void create_Tenant_DefaultActiveTrue() {
        // Given
        Tenant tenant = new Tenant();

        // Then
        assertThat(tenant.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should allow setting active status to false")
    void setActive_False_Success() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("acme-corp");
        tenant.setName("Acme Corporation");
        tenant.setActive(false);

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isEmpty();
        assertThat(tenant.isActive()).isFalse();
    }

    // ==================== Subscription Tier Tests ====================

    @Test
    @DisplayName("Should default to FREE subscription tier")
    void create_Tenant_DefaultSubscriptionTierFree() {
        // Given
        Tenant tenant = new Tenant();

        // Then
        assertThat(tenant.getSubscriptionTier()).isEqualTo(SubscriptionTier.FREE);
    }

    @Test
    @DisplayName("Should allow setting subscription tier to PRO")
    void setSubscriptionTier_Pro_Success() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("acme-corp");
        tenant.setName("Acme Corporation");
        tenant.setSubscriptionTier(SubscriptionTier.PRO);

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isEmpty();
        assertThat(tenant.getSubscriptionTier()).isEqualTo(SubscriptionTier.PRO);
    }

    @Test
    @DisplayName("Should allow setting subscription tier to ENTERPRISE")
    void setSubscriptionTier_Enterprise_Success() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("acme-corp");
        tenant.setName("Acme Corporation");
        tenant.setSubscriptionTier(SubscriptionTier.ENTERPRISE);

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isEmpty();
        assertThat(tenant.getSubscriptionTier()).isEqualTo(SubscriptionTier.ENTERPRISE);
    }

    @Test
    @DisplayName("Should fail validation when subscription tier is null")
    void validate_SubscriptionTierNull_Fails() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("acme-corp");
        tenant.setName("Acme Corporation");
        tenant.setSubscriptionTier(null);

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("subscriptionTier")
        );
    }

    // ==================== Quota Management Tests ====================

    @Test
    @DisplayName("Should set quota to 50 for FREE tier in prePersist")
    void prePersist_FreeTier_SetsQuotaTo50() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("free-tenant");
        tenant.setName("Free Tenant");
        tenant.setSubscriptionTier(SubscriptionTier.FREE);

        // When
        tenant.prePersist();

        // Then
        assertThat(tenant.getQuotaLimit()).isEqualTo(50);
    }

    @Test
    @DisplayName("Should set quota to 1000 for PRO tier in prePersist")
    void prePersist_ProTier_SetsQuotaTo1000() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("pro-tenant");
        tenant.setName("Pro Tenant");
        tenant.setSubscriptionTier(SubscriptionTier.PRO);

        // When
        tenant.prePersist();

        // Then
        assertThat(tenant.getQuotaLimit()).isEqualTo(1000);
    }

    @Test
    @DisplayName("Should set quota to null (unlimited) for ENTERPRISE tier in prePersist")
    void prePersist_EnterpriseTier_SetsQuotaToNull() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("enterprise-tenant");
        tenant.setName("Enterprise Tenant");
        tenant.setSubscriptionTier(SubscriptionTier.ENTERPRISE);

        // When
        tenant.prePersist();

        // Then
        assertThat(tenant.getQuotaLimit()).isNull(); // Unlimited
    }

    @Test
    @DisplayName("Should not override custom quota if already set in prePersist")
    void prePersist_CustomQuota_NotOverridden() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("custom-tenant");
        tenant.setName("Custom Tenant");
        tenant.setSubscriptionTier(SubscriptionTier.PRO);
        tenant.setQuotaLimit(5000); // Custom quota

        // When
        tenant.prePersist();

        // Then
        assertThat(tenant.getQuotaLimit()).isEqualTo(5000); // Not overridden
    }

    @Test
    @DisplayName("Should allow custom quota limit")
    void setQuotaLimit_CustomValue_Success() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("acme-corp");
        tenant.setName("Acme Corporation");
        tenant.setQuotaLimit(2500);

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isEmpty();
        assertThat(tenant.getQuotaLimit()).isEqualTo(2500);
    }

    @Test
    @DisplayName("Should allow null quota limit (unlimited)")
    void setQuotaLimit_Null_Success() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("acme-corp");
        tenant.setName("Acme Corporation");
        tenant.setQuotaLimit(null);

        // When
        Set<ConstraintViolation<Tenant>> violations = validator.validate(tenant);

        // Then
        assertThat(violations).isEmpty();
        assertThat(tenant.getQuotaLimit()).isNull();
    }

    // ==================== Lifecycle Hook Tests ====================

    @Test
    @DisplayName("Should call prePersist successfully for valid non-reserved subdomain")
    void prePersist_ValidSubdomain_Success() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("acme-corp");
        tenant.setName("Acme Corporation");
        tenant.setSubscriptionTier(SubscriptionTier.FREE);

        // When/Then (no exception thrown)
        tenant.prePersist();

        // Then
        assertThat(tenant.getQuotaLimit()).isEqualTo(50);
    }

    @Test
    @DisplayName("Should reject all reserved subdomains in prePersist")
    void prePersist_AllReservedSubdomains_ThrowsException() {
        // Given
        String[] reservedSubdomains = {"www", "api", "admin", "app", "platform"};

        for (String reserved : reservedSubdomains) {
            Tenant tenant = new Tenant();
            tenant.setSubdomain(reserved);
            tenant.setName("Test Company");

            // When/Then
            assertThatThrownBy(tenant::prePersist)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reserved")
                .hasMessageContaining(reserved);
        }
    }

    @Test
    @DisplayName("Should reject reserved subdomains case-insensitively in prePersist")
    void prePersist_ReservedSubdomainCaseInsensitive_ThrowsException() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("API"); // Uppercase, but still reserved

        // When/Then
        assertThatThrownBy(tenant::prePersist)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("reserved");
    }

    @Test
    @DisplayName("Should call preUpdate successfully for valid non-reserved subdomain")
    void preUpdate_ValidSubdomain_Success() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("acme-corp");
        tenant.setName("Acme Corporation");

        // When/Then (no exception thrown)
        tenant.preUpdate();
    }

    @Test
    @DisplayName("Should reject reserved subdomain in preUpdate")
    void preUpdate_ReservedSubdomain_ThrowsException() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("admin");
        tenant.setName("Test Company");

        // When/Then
        assertThatThrownBy(tenant::preUpdate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("reserved");
    }

    // ==================== Business Method Tests ====================

    @Test
    @DisplayName("Should return false when quota is unlimited (null)")
    void isQuotaExceeded_UnlimitedQuota_ReturnsFalse() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("enterprise-tenant");
        tenant.setName("Enterprise Tenant");
        tenant.setSubscriptionTier(SubscriptionTier.ENTERPRISE);
        tenant.setQuotaLimit(null); // Unlimited

        // When
        boolean exceeded = tenant.isQuotaExceeded(1000000L);

        // Then
        assertThat(exceeded).isFalse();
    }

    @Test
    @DisplayName("Should return false when usage is below quota limit")
    void isQuotaExceeded_UsageBelowLimit_ReturnsFalse() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("pro-tenant");
        tenant.setName("Pro Tenant");
        tenant.setQuotaLimit(1000);

        // When
        boolean exceeded = tenant.isQuotaExceeded(500L);

        // Then
        assertThat(exceeded).isFalse();
    }

    @Test
    @DisplayName("Should return true when usage equals quota limit")
    void isQuotaExceeded_UsageEqualsLimit_ReturnsTrue() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("free-tenant");
        tenant.setName("Free Tenant");
        tenant.setQuotaLimit(50);

        // When
        boolean exceeded = tenant.isQuotaExceeded(50L);

        // Then
        assertThat(exceeded).isTrue();
    }

    @Test
    @DisplayName("Should return true when usage exceeds quota limit")
    void isQuotaExceeded_UsageExceedsLimit_ReturnsTrue() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("free-tenant");
        tenant.setName("Free Tenant");
        tenant.setQuotaLimit(50);

        // When
        boolean exceeded = tenant.isQuotaExceeded(75L);

        // Then
        assertThat(exceeded).isTrue();
    }

    // ==================== Equals/HashCode Tests ====================

    @Test
    @DisplayName("Should be equal when same tenant ID")
    void equals_SameTenantId_ReturnsTrue() {
        // Given
        Tenant tenant1 = new Tenant();
        tenant1.setId(java.util.UUID.randomUUID());
        tenant1.setSubdomain("acme-corp");
        tenant1.setName("Acme Corporation");

        Tenant tenant2 = new Tenant();
        tenant2.setId(tenant1.getId());
        tenant2.setSubdomain("different-subdomain");
        tenant2.setName("Different Name");

        // When/Then
        assertThat(tenant1).isEqualTo(tenant2);
        assertThat(tenant1.hashCode()).isEqualTo(tenant2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when different tenant IDs")
    void equals_DifferentTenantId_ReturnsFalse() {
        // Given
        Tenant tenant1 = new Tenant();
        tenant1.setId(java.util.UUID.randomUUID());
        tenant1.setSubdomain("acme-corp");
        tenant1.setName("Acme Corporation");

        Tenant tenant2 = new Tenant();
        tenant2.setId(java.util.UUID.randomUUID());
        tenant2.setSubdomain("acme-corp");
        tenant2.setName("Acme Corporation");

        // When/Then
        assertThat(tenant1).isNotEqualTo(tenant2);
    }

    @Test
    @DisplayName("Should be equal to itself")
    void equals_SameInstance_ReturnsTrue() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setId(java.util.UUID.randomUUID());
        tenant.setSubdomain("acme-corp");
        tenant.setName("Acme Corporation");

        // When/Then
        assertThat(tenant).isEqualTo(tenant);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void equals_Null_ReturnsFalse() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setId(java.util.UUID.randomUUID());
        tenant.setSubdomain("acme-corp");
        tenant.setName("Acme Corporation");

        // When/Then
        assertThat(tenant).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should not be equal to different class")
    void equals_DifferentClass_ReturnsFalse() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setId(java.util.UUID.randomUUID());
        tenant.setSubdomain("acme-corp");
        tenant.setName("Acme Corporation");

        String notATenant = "Not a tenant";

        // When/Then
        assertThat(tenant).isNotEqualTo(notATenant);
    }

    // ==================== ToString Tests ====================

    @Test
    @DisplayName("Should include subdomain and name in toString")
    void toString_ContainsSubdomainAndName() {
        // Given
        Tenant tenant = new Tenant();
        tenant.setSubdomain("acme-corp");
        tenant.setName("Acme Corporation");

        // When
        String result = tenant.toString();

        // Then
        assertThat(result).contains("acme-corp");
        assertThat(result).contains("Acme Corporation");
    }

    @Test
    @DisplayName("Should not throw exception when calling toString on empty tenant")
    void toString_EmptyTenant_NoException() {
        // Given
        Tenant tenant = new Tenant();

        // When/Then (no exception)
        String result = tenant.toString();
        assertThat(result).isNotNull();
    }

    // ==================== Entity Metadata Tests ====================

    @Test
    @DisplayName("Should be annotated with @Entity")
    void metadata_EntityAnnotation_Present() {
        // When/Then
        assertThat(Tenant.class.isAnnotationPresent(Entity.class)).isTrue();
    }

    @Test
    @DisplayName("Should be annotated with @Table")
    void metadata_TableAnnotation_Present() {
        // When/Then
        assertThat(Tenant.class.isAnnotationPresent(Table.class)).isTrue();
    }

    @Test
    @DisplayName("Should have table name 'tenants'")
    void metadata_TableName_IsTenants() {
        // When
        Table tableAnnotation = Tenant.class.getAnnotation(Table.class);

        // Then
        assertThat(tableAnnotation.name()).isEqualTo("tenants");
    }
}
