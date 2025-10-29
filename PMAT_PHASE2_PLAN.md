# PMAT Phase 2 - Test Implementation Plan

**Date**: 2025-10-27
**Status**: 🚀 IN PROGRESS
**Priority**: 🎯 HIGH - Ensure Quality & PMAT Compliance

---

## 🎯 Phase 2 Objectives

Implement comprehensive test suites with PMAT validation to ensure:
1. ✅ 85% test coverage (backend & frontend)
2. ✅ All refactored components have tests
3. ✅ PMAT thresholds validated in tests
4. ✅ Integration tests for critical workflows
5. ✅ Regression prevention mechanisms

---

## 📋 Phase 2 Scope

### Frontend Testing (200+ tests, ~24 hours):

**Automation Components (7 components, ~50 tests)**:
- StatusBadge.tsx (4 tests)
- RuleCard.tsx (8 tests)
- EventLogRow.tsx (4 tests)
- RuleForm.tsx (12 tests)
- RuleModal.tsx (6 tests)
- EventLogTable.tsx (8 tests)
- AlertBanner.tsx (4 tests)
- AutomationPage.tsx (20 integration tests)

**Task Components (9 components, ~85 tests)**:
- StatusBadge.tsx (4 tests)
- PriorityBadge.tsx (4 tests)
- ProgressBar.tsx (4 tests)
- TaskFilters.tsx (10 tests)
- TaskRow.tsx (10 tests)
- TaskTable.tsx (8 tests)
- TaskForm.tsx (15 tests)
- TaskModal.tsx (6 tests)
- ErrorBanner.tsx (4 tests)
- TasksPage.tsx (25 integration tests)

**Settings Components (5 components, ~35 tests)**:
- RoleBadge.tsx (4 tests)
- MessageBanner.tsx (4 tests)
- InvitationForm.tsx (12 tests)
- UserRow.tsx (6 tests)
- UserTable.tsx (8 tests)
- SettingsPage.tsx (15 integration tests)

**Frontend Utilities & Hooks (~30 tests)**:
- Context providers
- Custom hooks
- Services

### Backend Testing (350+ tests, ~24 hours):

**Controllers (~80 tests)**:
- ProjectController (25 tests)
- TaskController (25 tests)
- UserController (15 tests)
- AutomationController (15 tests)

**Services (~150 tests)**:
- ProjectService (40 tests)
- TaskService (40 tests)
- UserService (30 tests)
- AutomationService (30 tests)
- EventPublisher (10 tests)

**Repositories (~50 tests)**:
- ProjectRepository (15 tests)
- TaskRepository (15 tests)
- UserRepository (10 tests)
- AutomationRuleRepository (10 tests)

**Integration Tests (~70 tests)**:
- End-to-end workflows
- Multi-tenant isolation
- Quota enforcement
- Event publishing

---

## 🛠️ Testing Framework Setup

### Frontend (Vitest + React Testing Library):

**Dependencies**:
```json
{
  "devDependencies": {
    "vitest": "^1.0.0",
    "@testing-library/react": "^14.0.0",
    "@testing-library/jest-dom": "^6.0.0",
    "@testing-library/user-event": "^14.0.0",
    "jsdom": "^23.0.0"
  }
}
```

**Configuration** (vite.config.ts):
```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      lines: 85,
      functions: 85,
      branches: 85,
      statements: 85
    }
  }
})
```

### Backend (JUnit 5 + Mockito):

**Dependencies** (pom.xml):
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 📝 Test Templates & Patterns

### Frontend Component Test Template:

```typescript
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import ComponentName from './ComponentName';

/**
 * Tests for ComponentName component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 */
describe('ComponentName', () => {
  it('renders correctly with required props', () => {
    render(<ComponentName prop="value" />);
    expect(screen.getByText('expected text')).toBeInTheDocument();
  });

  it('handles user interactions correctly', async () => {
    const handleClick = vi.fn();
    render(<ComponentName onClick={handleClick} />);

    await userEvent.click(screen.getByRole('button'));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  // PMAT validation test
  it('maintains PMAT compliance', () => {
    // Verify component complexity is within thresholds
    // This can be automated with static analysis tools
  });
});
```

### Backend Service Test Template:

```java
@ExtendWith(MockitoExtension.class)
class ServiceNameTest {

    @Mock
    private DependencyRepository repository;

    @InjectMocks
    private ServiceName service;

    /**
     * Tests for ServiceName.
     * PMAT: Ensures service methods meet thresholds (Cyc≤10, Cog≤15)
     */
    @Test
    void testMethodName_whenCondition_thenExpectedBehavior() {
        // Arrange
        when(repository.findById(any())).thenReturn(Optional.of(entity));

        // Act
        Result result = service.methodName(params);

        // Assert
        assertNotNull(result);
        verify(repository).findById(any());
    }

    @Test
    void testMethodName_pmatCompliance() {
        // Verify method complexity is within PMAT thresholds
        // Use reflection or static analysis if needed
    }
}
```

---

## 🎯 Test Coverage Targets

### Frontend Coverage Goals:

| Component Type | Target Coverage | Priority |
|----------------|-----------------|----------|
| Pages (refactored) | 75%+ | HIGH |
| Components (new) | 85%+ | HIGH |
| Services | 85%+ | MEDIUM |
| Contexts | 80%+ | MEDIUM |
| Hooks | 90%+ | HIGH |

### Backend Coverage Goals:

