# PMAT Phase 2 - Backend Testing Plan

**Date**: 2025-10-27
**Status**: 📋 PLANNING
**Priority**: 🎯 HIGH

---

## 🎯 Overview

This document outlines the comprehensive testing strategy for the Spring Boot backend, including controller tests, service tests, repository tests, and integration tests.

**Testing Framework**: JUnit 5 (Jupiter) + Spring Boot Test + Mockito
**Coverage Tool**: JaCoCo (80% target configured in pom.xml)
**Integration Testing**: Testcontainers (PostgreSQL)
**Total Estimated Tests**: ~350 tests

---

## ✅ Framework Verification (COMPLETE)

**Dependencies Confirmed** (from pom.xml):
- ✅ `spring-boot-starter-test` - JUnit 5, Mockito, AssertJ, Hamcrest
- ✅ `spring-security-test` - Security testing support
- ✅ `testcontainers` (v1.19.3) - Container-based integration tests
- ✅ `jacoco-maven-plugin` (v0.8.11) - 80% line coverage minimum
- ✅ `maven-surefire-plugin` (v3.0.0-M9) - Test execution

**Test Directory Structure**:
```
backend/src/test/
├── java/com/platform/saas/
│   ├── controller/    (7 controllers to test)
│   ├── service/       (7+ services to test)
│   ├── repository/    (7 repositories to test)
│   └── integration/   (Full stack integration tests)
└── resources/
    └── application-test.properties
```

---

## 📊 Testing Breakdown

### Summary:

| Category | Components | Estimated Tests | Priority |
|----------|-----------|-----------------|----------|
| **Controller Tests** | 7 controllers | ~80 tests | HIGH |
| **Service Tests** | 7+ services | ~150 tests | HIGH |
| **Repository Tests** | 7 repositories | ~50 tests | MEDIUM |
| **Integration Tests** | End-to-end flows | ~70 tests | MEDIUM |
| **TOTAL** | **25+ components** | **~350 tests** | - |

---

## 🎯 1. Controller Tests (~80 tests)

**Testing Strategy**: Use `@WebMvcTest` for isolated controller testing with mocked services.

### Controllers to Test:

#### 1.1 ProjectController (~12 tests)
**File**: `ProjectController.java`
**Endpoints**:
- `POST /api/projects` - Create project
- `GET /api/projects` - List all (with filters)
- `GET /api/projects/{id}` - Get by ID
- `PUT /api/projects/{id}` - Update project
- `DELETE /api/projects/{id}` - Delete project
- `GET /api/projects/count` - Count projects

**Tests**:
- ✅ Create project successfully (ADMINISTRATOR/EDITOR)
- ✅ Create project fails without permission (VIEWER)
- ✅ Get all projects without filters
- ✅ Get projects filtered by status
- ✅ Get projects filtered by priority
- ✅ Get projects filtered by owner
- ✅ Get overdue projects only
- ✅ Get active projects only
- ✅ Get project by ID successfully
- ✅ Update project successfully
- ✅ Delete project successfully
- ✅ Get project count

#### 1.2 TaskController (~12 tests)
**File**: `TaskController.java`
**Endpoints**:
- `POST /api/tasks` - Create task
- `GET /api/tasks` - List all (with filters)
- `GET /api/tasks/{id}` - Get by ID
- `PUT /api/tasks/{id}` - Update task
- `DELETE /api/tasks/{id}` - Delete task
- `GET /api/tasks/count` - Count tasks

**Tests**:
- ✅ Create task successfully
- ✅ Create task fails without permission
- ✅ Get all tasks without filters
- ✅ Get tasks filtered by project
- ✅ Get tasks filtered by status
- ✅ Get tasks filtered by priority
- ✅ Get overdue tasks only
- ✅ Get task by ID successfully
- ✅ Update task successfully
- ✅ Update task fails with invalid data
- ✅ Delete task successfully
- ✅ Get task count

#### 1.3 AutomationController (~10 tests)
**File**: `AutomationController.java`
**Endpoints**:
- `POST /api/automation/rules` - Create rule
- `GET /api/automation/rules` - List all rules
- `GET /api/automation/rules/{id}` - Get rule by ID
- `PUT /api/automation/rules/{id}` - Update rule
- `DELETE /api/automation/rules/{id}` - Delete rule
- `PUT /api/automation/rules/{id}/toggle` - Toggle rule status
- `GET /api/automation/logs` - Get recent logs

