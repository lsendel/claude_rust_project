package com.platform.saas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.saas.model.Priority;
import com.platform.saas.model.Task;
import com.platform.saas.model.TaskStatus;
import com.platform.saas.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for TaskController.
 * Tests REST endpoints, validation, and service integration.
 * Note: Role-based authorization tests require integration tests with full Spring Security context.
 * PMAT: Test code Cyc≤10, Cog≤15
 */
@WebMvcTest(controllers = TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TaskController Tests")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private com.platform.saas.repository.TenantRepository tenantRepository;

    private Task testTask;
    private UUID testTaskId;
    private UUID testTenantId;
    private UUID testProjectId;

    @BeforeEach
    void setUp() {
        testTaskId = UUID.randomUUID();
        testTenantId = UUID.randomUUID();
        testProjectId = UUID.randomUUID();

        testTask = new Task();
        testTask.setId(testTaskId);
        testTask.setTenantId(testTenantId);
        testTask.setProjectId(testProjectId);
        testTask.setName("Test Task");
        testTask.setDescription("Test task description");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setPriority(Priority.MEDIUM);
        testTask.setProgressPercentage(0);
        testTask.setDueDate(LocalDate.now().plusDays(7));
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());
    }

    // ========== CREATE TASK TESTS ==========

    @Test
    @WithMockUser(authorities = {"ADMINISTRATOR"})
    @DisplayName("Should create task successfully with ADMINISTRATOR role")
    void createTask_AsAdministrator_Success() throws Exception {
        // Given
        when(taskService.createTask(any(Task.class))).thenReturn(testTask);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTask)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testTaskId.toString()))
                .andExpect(jsonPath("$.name").value("Test Task"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));

        verify(taskService, times(1)).createTask(any(Task.class));
    }

    @Test
    @WithMockUser(authorities = {"EDITOR"})
    @DisplayName("Should create task successfully with EDITOR role")
    void createTask_AsEditor_Success() throws Exception {
        // Given
        when(taskService.createTask(any(Task.class))).thenReturn(testTask);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTask)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Task"));

        verify(taskService, times(1)).createTask(any(Task.class));
    }

    // ========== GET ALL TASKS TESTS ==========

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get all tasks without filters")
    void getAllTasks_WithoutFilters_Success() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskService.getAllTasks()).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Test Task"));

        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get tasks filtered by project")
    void getAllTasks_FilterByProject_Success() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskService.getTasksByProject(testProjectId)).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks")
                        .param("projectId", testProjectId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].projectId").value(testProjectId.toString()));

        verify(taskService, times(1)).getTasksByProject(testProjectId);
    }

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get tasks filtered by status")
    void getAllTasks_FilterByStatus_Success() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskService.getTasksByStatus(TaskStatus.TODO)).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks")
                        .param("status", "TODO")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("TODO"));

        verify(taskService, times(1)).getTasksByStatus(TaskStatus.TODO);
    }

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get tasks filtered by priority")
    void getAllTasks_FilterByPriority_Success() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskService.getTasksByPriority(Priority.MEDIUM)).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks")
                        .param("priority", "MEDIUM")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].priority").value("MEDIUM"));

        verify(taskService, times(1)).getTasksByPriority(Priority.MEDIUM);
    }

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get tasks filtered by project and status")
    void getAllTasks_FilterByProjectAndStatus_Success() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskService.getTasksByProjectAndStatus(testProjectId, TaskStatus.TODO)).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks")
                        .param("projectId", testProjectId.toString())
                        .param("status", "TODO")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("TODO"));

        verify(taskService, times(1)).getTasksByProjectAndStatus(testProjectId, TaskStatus.TODO);
    }

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get overdue tasks only")
    void getAllTasks_OverdueOnly_Success() throws Exception {
        // Given
        Task overdueTask = new Task();
        overdueTask.setId(UUID.randomUUID());
        overdueTask.setTenantId(testTenantId);
        overdueTask.setProjectId(testProjectId);
        overdueTask.setName("Overdue Task");
        overdueTask.setStatus(TaskStatus.IN_PROGRESS);
        overdueTask.setDueDate(LocalDate.now().minusDays(5));
        overdueTask.setPriority(Priority.HIGH);

        List<Task> tasks = Arrays.asList(overdueTask);
        when(taskService.getOverdueTasks()).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks")
                        .param("overdueOnly", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Overdue Task"));

        verify(taskService, times(1)).getOverdueTasks();
    }

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get overdue tasks for specific project")
    void getAllTasks_OverdueForProject_Success() throws Exception {
        // Given
        Task overdueTask = new Task();
        overdueTask.setId(UUID.randomUUID());
        overdueTask.setTenantId(testTenantId);
        overdueTask.setProjectId(testProjectId);
        overdueTask.setName("Overdue Task");
        overdueTask.setStatus(TaskStatus.IN_PROGRESS);
        overdueTask.setDueDate(LocalDate.now().minusDays(5));
        overdueTask.setPriority(Priority.HIGH);

        List<Task> tasks = Arrays.asList(overdueTask);
        when(taskService.getOverdueTasksForProject(testProjectId)).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks")
                        .param("projectId", testProjectId.toString())
                        .param("overdueOnly", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Overdue Task"));

        verify(taskService, times(1)).getOverdueTasksForProject(testProjectId);
    }

    // ========== GET TASK BY ID TEST ==========

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get task by ID successfully")
    void getTask_ById_Success() throws Exception {
        // Given
        when(taskService.getTask(testTaskId)).thenReturn(testTask);

        // When & Then
        mockMvc.perform(get("/api/tasks/{id}", testTaskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTaskId.toString()))
                .andExpect(jsonPath("$.name").value("Test Task"))
                .andExpect(jsonPath("$.status").value("TODO"));

        verify(taskService, times(1)).getTask(testTaskId);
    }

    // ========== UPDATE TASK TEST ==========

    @Test
    @WithMockUser(authorities = {"ADMINISTRATOR"})
    @DisplayName("Should update task successfully")
    void updateTask_Success() throws Exception {
        // Given
        Task updatedTask = new Task();
        updatedTask.setId(testTaskId);
        updatedTask.setTenantId(testTenantId);
        updatedTask.setProjectId(testProjectId);
        updatedTask.setName("Updated Task Name");
        updatedTask.setDescription("Updated description");
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);
        updatedTask.setPriority(Priority.HIGH);
        updatedTask.setProgressPercentage(50);

        when(taskService.updateTask(eq(testTaskId), any(Task.class)))
                .thenReturn(updatedTask);

        // When & Then
        mockMvc.perform(put("/api/tasks/{id}", testTaskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTaskId.toString()))
                .andExpect(jsonPath("$.name").value("Updated Task Name"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.progressPercentage").value(50));

        verify(taskService, times(1)).updateTask(eq(testTaskId), any(Task.class));
    }

    // ========== DELETE TASK TEST ==========

    @Test
    @WithMockUser(authorities = {"ADMINISTRATOR"})
    @DisplayName("Should delete task successfully")
    void deleteTask_Success() throws Exception {
        // Given
        doNothing().when(taskService).deleteTask(testTaskId);

        // When & Then
        mockMvc.perform(delete("/api/tasks/{id}", testTaskId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask(testTaskId);
    }

    // ========== COUNT TASKS TESTS ==========

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get total task count")
    void countTasks_Total_Success() throws Exception {
        // Given
        when(taskService.countTasks()).thenReturn(10L);

        // When & Then
        mockMvc.perform(get("/api/tasks/count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));

        verify(taskService, times(1)).countTasks();
    }

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get task count for specific project")
    void countTasks_ByProject_Success() throws Exception {
        // Given
        when(taskService.countTasksByProject(testProjectId)).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/tasks/count")
                        .param("projectId", testProjectId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(taskService, times(1)).countTasksByProject(testProjectId);
        verify(taskService, never()).countTasks();
    }

    // ========== AVERAGE PROGRESS TEST ==========

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get average progress for project")
    void getAverageProgress_Success() throws Exception {
        // Given
        when(taskService.calculateAverageProgress(testProjectId)).thenReturn(45.5);

        // When & Then
        mockMvc.perform(get("/api/tasks/progress/average")
                        .param("projectId", testProjectId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("45.5"));

        verify(taskService, times(1)).calculateAverageProgress(testProjectId);
    }

    // ========== ERROR CASE TESTS ==========

    @Test
    @WithMockUser
    @DisplayName("Should return 404 when task not found by ID")
    void getTask_NotFound_Returns404() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(taskService.getTask(nonExistentId))
                .thenThrow(new com.platform.saas.exception.TenantNotFoundException("Task not found with id: " + nonExistentId));

        // When & Then
        mockMvc.perform(get("/api/tasks/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).getTask(nonExistentId);
    }

    @Test
    @WithMockUser(authorities = {"EDITOR"})
    @DisplayName("Should return 404 when updating non-existent task")
    void updateTask_NotFound_Returns404() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(taskService.updateTask(eq(nonExistentId), any(com.platform.saas.model.Task.class)))
                .thenThrow(new com.platform.saas.exception.TenantNotFoundException("Task not found"));

        // When & Then
        mockMvc.perform(put("/api/tasks/{id}", nonExistentId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTask)))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).updateTask(eq(nonExistentId), any(com.platform.saas.model.Task.class));
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRATOR"})
    @DisplayName("Should return 404 when deleting non-existent task")
    void deleteTask_NotFound_Returns404() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new com.platform.saas.exception.TenantNotFoundException("Task not found"))
                .when(taskService).deleteTask(nonExistentId);

        // When & Then
        mockMvc.perform(delete("/api/tasks/{id}", nonExistentId)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).deleteTask(nonExistentId);
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 when creating task with invalid data")
    void createTask_InvalidData_Returns400() throws Exception {
        // Given - task with empty name (violates @NotBlank)
        com.platform.saas.model.Task invalidTask = new com.platform.saas.model.Task();
        invalidTask.setName("");  // Invalid: empty name
        invalidTask.setTenantId(testTenantId);
        invalidTask.setProjectId(testProjectId);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any(com.platform.saas.model.Task.class));
    }
}
