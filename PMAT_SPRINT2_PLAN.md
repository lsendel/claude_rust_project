# PMAT Sprint 2 Planning Document
**Multi-Tenant SaaS Platform - Model & Repository Layer Testing**

**Date**: October 28, 2025
**Sprint Goal**: Achieve 75%+ backend test coverage through comprehensive model and repository testing
**Status**: 📋 Planning Complete - Ready to Execute

---

## Sprint 2 Overview

**Duration**: Week 2 of 8-week PMAT improvement roadmap
**Focus**: Model entities, JPA relationships, and repository layer
**Current Coverage**: 66%
**Target Coverage**: 75%+
**Estimated Effort**: 18-20 hours

### Sprint Context

**Sprint 1 Results** (completed):
- 66% backend coverage achieved (target: 65%)
- Security layer: 67% coverage
- Service layer: 70% coverage
- **Gap identified**: Model layer at 29%, Repository layer untested

**Sprint 2 Mission**: Close the model/repository testing gap to achieve 75%+ overall coverage

---

## Current State Analysis

### Coverage by Package (Post-Sprint 1)

| Package | Coverage | Lines | Priority |
|---------|----------|-------|----------|
| **com.platform.saas.service** | 70% | 746 | ✅ Sprint 1 |
| **com.platform.saas.security** | 67% | 167 | ✅ Sprint 1 |
| **com.platform.saas.controller** | 97% | 264 | ✅ Excellent |
| **com.platform.saas.dto** | 100% | 23 | ✅ Perfect |
| **com.platform.saas.exception** | 100% | 19 | ✅ Perfect |
| **com.platform.saas.model** | **29%** | 139 | 🎯 **Sprint 2** |
| **com.platform.saas.repository** | **0%** | - | 🎯 **Sprint 2** |
| **com.platform.saas.config** | 0% | 25 | ⏳ Sprint 3 |

### Model Layer Inventory

**17 Model Classes** identified:

**Core Entities** (CRITICAL):
1. `User.java` - User accounts and authentication
2. `Tenant.java` - Multi-tenant organization
3. `UserTenant.java` - User-tenant membership (composite key)
4. `UserTenantId.java` - Composite key for UserTenant

**Business Entities** (HIGH):
5. `Project.java` - Project management
6. `Task.java` - Task tracking
7. `TaskAssignee.java` - Task assignment (composite key)
8. `TaskAssigneeId.java` - Composite key for TaskAssignee
9. `TaskDependency.java` - Task dependencies (composite key)
10. `TaskDependencyId.java` - Composite key for TaskDependency

**Supporting Entities** (MEDIUM):
11. `AutomationRule.java` - Business rules
12. `EventLog.java` - Audit trail

**Enums** (LOW - but important for validation):
13. `UserRole.java` - ADMINISTRATOR, EDITOR, VIEWER
14. `SubscriptionTier.java` - FREE, STANDARD, PREMIUM, ENTERPRISE
15. `Priority.java` - LOW, MEDIUM, HIGH, CRITICAL
16. `TaskStatus.java` - TODO, IN_PROGRESS, DONE, CANCELLED
17. `ProjectStatus.java` - ACTIVE, ARCHIVED, DELETED

### Repository Layer Inventory

**7 Repository Interfaces** identified:

**Core Repositories** (CRITICAL):
1. `UserRepository` - User data access
2. `TenantRepository` - Tenant data access
3. `UserTenantRepository` - Membership data access

**Business Repositories** (HIGH):
4. `ProjectRepository` - Project data access
5. `TaskRepository` - Task data access

**Supporting Repositories** (MEDIUM):
6. `AutomationRuleRepository` - Automation data access
7. `EventLogRepository` - Audit log data access

---

## Sprint 2 Strategy

### Testing Approach

**Model Testing Focus**:
1. **Entity Validation** - JSR-303 constraints (@NotNull, @Size, @Email, etc.)
2. **Builder Patterns** - Lombok @Builder functionality
3. **Equals/HashCode** - Entity comparison (especially for composite keys)
4. **Relationships** - @OneToMany, @ManyToOne, @ManyToMany
5. **Lifecycle Callbacks** - @PrePersist, @PreUpdate hooks
6. **Business Logic** - Any validation methods in entities
7. **JSON Serialization** - Jackson annotations

**Repository Testing Focus**:
1. **Spring Data Methods** - findById, findAll, save, delete
2. **Custom Queries** - @Query annotations
3. **Method Naming** - findByXXX derived queries
4. **Transactions** - @Transactional behavior
5. **Projections** - DTO projections
6. **Pagination** - Pageable support
7. **Multi-tenancy** - Tenant-specific queries

