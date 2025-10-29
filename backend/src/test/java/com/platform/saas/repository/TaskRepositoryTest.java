package com.platform.saas.repository;

import com.platform.saas.model.Priority;
import com.platform.saas.model.Task;
import com.platform.saas.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TaskRepository.
 *
 * Sprint 3 - Repository Layer Testing
 * Uses @DataJpaTest for in-memory H2 database testing
 * Excludes Flyway and uses JPA schema generation
 *
 * Test Categories:
 * 1. findByIdAndTenantId (2 tests)
 * 2. findByTenantIdAndProjectId (2 tests)
 * 3. findByTenantIdAndStatus (2 tests)
 * 4. findByTenantIdAndProjectIdAndStatus (2 tests)
 * 5. findByTenantIdAndPriority (2 tests)
 * 6. findOverdueTasks (3 tests)
 * 7. findOverdueTasksForProject (2 tests)
 * 8. countByTenantId (2 tests)
 * 9. countByTenantIdAndProjectId (2 tests)
 * 10. calculateAverageProgress (3 tests)
 */
@DataJpaTest(excludeAutoConfiguration = FlywayAutoConfiguration.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("TaskRepository Integration Tests")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private UUID tenantId;
    private UUID projectId;
    private Task testTask;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        testTask = new Task();
        testTask.setTenantId(tenantId);
        testTask.setProjectId(projectId);
        testTask.setName("Test Task");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setPriority(Priority.MEDIUM);
        testTask.setProgressPercentage(0);
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());
    }

    // ==================== findByIdAndTenantId Tests ====================

    @Test
    @DisplayName("Should find task by ID and tenant ID when exists")
    void findByIdAndTenantId_ExistingTask_Found() {
        // Given
        entityManager.persist(testTask);
        entityManager.flush();

        // When
        Optional<Task> found = taskRepository.findByIdAndTenantId(testTask.getId(), tenantId);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Task");
        assertThat(found.get().getTenantId()).isEqualTo(tenantId);
    }

    @Test
    @DisplayName("Should return empty when task not found by ID and tenant ID")
    void findByIdAndTenantId_NonExistingTask_Empty() {
        // When
        Optional<Task> found = taskRepository.findByIdAndTenantId(UUID.randomUUID(), tenantId);

        // Then
        assertThat(found).isEmpty();
    }

    // ==================== findByTenantIdAndProjectId Tests ====================

    @Test
    @DisplayName("Should find all tasks by tenant ID and project ID")
    void findByTenantIdAndProjectId_MultipleTasks_FoundAll() {
        // Given
        Task task1 = createTask(tenantId, projectId, "Task 1");
        Task task2 = createTask(tenantId, projectId, "Task 2");

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.flush();

        // When
        List<Task> found = taskRepository.findByTenantIdAndProjectId(tenantId, projectId);

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Task::getName).containsExactlyInAnyOrder("Task 1", "Task 2");
    }

    @Test
    @DisplayName("Should return empty list when no tasks for project")
    void findByTenantIdAndProjectId_NoTasks_EmptyList() {
        // When
        List<Task> found = taskRepository.findByTenantIdAndProjectId(tenantId, projectId);

        // Then
        assertThat(found).isEmpty();
    }

    // ==================== findByTenantIdAndStatus Tests ====================

    @Test
    @DisplayName("Should find tasks by tenant ID and status")
    void findByTenantIdAndStatus_FilteredByStatus_FoundMatching() {
        // Given
        Task todoTask = createTaskWithStatus(tenantId, projectId, "Todo Task", TaskStatus.TODO);
        Task inProgressTask = createTaskWithStatus(tenantId, projectId, "In Progress", TaskStatus.IN_PROGRESS);

        entityManager.persist(todoTask);
        entityManager.persist(inProgressTask);
        entityManager.flush();

        // When
        List<Task> found = taskRepository.findByTenantIdAndStatus(tenantId, TaskStatus.TODO);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Todo Task");
        assertThat(found.get(0).getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    @DisplayName("Should return empty list when no tasks match status")
    void findByTenantIdAndStatus_NoMatchingStatus_EmptyList() {
        // Given
        Task todoTask = createTaskWithStatus(tenantId, projectId, "Todo Task", TaskStatus.TODO);
        entityManager.persist(todoTask);
        entityManager.flush();

        // When
        List<Task> found = taskRepository.findByTenantIdAndStatus(tenantId, TaskStatus.COMPLETED);

        // Then
        assertThat(found).isEmpty();
    }

    // ==================== findByTenantIdAndProjectIdAndStatus Tests ====================

    @Test
    @DisplayName("Should find tasks by tenant, project, and status")
    void findByTenantIdAndProjectIdAndStatus_Filtered_FoundMatching() {
        // Given
        Task todoTask = createTaskWithStatus(tenantId, projectId, "Todo", TaskStatus.TODO);
        Task completedTask = createTaskWithStatus(tenantId, projectId, "Completed", TaskStatus.COMPLETED);

        entityManager.persist(todoTask);
        entityManager.persist(completedTask);
        entityManager.flush();

        // When
        List<Task> found = taskRepository.findByTenantIdAndProjectIdAndStatus(tenantId, projectId, TaskStatus.TODO);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Todo");
    }

    @Test
    @DisplayName("Should return empty when no tasks match all criteria")
    void findByTenantIdAndProjectIdAndStatus_NoMatch_EmptyList() {
        // Given
        Task todoTask = createTaskWithStatus(tenantId, projectId, "Todo", TaskStatus.TODO);
        entityManager.persist(todoTask);
        entityManager.flush();

        // When
        List<Task> found = taskRepository.findByTenantIdAndProjectIdAndStatus(tenantId, projectId, TaskStatus.BLOCKED);

        // Then
        assertThat(found).isEmpty();
    }

    // ==================== findByTenantIdAndPriority Tests ====================

    @Test
    @DisplayName("Should find tasks by tenant ID and priority")
    void findByTenantIdAndPriority_FilteredByPriority_FoundMatching() {
        // Given
        Task highTask = createTaskWithPriority(tenantId, projectId, "High", Priority.HIGH);
        Task lowTask = createTaskWithPriority(tenantId, projectId, "Low", Priority.LOW);

        entityManager.persist(highTask);
        entityManager.persist(lowTask);
        entityManager.flush();

        // When
        List<Task> found = taskRepository.findByTenantIdAndPriority(tenantId, Priority.HIGH);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("High");
    }

    @Test
    @DisplayName("Should return empty when no tasks match priority")
    void findByTenantIdAndPriority_NoMatch_EmptyList() {
        // Given
        Task lowTask = createTaskWithPriority(tenantId, projectId, "Low", Priority.LOW);
        entityManager.persist(lowTask);
        entityManager.flush();

        // When
        List<Task> found = taskRepository.findByTenantIdAndPriority(tenantId, Priority.CRITICAL);

        // Then
        assertThat(found).isEmpty();
    }

    // ==================== findOverdueTasks Tests ====================

    @Test
    @DisplayName("Should find overdue tasks")
    void findOverdueTasks_PastDueDate_FoundOverdue() {
        // Given
        Task overdueTask = createTask(tenantId, projectId, "Overdue Task");
        overdueTask.setDueDate(LocalDate.now().minusDays(5));
        overdueTask.setStatus(TaskStatus.TODO);

        Task futureTask = createTask(tenantId, projectId, "Future Task");
        futureTask.setDueDate(LocalDate.now().plusDays(5));
        futureTask.setStatus(TaskStatus.TODO);

        entityManager.persist(overdueTask);
        entityManager.persist(futureTask);
        entityManager.flush();

        // When
        List<Task> found = taskRepository.findOverdueTasks(tenantId, LocalDate.now());

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Overdue Task");
    }

    @Test
    @DisplayName("Should not find completed tasks as overdue")
    void findOverdueTasks_CompletedTask_NotIncluded() {
        // Given
        Task completedOverdue = createTaskWithStatus(tenantId, projectId, "Completed", TaskStatus.COMPLETED);
        completedOverdue.setDueDate(LocalDate.now().minusDays(5));

        entityManager.persist(completedOverdue);
        entityManager.flush();

        // When
        List<Task> found = taskRepository.findOverdueTasks(tenantId, LocalDate.now());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when no overdue tasks")
    void findOverdueTasks_AllOnTime_EmptyList() {
        // Given
        Task futureTask = createTask(tenantId, projectId, "Future");
        futureTask.setDueDate(LocalDate.now().plusDays(10));
        entityManager.persist(futureTask);
        entityManager.flush();

        // When
        List<Task> found = taskRepository.findOverdueTasks(tenantId, LocalDate.now());

        // Then
        assertThat(found).isEmpty();
    }

    // ==================== findOverdueTasksForProject Tests ====================

    @Test
    @DisplayName("Should find overdue tasks for specific project")
    void findOverdueTasksForProject_PastDueDate_FoundOverdue() {
        // Given
        UUID otherProjectId = UUID.randomUUID();

        Task overdueTask = createTask(tenantId, projectId, "Overdue Task");
        overdueTask.setDueDate(LocalDate.now().minusDays(5));
        overdueTask.setStatus(TaskStatus.TODO);

        Task overdueOtherProject = createTask(tenantId, otherProjectId, "Other Project Overdue");
        overdueOtherProject.setDueDate(LocalDate.now().minusDays(5));
        overdueOtherProject.setStatus(TaskStatus.TODO);

        entityManager.persist(overdueTask);
        entityManager.persist(overdueOtherProject);
        entityManager.flush();

        // When
        List<Task> found = taskRepository.findOverdueTasksForProject(tenantId, projectId, LocalDate.now());

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Overdue Task");
        assertThat(found.get(0).getProjectId()).isEqualTo(projectId);
    }

    @Test
    @DisplayName("Should return empty when no overdue tasks for project")
    void findOverdueTasksForProject_NoOverdue_EmptyList() {
        // Given
        Task futureTask = createTask(tenantId, projectId, "Future");
        futureTask.setDueDate(LocalDate.now().plusDays(10));
        entityManager.persist(futureTask);
        entityManager.flush();

        // When
        List<Task> found = taskRepository.findOverdueTasksForProject(tenantId, projectId, LocalDate.now());

        // Then
        assertThat(found).isEmpty();
    }

    // ==================== countByTenantId Tests ====================

    @Test
    @DisplayName("Should count all tasks for tenant")
    void countByTenantId_MultipleTasks_ReturnsCorrectCount() {
        // Given
        Task task1 = createTask(tenantId, projectId, "Task 1");
        Task task2 = createTask(tenantId, UUID.randomUUID(), "Task 2");
        Task task3 = createTask(tenantId, UUID.randomUUID(), "Task 3");

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.persist(task3);
        entityManager.flush();

        // When
        long count = taskRepository.countByTenantId(tenantId);

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should return zero when no tasks for tenant")
    void countByTenantId_NoTasks_ReturnsZero() {
        // When
        long count = taskRepository.countByTenantId(tenantId);

        // Then
        assertThat(count).isZero();
    }

    // ==================== countByTenantIdAndProjectId Tests ====================

    @Test
    @DisplayName("Should count tasks by tenant and project")
    void countByTenantIdAndProjectId_MultipleTasks_ReturnsCorrectCount() {
        // Given
        Task task1 = createTask(tenantId, projectId, "Task 1");
        Task task2 = createTask(tenantId, projectId, "Task 2");
        Task task3 = createTask(tenantId, projectId, "Task 3");

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.persist(task3);
        entityManager.flush();

        // When
        long count = taskRepository.countByTenantIdAndProjectId(tenantId, projectId);

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should return zero when no tasks for project")
    void countByTenantIdAndProjectId_NoTasks_ReturnsZero() {
        // When
        long count = taskRepository.countByTenantIdAndProjectId(tenantId, projectId);

        // Then
        assertThat(count).isZero();
    }

    // ==================== calculateAverageProgress Tests ====================

    @Test
    @DisplayName("Should calculate average progress for project tasks")
    void calculateAverageProgress_MultipleTasks_ReturnsAverage() {
        // Given
        Task task1 = createTaskWithProgress(tenantId, projectId, "Task 1", 25);
        Task task2 = createTaskWithProgress(tenantId, projectId, "Task 2", 75);

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.flush();

        // When
        Double average = taskRepository.calculateAverageProgress(tenantId, projectId);

        // Then
        assertThat(average).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Should return null when no tasks for average calculation")
    void calculateAverageProgress_NoTasks_ReturnsNull() {
        // When
        Double average = taskRepository.calculateAverageProgress(tenantId, projectId);

        // Then
        assertThat(average).isNull();
    }

    @Test
    @DisplayName("Should calculate zero average when all tasks at 0%")
    void calculateAverageProgress_AllZero_ReturnsZero() {
        // Given
        Task task1 = createTaskWithProgress(tenantId, projectId, "Task 1", 0);
        Task task2 = createTaskWithProgress(tenantId, projectId, "Task 2", 0);

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.flush();

        // When
        Double average = taskRepository.calculateAverageProgress(tenantId, projectId);

        // Then
        assertThat(average).isEqualTo(0.0);
    }

    // ==================== Helper Methods ====================

    private Task createTask(UUID tenantId, UUID projectId, String name) {
        Task task = new Task();
        task.setTenantId(tenantId);
        task.setProjectId(projectId);
        task.setName(name);
        task.setStatus(TaskStatus.TODO);
        task.setPriority(Priority.MEDIUM);
        task.setProgressPercentage(0);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }

    private Task createTaskWithStatus(UUID tenantId, UUID projectId, String name, TaskStatus status) {
        Task task = createTask(tenantId, projectId, name);
        task.setStatus(status);
        return task;
    }

    private Task createTaskWithPriority(UUID tenantId, UUID projectId, String name, Priority priority) {
        Task task = createTask(tenantId, projectId, name);
        task.setPriority(priority);
        return task;
    }

    private Task createTaskWithProgress(UUID tenantId, UUID projectId, String name, int progress) {
        Task task = createTask(tenantId, projectId, name);
        task.setProgressPercentage(progress);
        return task;
    }
}