**Tests**:
- ✅ Create automation rule successfully
- ✅ Get all rules
- ✅ Get rule by ID
- ✅ Update rule successfully
- ✅ Delete rule successfully
- ✅ Toggle rule status to inactive
- ✅ Toggle rule status to active
- ✅ Get recent event logs (default 50)
- ✅ Get recent event logs (custom limit)
- ✅ Permission checks for all endpoints

#### 1.4 UserController (~10 tests)
**File**: `UserController.java`
**Endpoints**:
- `POST /api/users/invite` - Invite user
- `GET /api/users` - List users
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Remove user
- `GET /api/users/me` - Get current user

**Tests**:
- ✅ Invite user successfully (ADMINISTRATOR)
- ✅ Invite user fails without permission
- ✅ List all users in tenant
- ✅ Get user by ID
- ✅ Update user successfully
- ✅ Remove user successfully
- ✅ Cannot remove self
- ✅ Get current user info
- ✅ Validation on invite (email, role)
- ✅ Duplicate email handling

#### 1.5 TenantController (~8 tests)
**File**: `TenantController.java`
**Endpoints**:
- `POST /api/tenants` - Create tenant
- `GET /api/tenants` - List tenants
- `GET /api/tenants/{id}` - Get tenant by ID
- `PUT /api/tenants/{id}` - Update tenant
- `DELETE /api/tenants/{id}` - Delete tenant

**Tests**:
- ✅ Create tenant successfully
- ✅ Get all tenants
- ✅ Get tenant by ID
- ✅ Update tenant successfully
- ✅ Delete tenant successfully
- ✅ Tenant isolation verification
- ✅ Validation on create
- ✅ Handle duplicate tenant names

#### 1.6 AuthController (~6 tests)
**File**: `AuthController.java`
**Endpoints**:
- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Register
- `POST /api/auth/refresh` - Refresh token
- `POST /api/auth/logout` - Logout

**Tests**:
- ✅ Login successfully with valid credentials
- ✅ Login fails with invalid credentials
- ✅ Register new user successfully
- ✅ Refresh token successfully
- ✅ Logout successfully
- ✅ Validation on registration

#### 1.7 InternalApiController (~5 tests)
**File**: `InternalApiController.java`
**Endpoints**:
- `GET /api/internal/health` - Health check
- `GET /api/internal/metrics` - Metrics
- `GET /api/internal/cache/clear` - Clear cache

**Tests**:
- ✅ Health check returns 200
- ✅ Metrics endpoint accessible
- ✅ Clear cache successfully
- ✅ Internal endpoints require authentication
- ✅ Response format validation

---

## 🔧 2. Service Tests (~150 tests)

**Testing Strategy**: Use `@ExtendWith(MockitoExtension.class)` for unit testing with mocked repositories.

### Services to Test:

#### 2.1 ProjectService (~25 tests)
**File**: `ProjectService.java`
**Methods**:
- `createProject(Project)` - Create new project
- `getAllProjects()` - Get all projects
- `getProjectById(UUID)` - Get by ID
- `updateProject(UUID, Project)` - Update project
- `deleteProject(UUID)` - Delete project
- `getProjectsByStatus(ProjectStatus)` - Filter by status
- `getProjectsByPriority(Priority)` - Filter by priority
- `getProjectsByOwner(UUID)` - Filter by owner
- `getOverdueProjects()` - Get overdue projects
- `getActiveProjects()` - Get active projects
- `countProjects()` - Count all projects

**Tests**:
- ✅ Create project with valid data
- ✅ Create project validates required fields
- ✅ Create project validates due date
- ✅ Get all projects returns list
- ✅ Get all projects returns empty when none
- ✅ Get project by ID successfully
- ✅ Get project by ID throws NotFoundException
- ✅ Update project successfully
- ✅ Update project throws NotFoundException
- ✅ Update project validates data
- ✅ Delete project successfully
- ✅ Delete project throws NotFoundException
- ✅ Get projects by status filters correctly
- ✅ Get projects by priority filters correctly
- ✅ Get projects by owner filters correctly
- ✅ Get overdue projects only returns overdue
- ✅ Get active projects excludes completed/cancelled
- ✅ Count projects returns correct count
- ✅ Tenant isolation enforced in all methods
- ✅ Business logic validation (status transitions)
- ✅ Concurrent update handling
- ✅ Audit trail creation
- ✅ Event publishing on create/update/delete
- ✅ Error handling for database errors
- ✅ Transaction management verification

