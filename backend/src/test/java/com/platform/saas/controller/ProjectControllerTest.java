package com.platform.saas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.saas.model.Priority;
import com.platform.saas.model.Project;
import com.platform.saas.model.ProjectStatus;
import com.platform.saas.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

/**
 * Web layer tests for ProjectController.
 * Tests REST endpoints, validation, and service integration.
 * Note: Role-based authorization tests require integration tests with full Spring Security context.
 * PMAT: Test code Cyc≤10, Cog≤15
 */
@WebMvcTest(controllers = ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ProjectController Tests")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private com.platform.saas.repository.TenantRepository tenantRepository;

    private Project testProject;
    private UUID testProjectId;
    private UUID testTenantId;
    private UUID testOwnerId;

    @BeforeEach
    void setUp() {
        testProjectId = UUID.randomUUID();
        testTenantId = UUID.randomUUID();
        testOwnerId = UUID.randomUUID();

        testProject = new Project();
        testProject.setId(testProjectId);
        testProject.setTenantId(testTenantId);
        testProject.setName("Test Project");
        testProject.setDescription("Test project description");
        testProject.setStatus(ProjectStatus.PLANNING);
        testProject.setPriority(Priority.MEDIUM);
        testProject.setOwnerId(testOwnerId);
        testProject.setProgressPercentage(0);
        testProject.setDueDate(LocalDate.now().plusDays(30));
        testProject.setCreatedAt(LocalDateTime.now());
        testProject.setUpdatedAt(LocalDateTime.now());
    }

    // ========== CREATE PROJECT TESTS ==========

    @Test
    @WithMockUser(authorities = {"ADMINISTRATOR"})
    @DisplayName("Should create project successfully with ADMINISTRATOR role")
    void createProject_AsAdministrator_Success() throws Exception {
        // Given
        when(projectService.createProject(any(Project.class))).thenReturn(testProject);

        // When & Then
        mockMvc.perform(post("/api/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProject)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testProjectId.toString()))
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.status").value("PLANNING"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));

        verify(projectService, times(1)).createProject(any(Project.class));
    }

    @Test
    @WithMockUser(authorities = {"EDITOR"})
    @DisplayName("Should create project successfully with EDITOR role")
    void createProject_AsEditor_Success() throws Exception {
        // Given
        when(projectService.createProject(any(Project.class))).thenReturn(testProject);

        // When & Then
        mockMvc.perform(post("/api/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProject)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Project"));

        verify(projectService, times(1)).createProject(any(Project.class));
    }

    // ========== GET ALL PROJECTS TESTS ==========

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get all projects without filters")
    void getAllProjects_WithoutFilters_Success() throws Exception {
        // Given
        List<Project> projects = Arrays.asList(testProject);
        when(projectService.getAllProjects()).thenReturn(projects);

        // When & Then
        mockMvc.perform(get("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Test Project"));

        verify(projectService, times(1)).getAllProjects();
    }

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get projects filtered by status")
    void getAllProjects_FilterByStatus_Success() throws Exception {
        // Given
        List<Project> projects = Arrays.asList(testProject);
        when(projectService.getProjectsByStatus(ProjectStatus.PLANNING)).thenReturn(projects);

        // When & Then
        mockMvc.perform(get("/api/projects")
                        .param("status", "PLANNING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("PLANNING"));

        verify(projectService, times(1)).getProjectsByStatus(ProjectStatus.PLANNING);
        verify(projectService, never()).getAllProjects();
    }

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get projects filtered by priority")
    void getAllProjects_FilterByPriority_Success() throws Exception {
        // Given
        List<Project> projects = Arrays.asList(testProject);
        when(projectService.getProjectsByPriority(Priority.MEDIUM)).thenReturn(projects);

        // When & Then
        mockMvc.perform(get("/api/projects")
                        .param("priority", "MEDIUM")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].priority").value("MEDIUM"));

        verify(projectService, times(1)).getProjectsByPriority(Priority.MEDIUM);
        verify(projectService, never()).getAllProjects();
    }

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get projects filtered by owner")
    void getAllProjects_FilterByOwner_Success() throws Exception {
        // Given
        List<Project> projects = Arrays.asList(testProject);
        when(projectService.getProjectsByOwner(testOwnerId)).thenReturn(projects);

        // When & Then
        mockMvc.perform(get("/api/projects")
                        .param("ownerId", testOwnerId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ownerId").value(testOwnerId.toString()));

        verify(projectService, times(1)).getProjectsByOwner(testOwnerId);
        verify(projectService, never()).getAllProjects();
    }

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get overdue projects only")
    void getAllProjects_OverdueOnly_Success() throws Exception {
        // Given
        Project overdueProject = new Project();
        overdueProject.setId(UUID.randomUUID());
        overdueProject.setTenantId(testTenantId);
        overdueProject.setName("Overdue Project");
        overdueProject.setStatus(ProjectStatus.ACTIVE);
        overdueProject.setDueDate(LocalDate.now().minusDays(5));
        overdueProject.setOwnerId(testOwnerId);
        overdueProject.setPriority(Priority.HIGH);

        List<Project> projects = Arrays.asList(overdueProject);
        when(projectService.getOverdueProjects()).thenReturn(projects);

        // When & Then
        mockMvc.perform(get("/api/projects")
                        .param("overdueOnly", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Overdue Project"));

        verify(projectService, times(1)).getOverdueProjects();
        verify(projectService, never()).getAllProjects();
    }

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get active projects only")
    void getAllProjects_ActiveOnly_Success() throws Exception {
        // Given
        List<Project> projects = Arrays.asList(testProject);
        when(projectService.getActiveProjects()).thenReturn(projects);

        // When & Then
        mockMvc.perform(get("/api/projects")
                        .param("activeOnly", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("PLANNING"));

        verify(projectService, times(1)).getActiveProjects();
        verify(projectService, never()).getAllProjects();
    }

    // ========== GET PROJECT BY ID TESTS ==========

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get project by ID successfully")
    void getProject_ById_Success() throws Exception {
        // Given
        when(projectService.getProject(testProjectId)).thenReturn(testProject);

        // When & Then
        mockMvc.perform(get("/api/projects/{id}", testProjectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testProjectId.toString()))
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.status").value("PLANNING"));

        verify(projectService, times(1)).getProject(testProjectId);
    }

    // ========== UPDATE PROJECT TESTS ==========

    @Test
    @WithMockUser(authorities = {"ADMINISTRATOR"})
    @DisplayName("Should update project successfully")
    void updateProject_Success() throws Exception {
        // Given
        Project updatedProject = new Project();
        updatedProject.setId(testProjectId);
        updatedProject.setTenantId(testTenantId);
        updatedProject.setName("Updated Project Name");
        updatedProject.setDescription("Updated description");
        updatedProject.setStatus(ProjectStatus.ACTIVE);
        updatedProject.setPriority(Priority.HIGH);
        updatedProject.setOwnerId(testOwnerId);
        updatedProject.setProgressPercentage(50);

        when(projectService.updateProject(eq(testProjectId), any(Project.class)))
                .thenReturn(updatedProject);

        // When & Then
        mockMvc.perform(put("/api/projects/{id}", testProjectId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProject)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testProjectId.toString()))
                .andExpect(jsonPath("$.name").value("Updated Project Name"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.progressPercentage").value(50));

        verify(projectService, times(1)).updateProject(eq(testProjectId), any(Project.class));
    }

    // ========== DELETE PROJECT TESTS ==========

    @Test
    @WithMockUser(authorities = {"ADMINISTRATOR"})
    @DisplayName("Should delete project successfully")
    void deleteProject_Success() throws Exception {
        // Given
        doNothing().when(projectService).deleteProject(testProjectId);

        // When & Then
        mockMvc.perform(delete("/api/projects/{id}", testProjectId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(projectService, times(1)).deleteProject(testProjectId);
    }

    // ========== COUNT PROJECTS TESTS ==========

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get total project count")
    void countProjects_Total_Success() throws Exception {
        // Given
        when(projectService.countProjects()).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/projects/count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(projectService, times(1)).countProjects();
        verify(projectService, never()).countActiveProjects();
    }

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    @DisplayName("Should get active project count")
    void countProjects_ActiveOnly_Success() throws Exception {
        // Given
        when(projectService.countActiveProjects()).thenReturn(3L);

        // When & Then
        mockMvc.perform(get("/api/projects/count")
                        .param("activeOnly", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(projectService, times(1)).countActiveProjects();
        verify(projectService, never()).countProjects();
    }

    // ========== ERROR CASE TESTS ==========

    @Test
    @WithMockUser
    @DisplayName("Should return 404 when project not found by ID")
    void getProject_NotFound_Returns404() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(projectService.getProject(nonExistentId))
                .thenThrow(new com.platform.saas.exception.TenantNotFoundException("Project not found with id: " + nonExistentId));

        // When & Then
        mockMvc.perform(get("/api/projects/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).getProject(nonExistentId);
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRATOR"})
    @DisplayName("Should return 404 when updating non-existent project")
    void updateProject_NotFound_Returns404() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(projectService.updateProject(eq(nonExistentId), any(Project.class)))
                .thenThrow(new com.platform.saas.exception.TenantNotFoundException("Project not found"));

        // When & Then
        mockMvc.perform(put("/api/projects/{id}", nonExistentId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProject)))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).updateProject(eq(nonExistentId), any(Project.class));
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRATOR"})
    @DisplayName("Should return 404 when deleting non-existent project")
    void deleteProject_NotFound_Returns404() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new com.platform.saas.exception.TenantNotFoundException("Project not found"))
                .when(projectService).deleteProject(nonExistentId);

        // When & Then
        mockMvc.perform(delete("/api/projects/{id}", nonExistentId)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(projectService, times(1)).deleteProject(nonExistentId);
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 when creating project with invalid data")
    void createProject_InvalidData_Returns400() throws Exception {
        // Given - project with empty name (violates @NotBlank)
        Project invalidProject = new Project();
        invalidProject.setName("");  // Invalid: empty name
        invalidProject.setTenantId(testTenantId);

        // When & Then
        mockMvc.perform(post("/api/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProject)))
                .andExpect(status().isBadRequest());

        verify(projectService, never()).createProject(any(Project.class));
    }
}
