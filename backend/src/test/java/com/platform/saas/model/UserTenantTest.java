package com.platform.saas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive test suite for UserTenant join entity and UserTenantId composite key.
 *
 * Sprint 2 - Model Layer Testing
 * Target: Validate all constraints, business logic, and entity behavior
 *
 * Test Categories:
 * 1. Entity Creation (4 tests)
 * 2. Composite Key Tests (8 tests)
 * 3. Role Validation (4 tests)
 * 4. Business Methods (9 tests)
 * 5. InvitedBy Field (2 tests)
 * 6. Equals/HashCode (6 tests)
 * 7. ToString (2 tests)
 * 8. Entity Metadata (3 tests)
 * 9. UserTenantId Tests (6 tests)
 */
@DisplayName("UserTenant Entity Tests - Multi-Tenant User Association")
class UserTenantTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==================== Entity Creation Tests ====================

    @Test
    @DisplayName("Should create valid UserTenant with all required fields")
    void create_ValidUserTenant_Success() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(UserRole.EDITOR);

        // When
        Set<ConstraintViolation<UserTenant>> violations = validator.validate(userTenant);

        // Then
        assertThat(violations).isEmpty();
        assertThat(userTenant.getUserId()).isNotNull();
        assertThat(userTenant.getTenantId()).isNotNull();
        assertThat(userTenant.getRole()).isEqualTo(UserRole.EDITOR);
    }

    @Test
    @DisplayName("Should create UserTenant with all fields including optional invitedBy")
    void create_UserTenantWithInvitedBy_Success() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID invitedByUserId = UUID.randomUUID();

        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(userId);
        userTenant.setTenantId(tenantId);
        userTenant.setRole(UserRole.ADMINISTRATOR);
        userTenant.setInvitedBy(invitedByUserId);

        // When
        Set<ConstraintViolation<UserTenant>> violations = validator.validate(userTenant);

        // Then
        assertThat(violations).isEmpty();
        assertThat(userTenant.getInvitedBy()).isEqualTo(invitedByUserId);
    }

    @Test
    @DisplayName("Should create UserTenant using AllArgsConstructor")
    void create_UserTenantWithConstructor_Success() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID invitedBy = UUID.randomUUID();

        // When
        UserTenant userTenant = new UserTenant(
            userId,
            tenantId,
            null, // user (lazy loaded)
            null, // tenant (lazy loaded)
            UserRole.VIEWER,
            invitedBy,
            null  // joinedAt (set by @CreatedDate)
        );

        // Then
        assertThat(userTenant.getUserId()).isEqualTo(userId);
        assertThat(userTenant.getTenantId()).isEqualTo(tenantId);
        assertThat(userTenant.getRole()).isEqualTo(UserRole.VIEWER);
        assertThat(userTenant.getInvitedBy()).isEqualTo(invitedBy);
    }

    @Test
    @DisplayName("Should fail validation when required fields are missing")
    void validate_MissingRequiredFields_Fails() {
        // Given
        UserTenant userTenant = new UserTenant();
        // No userId, tenantId, or role set

        // When
        Set<ConstraintViolation<UserTenant>> violations = validator.validate(userTenant);

        // Then
        assertThat(violations).isNotEmpty();
        // Should have at least 1 violation for role (userId and tenantId are part of composite key)
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("role")
        );
    }

    // ==================== Composite Key Tests ====================

    @Test
    @DisplayName("Should support composite primary key with userId and tenantId")
    void compositeKey_UserIdAndTenantId_Valid() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(userId);
        userTenant.setTenantId(tenantId);
        userTenant.setRole(UserRole.EDITOR);

        // When
        Set<ConstraintViolation<UserTenant>> violations = validator.validate(userTenant);

        // Then
        assertThat(violations).isEmpty();
        assertThat(userTenant.getUserId()).isEqualTo(userId);
        assertThat(userTenant.getTenantId()).isEqualTo(tenantId);
    }

    @Test
    @DisplayName("Should allow same user in different tenants")
    void compositeKey_SameUserDifferentTenants_Unique() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID tenant1Id = UUID.randomUUID();
        UUID tenant2Id = UUID.randomUUID();

        UserTenant userTenant1 = new UserTenant();
        userTenant1.setUserId(userId);
        userTenant1.setTenantId(tenant1Id);
        userTenant1.setRole(UserRole.EDITOR);

        UserTenant userTenant2 = new UserTenant();
        userTenant2.setUserId(userId);
        userTenant2.setTenantId(tenant2Id);
        userTenant2.setRole(UserRole.VIEWER);

        // When
        Set<ConstraintViolation<UserTenant>> violations1 = validator.validate(userTenant1);
        Set<ConstraintViolation<UserTenant>> violations2 = validator.validate(userTenant2);

        // Then
        assertThat(violations1).isEmpty();
        assertThat(violations2).isEmpty();
        assertThat(userTenant1).isNotEqualTo(userTenant2);
    }

    @Test
    @DisplayName("Should allow same tenant with different users")
    void compositeKey_SameTenantDifferentUsers_Unique() {
        // Given
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        UserTenant userTenant1 = new UserTenant();
        userTenant1.setUserId(user1Id);
        userTenant1.setTenantId(tenantId);
        userTenant1.setRole(UserRole.ADMINISTRATOR);

        UserTenant userTenant2 = new UserTenant();
        userTenant2.setUserId(user2Id);
        userTenant2.setTenantId(tenantId);
        userTenant2.setRole(UserRole.EDITOR);

        // When
        Set<ConstraintViolation<UserTenant>> violations1 = validator.validate(userTenant1);
        Set<ConstraintViolation<UserTenant>> violations2 = validator.validate(userTenant2);

        // Then
        assertThat(violations1).isEmpty();
        assertThat(violations2).isEmpty();
        assertThat(userTenant1).isNotEqualTo(userTenant2);
    }

    @Test
    @DisplayName("Should have IdClass annotation for composite key")
    void metadata_IdClassAnnotation_Present() {
        // When/Then
        assertThat(UserTenant.class.isAnnotationPresent(IdClass.class)).isTrue();
        IdClass idClass = UserTenant.class.getAnnotation(IdClass.class);
        assertThat(idClass.value()).isEqualTo(UserTenantId.class);
    }

    @Test
    @DisplayName("Should allow null userId initially")
    void compositeKey_NullUserId_Allowed() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(UserRole.EDITOR);

        // When/Then (no constraint violation on userId itself, database will enforce)
        assertThat(userTenant.getUserId()).isNull();
    }

    @Test
    @DisplayName("Should allow null tenantId initially")
    void compositeKey_NullTenantId_Allowed() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setRole(UserRole.EDITOR);

        // When/Then (no constraint violation on tenantId itself, database will enforce)
        assertThat(userTenant.getTenantId()).isNull();
    }

    @Test
    @DisplayName("Should support different roles for same user in different tenants")
    void compositeKey_SameUserDifferentRoles_Supported() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID tenant1Id = UUID.randomUUID();
        UUID tenant2Id = UUID.randomUUID();

        UserTenant adminInTenant1 = new UserTenant();
        adminInTenant1.setUserId(userId);
        adminInTenant1.setTenantId(tenant1Id);
        adminInTenant1.setRole(UserRole.ADMINISTRATOR);

        UserTenant viewerInTenant2 = new UserTenant();
        viewerInTenant2.setUserId(userId);
        viewerInTenant2.setTenantId(tenant2Id);
        viewerInTenant2.setRole(UserRole.VIEWER);

        // When
        Set<ConstraintViolation<UserTenant>> violations1 = validator.validate(adminInTenant1);
        Set<ConstraintViolation<UserTenant>> violations2 = validator.validate(viewerInTenant2);

        // Then
        assertThat(violations1).isEmpty();
        assertThat(violations2).isEmpty();
        assertThat(adminInTenant1.getRole()).isEqualTo(UserRole.ADMINISTRATOR);
        assertThat(viewerInTenant2.getRole()).isEqualTo(UserRole.VIEWER);
    }

    @Test
    @DisplayName("Should not be equal when same user but different tenant")
    void equals_SameUserDifferentTenant_NotEqual() {
        // Given
        UUID userId = UUID.randomUUID();

        UserTenant userTenant1 = new UserTenant();
        userTenant1.setUserId(userId);
        userTenant1.setTenantId(UUID.randomUUID());
        userTenant1.setRole(UserRole.EDITOR);

        UserTenant userTenant2 = new UserTenant();
        userTenant2.setUserId(userId);
        userTenant2.setTenantId(UUID.randomUUID());
        userTenant2.setRole(UserRole.EDITOR);

        // When/Then
        assertThat(userTenant1).isNotEqualTo(userTenant2);
    }

    // ==================== Role Validation Tests ====================

    @Test
    @DisplayName("Should fail validation when role is null")
    void validate_RoleNull_Fails() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(null);

        // When
        Set<ConstraintViolation<UserTenant>> violations = validator.validate(userTenant);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("role") &&
            v.getMessage().contains("cannot be null")
        );
    }

    @Test
    @DisplayName("Should pass validation with ADMINISTRATOR role")
    void validate_RoleAdministrator_Success() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(UserRole.ADMINISTRATOR);

        // When
        Set<ConstraintViolation<UserTenant>> violations = validator.validate(userTenant);

        // Then
        assertThat(violations).isEmpty();
        assertThat(userTenant.getRole()).isEqualTo(UserRole.ADMINISTRATOR);
    }

    @Test
    @DisplayName("Should pass validation with EDITOR role")
    void validate_RoleEditor_Success() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(UserRole.EDITOR);

        // When
        Set<ConstraintViolation<UserTenant>> violations = validator.validate(userTenant);

        // Then
        assertThat(violations).isEmpty();
        assertThat(userTenant.getRole()).isEqualTo(UserRole.EDITOR);
    }

    @Test
    @DisplayName("Should pass validation with VIEWER role")
    void validate_RoleViewer_Success() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(UserRole.VIEWER);

        // When
        Set<ConstraintViolation<UserTenant>> violations = validator.validate(userTenant);

        // Then
        assertThat(violations).isEmpty();
        assertThat(userTenant.getRole()).isEqualTo(UserRole.VIEWER);
    }

    // ==================== Business Method Tests ====================

    @Test
    @DisplayName("Should return true when role is ADMINISTRATOR for isAdministrator()")
    void isAdministrator_AdministratorRole_ReturnsTrue() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(UserRole.ADMINISTRATOR);

        // When
        boolean result = userTenant.isAdministrator();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when role is EDITOR for isAdministrator()")
    void isAdministrator_EditorRole_ReturnsFalse() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(UserRole.EDITOR);

        // When
        boolean result = userTenant.isAdministrator();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when role is VIEWER for isAdministrator()")
    void isAdministrator_ViewerRole_ReturnsFalse() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(UserRole.VIEWER);

        // When
        boolean result = userTenant.isAdministrator();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return true when role is ADMINISTRATOR for canEdit()")
    void canEdit_AdministratorRole_ReturnsTrue() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(UserRole.ADMINISTRATOR);

        // When
        boolean result = userTenant.canEdit();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return true when role is EDITOR for canEdit()")
    void canEdit_EditorRole_ReturnsTrue() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(UserRole.EDITOR);

        // When
        boolean result = userTenant.canEdit();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when role is VIEWER for canEdit()")
    void canEdit_ViewerRole_ReturnsFalse() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(UserRole.VIEWER);

        // When
        boolean result = userTenant.canEdit();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when role is ADMINISTRATOR for isViewerOnly()")
    void isViewerOnly_AdministratorRole_ReturnsFalse() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(UserRole.ADMINISTRATOR);

        // When
        boolean result = userTenant.isViewerOnly();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when role is EDITOR for isViewerOnly()")
    void isViewerOnly_EditorRole_ReturnsFalse() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(UserRole.EDITOR);

        // When
        boolean result = userTenant.isViewerOnly();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return true when role is VIEWER for isViewerOnly()")
    void isViewerOnly_ViewerRole_ReturnsTrue() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(UserRole.VIEWER);

        // When
        boolean result = userTenant.isViewerOnly();

        // Then
        assertThat(result).isTrue();
    }

    // ==================== InvitedBy Field Tests ====================

    @Test
    @DisplayName("Should allow null invitedBy")
    void validate_InvitedByNull_Success() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(UserRole.EDITOR);
        userTenant.setInvitedBy(null);

        // When
        Set<ConstraintViolation<UserTenant>> violations = validator.validate(userTenant);

        // Then
        assertThat(violations).isEmpty();
        assertThat(userTenant.getInvitedBy()).isNull();
    }

    @Test
    @DisplayName("Should allow valid invitedBy UUID")
    void validate_InvitedByValid_Success() {
        // Given
        UUID invitedByUserId = UUID.randomUUID();

        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(UserRole.EDITOR);
        userTenant.setInvitedBy(invitedByUserId);

        // When
        Set<ConstraintViolation<UserTenant>> violations = validator.validate(userTenant);

        // Then
        assertThat(violations).isEmpty();
        assertThat(userTenant.getInvitedBy()).isEqualTo(invitedByUserId);
    }

    // ==================== Equals/HashCode Tests ====================

    @Test
    @DisplayName("Should be equal when all fields are same")
    void equals_AllFieldsSame_ReturnsTrue() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID invitedBy = UUID.randomUUID();

        UserTenant userTenant1 = new UserTenant();
        userTenant1.setUserId(userId);
        userTenant1.setTenantId(tenantId);
        userTenant1.setRole(UserRole.EDITOR);
        userTenant1.setInvitedBy(invitedBy);

        UserTenant userTenant2 = new UserTenant();
        userTenant2.setUserId(userId);
        userTenant2.setTenantId(tenantId);
        userTenant2.setRole(UserRole.EDITOR);
        userTenant2.setInvitedBy(invitedBy);

        // When/Then
        assertThat(userTenant1).isEqualTo(userTenant2);
        assertThat(userTenant1.hashCode()).isEqualTo(userTenant2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when different role")
    void equals_DifferentRole_ReturnsFalse() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        UserTenant userTenant1 = new UserTenant();
        userTenant1.setUserId(userId);
        userTenant1.setTenantId(tenantId);
        userTenant1.setRole(UserRole.EDITOR);

        UserTenant userTenant2 = new UserTenant();
        userTenant2.setUserId(userId);
        userTenant2.setTenantId(tenantId);
        userTenant2.setRole(UserRole.ADMINISTRATOR);

        // When/Then
        assertThat(userTenant1).isNotEqualTo(userTenant2);
    }

    @Test
    @DisplayName("Should not be equal when different userId")
    void equals_DifferentUserId_ReturnsFalse() {
        // Given
        UUID tenantId = UUID.randomUUID();

        UserTenant userTenant1 = new UserTenant();
        userTenant1.setUserId(UUID.randomUUID());
        userTenant1.setTenantId(tenantId);
        userTenant1.setRole(UserRole.EDITOR);

        UserTenant userTenant2 = new UserTenant();
        userTenant2.setUserId(UUID.randomUUID());
        userTenant2.setTenantId(tenantId);
        userTenant2.setRole(UserRole.EDITOR);

        // When/Then
        assertThat(userTenant1).isNotEqualTo(userTenant2);
    }

    @Test
    @DisplayName("Should not be equal when different tenantId")
    void equals_DifferentTenantId_ReturnsFalse() {
        // Given
        UUID userId = UUID.randomUUID();

        UserTenant userTenant1 = new UserTenant();
        userTenant1.setUserId(userId);
        userTenant1.setTenantId(UUID.randomUUID());
        userTenant1.setRole(UserRole.EDITOR);

        UserTenant userTenant2 = new UserTenant();
        userTenant2.setUserId(userId);
        userTenant2.setTenantId(UUID.randomUUID());
        userTenant2.setRole(UserRole.EDITOR);

        // When/Then
        assertThat(userTenant1).isNotEqualTo(userTenant2);
    }

    @Test
    @DisplayName("Should be equal to itself")
    void equals_SameInstance_ReturnsTrue() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(UserRole.EDITOR);

        // When/Then
        assertThat(userTenant).isEqualTo(userTenant);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void equals_Null_ReturnsFalse() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(UserRole.EDITOR);

        // When/Then
        assertThat(userTenant).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should not be equal to different class")
    void equals_DifferentClass_ReturnsFalse() {
        // Given
        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(UUID.randomUUID());
        userTenant.setTenantId(UUID.randomUUID());
        userTenant.setRole(UserRole.EDITOR);

        String notAUserTenant = "Not a user tenant";

        // When/Then
        assertThat(userTenant).isNotEqualTo(notAUserTenant);
    }

    // ==================== ToString Tests ====================

    @Test
    @DisplayName("Should include userId, tenantId, and role in toString")
    void toString_ContainsKeyFields() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        UserTenant userTenant = new UserTenant();
        userTenant.setUserId(userId);
        userTenant.setTenantId(tenantId);
        userTenant.setRole(UserRole.ADMINISTRATOR);

        // When
        String result = userTenant.toString();

        // Then
        assertThat(result).contains(userId.toString());
        assertThat(result).contains(tenantId.toString());
        assertThat(result).contains("ADMINISTRATOR");
    }

    @Test
    @DisplayName("Should not throw exception when calling toString on empty UserTenant")
    void toString_EmptyUserTenant_NoException() {
        // Given
        UserTenant userTenant = new UserTenant();

        // When/Then (no exception)
        String result = userTenant.toString();
        assertThat(result).isNotNull();
    }

    // ==================== Entity Metadata Tests ====================

    @Test
    @DisplayName("Should be annotated with @Entity")
    void metadata_EntityAnnotation_Present() {
        // When/Then
        assertThat(UserTenant.class.isAnnotationPresent(Entity.class)).isTrue();
    }

    @Test
    @DisplayName("Should be annotated with @Table")
    void metadata_TableAnnotation_Present() {
        // When/Then
        assertThat(UserTenant.class.isAnnotationPresent(Table.class)).isTrue();
    }

    @Test
    @DisplayName("Should have table name 'user_tenants'")
    void metadata_TableName_IsUserTenants() {
        // When
        Table tableAnnotation = UserTenant.class.getAnnotation(Table.class);

        // Then
        assertThat(tableAnnotation.name()).isEqualTo("user_tenants");
    }

    // ==================== UserTenantId Composite Key Tests ====================

    @Test
    @DisplayName("UserTenantId: Should be equal when same userId and tenantId")
    void userTenantId_Equals_SameIds_ReturnsTrue() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        UserTenantId id1 = new UserTenantId(userId, tenantId);
        UserTenantId id2 = new UserTenantId(userId, tenantId);

        // When/Then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    @DisplayName("UserTenantId: Should not be equal when different userId")
    void userTenantId_Equals_DifferentUserId_ReturnsFalse() {
        // Given
        UUID tenantId = UUID.randomUUID();

        UserTenantId id1 = new UserTenantId(UUID.randomUUID(), tenantId);
        UserTenantId id2 = new UserTenantId(UUID.randomUUID(), tenantId);

        // When/Then
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    @DisplayName("UserTenantId: Should not be equal when different tenantId")
    void userTenantId_Equals_DifferentTenantId_ReturnsFalse() {
        // Given
        UUID userId = UUID.randomUUID();

        UserTenantId id1 = new UserTenantId(userId, UUID.randomUUID());
        UserTenantId id2 = new UserTenantId(userId, UUID.randomUUID());

        // When/Then
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    @DisplayName("UserTenantId: Should support NoArgsConstructor")
    void userTenantId_NoArgsConstructor_Success() {
        // Given/When
        UserTenantId id = new UserTenantId();
        id.setUserId(UUID.randomUUID());
        id.setTenantId(UUID.randomUUID());

        // Then
        assertThat(id.getUserId()).isNotNull();
        assertThat(id.getTenantId()).isNotNull();
    }

    @Test
    @DisplayName("UserTenantId: Should support AllArgsConstructor")
    void userTenantId_AllArgsConstructor_Success() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        // When
        UserTenantId id = new UserTenantId(userId, tenantId);

        // Then
        assertThat(id.getUserId()).isEqualTo(userId);
        assertThat(id.getTenantId()).isEqualTo(tenantId);
    }

    @Test
    @DisplayName("UserTenantId: Should implement Serializable")
    void userTenantId_ImplementsSerializable() {
        // When/Then
        assertThat(java.io.Serializable.class.isAssignableFrom(UserTenantId.class)).isTrue();
    }
}