### Test Types

**Unit Tests** (Primary):
- Fast execution
- No database required
- Mock repositories
- Focus on validation logic

**Integration Tests** (Secondary - if time allows):
- @DataJpaTest for repositories
- In-memory H2 database
- Test custom queries
- Verify JPA relationships

---

## Sprint 2 Tasks

### Phase 1: Core Entity Tests (6-7 hours)

#### Task 1: User Entity Tests (2 hours)
**File**: `src/test/java/com/platform/saas/model/UserTest.java`
**Priority**: CRITICAL
**Coverage Target**: 80%+

**Test Scenarios**:
- ✅ Valid user creation with builder
- ✅ Email validation (@Email, @NotNull)
- ✅ Name validation (@NotNull, @Size)
- ✅ cognitoUserId validation
- ✅ lastLogin timestamp handling
- ✅ createdAt/updatedAt lifecycle
- ✅ equals/hashCode (UUID-based)
- ✅ toString output
- ✅ UserTenant relationship (one-to-many)

**Validation Rules to Test**:
```java
@Email(message = "Email must be valid")
@NotNull(message = "Email is required")
@Size(max = 255)
@Size(max = 100, message = "Name cannot exceed 100 characters")
```

#### Task 2: Tenant Entity Tests (2 hours)
**File**: `src/test/java/com/platform/saas/model/TenantTest.java`
**Priority**: CRITICAL
**Coverage Target**: 80%+

**Test Scenarios**:
- ✅ Valid tenant creation with builder
- ✅ Subdomain validation (@NotNull, @Pattern)
- ✅ Company name validation (@NotNull, @Size)
- ✅ Subscription tier handling
- ✅ Active/inactive status
- ✅ Project quota management
- ✅ createdAt lifecycle
- ✅ equals/hashCode (UUID-based)
- ✅ UserTenant relationship (one-to-many)
- ✅ Project relationship (one-to-many)

**Validation Rules to Test**:
```java
@Pattern(regexp = "^[a-z0-9-]+$", message = "Subdomain must be lowercase alphanumeric with hyphens")
@Size(min = 3, max = 50)
@NotNull(message = "Company name is required")
@Size(max = 100)
```

#### Task 3: UserTenant Entity Tests (2 hours)
**File**: `src/test/java/com/platform/saas/model/UserTenantTest.java`
**Priority**: CRITICAL
**Coverage Target**: 80%+

**Test Scenarios**:
- ✅ Valid membership creation with builder
- ✅ Composite key (UserTenantId) validation
- ✅ User relationship (@ManyToOne)
- ✅ Tenant relationship (@ManyToOne)
- ✅ Role validation (@NotNull, UserRole enum)
- ✅ invitedBy tracking
- ✅ joinedAt timestamp
- ✅ equals/hashCode (composite key)
- ✅ Unique constraint (user + tenant)

**Composite Key Test**:
```java
@EmbeddedId
private UserTenantId id;

// Test composite key equality
// Test composite key hashCode
// Test null handling
```

#### Task 4: UserTenantId Tests (1 hour)
**File**: `src/test/java/com/platform/saas/model/UserTenantIdTest.java`
**Priority**: HIGH
**Coverage Target**: 100% (simple class)

**Test Scenarios**:
- ✅ Composite key creation
- ✅ equals/hashCode for composite key
- ✅ Serializable compliance
- ✅ Null handling

---

### Phase 2: Business Entity Tests (4-5 hours)

#### Task 5: Project Entity Tests (2 hours)
**File**: `src/test/java/com/platform/saas/model/ProjectTest.java`
**Priority**: HIGH
**Coverage Target**: 75%+

**Test Scenarios**:
- ✅ Valid project creation with builder
- ✅ Name validation (@NotNull, @Size)
- ✅ Tenant relationship (@ManyToOne, @NotNull)
- ✅ Status transitions (ACTIVE → ARCHIVED → DELETED)
- ✅ Task relationship (one-to-many)
- ✅ createdAt/updatedAt lifecycle
- ✅ equals/hashCode (UUID-based)

#### Task 6: Task Entity Tests (2-3 hours)
**File**: `src/test/java/com/platform/saas/model/TaskTest.java`
**Priority**: HIGH
**Coverage Target**: 75%+

**Test Scenarios**:
- ✅ Valid task creation with builder
- ✅ Title validation (@NotNull, @Size)
- ✅ Project relationship (@ManyToOne, @NotNull)
- ✅ Status validation (@NotNull, TaskStatus enum)
- ✅ Priority validation (@NotNull, Priority enum)
- ✅ dueDate handling (optional)
- ✅ TaskAssignee relationship (one-to-many)
- ✅ TaskDependency relationships
- ✅ createdAt/updatedAt lifecycle