#### 2.2 TaskService (~30 tests)
**File**: `TaskService.java`
**Methods**: Similar to ProjectService with additional task-specific logic

**Tests**:
- ✅ CRUD operations (create, read, update, delete) - 12 tests
- ✅ Filtering (by project, status, priority, overdue) - 6 tests
- ✅ Business logic (status transitions, validation) - 6 tests
- ✅ Tenant isolation - 2 tests
- ✅ Event publishing - 2 tests
- ✅ Error handling - 2 tests

#### 2.3 UserService (~20 tests)
**File**: `UserService.java`
**Methods**:
- User CRUD operations
- Authentication and authorization
- Password management
- User-tenant relationships

**Tests**:
- ✅ Create user successfully
- ✅ User authentication
- ✅ Password validation and hashing
- ✅ User role management
- ✅ User-tenant associations
- ✅ User deactivation
- ✅ Duplicate email handling
- ✅ User search and filtering
- ✅ Permission checks
- ✅ Error scenarios

#### 2.4 InvitationService (~15 tests)
**File**: `InvitationService.java`
**Methods**:
- Create invitation
- Send invitation email
- Accept invitation
- Revoke invitation
- List pending invitations

**Tests**:
- ✅ Create invitation successfully
- ✅ Send invitation email
- ✅ Accept invitation creates user
- ✅ Accept invitation assigns role
- ✅ Revoke invitation successfully
- ✅ Cannot accept expired invitation
- ✅ Cannot accept already-used invitation
- ✅ Email sending error handling
- ✅ Invitation validation
- ✅ Tenant isolation for invitations

#### 2.5 TenantService (~15 tests)
**File**: `TenantService.java`
**Methods**:
- Create tenant
- Update tenant
- Delete tenant
- Get tenant details
- Tenant configuration

**Tests**:
- ✅ Create tenant with initial admin
- ✅ Update tenant settings
- ✅ Delete tenant and all data
- ✅ Tenant isolation verification
- ✅ Tenant configuration management
- ✅ Multi-tenancy enforcement
- ✅ Error scenarios

#### 2.6 EmailService (~15 tests)
**File**: `EmailService.java`
**Methods**:
- Send invitation email
- Send notification email
- Email template rendering
- Email queue management

**Tests**:
- ✅ Send email successfully
- ✅ Email template rendering
- ✅ Handle email sending failures
- ✅ Queue management
- ✅ Retry logic
- ✅ Email validation
- ✅ Template variables replacement
- ✅ Attachment handling
- ✅ HTML vs plain text
- ✅ Error scenarios

#### 2.7 AutomationService (~30 tests)
**File**: `AutomationService.java`
**Methods**:
- Create automation rule
- Update rule
- Delete rule
- Toggle rule status
- Execute rule
- Get rule execution logs

**Tests**:
- ✅ Rule CRUD operations - 12 tests
- ✅ Rule execution logic - 10 tests
- ✅ Event matching and filtering - 4 tests
- ✅ Action execution - 4 tests

---

## 💾 3. Repository Tests (~50 tests)

**Testing Strategy**: Use `@DataJpaTest` with Testcontainers for PostgreSQL integration.

### Repositories to Test:

#### 3.1 ProjectRepository (~7 tests)
**File**: `ProjectRepository.java`
**Custom Queries**:
- `findByTenantId(UUID)` - Find projects by tenant
- `findByStatus(ProjectStatus)` - Find by status
- `findByPriority(Priority)` - Find by priority
- `findOverdue()` - Find overdue projects
- `countByTenantId(UUID)` - Count by tenant

**Tests**:
- ✅ Find projects by tenant ID
- ✅ Find projects by status
- ✅ Find projects by priority
- ✅ Find overdue projects
- ✅ Count projects by tenant
- ✅ Save and retrieve project
- ✅ Delete project cascade behavior

#### 3.2 TaskRepository (~7 tests)
**File**: `TaskRepository.java`
**Tests**: Similar to ProjectRepository

#### 3.3 UserRepository (~7 tests)
**File**: `UserRepository.java`
**Custom Queries**:
- `findByEmail(String)` - Find user by email
- `findByTenantId(UUID)` - Find users in tenant
- `existsByEmail(String)` - Check email exists

