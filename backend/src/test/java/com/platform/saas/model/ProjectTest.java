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

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive test suite for Project entity.
 *
 * Sprint 2 - Model Layer Testing
 * Target: Validate all constraints, business logic, and entity behavior
 *
 * Test Categories:
 * 1. Entity Creation (4 tests)
 * 2. TenantId Validation (2 tests)
 * 3. Name Validation (4 tests)
 * 4. Description Validation (2 tests)
 * 5. Status Validation (6 tests)
 * 6. DueDate Validation (3 tests)
 * 7. OwnerId Validation (2 tests)
 * 8. ProgressPercentage Validation (5 tests)
 * 9. Priority Validation (5 tests)
 * 10. Business Methods - isOverdue (5 tests)
 * 11. Business Methods - isActive (5 tests)
 * 12. Business Methods - canBeEdited (5 tests)
 * 13. Equals/HashCode (5 tests)
 * 14. ToString (2 tests)
 * 15. Entity Metadata (3 tests)
 */
@DisplayName("Project Entity Tests - Project Management Component")
class ProjectTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==================== Entity Creation Tests ====================

    @Test
    @DisplayName("Should create valid project with all required fields")
    void create_ValidProject_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Customer Portal Redesign");
        project.setOwnerId(UUID.randomUUID());

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.PLANNING); // Default
        assertThat(project.getPriority()).isEqualTo(Priority.MEDIUM); // Default
        assertThat(project.getProgressPercentage()).isEqualTo(0); // Default
    }

    @Test
    @DisplayName("Should set default values on creation")
    void create_Project_DefaultValues() {
        // Given
        Project project = new Project();

        // Then
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.PLANNING);
        assertThat(project.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(project.getProgressPercentage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should create project with all optional fields")
    void create_ProjectWithOptionalFields_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Customer Portal Redesign");
        project.setDescription("Redesign the customer-facing portal with modern UX");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setDueDate(LocalDate.now().plusDays(30));
        project.setOwnerId(UUID.randomUUID());
        project.setProgressPercentage(45);
        project.setPriority(Priority.HIGH);

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
        assertThat(project.getDescription()).isEqualTo("Redesign the customer-facing portal with modern UX");
        assertThat(project.getProgressPercentage()).isEqualTo(45);
    }

    @Test
    @DisplayName("Should fail validation when required fields are missing")
    void validate_MissingRequiredFields_Fails() {
        // Given
        Project project = new Project();
        // No tenantId, name, or ownerId set

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSizeGreaterThanOrEqualTo(3);
    }

    // ==================== TenantId Validation Tests ====================

    @Test
    @DisplayName("Should fail validation when tenantId is null")
    void validate_TenantIdNull_Fails() {
        // Given
        Project project = new Project();
        project.setTenantId(null);
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("tenantId") &&
            v.getMessage().contains("cannot be null")
        );
    }

    @Test
    @DisplayName("Should pass validation with valid tenantId")
    void validate_TenantIdValid_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
    }

    // ==================== Name Validation Tests ====================

    @Test
    @DisplayName("Should fail validation when name is blank")
    void validate_NameBlank_Fails() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("");
        project.setOwnerId(UUID.randomUUID());

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("name") &&
            v.getMessage().contains("cannot be blank")
        );
    }

    @Test
    @DisplayName("Should pass validation with valid name")
    void validate_NameValid_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Customer Portal Redesign");
        project.setOwnerId(UUID.randomUUID());

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should pass validation when name is exactly 255 characters")
    void validate_NameMaxLength_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("A".repeat(255));
        project.setOwnerId(UUID.randomUUID());

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
        assertThat(project.getName()).hasSize(255);
    }

    @Test
    @DisplayName("Should fail validation when name exceeds 255 characters")
    void validate_NameTooLong_Fails() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("A".repeat(256));
        project.setOwnerId(UUID.randomUUID());

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

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
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setDescription(null);
        project.setOwnerId(UUID.randomUUID());

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should allow non-empty description")
    void validate_DescriptionProvided_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setDescription("A comprehensive project description with detailed information");
        project.setOwnerId(UUID.randomUUID());

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
        assertThat(project.getDescription()).isEqualTo("A comprehensive project description with detailed information");
    }

    // ==================== Status Validation Tests ====================

    @Test
    @DisplayName("Should fail validation when status is null")
    void validate_StatusNull_Fails() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setStatus(null);

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("status") &&
            v.getMessage().contains("cannot be null")
        );
    }

    @Test
    @DisplayName("Should pass validation with PLANNING status")
    void validate_StatusPlanning_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setStatus(ProjectStatus.PLANNING);

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.PLANNING);
    }

    @Test
    @DisplayName("Should pass validation with ACTIVE status")
    void validate_StatusActive_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setStatus(ProjectStatus.ACTIVE);

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should pass validation with ON_HOLD status")
    void validate_StatusOnHold_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setStatus(ProjectStatus.ON_HOLD);

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.ON_HOLD);
    }

    @Test
    @DisplayName("Should pass validation with COMPLETED status")
    void validate_StatusCompleted_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setStatus(ProjectStatus.COMPLETED);

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should pass validation with ARCHIVED status")
    void validate_StatusArchived_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setStatus(ProjectStatus.ARCHIVED);

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.ARCHIVED);
    }

    // ==================== DueDate Validation Tests ====================

    @Test
    @DisplayName("Should allow null dueDate")
    void validate_DueDateNull_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setDueDate(null);

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should allow future dueDate")
    void validate_DueDateFuture_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setDueDate(LocalDate.now().plusDays(30));

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should allow past dueDate")
    void validate_DueDatePast_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setDueDate(LocalDate.now().minusDays(10));

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
    }

    // ==================== OwnerId Validation Tests ====================

    @Test
    @DisplayName("Should fail validation when ownerId is null")
    void validate_OwnerIdNull_Fails() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(null);

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("ownerId") &&
            v.getMessage().contains("cannot be null")
        );
    }

    @Test
    @DisplayName("Should pass validation with valid ownerId")
    void validate_OwnerIdValid_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
    }

    // ==================== ProgressPercentage Validation Tests ====================

    @Test
    @DisplayName("Should fail validation when progressPercentage is negative")
    void validate_ProgressPercentageNegative_Fails() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setProgressPercentage(-1);

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("progressPercentage") &&
            v.getMessage().contains("cannot be negative")
        );
    }

    @Test
    @DisplayName("Should pass validation when progressPercentage is 0")
    void validate_ProgressPercentageZero_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setProgressPercentage(0);

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should pass validation when progressPercentage is 50")
    void validate_ProgressPercentageMid_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setProgressPercentage(50);

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should pass validation when progressPercentage is 100")
    void validate_ProgressPercentage100_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setProgressPercentage(100);

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when progressPercentage exceeds 100")
    void validate_ProgressPercentageExceeds100_Fails() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setProgressPercentage(101);

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("progressPercentage") &&
            v.getMessage().contains("cannot exceed 100")
        );
    }

    // ==================== Priority Validation Tests ====================

    @Test
    @DisplayName("Should fail validation when priority is null")
    void validate_PriorityNull_Fails() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setPriority(null);

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("priority") &&
            v.getMessage().contains("cannot be null")
        );
    }

    @Test
    @DisplayName("Should pass validation with LOW priority")
    void validate_PriorityLow_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setPriority(Priority.LOW);

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
        assertThat(project.getPriority()).isEqualTo(Priority.LOW);
    }

    @Test
    @DisplayName("Should pass validation with MEDIUM priority")
    void validate_PriorityMedium_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setPriority(Priority.MEDIUM);

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
        assertThat(project.getPriority()).isEqualTo(Priority.MEDIUM);
    }

    @Test
    @DisplayName("Should pass validation with HIGH priority")
    void validate_PriorityHigh_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setPriority(Priority.HIGH);

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
        assertThat(project.getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    @DisplayName("Should pass validation with CRITICAL priority")
    void validate_PriorityCritical_Success() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setPriority(Priority.CRITICAL);

        // When
        Set<ConstraintViolation<Project>> violations = validator.validate(project);

        // Then
        assertThat(violations).isEmpty();
        assertThat(project.getPriority()).isEqualTo(Priority.CRITICAL);
    }

    // ==================== Business Method Tests - isOverdue ====================

    @Test
    @DisplayName("Should return false when dueDate is null for isOverdue()")
    void isOverdue_NoDueDate_ReturnsFalse() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setDueDate(null);
        project.setStatus(ProjectStatus.ACTIVE);

        // When
        boolean overdue = project.isOverdue();

        // Then
        assertThat(overdue).isFalse();
    }

    @Test
    @DisplayName("Should return false when dueDate is in the future for isOverdue()")
    void isOverdue_FutureDueDate_ReturnsFalse() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setDueDate(LocalDate.now().plusDays(10));
        project.setStatus(ProjectStatus.ACTIVE);

        // When
        boolean overdue = project.isOverdue();

        // Then
        assertThat(overdue).isFalse();
    }

    @Test
    @DisplayName("Should return true when dueDate is in the past and status is ACTIVE for isOverdue()")
    void isOverdue_PastDueDateActiveStatus_ReturnsTrue() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setDueDate(LocalDate.now().minusDays(5));
        project.setStatus(ProjectStatus.ACTIVE);

        // When
        boolean overdue = project.isOverdue();

        // Then
        assertThat(overdue).isTrue();
    }

    @Test
    @DisplayName("Should return false when dueDate is past but status is COMPLETED for isOverdue()")
    void isOverdue_PastDueDateCompletedStatus_ReturnsFalse() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setDueDate(LocalDate.now().minusDays(5));
        project.setStatus(ProjectStatus.COMPLETED);

        // When
        boolean overdue = project.isOverdue();

        // Then
        assertThat(overdue).isFalse();
    }

    @Test
    @DisplayName("Should return false when dueDate is past but status is ARCHIVED for isOverdue()")
    void isOverdue_PastDueDateArchivedStatus_ReturnsFalse() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setDueDate(LocalDate.now().minusDays(5));
        project.setStatus(ProjectStatus.ARCHIVED);

        // When
        boolean overdue = project.isOverdue();

        // Then
        assertThat(overdue).isFalse();
    }

    // ==================== Business Method Tests - isActive ====================

    @Test
    @DisplayName("Should return true when status is PLANNING for isActive()")
    void isActive_PlanningStatus_ReturnsTrue() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setStatus(ProjectStatus.PLANNING);

        // When
        boolean active = project.isActive();

        // Then
        assertThat(active).isTrue();
    }

    @Test
    @DisplayName("Should return true when status is ACTIVE for isActive()")
    void isActive_ActiveStatus_ReturnsTrue() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setStatus(ProjectStatus.ACTIVE);

        // When
        boolean active = project.isActive();

        // Then
        assertThat(active).isTrue();
    }

    @Test
    @DisplayName("Should return true when status is ON_HOLD for isActive()")
    void isActive_OnHoldStatus_ReturnsTrue() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setStatus(ProjectStatus.ON_HOLD);

        // When
        boolean active = project.isActive();

        // Then
        assertThat(active).isTrue();
    }

    @Test
    @DisplayName("Should return false when status is COMPLETED for isActive()")
    void isActive_CompletedStatus_ReturnsFalse() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setStatus(ProjectStatus.COMPLETED);

        // When
        boolean active = project.isActive();

        // Then
        assertThat(active).isFalse();
    }

    @Test
    @DisplayName("Should return false when status is ARCHIVED for isActive()")
    void isActive_ArchivedStatus_ReturnsFalse() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setStatus(ProjectStatus.ARCHIVED);

        // When
        boolean active = project.isActive();

        // Then
        assertThat(active).isFalse();
    }

    // ==================== Business Method Tests - canBeEdited ====================

    @Test
    @DisplayName("Should return true when status is PLANNING for canBeEdited()")
    void canBeEdited_PlanningStatus_ReturnsTrue() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setStatus(ProjectStatus.PLANNING);

        // When
        boolean canEdit = project.canBeEdited();

        // Then
        assertThat(canEdit).isTrue();
    }

    @Test
    @DisplayName("Should return true when status is ACTIVE for canBeEdited()")
    void canBeEdited_ActiveStatus_ReturnsTrue() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setStatus(ProjectStatus.ACTIVE);

        // When
        boolean canEdit = project.canBeEdited();

        // Then
        assertThat(canEdit).isTrue();
    }

    @Test
    @DisplayName("Should return true when status is ON_HOLD for canBeEdited()")
    void canBeEdited_OnHoldStatus_ReturnsTrue() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setStatus(ProjectStatus.ON_HOLD);

        // When
        boolean canEdit = project.canBeEdited();

        // Then
        assertThat(canEdit).isTrue();
    }

    @Test
    @DisplayName("Should return false when status is COMPLETED for canBeEdited()")
    void canBeEdited_CompletedStatus_ReturnsFalse() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setStatus(ProjectStatus.COMPLETED);

        // When
        boolean canEdit = project.canBeEdited();

        // Then
        assertThat(canEdit).isFalse();
    }

    @Test
    @DisplayName("Should return false when status is ARCHIVED for canBeEdited()")
    void canBeEdited_ArchivedStatus_ReturnsFalse() {
        // Given
        Project project = new Project();
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());
        project.setStatus(ProjectStatus.ARCHIVED);

        // When
        boolean canEdit = project.canBeEdited();

        // Then
        assertThat(canEdit).isFalse();
    }

    // ==================== Equals/HashCode Tests ====================

    @Test
    @DisplayName("Should be equal when same project ID")
    void equals_SameProjectId_ReturnsTrue() {
        // Given
        UUID projectId = UUID.randomUUID();

        Project project1 = new Project();
        project1.setId(projectId);
        project1.setTenantId(UUID.randomUUID());
        project1.setName("Project 1");
        project1.setOwnerId(UUID.randomUUID());

        Project project2 = new Project();
        project2.setId(projectId);
        project2.setTenantId(UUID.randomUUID());
        project2.setName("Different Name");
        project2.setOwnerId(UUID.randomUUID());

        // When/Then
        assertThat(project1).isEqualTo(project2);
        assertThat(project1.hashCode()).isEqualTo(project2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when different project IDs")
    void equals_DifferentProjectId_ReturnsFalse() {
        // Given
        Project project1 = new Project();
        project1.setId(UUID.randomUUID());
        project1.setTenantId(UUID.randomUUID());
        project1.setName("Project 1");
        project1.setOwnerId(UUID.randomUUID());

        Project project2 = new Project();
        project2.setId(UUID.randomUUID());
        project2.setTenantId(UUID.randomUUID());
        project2.setName("Project 1");
        project2.setOwnerId(UUID.randomUUID());

        // When/Then
        assertThat(project1).isNotEqualTo(project2);
    }

    @Test
    @DisplayName("Should be equal to itself")
    void equals_SameInstance_ReturnsTrue() {
        // Given
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());

        // When/Then
        assertThat(project).isEqualTo(project);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void equals_Null_ReturnsFalse() {
        // Given
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());

        // When/Then
        assertThat(project).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should not be equal to different class")
    void equals_DifferentClass_ReturnsFalse() {
        // Given
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setTenantId(UUID.randomUUID());
        project.setName("Test Project");
        project.setOwnerId(UUID.randomUUID());

        String notAProject = "Not a project";

        // When/Then
        assertThat(project).isNotEqualTo(notAProject);
    }

    // ==================== ToString Tests ====================

    @Test
    @DisplayName("Should include key fields in toString")
    void toString_ContainsKeyFields() {
        // Given
        UUID projectId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        Project project = new Project();
        project.setId(projectId);
        project.setTenantId(tenantId);
        project.setName("Customer Portal");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setPriority(Priority.HIGH);
        project.setProgressPercentage(75);
        project.setOwnerId(UUID.randomUUID());

        // When
        String result = project.toString();

        // Then
        assertThat(result).contains(projectId.toString());
        assertThat(result).contains(tenantId.toString());
        assertThat(result).contains("Customer Portal");
        assertThat(result).contains("ACTIVE");
        assertThat(result).contains("HIGH");
        assertThat(result).contains("75");
    }

    @Test
    @DisplayName("Should not throw exception when calling toString on empty project")
    void toString_EmptyProject_NoException() {
        // Given
        Project project = new Project();

        // When/Then (no exception)
        String result = project.toString();
        assertThat(result).isNotNull();
    }

    // ==================== Entity Metadata Tests ====================

    @Test
    @DisplayName("Should be annotated with @Entity")
    void metadata_EntityAnnotation_Present() {
        // When/Then
        assertThat(Project.class.isAnnotationPresent(Entity.class)).isTrue();
    }

    @Test
    @DisplayName("Should be annotated with @Table")
    void metadata_TableAnnotation_Present() {
        // When/Then
        assertThat(Project.class.isAnnotationPresent(Table.class)).isTrue();
    }

    @Test
    @DisplayName("Should have table name 'projects'")
    void metadata_TableName_IsProjects() {
        // When
        Table tableAnnotation = Project.class.getAnnotation(Table.class);

        // Then
        assertThat(tableAnnotation.name()).isEqualTo("projects");
    }
}