---

### Phase 3: Repository Tests (6-7 hours)

#### Task 7: UserRepository Tests (2 hours)
**File**: `src/test/java/com/platform/saas/repository/UserRepositoryTest.java`
**Priority**: CRITICAL
**Coverage Target**: 70%+

**Test Scenarios**:
- ✅ findById (standard Spring Data)
- ✅ findByEmail (custom query)
- ✅ findByCognitoUserId (custom query)
- ✅ existsByEmail (exists query)
- ✅ existsByCognitoUserId (exists query)
- ✅ save (create and update)
- ✅ delete
- ✅ Email uniqueness constraint
- ✅ Case-insensitive email queries

**Custom Queries to Test**:
```java
Optional<User> findByEmail(String email);
Optional<User> findByCognitoUserId(String cognitoUserId);
boolean existsByEmail(String email);
boolean existsByCognitoUserId(String cognitoUserId);
```

#### Task 8: TenantRepository Tests (2 hours)
**File**: `src/test/java/com/platform/saas/repository/TenantRepositoryTest.java`
**Priority**: CRITICAL
**Coverage Target**: 70%+

**Test Scenarios**:
- ✅ findById (standard)
- ✅ findBySubdomain (custom query)
- ✅ existsBySubdomain (exists query)
- ✅ findAllActive (custom query)
- ✅ save (create and update)
- ✅ Subdomain uniqueness constraint
- ✅ Case-insensitive subdomain queries

**Custom Queries to Test**:
```java
Optional<Tenant> findBySubdomain(String subdomain);
boolean existsBySubdomain(String subdomain);
List<Tenant> findAllByActiveTrue();
```

#### Task 9: UserTenantRepository Tests (2 hours)
**File**: `src/test/java/com/platform/saas/repository/UserTenantRepositoryTest.java`
**Priority**: CRITICAL
**Coverage Target**: 70%+