**Tests**:
- ✅ Find user by email
- ✅ Find users by tenant ID
- ✅ Check email exists
- ✅ Save and retrieve user
- ✅ Update user
- ✅ Delete user
- ✅ Unique email constraint

#### 3.4 TenantRepository (~5 tests)
**Tests**: Basic CRUD and query operations

#### 3.5 AutomationRuleRepository (~7 tests)
**Tests**: Rule CRUD, filtering, and status queries

#### 3.6 EventLogRepository (~7 tests)
**Tests**: Log creation, filtering, and cleanup

#### 3.7 UserTenantRepository (~7 tests)
**Tests**: User-tenant associations, role management

---

## 🔄 4. Integration Tests (~70 tests)

**Testing Strategy**: Use `@SpringBootTest` with Testcontainers for full-stack testing.

### Integration Test Suites:

#### 4.1 Project Management Workflow (~15 tests)
**Scenarios**:
- ✅ Create project → Add tasks → Update status → Complete
- ✅ User permissions for project operations
- ✅ Project filtering and search
- ✅ Overdue project detection
- ✅ Project deletion cascade

#### 4.2 Task Management Workflow (~15 tests)
**Scenarios**:
- ✅ Create task → Assign → Update → Complete
- ✅ Task filtering by multiple criteria
- ✅ Overdue task notifications
- ✅ Task dependencies (if implemented)

#### 4.3 User Management Workflow (~10 tests)
**Scenarios**:
- ✅ Invite user → Accept invitation → Assign role
- ✅ User permissions and access control
- ✅ Remove user from tenant

#### 4.4 Automation Workflow (~15 tests)
**Scenarios**:
- ✅ Create rule → Trigger event → Execute action
- ✅ Rule conditions evaluation
- ✅ Event logging
- ✅ Rule enable/disable

#### 4.5 Multi-Tenancy Integration (~10 tests)
**Scenarios**:
- ✅ Tenant isolation verification
- ✅ Cross-tenant access prevention
- ✅ Tenant data cleanup

#### 4.6 Security Integration (~5 tests)
**Scenarios**:
- ✅ Authentication flow
- ✅ Authorization checks
- ✅ Token management

---

## 🎨 Test Patterns and Examples

### Pattern 1: Controller Test Template

```java
@WebMvcTest(ProjectController.class)
@Import(SecurityConfig.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Test
    @WithMockUser(authorities = {"ADMINISTRATOR"})
    void createProject_Success() throws Exception {
        // Given
        Project project = new Project();
        project.setName("Test Project");
        project.setTenantId(UUID.randomUUID());

        when(projectService.createProject(any(Project.class)))
            .thenReturn(project);

        // When & Then
        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(project)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Test Project"));

        verify(projectService, times(1)).createProject(any(Project.class));
    }

    @Test
    @WithMockUser(authorities = {"VIEWER"})
    void createProject_Forbidden() throws Exception {
        // VIEWER should not be able to create projects
        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }
}
```

### Pattern 2: Service Test Template

```java
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void createProject_Success() {
        // Given
        Project project = new Project();
        project.setName("Test Project");

        when(projectRepository.save(any(Project.class)))
            .thenReturn(project);

        // When
        Project created = projectService.createProject(project);

        // Then
        assertNotNull(created);
        assertEquals("Test Project", created.getName());
        verify(projectRepository, times(1)).save(project);
    }

    @Test
    void getProjectById_NotFound_ThrowsException() {
        // Given
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> {
            projectService.getProjectById(projectId);
        });
    }
}
```

### Pattern 3: Repository Test Template

```java
@DataJpaTest
@Testcontainers
class ProjectRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void findByTenantId_ReturnsProjects() {
        // Given
        UUID tenantId = UUID.randomUUID();
        Project project1 = new Project();
        project1.setTenantId(tenantId);
        project1.setName("Project 1");
        projectRepository.save(project1);

        // When
        List<Project> projects = projectRepository.findByTenantId(tenantId);

        // Then
        assertEquals(1, projects.size());
        assertEquals("Project 1", projects.get(0).getName());
    }
}
```

