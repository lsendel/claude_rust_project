package com.platform.saas.service;

import com.platform.saas.exception.QuotaExceededException;
import com.platform.saas.exception.TenantNotFoundException;
import com.platform.saas.model.Priority;
import com.platform.saas.model.Project;
import com.platform.saas.model.ProjectStatus;
import com.platform.saas.model.SubscriptionTier;
import com.platform.saas.model.Tenant;
import com.platform.saas.repository.ProjectRepository;
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
 * Unit tests for ProjectService.
 * Tests project CRUD operations, quota enforcement, and tenant isolation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService Tests")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ProjectService projectService;

    private MockedStatic<TenantContext> mockedTenantContext;

    private UUID testTenantId;
    private UUID testProjectId;
    private UUID testOwnerId;
    private Tenant testTenant;
    private Project testProject;

    @BeforeEach
    void setUp() {
        testTenantId = UUID.randomUUID();
        testProjectId = UUID.randomUUID();
        testOwnerId = UUID.randomUUID();

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
        testProject.setDescription("Test Description");
        testProject.setStatus(ProjectStatus.ACTIVE);
        testProject.setPriority(Priority.HIGH);
        testProject.setOwnerId(testOwnerId);
        testProject.setProgressPercentage(0);
        testProject.setDueDate(LocalDate.now().plusDays(7));

        // Mock TenantContext to return testTenantId
        mockedTenantContext = mockStatic(TenantContext.class);
        mockedTenantContext.when(TenantContext::getTenantId).thenReturn(testTenantId);
    }

    @AfterEach
    void tearDown() {
        mockedTenantContext.close();
    }

    // ========== Create Project Tests ==========

    @Test
    @DisplayName("Should create project successfully within quota")
    void createProject_WithinQuota_Success() {
        // Given
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(projectRepository.countByTenantId(testTenantId)).thenReturn(10L);
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        // When
        Project result = projectService.createProject(testProject);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTenantId()).isEqualTo(testTenantId);
        assertThat(result.getName()).isEqualTo("Test Project");

        verify(tenantRepository).findById(testTenantId);
        verify(projectRepository).countByTenantId(testTenantId);
        verify(projectRepository).save(testProject);
        verify(eventPublisher).publishEvent(
                eq(testTenantId),
                eq("project.created"),
                eq(testProjectId),
                eq("project"),
                anyMap()
        );
    }

    @Test
    @DisplayName("Should throw exception when quota exceeded")
    void createProject_QuotaExceeded_ThrowsException() {
        // Given
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(projectRepository.countByTenantId(testTenantId)).thenReturn(50L);

        // When & Then
        assertThatThrownBy(() -> projectService.createProject(testProject))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("projects");

        verify(projectRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw exception when quota at limit")
    void createProject_QuotaAtLimit_ThrowsException() {
        // Given
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(projectRepository.countByTenantId(testTenantId)).thenReturn(49L);

        // When & Then - At limit (49) should allow creation since check is >=
        // Actually, let me recheck the logic: currentCount >= quotaLimit
        // If limit is 50 and current is 49, that's < 50, so it should be OK
        // If limit is 50 and current is 50, that's >= 50, so it should throw

        // Let me correct this - at exactly 50 should throw
        when(projectRepository.countByTenantId(testTenantId)).thenReturn(50L);

        assertThatThrownBy(() -> projectService.createProject(testProject))
                .isInstanceOf(QuotaExceededException.class);

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should allow unlimited projects for ENTERPRISE tier")
    void createProject_EnterpriseTier_NoLimit() {
        // Given
        testTenant.setSubscriptionTier(SubscriptionTier.ENTERPRISE);
        testTenant.setQuotaLimit(null);

        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        // When
        Project result = projectService.createProject(testProject);

        // Then
        assertThat(result).isNotNull();
        verify(projectRepository, never()).countByTenantId(any());
        verify(projectRepository).save(testProject);
    }

    @Test
    @DisplayName("Should throw exception when tenant context not set")
    void createProject_NoTenantContext_ThrowsException() {
        // Given
        mockedTenantContext.when(TenantContext::getTenantId).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> projectService.createProject(testProject))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tenant context not set");

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when tenant not found")
    void createProject_TenantNotFound_ThrowsException() {
        // Given
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.createProject(testProject))
                .isInstanceOf(TenantNotFoundException.class);

        verify(projectRepository, never()).save(any());
    }

    // ========== Get Project Tests ==========

    @Test
    @DisplayName("Should get project by ID successfully")
    void getProject_ValidId_Success() {
        // Given
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.of(testProject));

        // When
        Project result = projectService.getProject(testProjectId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testProjectId);
        assertThat(result.getName()).isEqualTo("Test Project");

        verify(projectRepository).findByIdAndTenantId(testProjectId, testTenantId);
    }

    @Test
    @DisplayName("Should throw exception when project not found")
    void getProject_NotFound_ThrowsException() {
        // Given
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.getProject(testProjectId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Project not found");
    }

    // ========== Get All Projects Tests ==========

    @Test
    @DisplayName("Should get all projects for tenant")
    void getAllProjects_Success() {
        // Given
        Project project2 = new Project();
        project2.setId(UUID.randomUUID());
        project2.setTenantId(testTenantId);
        project2.setName("Project 2");

        List<Project> projects = List.of(testProject, project2);
        when(projectRepository.findByTenantId(testTenantId)).thenReturn(projects);

        // When
        List<Project> result = projectService.getAllProjects();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Project::getName)
                .containsExactly("Test Project", "Project 2");
    }

    @Test
    @DisplayName("Should return empty list when no projects")
    void getAllProjects_Empty_ReturnsEmptyList() {
        // Given
        when(projectRepository.findByTenantId(testTenantId)).thenReturn(List.of());

        // When
        List<Project> result = projectService.getAllProjects();

        // Then
        assertThat(result).isEmpty();
    }

    // ========== Get Projects by Status Tests ==========

    @Test
    @DisplayName("Should get projects by status")
    void getProjectsByStatus_ActiveStatus_Success() {
        // Given
        when(projectRepository.findByTenantIdAndStatus(testTenantId, ProjectStatus.ACTIVE))
                .thenReturn(List.of(testProject));

        // When
        List<Project> result = projectService.getProjectsByStatus(ProjectStatus.ACTIVE);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(ProjectStatus.ACTIVE);
    }

    // ========== Get Projects by Priority Tests ==========

    @Test
    @DisplayName("Should get projects by priority")
    void getProjectsByPriority_HighPriority_Success() {
        // Given
        when(projectRepository.findByTenantIdAndPriority(testTenantId, Priority.HIGH))
                .thenReturn(List.of(testProject));

        // When
        List<Project> result = projectService.getProjectsByPriority(Priority.HIGH);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPriority()).isEqualTo(Priority.HIGH);
    }

    // ========== Get Projects by Owner Tests ==========

    @Test
    @DisplayName("Should get projects by owner")
    void getProjectsByOwner_ValidOwner_Success() {
        // Given
        when(projectRepository.findByTenantIdAndOwnerId(testTenantId, testOwnerId))
                .thenReturn(List.of(testProject));

        // When
        List<Project> result = projectService.getProjectsByOwner(testOwnerId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOwnerId()).isEqualTo(testOwnerId);
    }

    // ========== Get Overdue Projects Tests ==========

    @Test
    @DisplayName("Should get overdue projects")
    void getOverdueProjects_Success() {
        // Given
        Project overdueProject = new Project();
        overdueProject.setId(UUID.randomUUID());
        overdueProject.setDueDate(LocalDate.now().minusDays(5));

        when(projectRepository.findOverdueProjects(eq(testTenantId), any(LocalDate.class)))
                .thenReturn(List.of(overdueProject));

        // When
        List<Project> result = projectService.getOverdueProjects();

        // Then
        assertThat(result).hasSize(1);
        verify(projectRepository).findOverdueProjects(eq(testTenantId), any(LocalDate.class));
    }

    // ========== Get Active Projects Tests ==========

    @Test
    @DisplayName("Should get active projects")
    void getActiveProjects_Success() {
        // Given
        when(projectRepository.findActiveProjects(testTenantId))
                .thenReturn(List.of(testProject));

        // When
        List<Project> result = projectService.getActiveProjects();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(ProjectStatus.ACTIVE);
    }

    // ========== Update Project Tests ==========

    @Test
    @DisplayName("Should update project with changes")
    void updateProject_WithChanges_Success() {
        // Given
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        Project updates = new Project();
        updates.setName("Updated Name");
        updates.setDescription("Updated Description");
        updates.setStatus(ProjectStatus.COMPLETED);

        // When
        Project result = projectService.updateProject(testProjectId, updates);

        // Then
        assertThat(result).isNotNull();
        verify(projectRepository).save(any(Project.class));
        verify(eventPublisher).publishEvent(
                eq(testTenantId),
                eq("project.updated"),
                eq(testProjectId),
                eq("project"),
                anyMap()
        );
    }

    @Test
    @DisplayName("Should not publish event when no changes")
    void updateProject_NoChanges_NoEvent() {
        // Given
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        // Project with all null fields (no changes)
        Project updates = new Project();
        updates.setName(null);
        updates.setDescription(null);
        updates.setStatus(null);
        updates.setPriority(null);
        updates.setDueDate(null);
        updates.setOwnerId(null);
        updates.setProgressPercentage(null);

        // When
        Project result = projectService.updateProject(testProjectId, updates);

        // Then
        assertThat(result).isNotNull();
        verify(projectRepository).save(any(Project.class));
        verify(eventPublisher, never()).publishEvent(any(), any(), any(), any(), anyMap());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent project")
    void updateProject_NotFound_ThrowsException() {
        // Given
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.empty());

        Project updates = new Project();
        updates.setName("Updated Name");

        // When & Then
        assertThatThrownBy(() -> projectService.updateProject(testProjectId, updates))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Project not found");

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update multiple fields correctly")
    void updateProject_MultipleFields_Success() {
        // Given
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        Project updates = new Project();
        updates.setName("New Name");
        updates.setDescription("New Description");
        updates.setStatus(ProjectStatus.PLANNING);
        updates.setPriority(Priority.LOW);
        updates.setProgressPercentage(50);
        updates.setDueDate(LocalDate.now().plusDays(14));
        updates.setOwnerId(UUID.randomUUID());

        // When
        Project result = projectService.updateProject(testProjectId, updates);

        // Then
        assertThat(result).isNotNull();
        verify(projectRepository).save(any(Project.class));
        verify(eventPublisher).publishEvent(
                eq(testTenantId),
                eq("project.updated"),
                eq(testProjectId),
                eq("project"),
                anyMap()
        );
    }

    // ========== Delete Project Tests ==========

    @Test
    @DisplayName("Should delete project successfully")
    void deleteProject_ValidId_Success() {
        // Given
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.of(testProject));

        // When
        projectService.deleteProject(testProjectId);

        // Then
        verify(projectRepository).delete(testProject);
        verify(eventPublisher).publishEvent(
                eq(testTenantId),
                eq("project.deleted"),
                eq(testProjectId),
                eq("project"),
                anyMap()
        );
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent project")
    void deleteProject_NotFound_ThrowsException() {
        // Given
        when(projectRepository.findByIdAndTenantId(testProjectId, testTenantId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.deleteProject(testProjectId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Project not found");

        verify(projectRepository, never()).delete(any());
        verify(eventPublisher, never()).publishEvent(any(), any(), any(), any(), any());
    }

    // ========== Count Projects Tests ==========

    @Test
    @DisplayName("Should count all projects correctly")
    void countProjects_Success() {
        // Given
        when(projectRepository.countByTenantId(testTenantId)).thenReturn(15L);

        // When
        long count = projectService.countProjects();

        // Then
        assertThat(count).isEqualTo(15L);
        verify(projectRepository).countByTenantId(testTenantId);
    }

    @Test
    @DisplayName("Should count active projects correctly")
    void countActiveProjects_Success() {
        // Given
        when(projectRepository.countActiveProjects(testTenantId)).thenReturn(8L);

        // When
        long count = projectService.countActiveProjects();

        // Then
        assertThat(count).isEqualTo(8L);
        verify(projectRepository).countActiveProjects(testTenantId);
    }
}