**Test Scenarios**:
- ✅ findById (composite key)
- ✅ findByUserId (user's tenants)
- ✅ findByTenantId (tenant's users)
- ✅ findByUserIdAndTenantId (membership check)
- ✅ existsByUserIdAndTenantId (exists check)
- ✅ findByTenantIdAndRole (role filtering)
- ✅ Unique constraint (user + tenant)
- ✅ Composite key handling

**Custom Queries to Test**:
```java
List<UserTenant> findByUserId(UUID userId);
List<UserTenant> findByTenantId(UUID tenantId);
Optional<UserTenant> findByUserIdAndTenantId(UUID userId, UUID tenantId);
boolean existsByUserIdAndTenantId(UUID userId, UUID tenantId);
List<UserTenant> findByTenantIdAndRole(UUID tenantId, UserRole role);
```

#### Task 10: ProjectRepository Tests (1 hour) - If time allows
**File**: `src/test/java/com/platform/saas/repository/ProjectRepositoryTest.java`
**Priority**: MEDIUM

**Test Scenarios**:
- ✅ findById (standard)
- ✅ findByTenantId (tenant's projects)
- ✅ findByTenantIdAndStatus (filtered projects)
- ✅ countByTenantId (project count for quota)

---

### Phase 4: Validation & Reporting (1-2 hours)

#### Task 11: Full Test Suite Execution
- Run complete test suite
- Generate JaCoCo coverage report
- Verify 75%+ backend coverage
- Identify any gaps

#### Task 12: Sprint 2 Documentation
- Create PMAT_SPRINT2_PROGRESS.md
- Update coverage metrics
- Document achievements
- Plan Sprint 3

---

## Test Patterns & Best Practices

### Entity Test Pattern

```java
@DisplayName("Entity Tests - Business Critical Component")
class EntityTest {

    @Test
    @DisplayName("Should create valid entity with builder")
    void createValid_WithBuilder_Success() {
        // Given
        Entity entity = Entity.builder()
            .field1("value1")
            .field2("value2")
            .build();

        // Then
        assertThat(entity.getField1()).isEqualTo("value1");
        assertThat(entity.getField2()).isEqualTo("value2");
    }

    @Test
    @DisplayName("Should validate required fields")
    void validate_RequiredFields_ThrowsException() {
        // Use Hibernate Validator for JSR-303 testing
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Entity entity = Entity.builder().build();

        Set<ConstraintViolation<Entity>> violations = validator.validate(entity);

        assertThat(violations).isNotEmpty();
    }
}
```

### Repository Test Pattern

```java
@DataJpaTest
@DisplayName("Repository Tests - Data Access Critical")
class RepositoryTest {

    @Autowired
    private EntityRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should find entity by custom query")
    void findByCustomField_ExistingEntity_Found() {
        // Given
        Entity entity = createTestEntity();
        entityManager.persist(entity);
        entityManager.flush();

        // When
        Optional<Entity> found = repository.findByCustomField("value");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCustomField()).isEqualTo("value");
    }
}
```

---

## Expected Outcomes

### Coverage Targets

| Component | Before Sprint 2 | After Sprint 2 | Improvement |
|-----------|-----------------|----------------|-------------|
| **User Entity** | 29% | 80%+ | +51% |
| **Tenant Entity** | 29% | 80%+ | +51% |
| **UserTenant Entity** | 29% | 80%+ | +51% |
| **Project Entity** | 29% | 75%+ | +46% |
| **Task Entity** | 29% | 75%+ | +46% |
| **Repositories** | 0% | 70%+ | +70% |
| **Overall Backend** | 66% | **75%+** | **+9%** |

### Test Count Projection

| Category | Sprint 1 | Sprint 2 | Total |
|----------|----------|----------|-------|
| Security Tests | 58 | 0 | 58 |
| Service Tests | 90 | 0 | 90 |
| Controller Tests | 128 | 0 | 128 |
| **Model Tests** | 0 | **60-80** | **60-80** |
| **Repository Tests** | 0 | **30-40** | **30-40** |
| **Total** | 276 | **90-120** | **366-396** |

### PMAT Score Impact

| Metric | Sprint 1 | Sprint 2 (Projected) | Sprint 8 (Goal) |
|--------|----------|----------------------|-----------------|
| Backend Coverage | 66% | 75%+ | 90%+ |
| Testing Score | 76/100 | 80/100 | 90/100 |
| Overall PMAT | 82/100 | 84/100 | 90/100 |

---

## Risk Assessment

### Potential Challenges

1. **Composite Key Complexity**
   - Risk: UserTenantId, TaskAssigneeId testing
   - Mitigation: Focus on equals/hashCode thoroughly

2. **JPA Relationship Testing**
   - Risk: Lazy loading, cascade operations
   - Mitigation: Use @DataJpaTest for repository tests

3. **Validation Framework**
   - Risk: JSR-303 constraint testing
   - Mitigation: Use Hibernate Validator in tests

4. **Time Constraints**
   - Risk: 20 hours may not cover all entities
   - Mitigation: Prioritize core entities (User, Tenant, UserTenant)

### Contingency Plan

If time runs short:
1. **Must Complete**: User, Tenant, UserTenant entities + their repositories
2. **Should Complete**: Project, Task entities
3. **Nice to Have**: AutomationRule, EventLog, TaskAssignee/Dependency
4. **Can Defer**: ProjectRepository, TaskRepository detailed tests (basic coverage only)

---

## Success Criteria

Sprint 2 is considered successful if:

✅ **75%+ backend coverage achieved**
✅ **Core entities (User, Tenant, UserTenant) at 80%+ coverage**
✅ **Core repositories (User, Tenant, UserTenant) at 70%+ coverage**
✅ **All tests passing (0 failures)**
✅ **Composite key testing complete**
✅ **JPA relationship validation complete**
✅ **Sprint completed within 20 hours**

---

## Timeline

**Estimated Duration**: 18-20 hours
**Target Completion**: End of Week 2

**Phase Breakdown**:
- Phase 1 (Core Entities): 6-7 hours
- Phase 2 (Business Entities): 4-5 hours
- Phase 3 (Repositories): 6-7 hours
- Phase 4 (Validation): 1-2 hours

**Daily Progress** (if working 4-5 hours/day):
- Day 1: User + Tenant entity tests
- Day 2: UserTenant + UserTenantId tests
- Day 3: Project + Task entity tests
- Day 4: Repository tests (User, Tenant, UserTenant)
- Day 5: Final validation + documentation

---

## Next Steps

1. ✅ Sprint 2 planning complete
2. ⏳ Start with User entity tests
3. ⏳ Progress through core entities
4. ⏳ Complete repository tests
5. ⏳ Validate 75%+ coverage
6. ⏳ Create Sprint 2 completion report

---

**Sprint 2 Status**: 📋 **PLANNING COMPLETE - READY TO EXECUTE**

**First Task**: Create UserTest.java with comprehensive entity validation tests

**Maintained by**: PMAT Improvement Team
**Created**: October 28, 2025
**Version**: 1.0.0