### Pattern 4: Integration Test Template

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProjectManagementIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void completeProjectWorkflow() {
        // 1. Create project
        Project project = new Project();
        project.setName("Integration Test Project");

        ResponseEntity<Project> createResponse =
            restTemplate.postForEntity("/api/projects", project, Project.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        UUID projectId = createResponse.getBody().getId();

        // 2. Get project
        ResponseEntity<Project> getResponse =
            restTemplate.getForEntity("/api/projects/" + projectId, Project.class);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());

        // 3. Update project
        project.setStatus(ProjectStatus.IN_PROGRESS);
        restTemplate.put("/api/projects/" + projectId, project);

        // 4. Verify update
        ResponseEntity<Project> updatedResponse =
            restTemplate.getForEntity("/api/projects/" + projectId, Project.class);

        assertEquals(ProjectStatus.IN_PROGRESS, updatedResponse.getBody().getStatus());
    }
}
```

---

## 📊 Coverage Targets

**JaCoCo Configuration** (from pom.xml):
- **Line Coverage**: 80% minimum (configured)
- **Branch Coverage**: 75% target (recommended)
- **Method Coverage**: 80% target (recommended)

**Component-Specific Targets**:

| Component Type | Line Coverage | Branch Coverage | Method Coverage |
|---------------|---------------|-----------------|-----------------|
| **Controllers** | 85%+ | 80%+ | 90%+ |
| **Services** | 85%+ | 75%+ | 85%+ |
| **Repositories** | 80%+ | 70%+ | 80%+ |
| **Integration** | N/A (measures overall) | N/A | N/A |

---

## ⏱️ Implementation Timeline

### Week 1: Controllers (HIGH Priority)
- **Day 1-2**: ProjectController + TaskController (~24 tests)
- **Day 3**: AutomationController (~10 tests)
- **Day 4**: UserController + TenantController (~18 tests)
- **Day 5**: AuthController + InternalApiController (~11 tests)
- **Deliverable**: ~80 controller tests passing

### Week 2: Services (HIGH Priority)
- **Day 1-2**: ProjectService + TaskService (~55 tests)
- **Day 3**: UserService + InvitationService (~35 tests)
- **Day 4**: TenantService + EmailService (~30 tests)
- **Day 5**: AutomationService (~30 tests)
- **Deliverable**: ~150 service tests passing

### Week 3: Repositories + Integration (MEDIUM Priority)
- **Day 1-2**: All Repository tests (~50 tests)
- **Day 3-4**: Integration test suites (~70 tests)
- **Day 5**: Coverage verification and gap filling
- **Deliverable**: All backend tests complete, 80%+ coverage

---

## 🎯 Success Criteria

### Test Quality:
- ✅ All tests passing (100% pass rate)
- ✅ Fast execution (<5 minutes for full suite)
- ✅ No flaky tests
- ✅ Clear test descriptions
- ✅ PMAT-compliant test code (Cyc≤10, Cog≤15)

### Coverage:
- ✅ Overall line coverage ≥80% (JaCoCo enforced)
- ✅ Controllers ≥85% coverage
- ✅ Services ≥85% coverage
- ✅ Repositories ≥80% coverage

### Documentation:
- ✅ All test files have PMAT comments
- ✅ Complex test logic documented
- ✅ Test patterns established and reusable
- ✅ Coverage reports generated

---

## 🚀 Getting Started

### Run Tests:
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ProjectControllerTest

# Run with coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Test Configuration:

**`application-test.properties`** (create if not exists):
```properties
spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver
spring.datasource.url=jdbc:tc:postgresql:15-alpine:///testdb
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Disable email sending in tests
mail.enabled=false
```

---

## 📝 Notes

### Best Practices:

1. **Isolation**: Each test should be independent
2. **Mocking**: Mock external dependencies (DB, email, etc.)
3. **Naming**: Use descriptive test method names (Given_When_Then pattern)
4. **Arrange-Act-Assert**: Structure all tests consistently
5. **Coverage**: Aim for high coverage but focus on meaningful tests
6. **Speed**: Keep unit tests fast (<100ms each), integration tests reasonable

### Common Pitfalls to Avoid:

1. ❌ Don't test Spring framework code
2. ❌ Don't test getters/setters unless they contain logic
3. ❌ Don't use real database in unit tests
4. ❌ Don't create brittle tests tied to implementation details
5. ❌ Don't ignore test failures or make tests "eventually pass"

---

**Last Updated**: 2025-10-27
**Status**: Ready to begin implementation
**Next Step**: Create ProjectControllerTest.java
