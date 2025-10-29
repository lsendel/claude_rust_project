package com.platform.saas.repository;

import com.platform.saas.model.Priority;
import com.platform.saas.model.Project;
import com.platform.saas.model.ProjectStatus;
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
 * Integration tests for ProjectRepository.
 *
 * Sprint 3 - Repository Layer Testing
 * Uses @DataJpaTest for in-memory H2 database testing
 * Excludes Flyway and uses JPA schema generation
 *
 * Test Categories:
 * 1. findByIdAndTenantId (2 tests)
 * 2. findByTenantId (2 tests)
 * 3. findByTenantIdAndStatus (2 tests)
 * 4. findByTenantIdAndPriority (2 tests)
 * 5. findByTenantIdAndOwnerId (2 tests)
 * 6. findOverdueProjects (3 tests)
 * 7. findActiveProjects (2 tests)
 * 8. countByTenantId (2 tests)
 */
@DataJpaTest(excludeAutoConfiguration = FlywayAutoConfiguration.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("ProjectRepository Integration Tests")
class ProjectRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProjectRepository projectRepository;

    private UUID tenantId;
    private UUID ownerId;
    private Project testProject;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        ownerId = UUID.randomUUID();

        testProject = new Project();
        testProject.setTenantId(tenantId);
        testProject.setName("Test Project");
        testProject.setStatus(ProjectStatus.ACTIVE);
        testProject.setOwnerId(ownerId);
        testProject.setPriority(Priority.MEDIUM);
        testProject.setCreatedAt(LocalDateTime.now());
        testProject.setUpdatedAt(LocalDateTime.now());
    }

    // ==================== findByIdAndTenantId Tests ====================

    @Test
    @DisplayName("Should find project by ID and tenant ID when exists")
    void findByIdAndTenantId_ExistingProject_Found() {
        // Given
        entityManager.persist(testProject);
        entityManager.flush();

        // When
        Optional<Project> found = projectRepository.findByIdAndTenantId(testProject.getId(), tenantId);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Project");
        assertThat(found.get().getTenantId()).isEqualTo(tenantId);
    }

    @Test
    @DisplayName("Should return empty when project not found by ID and tenant ID")
    void findByIdAndTenantId_NonExistingProject_Empty() {
        // When
        Optional<Project> found = projectRepository.findByIdAndTenantId(UUID.randomUUID(), tenantId);

        // Then
        assertThat(found).isEmpty();
    }

    // ==================== findByTenantId Tests ====================

    @Test
    @DisplayName("Should find all projects by tenant ID")
    void findByTenantId_MultipleProjects_FoundAll() {
        // Given
        Project project1 = createProject(tenantId, "Project 1", ProjectStatus.ACTIVE);
        Project project2 = createProject(tenantId, "Project 2", ProjectStatus.PLANNING);

        entityManager.persist(project1);
        entityManager.persist(project2);
        entityManager.flush();

        // When
        List<Project> found = projectRepository.findByTenantId(tenantId);

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Project::getName).containsExactlyInAnyOrder("Project 1", "Project 2");
    }

    @Test
    @DisplayName("Should return empty list when no projects for tenant")
    void findByTenantId_NoProjects_EmptyList() {
        // When
        List<Project> found = projectRepository.findByTenantId(tenantId);

        // Then
        assertThat(found).isEmpty();
    }

    // ==================== findByTenantIdAndStatus Tests ====================

    @Test
    @DisplayName("Should find projects by tenant ID and status")
    void findByTenantIdAndStatus_FilteredByStatus_FoundMatching() {
        // Given
        Project activeProject = createProject(tenantId, "Active Project", ProjectStatus.ACTIVE);
        Project completedProject = createProject(tenantId, "Completed Project", ProjectStatus.COMPLETED);

        entityManager.persist(activeProject);
        entityManager.persist(completedProject);
        entityManager.flush();

        // When
        List<Project> found = projectRepository.findByTenantIdAndStatus(tenantId, ProjectStatus.ACTIVE);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Active Project");
        assertThat(found.get(0).getStatus()).isEqualTo(ProjectStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should return empty list when no projects match status")
    void findByTenantIdAndStatus_NoMatchingStatus_EmptyList() {
        // Given
        Project activeProject = createProject(tenantId, "Active Project", ProjectStatus.ACTIVE);
        entityManager.persist(activeProject);
        entityManager.flush();

        // When
        List<Project> found = projectRepository.findByTenantIdAndStatus(tenantId, ProjectStatus.ARCHIVED);

        // Then
        assertThat(found).isEmpty();
    }

    // ==================== findByTenantIdAndPriority Tests ====================

    @Test
    @DisplayName("Should find projects by tenant ID and priority")
    void findByTenantIdAndPriority_FilteredByPriority_FoundMatching() {
        // Given
        Project highPriorityProject = createProjectWithPriority(tenantId, "High Priority", Priority.HIGH);
        Project lowPriorityProject = createProjectWithPriority(tenantId, "Low Priority", Priority.LOW);

        entityManager.persist(highPriorityProject);
        entityManager.persist(lowPriorityProject);
        entityManager.flush();

        // When
        List<Project> found = projectRepository.findByTenantIdAndPriority(tenantId, Priority.HIGH);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("High Priority");
        assertThat(found.get(0).getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    @DisplayName("Should return empty list when no projects match priority")
    void findByTenantIdAndPriority_NoMatchingPriority_EmptyList() {
        // Given
        Project lowPriorityProject = createProjectWithPriority(tenantId, "Low Priority", Priority.LOW);
        entityManager.persist(lowPriorityProject);
        entityManager.flush();

        // When
        List<Project> found = projectRepository.findByTenantIdAndPriority(tenantId, Priority.CRITICAL);

        // Then
        assertThat(found).isEmpty();
    }

    // ==================== findByTenantIdAndOwnerId Tests ====================

    @Test
    @DisplayName("Should find projects by tenant ID and owner ID")
    void findByTenantIdAndOwnerId_FilteredByOwner_FoundMatching() {
        // Given
        UUID owner1 = UUID.randomUUID();
        UUID owner2 = UUID.randomUUID();

        Project project1 = createProjectWithOwner(tenantId, "Owner 1 Project", owner1);
        Project project2 = createProjectWithOwner(tenantId, "Owner 2 Project", owner2);

        entityManager.persist(project1);
        entityManager.persist(project2);
        entityManager.flush();

        // When
        List<Project> found = projectRepository.findByTenantIdAndOwnerId(tenantId, owner1);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Owner 1 Project");
        assertThat(found.get(0).getOwnerId()).isEqualTo(owner1);
    }

    @Test
    @DisplayName("Should return empty list when no projects for owner")
    void findByTenantIdAndOwnerId_NoMatchingOwner_EmptyList() {
        // Given
        Project project = createProjectWithOwner(tenantId, "Test Project", UUID.randomUUID());
        entityManager.persist(project);
        entityManager.flush();

        // When
        List<Project> found = projectRepository.findByTenantIdAndOwnerId(tenantId, UUID.randomUUID());

        // Then
        assertThat(found).isEmpty();
    }

    // ==================== findOverdueProjects Tests ====================

    @Test
    @DisplayName("Should find overdue projects")
    void findOverdueProjects_PastDueDate_FoundOverdue() {
        // Given
        Project overdueProject = createProject(tenantId, "Overdue Project", ProjectStatus.ACTIVE);
        overdueProject.setDueDate(LocalDate.now().minusDays(5));

        Project futureProject = createProject(tenantId, "Future Project", ProjectStatus.ACTIVE);
        futureProject.setDueDate(LocalDate.now().plusDays(5));

        entityManager.persist(overdueProject);
        entityManager.persist(futureProject);
        entityManager.flush();

        // When
        List<Project> found = projectRepository.findOverdueProjects(tenantId, LocalDate.now());

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Overdue Project");
    }

    @Test
    @DisplayName("Should not find completed projects as overdue")
    void findOverdueProjects_CompletedProject_NotIncluded() {
        // Given
        Project completedOverdue = createProject(tenantId, "Completed Overdue", ProjectStatus.COMPLETED);
        completedOverdue.setDueDate(LocalDate.now().minusDays(5));

        entityManager.persist(completedOverdue);
        entityManager.flush();

        // When
        List<Project> found = projectRepository.findOverdueProjects(tenantId, LocalDate.now());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when no overdue projects")
    void findOverdueProjects_AllOnTime_EmptyList() {
        // Given
        Project futureProject = createProject(tenantId, "Future Project", ProjectStatus.ACTIVE);
        futureProject.setDueDate(LocalDate.now().plusDays(10));
        entityManager.persist(futureProject);
        entityManager.flush();

        // When
        List<Project> found = projectRepository.findOverdueProjects(tenantId, LocalDate.now());

        // Then
        assertThat(found).isEmpty();
    }

    // ==================== findActiveProjects Tests ====================

    @Test
    @DisplayName("Should find active projects")
    void findActiveProjects_ExcludesCompletedAndArchived_FoundActive() {
        // Given
        Project activeProject = createProject(tenantId, "Active", ProjectStatus.ACTIVE);
        Project planningProject = createProject(tenantId, "Planning", ProjectStatus.PLANNING);
        Project completedProject = createProject(tenantId, "Completed", ProjectStatus.COMPLETED);
        Project archivedProject = createProject(tenantId, "Archived", ProjectStatus.ARCHIVED);

        entityManager.persist(activeProject);
        entityManager.persist(planningProject);
        entityManager.persist(completedProject);
        entityManager.persist(archivedProject);
        entityManager.flush();

        // When
        List<Project> found = projectRepository.findActiveProjects(tenantId);

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Project::getName).containsExactlyInAnyOrder("Active", "Planning");
    }

    @Test
    @DisplayName("Should return empty when no active projects")
    void findActiveProjects_AllCompleted_EmptyList() {
        // Given
        Project completedProject = createProject(tenantId, "Completed", ProjectStatus.COMPLETED);
        entityManager.persist(completedProject);
        entityManager.flush();

        // When
        List<Project> found = projectRepository.findActiveProjects(tenantId);

        // Then
        assertThat(found).isEmpty();
    }

    // ==================== countByTenantId Tests ====================

    @Test
    @DisplayName("Should count projects by tenant ID")
    void countByTenantId_MultipleProjects_ReturnsCorrectCount() {
        // Given
        Project project1 = createProject(tenantId, "Project 1", ProjectStatus.ACTIVE);
        Project project2 = createProject(tenantId, "Project 2", ProjectStatus.PLANNING);
        Project project3 = createProject(tenantId, "Project 3", ProjectStatus.COMPLETED);

        entityManager.persist(project1);
        entityManager.persist(project2);
        entityManager.persist(project3);
        entityManager.flush();

        // When
        long count = projectRepository.countByTenantId(tenantId);

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should return zero when no projects for tenant")
    void countByTenantId_NoProjects_ReturnsZero() {
        // When
        long count = projectRepository.countByTenantId(tenantId);

        // Then
        assertThat(count).isZero();
    }

    // ==================== Helper Methods ====================

    private Project createProject(UUID tenantId, String name, ProjectStatus status) {
        Project project = new Project();
        project.setTenantId(tenantId);
        project.setName(name);
        project.setStatus(status);
        project.setOwnerId(UUID.randomUUID());
        project.setPriority(Priority.MEDIUM);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        return project;
    }

    private Project createProjectWithPriority(UUID tenantId, String name, Priority priority) {
        Project project = createProject(tenantId, name, ProjectStatus.ACTIVE);
        project.setPriority(priority);
        return project;
    }

    private Project createProjectWithOwner(UUID tenantId, String name, UUID ownerId) {
        Project project = createProject(tenantId, name, ProjectStatus.ACTIVE);
        project.setOwnerId(ownerId);
        return project;
    }
}