| Component Type | Target Coverage | Priority |
|----------------|-----------------|----------|
| Controllers | 80%+ | HIGH |
| Services | 85%+ | HIGH |
| Repositories | 90%+ | MEDIUM |
| Entities | 70%+ | LOW |
| Utils | 85%+ | MEDIUM |

---

## 📊 PMAT Test Integration

### PMAT Validation in Tests:

**1. Complexity Assertions**:
```typescript
// Frontend example
describe('PMAT Compliance', () => {
  it('component complexity is within thresholds', () => {
    // Can use static analysis tools or manual verification
    const complexity = analyzeComplexity(ComponentName);
    expect(complexity.cyclomatic).toBeLessThanOrEqual(8);
    expect(complexity.cognitive).toBeLessThanOrEqual(15);
  });
});
```

**2. Test Method Complexity**:
```java
// Backend example
@Test
void testComplexScenario_maintainsPmatCompliance() {
    // Test itself should be simple (Cyc≤5)
    // Break into multiple tests if needed
    setupTestData();
    Result result = service.complexMethod(params);
    verifyResult(result);
}

// Instead of one complex test, split into multiple simple tests
@Test void testComplexScenario_part1() { /* ... */ }
@Test void testComplexScenario_part2() { /* ... */ }
```

**3. Coverage Requirements**:
- All PMAT-compliant components must have ≥85% coverage
- Critical paths must have 100% coverage
- Edge cases must be tested

---

## 🔄 Test Execution Strategy

### Development Workflow:

**1. Unit Tests First**:
```bash
# Frontend
npm run test:unit

# Backend
mvn test -Dtest=*UnitTest
```

**2. Integration Tests**:
```bash
# Frontend
npm run test:integration

# Backend
mvn test -Dtest=*IntegrationTest
```

**3. Coverage Reports**:
```bash
# Frontend
npm run test:coverage

# Backend
mvn test jacoco:report
```

### CI/CD Integration (Phase 3):
- Run tests on every PR
- Block merge if coverage < 85%
- Generate PMAT reports
- Post results as PR comments

---

## 📋 Phase 2 Task Breakdown

### Week 1: Frontend Tests (~24 hours)

**Day 1-2: Setup & Badge Components (8 hours)**:
- ✅ Set up Vitest framework
- ✅ Configure test utilities
- ✅ Write tests for badge components (StatusBadge, PriorityBadge, RoleBadge)
- ✅ Write tests for simple components (ProgressBar, MessageBanner, AlertBanner)

**Day 3-4: Form & Table Components (8 hours)**:
- ✅ Write tests for form components (RuleForm, TaskForm, InvitationForm)
- ✅ Write tests for table components (TaskTable, UserTable, EventLogTable)
- ✅ Write tests for filter components (TaskFilters)

**Day 5: Page Integration Tests (8 hours)**:
- ✅ Write integration tests for AutomationPage
- ✅ Write integration tests for TasksPage
- ✅ Write integration tests for SettingsPage
- ✅ Verify coverage targets met

### Week 2: Backend Tests (~24 hours)

**Day 1-2: Service Tests (8 hours)**:
- ✅ Write tests for ProjectService
- ✅ Write tests for TaskService
- ✅ Write tests for UserService
- ✅ Write tests for AutomationService

**Day 3-4: Controller Tests (8 hours)**:
- ✅ Write tests for ProjectController
- ✅ Write tests for TaskController
- ✅ Write tests for UserController
- ✅ Write tests for AutomationController

**Day 5: Integration Tests (8 hours)**:
- ✅ Write end-to-end workflow tests
- ✅ Write multi-tenant isolation tests
- ✅ Write quota enforcement tests
- ✅ Verify coverage targets met

---

## ✅ Success Criteria

Phase 2 will be complete when:

**Coverage**:
- ✅ Frontend test coverage ≥ 85%
- ✅ Backend test coverage ≥ 85%
- ✅ All refactored components have tests
- ✅ All PMAT-compliant code tested

**Quality**:
- ✅ All tests pass consistently
- ✅ No flaky tests
- ✅ Fast test execution (<5 min total)
- ✅ Clear test descriptions

**PMAT Compliance**:
- ✅ Test code itself is PMAT-compliant
- ✅ Tests validate PMAT thresholds
- ✅ Regression tests prevent violations
- ✅ Coverage reports integrated

**Documentation**:
- ✅ Test README created
- ✅ Testing patterns documented
- ✅ Coverage reports generated
- ✅ Phase 2 completion report written

---

## 🚀 Getting Started

### Immediate Actions:

1. **Set up frontend testing** (30 min):
   ```bash
   cd frontend
   npm install -D vitest @testing-library/react @testing-library/jest-dom @testing-library/user-event jsdom
   ```

2. **Create test setup files** (15 min):
   - Create `src/test/setup.ts`
   - Configure `vite.config.ts`
   - Create test utilities

3. **Write first tests** (2 hours):
   - Start with StatusBadge (simplest)
   - Then PriorityBadge
   - Then RoleBadge
   - Build confidence with simple components

4. **Run tests** (5 min):
   ```bash
   npm run test
   npm run test:coverage
   ```

---

## 📈 Progress Tracking

Track progress in:
- This document (update as tests are written)
- `PMAT_INTEGRATION_PROGRESS.md` (overall progress)
- Todo list (task completion)
- Coverage reports (metrics)

---

**Plan Created**: 2025-10-27
**Status**: 🚀 READY TO START
**First Task**: Set up Vitest framework
**Estimated Duration**: 48 hours (2 weeks)
