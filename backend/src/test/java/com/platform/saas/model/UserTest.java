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

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for User entity.
 * Sprint 2 - Model Layer Testing
 *
 * Test Coverage:
 * - Entity creation and initialization
 * - JSR-303 validation constraints
 * - Equals/HashCode contract
 * - ToString output
 * - Business methods (updateLastLogin)
 * - JPA entity metadata
 */
@DisplayName("User Entity Tests - Core Model Component")
class UserTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ============================================================================
    // Entity Creation Tests
    // ============================================================================

    @Test
    @DisplayName("Should create valid user with all fields")
    void createUser_WithAllFields_Success() {
        // Given / When
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setId(id);
        user.setCognitoUserId("cognito-abc-123");
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setCreatedAt(now);
        user.setLastLoginAt(now);

        // Then
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getCognitoUserId()).isEqualTo("cognito-abc-123");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getName()).isEqualTo("Test User");
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getLastLoginAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should create user with no-args constructor")
    void createUser_NoArgsConstructor_Success() {
        // When
        User user = new User();

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNull();
        assertThat(user.getCognitoUserId()).isNull();
        assertThat(user.getEmail()).isNull();
    }

    @Test
    @DisplayName("Should create user with all-args constructor")
    void createUser_AllArgsConstructor_Success() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // When
        User user = new User(id, "cognito-123", "test@example.com", "Test User", now, now);

        // Then
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getCognitoUserId()).isEqualTo("cognito-123");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Should create user with optional name as null")
    void createUser_WithoutName_Success() {
        // Given / When
        User user = new User();
        user.setCognitoUserId("cognito-123");
        user.setEmail("test@example.com");
        user.setName(null);

        // Then - Should be valid even without name
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }

    // ============================================================================
    // CognitoUserId Validation Tests
    // ============================================================================

    @Test
    @DisplayName("Should fail validation when cognitoUserId is null")
    void validate_CognitoUserIdNull_Fails() {
        // Given
        User user = new User();
        user.setCognitoUserId(null);
        user.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("cognitoUserId") &&
            v.getMessage().contains("cannot be blank")
        );
    }

    @Test
    @DisplayName("Should fail validation when cognitoUserId is blank")
    void validate_CognitoUserIdBlank_Fails() {
        // Given
        User user = new User();
        user.setCognitoUserId("   ");
        user.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("cognitoUserId")
        );
    }

    @Test
    @DisplayName("Should fail validation when cognitoUserId exceeds 255 characters")
    void validate_CognitoUserIdTooLong_Fails() {
        // Given
        String longId = "a".repeat(256);
        User user = new User();
        user.setCognitoUserId(longId);
        user.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("cognitoUserId") &&
            v.getMessage().contains("must not exceed 255 characters")
        );
    }

    @Test
    @DisplayName("Should pass validation with cognitoUserId at 255 characters")
    void validate_CognitoUserId255Characters_Success() {
        // Given
        String maxLengthId = "a".repeat(255);
        User user = new User();
        user.setCognitoUserId(maxLengthId);
        user.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();
    }

    // ============================================================================
    // Email Validation Tests
    // ============================================================================

    @Test
    @DisplayName("Should fail validation when email is null")
    void validate_EmailNull_Fails() {
        // Given
        User user = new User();
        user.setCognitoUserId("cognito-123");
        user.setEmail(null);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("email") &&
            v.getMessage().contains("cannot be blank")
        );
    }

    @Test
    @DisplayName("Should fail validation when email is blank")
    void validate_EmailBlank_Fails() {
        // Given
        User user = new User();
        user.setCognitoUserId("cognito-123");
        user.setEmail("   ");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("email")
        );
    }

    @Test
    @DisplayName("Should fail validation when email is invalid format")
    void validate_EmailInvalidFormat_Fails() {
        // Given
        User user = new User();
        user.setCognitoUserId("cognito-123");
        user.setEmail("not-an-email");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("email") &&
            v.getMessage().contains("must be valid")
        );
    }

    @Test
    @DisplayName("Should fail validation when email exceeds 255 characters")
    void validate_EmailTooLong_Fails() {
        // Given
        String longEmail = "a".repeat(240) + "@example.com"; // 252 chars
        User user = new User();
        user.setCognitoUserId("cognito-123");
        user.setEmail(longEmail);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then - Should fail on size, not format
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should pass validation with valid email formats")
    void validate_ValidEmailFormats_Success() {
        // Valid email formats to test
        String[] validEmails = {
            "test@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "user_123@test-domain.org",
            "a@b.c"
        };

        for (String validEmail : validEmails) {
            // Given
            User user = new User();
            user.setCognitoUserId("cognito-123");
            user.setEmail(validEmail);

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations)
                .as("Email %s should be valid", validEmail)
                .isEmpty();
        }
    }

    // ============================================================================
    // Name Validation Tests
    // ============================================================================

    @Test
    @DisplayName("Should pass validation when name is null")
    void validate_NameNull_Success() {
        // Given
        User user = new User();
        user.setCognitoUserId("cognito-123");
        user.setEmail("test@example.com");
        user.setName(null);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when name exceeds 255 characters")
    void validate_NameTooLong_Fails() {
        // Given
        String longName = "a".repeat(256);
        User user = new User();
        user.setCognitoUserId("cognito-123");
        user.setEmail("test@example.com");
        user.setName(longName);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("name") &&
            v.getMessage().contains("must not exceed 255 characters")
        );
    }

    @Test
    @DisplayName("Should pass validation with name at 255 characters")
    void validate_Name255Characters_Success() {
        // Given
        String maxLengthName = "a".repeat(255);
        User user = new User();
        user.setCognitoUserId("cognito-123");
        user.setEmail("test@example.com");
        user.setName(maxLengthName);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();
    }

    // ============================================================================
    // Equals and HashCode Tests
    // ============================================================================

    @Test
    @DisplayName("Should be equal when same id")
    void equals_SameId_ReturnsTrue() {
        // Given
        UUID id = UUID.randomUUID();
        User user1 = new User();
        user1.setId(id);
        user1.setEmail("test1@example.com");

        User user2 = new User();
        user2.setId(id);
        user2.setEmail("test2@example.com");

        // When / Then
        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when different id")
    void equals_DifferentId_ReturnsFalse() {
        // Given
        User user1 = new User();
        user1.setId(UUID.randomUUID());

        User user2 = new User();
        user2.setId(UUID.randomUUID());

        // When / Then
        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    @DisplayName("Should be equal to itself")
    void equals_SameInstance_ReturnsTrue() {
        // Given
        User user = new User();
        user.setId(UUID.randomUUID());

        // When / Then
        assertThat(user).isEqualTo(user);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void equals_Null_ReturnsFalse() {
        // Given
        User user = new User();
        user.setId(UUID.randomUUID());

        // When / Then
        assertThat(user).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should not be equal to different class")
    void equals_DifferentClass_ReturnsFalse() {
        // Given
        User user = new User();
        user.setId(UUID.randomUUID());
        String notAUser = "not a user";

        // When / Then
        assertThat(user).isNotEqualTo(notAUser);
    }

    @Test
    @DisplayName("Should handle null id in equals")
    void equals_NullId_HandlesGracefully() {
        // Given
        User user1 = new User();
        user1.setId(null);

        User user2 = new User();
        user2.setId(null);

        // When / Then
        assertThat(user1).isEqualTo(user2);
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    void hashCode_ConsistentWithEquals() {
        // Given
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);

        // When
        int hashCode1 = user.hashCode();
        int hashCode2 = user.hashCode();

        // Then
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    // ============================================================================
    // ToString Tests
    // ============================================================================

    @Test
    @DisplayName("Should generate toString with all fields")
    void toString_WithAllFields_ContainsAllInfo() {
        // Given
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setCognitoUserId("cognito-abc-123");
        user.setEmail("test@example.com");
        user.setName("Test User");

        // When
        String toString = user.toString();

        // Then
        assertThat(toString).contains("User{");
        assertThat(toString).contains("id=" + id);
        assertThat(toString).contains("cognitoUserId='cognito-abc-123'");
        assertThat(toString).contains("email='test@example.com'");
        assertThat(toString).contains("name='Test User'");
    }

    @Test
    @DisplayName("Should generate toString with null values")
    void toString_WithNullValues_HandlesGracefully() {
        // Given
        User user = new User();

        // When
        String toString = user.toString();

        // Then
        assertThat(toString).contains("User{");
        assertThat(toString).contains("id=null");
        assertThat(toString).contains("cognitoUserId='null'");
    }

    // ============================================================================
    // Business Method Tests
    // ============================================================================

    @Test
    @DisplayName("Should update last login timestamp")
    void updateLastLogin_SetsTimestamp() {
        // Given
        User user = new User();
        user.setCognitoUserId("cognito-123");
        user.setEmail("test@example.com");
        assertThat(user.getLastLoginAt()).isNull();

        LocalDateTime beforeUpdate = LocalDateTime.now();

        // When
        user.updateLastLogin();

        // Then
        LocalDateTime afterUpdate = LocalDateTime.now();
        assertThat(user.getLastLoginAt()).isNotNull();
        assertThat(user.getLastLoginAt()).isAfterOrEqualTo(beforeUpdate);
        assertThat(user.getLastLoginAt()).isBeforeOrEqualTo(afterUpdate);
    }

    @Test
    @DisplayName("Should update last login timestamp on multiple calls")
    void updateLastLogin_MultipleCalls_UpdatesEachTime() throws InterruptedException {
        // Given
        User user = new User();
        user.setCognitoUserId("cognito-123");
        user.setEmail("test@example.com");

        // When
        user.updateLastLogin();
        LocalDateTime firstLogin = user.getLastLoginAt();

        Thread.sleep(10); // Small delay to ensure different timestamp

        user.updateLastLogin();
        LocalDateTime secondLogin = user.getLastLoginAt();

        // Then
        assertThat(secondLogin).isAfter(firstLogin);
    }

    // ============================================================================
    // Combined Validation Tests
    // ============================================================================

    @Test
    @DisplayName("Should pass validation with all valid required fields")
    void validate_AllValidFields_Success() {
        // Given
        User user = new User();
        user.setCognitoUserId("cognito-abc-123");
        user.setEmail("test@example.com");
        user.setName("Test User");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should collect multiple validation errors")
    void validate_MultipleErrors_CollectsAll() {
        // Given - User with multiple invalid fields
        User user = new User();
        user.setCognitoUserId(null); // Invalid: null
        user.setEmail("not-an-email"); // Invalid: format
        user.setName("a".repeat(256)); // Invalid: too long

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(3);
    }

    // ============================================================================
    // Entity Metadata Tests
    // ============================================================================

    @Test
    @DisplayName("Should have correct entity annotation")
    void entityMetadata_HasEntityAnnotation() {
        // When
        boolean isEntity = User.class.isAnnotationPresent(Entity.class);

        // Then
        assertThat(isEntity).isTrue();
    }

    @Test
    @DisplayName("Should have correct table annotation")
    void entityMetadata_HasTableAnnotation() {
        // When
        Table table = User.class.getAnnotation(Table.class);

        // Then
        assertThat(table).isNotNull();
        assertThat(table.name()).isEqualTo("users");
    }

    @Test
    @DisplayName("Should have unique indexes on cognitoUserId and email")
    void entityMetadata_HasUniqueIndexes() {
        // When
        Table table = User.class.getAnnotation(Table.class);

        // Then
        assertThat(table.indexes()).hasSize(2);
        assertThat(table.indexes())
            .anyMatch(idx -> idx.name().equals("idx_user_cognito_id") && idx.unique())
            .anyMatch(idx -> idx.name().equals("idx_user_email") && idx.unique());
    }
}
