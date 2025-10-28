package com.platform.saas.service;

import com.platform.saas.exception.QuotaExceededException;
import com.platform.saas.exception.TenantNotFoundException;
import com.platform.saas.model.*;
import com.platform.saas.repository.ProjectRepository;
import com.platform.saas.repository.TaskRepository;
import com.platform.saas.repository.TenantRepository;
import com.platform.saas.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService.
 * Tests task CRUD operations, quota enforcement, project validation, and tenant isolation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private TaskService taskService;

    private MockedStatic<TenantContext> mockedTenantContext;

    private UUID testTenantId;
    private UUID testProjectId;
    private UUID testTaskId;
    private Tenant testTenant;
    private Project testProject;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testTenantId = UUID.randomUUID();
        testProjectId = UUID.randomUUID();
        testTaskId = UUID.randomUUID();

        // Setup test tenant with FREE tier
        testTenant = new Tenant();
        testTenant.setId(testTenantId);
        testTenant.setName("Test Company");
        testTenant.setSubdomain("test-company");
        testTenant.setSubscriptionTier(SubscriptionTier.FREE);
        testTenant.setQuotaLimit(50);

        // Setup test project
        testProject = new Project();
        testProject.setId(testProjectId);
        testProject.setTenantId(testTenantId);
        testProject.setName("Test Project");

        // Setup test task
        testTask = new Task();
        testTask.setId(testTaskId);
        testTask.setTenantId(testTenantId);
        testTask.setProjectId(testProjectId);
        testTask.setName("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setPriority(Priority.HIGH);
        testTask.setProgressPercentage(0);
        testTask.setDueDate(LocalDate.now().plusDays(7));

        // Mock TenantContext to return testTenantId
        mockedTenantContext = mockStatic(TenantContext.class);
        mockedTenantContext.when(TenantContext::getTenantId).thenReturn(testTenantId);
    }

    @AfterEach
    void tearDown() {
        mockedTenantContext.close();
    }

    // ========== Create Task Tests ==========

    @Test
    @DisplayName("Should create task successfully within quota")
    void createTask_WithinQuota_Success() {
        // Given
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.of(testProject));
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(projectRepository.countByTenantId(testTenantId)).thenReturn(5L);
        when(taskRepository.countByTenantId(testTenantId)).thenReturn(10L);
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        Task result = taskService.createTask(testTask);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTenantId()).isEqualTo(testTenantId);
        assertThat(result.getName()).isEqualTo("Test Task");

        verify(projectRepository).findByIdAndTenantId(testProjectId, testTenantId);
        verify(tenantRepository).findById(testTenantId);
        verify(taskRepository).save(testTask);
        verify(eventPublisher).publishEvent(
                eq(testTenantId),
                eq("task.created"),
                eq(testTaskId),
                eq("task"),
                anyMap()
        );
    }

    @Test
    @DisplayName("Should throw exception when quota exceeded")
    void createTask_QuotaExceeded_ThrowsException() {
        // Given
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.of(testProject));
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(projectRepository.countByTenantId(testTenantId)).thenReturn(30L);
        when(taskRepository.countByTenantId(testTenantId)).thenReturn(20L);

        // When & Then
        assertThatThrownBy(() -> taskService.createTask(testTask))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("projects+tasks");

        verify(taskRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should allow unlimited tasks for ENTERPRISE tier")
    void createTask_EnterpriseTier_NoLimit() {
        // Given
        testTenant.setSubscriptionTier(SubscriptionTier.ENTERPRISE);
        testTenant.setQuotaLimit(null);

        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.of(testProject));
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        Task result = taskService.createTask(testTask);

        // Then
        assertThat(result).isNotNull();
        verify(projectRepository, never()).countByTenantId(any());
        verify(taskRepository, never()).countByTenantId(any());
        verify(taskRepository).save(testTask);
    }

    @Test
    @DisplayName("Should throw exception when tenant context not set")
    void createTask_NoTenantContext_ThrowsException() {
        // Given
        mockedTenantContext.when(TenantContext::getTenantId).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> taskService.createTask(testTask))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tenant context not set");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when project not found")
    void createTask_ProjectNotFound_ThrowsException() {
        // Given
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.createTask(testTask))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Project not found");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when tenant not found")
    void createTask_TenantNotFound_ThrowsException() {
        // Given
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.of(testProject));
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.createTask(testTask))
                .isInstanceOf(TenantNotFoundException.class);

        verify(taskRepository, never()).save(any());
    }

    // ========== Get Task Tests ==========

    @Test
    @DisplayName("Should get task by ID successfully")
    void getTask_ValidId_Success() {
        // Given
        when(taskRepository.findByIdAndTenantId(testTaskId, testTenantId))
                .thenReturn(Optional.of(testTask));

        // When
        Task result = taskService.getTask(testTaskId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testTaskId);
        assertThat(result.getName()).isEqualTo("Test Task");
    }

    @Test
    @DisplayName("Should throw exception when task not found")
    void getTask_NotFound_ThrowsException() {
        // Given
        when(taskRepository.findByIdAndTenantId(testTaskId, testTenantId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.getTask(testTaskId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Task not found");
    }

    // ========== Get All Tasks Tests ==========

    @Test
    @DisplayName("Should get all tasks for tenant")
    void getAllTasks_Success() {
        // Given
        Task task2 = new Task();
        task2.setId(UUID.randomUUID());
        task2.setTenantId(testTenantId);
        task2.setName("Task 2");

        List<Task> tasks = List.of(testTask, task2);
        when(taskRepository.findByTenantId(testTenantId)).thenReturn(tasks);

        // When
        List<Task> result = taskService.getAllTasks();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Task::getName)
                .containsExactly("Test Task", "Task 2");
    }

    // ========== Get Tasks by Project Tests ==========

    @Test
    @DisplayName("Should get tasks by project")
    void getTasksByProject_ValidProject_Success() {
        // Given
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.of(testProject));
        when(taskRepository.findByTenantIdAndProjectId(testTenantId, testProjectId))
                .thenReturn(List.of(testTask));

        // When
        List<Task> result = taskService.getTasksByProject(testProjectId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProjectId()).isEqualTo(testProjectId);
    }

    @Test
    @DisplayName("Should throw exception when getting tasks for invalid project")
    void getTasksByProject_InvalidProject_ThrowsException() {
        // Given
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.getTasksByProject(testProjectId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Project not found");
    }

    // ========== Get Tasks by Status Tests ==========

    @Test
    @DisplayName("Should get tasks by status")
    void getTasksByStatus_TodoStatus_Success() {
        // Given
        when(taskRepository.findByTenantIdAndStatus(testTenantId, TaskStatus.TODO))
                .thenReturn(List.of(testTask));

        // When
        List<Task> result = taskService.getTasksByStatus(TaskStatus.TODO);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TaskStatus.TODO);
    }

    // ========== Get Tasks by Project and Status Tests ==========

    @Test
    @DisplayName("Should get tasks by project and status")
    void getTasksByProjectAndStatus_Success() {
        // Given
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.of(testProject));
        when(taskRepository.findByTenantIdAndProjectIdAndStatus(
                testTenantId, testProjectId, TaskStatus.TODO))
                .thenReturn(List.of(testTask));

        // When
        List<Task> result = taskService.getTasksByProjectAndStatus(testProjectId, TaskStatus.TODO);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(result.get(0).getProjectId()).isEqualTo(testProjectId);
    }

    // ========== Get Tasks by Priority Tests ==========

    @Test
    @DisplayName("Should get tasks by priority")
    void getTasksByPriority_HighPriority_Success() {
        // Given
        when(taskRepository.findByTenantIdAndPriority(testTenantId, Priority.HIGH))
                .thenReturn(List.of(testTask));

        // When
        List<Task> result = taskService.getTasksByPriority(Priority.HIGH);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPriority()).isEqualTo(Priority.HIGH);
    }

    // ========== Get Overdue Tasks Tests ==========

    @Test
    @DisplayName("Should get overdue tasks")
    void getOverdueTasks_Success() {
        // Given
        Task overdueTask = new Task();
        overdueTask.setId(UUID.randomUUID());
        overdueTask.setDueDate(LocalDate.now().minusDays(5));

        when(taskRepository.findOverdueTasks(eq(testTenantId), any(LocalDate.class)))
                .thenReturn(List.of(overdueTask));

        // When
        List<Task> result = taskService.getOverdueTasks();

        // Then
        assertThat(result).hasSize(1);
        verify(taskRepository).findOverdueTasks(eq(testTenantId), any(LocalDate.class));
    }

    @Test
    @DisplayName("Should get overdue tasks for project")
    void getOverdueTasksForProject_Success() {
        // Given
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.of(testProject));

        Task overdueTask = new Task();
        overdueTask.setId(UUID.randomUUID());
        overdueTask.setProjectId(testProjectId);
        overdueTask.setDueDate(LocalDate.now().minusDays(3));

        when(taskRepository.findOverdueTasksForProject(
                eq(testTenantId), eq(testProjectId), any(LocalDate.class)))
                .thenReturn(List.of(overdueTask));

        // When
        List<Task> result = taskService.getOverdueTasksForProject(testProjectId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProjectId()).isEqualTo(testProjectId);
    }

    // ========== Update Task Tests ==========

    @Test
    @DisplayName("Should update task with changes")
    void updateTask_WithChanges_Success() {
        // Given
        when(taskRepository.findByIdAndTenantId(testTaskId, testTenantId))
                .thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task updates = new Task();
        updates.setName("Updated Name");
        updates.setDescription("Updated Description");
        updates.setStatus(TaskStatus.IN_PROGRESS);

        // When
        Task result = taskService.updateTask(testTaskId, updates);

        // Then
        assertThat(result).isNotNull();
        verify(taskRepository).save(any(Task.class));
        verify(eventPublisher).publishEvent(
                eq(testTenantId),
                eq("task.updated"),
                eq(testTaskId),
                eq("task"),
                anyMap()
        );
    }

    @Test
    @DisplayName("Should publish status changed event when status changes")
    void updateTask_StatusChanged_PublishesStatusEvent() {
        // Given
        when(taskRepository.findByIdAndTenantId(testTaskId, testTenantId))
                .thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task updates = new Task();
        updates.setStatus(TaskStatus.COMPLETED);

        // When
        Task result = taskService.updateTask(testTaskId, updates);

        // Then
        assertThat(result).isNotNull();
        verify(eventPublisher).publishEvent(
                eq(testTenantId),
                eq("task.updated"),
                eq(testTaskId),
                eq("task"),
                anyMap()
        );
        verify(eventPublisher).publishEvent(
                eq(testTenantId),
                eq("task.status.changed"),
                eq(testTaskId),
                eq("task"),
                anyMap()
        );
    }

    @Test
    @DisplayName("Should not publish event when no changes")
    void updateTask_NoChanges_NoEvent() {
        // Given
        when(taskRepository.findByIdAndTenantId(testTaskId, testTenantId))
                .thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // Task with all null fields (no changes)
        Task updates = new Task();
        updates.setName(null);
        updates.setDescription(null);
        updates.setStatus(null);
        updates.setPriority(null);
        updates.setDueDate(null);
        updates.setProgressPercentage(null);

        // When
        Task result = taskService.updateTask(testTaskId, updates);

        // Then
        assertThat(result).isNotNull();
        verify(taskRepository).save(any(Task.class));
        verify(eventPublisher, never()).publishEvent(any(), any(), any(), any(), anyMap());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent task")
    void updateTask_NotFound_ThrowsException() {
        // Given
        when(taskRepository.findByIdAndTenantId(testTaskId, testTenantId))
                .thenReturn(Optional.empty());

        Task updates = new Task();
        updates.setName("Updated Name");

        // When & Then
        assertThatThrownBy(() -> taskService.updateTask(testTaskId, updates))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Task not found");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update multiple fields correctly")
    void updateTask_MultipleFields_Success() {
        // Given
        when(taskRepository.findByIdAndTenantId(testTaskId, testTenantId))
                .thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task updates = new Task();
        updates.setName("New Name");
        updates.setDescription("New Description");
        updates.setStatus(TaskStatus.IN_PROGRESS);
        updates.setPriority(Priority.LOW);
        updates.setProgressPercentage(50);
        updates.setDueDate(LocalDate.now().plusDays(14));

        // When
        Task result = taskService.updateTask(testTaskId, updates);

        // Then
        assertThat(result).isNotNull();
        verify(taskRepository).save(any(Task.class));
        verify(eventPublisher, times(2)).publishEvent(any(), any(), any(), any(), anyMap());
    }

    // ========== Delete Task Tests ==========

    @Test
    @DisplayName("Should delete task successfully")
    void deleteTask_ValidId_Success() {
        // Given
        when(taskRepository.findByIdAndTenantId(testTaskId, testTenantId))
                .thenReturn(Optional.of(testTask));

        // When
        taskService.deleteTask(testTaskId);

        // Then
        verify(taskRepository).delete(testTask);
        verify(eventPublisher).publishEvent(
                eq(testTenantId),
                eq("task.deleted"),
                eq(testTaskId),
                eq("task"),
                anyMap()
        );
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent task")
    void deleteTask_NotFound_ThrowsException() {
        // Given
        when(taskRepository.findByIdAndTenantId(testTaskId, testTenantId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.deleteTask(testTaskId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Task not found");

        verify(taskRepository, never()).delete(any());
        verify(eventPublisher, never()).publishEvent(any(), any(), any(), any(), any());
    }

    // ========== Count Tasks Tests ==========

    @Test
    @DisplayName("Should count all tasks correctly")
    void countTasks_Success() {
        // Given
        when(taskRepository.countByTenantId(testTenantId)).thenReturn(25L);

        // When
        long count = taskService.countTasks();

        // Then
        assertThat(count).isEqualTo(25L);
        verify(taskRepository).countByTenantId(testTenantId);
    }

    @Test
    @DisplayName("Should count tasks by project correctly")
    void countTasksByProject_Success() {
        // Given
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.of(testProject));
        when(taskRepository.countByTenantIdAndProjectId(testTenantId, testProjectId))
                .thenReturn(12L);

        // When
        long count = taskService.countTasksByProject(testProjectId);

        // Then
        assertThat(count).isEqualTo(12L);
        verify(taskRepository).countByTenantIdAndProjectId(testTenantId, testProjectId);
    }

    // ========== Calculate Average Progress Tests ==========

    @Test
    @DisplayName("Should calculate average progress correctly")
    void calculateAverageProgress_Success() {
        // Given
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.of(testProject));
        when(taskRepository.calculateAverageProgress(testTenantId, testProjectId))
                .thenReturn(65.5);

        // When
        Double avgProgress = taskService.calculateAverageProgress(testProjectId);

        // Then
        assertThat(avgProgress).isEqualTo(65.5);
        verify(taskRepository).calculateAverageProgress(testTenantId, testProjectId);
    }

    @Test
    @DisplayName("Should return 0.0 when average progress is null")
    void calculateAverageProgress_NullResult_ReturnsZero() {
        // Given
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.of(testProject));
        when(taskRepository.calculateAverageProgress(testTenantId, testProjectId))
                .thenReturn(null);

        // When
        Double avgProgress = taskService.calculateAverageProgress(testProjectId);

        // Then
        assertThat(avgProgress).isEqualTo(0.0);
    }

    // ========== Validate Dependency Tests ==========

    @Test
    @DisplayName("Should throw exception when task depends on itself")
    void validateDependency_SameTask_ThrowsException() {
        // Given
        UUID taskId = UUID.randomUUID();

        // When & Then
        assertThatThrownBy(() -> taskService.validateDependency(taskId, taskId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot depend on itself");
    }

    @Test
    @DisplayName("Should allow valid dependency")
    void validateDependency_DifferentTasks_Success() {
        // Given
        UUID task1 = UUID.randomUUID();
        UUID task2 = UUID.randomUUID();

        // When & Then - Should not throw exception
        assertThatCode(() -> taskService.validateDependency(task1, task2))
                .doesNotThrowAnyException();
    }
}
