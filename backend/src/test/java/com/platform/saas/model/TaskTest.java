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
 * Comprehensive test suite for Task entity.
 *
 * Sprint 2 - Model Layer Testing
 * Target: Validate all constraints, business logic, and entity behavior
 *
 * Test Categories:
 * 1. Entity Creation (4 tests)
 * 2. TenantId & ProjectId Validation (4 tests)
 * 3. Name Validation (4 tests)
 * 4. Description Validation (2 tests)
 * 5. Status Validation (5 tests)
 * 6. DueDate Validation (3 tests)
 * 7. ProgressPercentage Validation (5 tests)
 * 8. Priority Validation (5 tests)
 * 9. Business Methods - isOverdue (4 tests)
 * 10. Business Methods - Status Checks (6 tests)
 * 11. Business Methods - complete (2 tests)
 * 12. Business Methods - startWork (4 tests)
 * 13. Equals/HashCode (5 tests)
 * 14. ToString (2 tests)
 * 15. Entity Metadata (3 tests)
 */
@DisplayName("Task Entity Tests - Task Management Component")
class TaskTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==================== Entity Creation Tests ====================

    @Test
    @DisplayName("Should create valid task with all required fields")
    void create_ValidTask_Success() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Implement user authentication");

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO); // Default
        assertThat(task.getPriority()).isEqualTo(Priority.MEDIUM); // Default
        assertThat(task.getProgressPercentage()).isEqualTo(0); // Default
    }

    @Test
    @DisplayName("Should set default values on creation")
    void create_Task_DefaultValues() {
        // Given
        Task task = new Task();

        // Then
        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(task.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(task.getProgressPercentage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should create task with all optional fields")
    void create_TaskWithOptionalFields_Success() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Implement user authentication");
        task.setDescription("Add OAuth2 authentication with JWT tokens");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setDueDate(LocalDate.now().plusDays(7));
        task.setProgressPercentage(30);
        task.setPriority(Priority.HIGH);

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
        assertThat(task.getDescription()).isEqualTo("Add OAuth2 authentication with JWT tokens");
    }

    @Test
    @DisplayName("Should fail validation when required fields are missing")
    void validate_MissingRequiredFields_Fails() {
        // Given
        Task task = new Task();
        // No tenantId, projectId, or name set

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSizeGreaterThanOrEqualTo(3);
    }

    // ==================== TenantId & ProjectId Validation Tests ====================

    @Test
    @DisplayName("Should fail validation when tenantId is null")
    void validate_TenantIdNull_Fails() {
        // Given
        Task task = new Task();
        task.setTenantId(null);
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

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
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when projectId is null")
    void validate_ProjectIdNull_Fails() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(null);
        task.setName("Test Task");

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("projectId") &&
            v.getMessage().contains("cannot be null")
        );
    }

    @Test
    @DisplayName("Should pass validation with valid projectId")
    void validate_ProjectIdValid_Success() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
    }

    // ==================== Name Validation Tests ====================

    @Test
    @DisplayName("Should fail validation when name is blank")
    void validate_NameBlank_Fails() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("");

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

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
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Implement feature X");

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should pass validation when name is exactly 255 characters")
    void validate_NameMaxLength_Success() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("A".repeat(255));

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
        assertThat(task.getName()).hasSize(255);
    }

    @Test
    @DisplayName("Should fail validation when name exceeds 255 characters")
    void validate_NameTooLong_Fails() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("A".repeat(256));

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

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
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setDescription(null);

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should allow non-empty description")
    void validate_DescriptionProvided_Success() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setDescription("Detailed description of the task requirements and acceptance criteria");

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
        assertThat(task.getDescription()).isNotEmpty();
    }

    // ==================== Status Validation Tests ====================

    @Test
    @DisplayName("Should fail validation when status is null")
    void validate_StatusNull_Fails() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setStatus(null);

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("status") &&
            v.getMessage().contains("cannot be null")
        );
    }

    @Test
    @DisplayName("Should pass validation with TODO status")
    void validate_StatusTodo_Success() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setStatus(TaskStatus.TODO);

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should pass validation with IN_PROGRESS status")
    void validate_StatusInProgress_Success() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setStatus(TaskStatus.IN_PROGRESS);

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should pass validation with BLOCKED status")
    void validate_StatusBlocked_Success() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setStatus(TaskStatus.BLOCKED);

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should pass validation with COMPLETED status")
    void validate_StatusCompleted_Success() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setStatus(TaskStatus.COMPLETED);

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
    }

    // ==================== DueDate Validation Tests ====================

    @Test
    @DisplayName("Should allow null dueDate")
    void validate_DueDateNull_Success() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setDueDate(null);

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should allow future dueDate")
    void validate_DueDateFuture_Success() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setDueDate(LocalDate.now().plusDays(7));

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should allow past dueDate")
    void validate_DueDatePast_Success() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setDueDate(LocalDate.now().minusDays(3));

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
    }

    // ==================== ProgressPercentage Validation Tests ====================

    @Test
    @DisplayName("Should fail validation when progressPercentage is negative")
    void validate_ProgressPercentageNegative_Fails() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setProgressPercentage(-1);

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

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
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setProgressPercentage(0);

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should pass validation when progressPercentage is 50")
    void validate_ProgressPercentageMid_Success() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setProgressPercentage(50);

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should pass validation when progressPercentage is 100")
    void validate_ProgressPercentage100_Success() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setProgressPercentage(100);

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when progressPercentage exceeds 100")
    void validate_ProgressPercentageExceeds100_Fails() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setProgressPercentage(101);

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

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
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setPriority(null);

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

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
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setPriority(Priority.LOW);

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should pass validation with MEDIUM priority")
    void validate_PriorityMedium_Success() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setPriority(Priority.MEDIUM);

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should pass validation with HIGH priority")
    void validate_PriorityHigh_Success() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setPriority(Priority.HIGH);

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should pass validation with CRITICAL priority")
    void validate_PriorityCritical_Success() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setPriority(Priority.CRITICAL);

        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        // Then
        assertThat(violations).isEmpty();
    }

    // ==================== Business Method Tests - isOverdue ====================

    @Test
    @DisplayName("Should return false when dueDate is null for isOverdue()")
    void isOverdue_NoDueDate_ReturnsFalse() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setDueDate(null);
        task.setStatus(TaskStatus.IN_PROGRESS);

        // When
        boolean overdue = task.isOverdue();

        // Then
        assertThat(overdue).isFalse();
    }

    @Test
    @DisplayName("Should return false when dueDate is in the future for isOverdue()")
    void isOverdue_FutureDueDate_ReturnsFalse() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setDueDate(LocalDate.now().plusDays(5));
        task.setStatus(TaskStatus.IN_PROGRESS);

        // When
        boolean overdue = task.isOverdue();

        // Then
        assertThat(overdue).isFalse();
    }

    @Test
    @DisplayName("Should return true when dueDate is past and status is not COMPLETED for isOverdue()")
    void isOverdue_PastDueDateNotCompleted_ReturnsTrue() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setDueDate(LocalDate.now().minusDays(2));
        task.setStatus(TaskStatus.IN_PROGRESS);

        // When
        boolean overdue = task.isOverdue();

        // Then
        assertThat(overdue).isTrue();
    }

    @Test
    @DisplayName("Should return false when dueDate is past but status is COMPLETED for isOverdue()")
    void isOverdue_PastDueDateCompleted_ReturnsFalse() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setDueDate(LocalDate.now().minusDays(2));
        task.setStatus(TaskStatus.COMPLETED);

        // When
        boolean overdue = task.isOverdue();

        // Then
        assertThat(overdue).isFalse();
    }

    // ==================== Business Method Tests - Status Checks ====================

    @Test
    @DisplayName("Should return true when status is IN_PROGRESS for isInProgress()")
    void isInProgress_InProgressStatus_ReturnsTrue() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setStatus(TaskStatus.IN_PROGRESS);

        // When
        boolean inProgress = task.isInProgress();

        // Then
        assertThat(inProgress).isTrue();
    }

    @Test
    @DisplayName("Should return false when status is not IN_PROGRESS for isInProgress()")
    void isInProgress_OtherStatus_ReturnsFalse() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setStatus(TaskStatus.TODO);

        // When
        boolean inProgress = task.isInProgress();

        // Then
        assertThat(inProgress).isFalse();
    }

    @Test
    @DisplayName("Should return true when status is BLOCKED for isBlocked()")
    void isBlocked_BlockedStatus_ReturnsTrue() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setStatus(TaskStatus.BLOCKED);

        // When
        boolean blocked = task.isBlocked();

        // Then
        assertThat(blocked).isTrue();
    }

    @Test
    @DisplayName("Should return false when status is not BLOCKED for isBlocked()")
    void isBlocked_OtherStatus_ReturnsFalse() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setStatus(TaskStatus.TODO);

        // When
        boolean blocked = task.isBlocked();

        // Then
        assertThat(blocked).isFalse();
    }

    @Test
    @DisplayName("Should return true when status is COMPLETED for isCompleted()")
    void isCompleted_CompletedStatus_ReturnsTrue() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setStatus(TaskStatus.COMPLETED);

        // When
        boolean completed = task.isCompleted();

        // Then
        assertThat(completed).isTrue();
    }

    @Test
    @DisplayName("Should return false when status is not COMPLETED for isCompleted()")
    void isCompleted_OtherStatus_ReturnsFalse() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setStatus(TaskStatus.TODO);

        // When
        boolean completed = task.isCompleted();

        // Then
        assertThat(completed).isFalse();
    }

    // ==================== Business Method Tests - complete ====================

    @Test
    @DisplayName("Should set status to COMPLETED and progress to 100 when complete() is called")
    void complete_SetsStatusAndProgress() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setProgressPercentage(75);

        // When
        task.complete();

        // Then
        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(task.getProgressPercentage()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should mark TODO task as completed when complete() is called")
    void complete_FromTodoStatus_Success() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setStatus(TaskStatus.TODO);
        task.setProgressPercentage(0);

        // When
        task.complete();

        // Then
        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(task.getProgressPercentage()).isEqualTo(100);
    }

    // ==================== Business Method Tests - startWork ====================

    @Test
    @DisplayName("Should change status from TODO to IN_PROGRESS when startWork() is called")
    void startWork_FromTodo_ChangesToInProgress() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setStatus(TaskStatus.TODO);

        // When
        task.startWork();

        // Then
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Should change status from BLOCKED to IN_PROGRESS when startWork() is called")
    void startWork_FromBlocked_ChangesToInProgress() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setStatus(TaskStatus.BLOCKED);

        // When
        task.startWork();

        // Then
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Should not change status from COMPLETED when startWork() is called")
    void startWork_FromCompleted_NoChange() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setStatus(TaskStatus.COMPLETED);

        // When
        task.startWork();

        // Then
        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should not change status from IN_PROGRESS when startWork() is called")
    void startWork_FromInProgress_NoChange() {
        // Given
        Task task = new Task();
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");
        task.setStatus(TaskStatus.IN_PROGRESS);

        // When
        task.startWork();

        // Then
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    // ==================== Equals/HashCode Tests ====================

    @Test
    @DisplayName("Should be equal when same task ID")
    void equals_SameTaskId_ReturnsTrue() {
        // Given
        UUID taskId = UUID.randomUUID();

        Task task1 = new Task();
        task1.setId(taskId);
        task1.setTenantId(UUID.randomUUID());
        task1.setProjectId(UUID.randomUUID());
        task1.setName("Task 1");

        Task task2 = new Task();
        task2.setId(taskId);
        task2.setTenantId(UUID.randomUUID());
        task2.setProjectId(UUID.randomUUID());
        task2.setName("Different Task");

        // When/Then
        assertThat(task1).isEqualTo(task2);
        assertThat(task1.hashCode()).isEqualTo(task2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when different task IDs")
    void equals_DifferentTaskId_ReturnsFalse() {
        // Given
        Task task1 = new Task();
        task1.setId(UUID.randomUUID());
        task1.setTenantId(UUID.randomUUID());
        task1.setProjectId(UUID.randomUUID());
        task1.setName("Task 1");

        Task task2 = new Task();
        task2.setId(UUID.randomUUID());
        task2.setTenantId(UUID.randomUUID());
        task2.setProjectId(UUID.randomUUID());
        task2.setName("Task 1");

        // When/Then
        assertThat(task1).isNotEqualTo(task2);
    }

    @Test
    @DisplayName("Should be equal to itself")
    void equals_SameInstance_ReturnsTrue() {
        // Given
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");

        // When/Then
        assertThat(task).isEqualTo(task);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void equals_Null_ReturnsFalse() {
        // Given
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");

        // When/Then
        assertThat(task).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should not be equal to different class")
    void equals_DifferentClass_ReturnsFalse() {
        // Given
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setTenantId(UUID.randomUUID());
        task.setProjectId(UUID.randomUUID());
        task.setName("Test Task");

        String notATask = "Not a task";

        // When/Then
        assertThat(task).isNotEqualTo(notATask);
    }

    // ==================== ToString Tests ====================

    @Test
    @DisplayName("Should include key fields in toString")
    void toString_ContainsKeyFields() {
        // Given
        UUID taskId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        Task task = new Task();
        task.setId(taskId);
        task.setTenantId(tenantId);
        task.setProjectId(projectId);
        task.setName("Implement authentication");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setPriority(Priority.HIGH);

        // When
        String result = task.toString();

        // Then
        assertThat(result).contains(taskId.toString());
        assertThat(result).contains(tenantId.toString());
        assertThat(result).contains(projectId.toString());
        assertThat(result).contains("Implement authentication");
        assertThat(result).contains("IN_PROGRESS");
        assertThat(result).contains("HIGH");
    }

    @Test
    @DisplayName("Should not throw exception when calling toString on empty task")
    void toString_EmptyTask_NoException() {
        // Given
        Task task = new Task();

        // When/Then (no exception)
        String result = task.toString();
        assertThat(result).isNotNull();
    }

    // ==================== Entity Metadata Tests ====================

    @Test
    @DisplayName("Should be annotated with @Entity")
    void metadata_EntityAnnotation_Present() {
        // When/Then
        assertThat(Task.class.isAnnotationPresent(Entity.class)).isTrue();
    }

    @Test
    @DisplayName("Should be annotated with @Table")
    void metadata_TableAnnotation_Present() {
        // When/Then
        assertThat(Task.class.isAnnotationPresent(Table.class)).isTrue();
    }

    @Test
    @DisplayName("Should have table name 'tasks'")
    void metadata_TableName_IsTasks() {
        // When
        Table tableAnnotation = Task.class.getAnnotation(Table.class);

        // Then
        assertThat(tableAnnotation.name()).isEqualTo("tasks");
    }
}
