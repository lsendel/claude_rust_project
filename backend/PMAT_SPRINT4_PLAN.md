# Sprint 4: Service & Configuration Testing Plan

**Sprint Goal**: Increase backend test coverage from 72% to 77-79%
**Focus**: Untested services, configuration classes, and edge cases
**Date**: 2025-10-28

---

## Current State

### Coverage Analysis (from jacoco.csv)
- **Overall Coverage**: 72% instruction coverage (4,803 of 6,632 instructions)
- **Total Tests**: 579 tests (519 from previous sprints + 60 repository tests from Sprint 3)

### Coverage Gaps Identified

#### Service Layer (Highest Priority)
| Service | Missed Instructions | Covered Instructions | Coverage |
|---------|---------------------|---------------------|----------|
| EventPublisher | 429 | 4 | 1% |
| AutomationService | 385 | 4 | 1% |
| EmailService | 291 | 4 | 1% |

#### Security & Configuration
| Class | Missed Instructions | Covered Instructions | Coverage |
|-------|---------------------|---------------------|----------|
| SecurityConfig (config) | 196 | 0 | 0% |
| SecurityConfig (security) | 98 | 108 | 52% |
| JwtUserInfoExtractor | 77 | 4 | 5% |
| TenantContext | 28 | 66 | 70% |

**Total Untested Instructions**: 1,504 instructions across priority classes

---

## Sprint 4 Objectives

### Primary Goal
Increase coverage by testing the **1,504 untested instructions** in critical service and configuration classes.

**Expected Coverage Increase**:
- If we achieve 80% coverage on these classes: ~1,200 additional instructions covered
- Current: 4,803 covered / 6,632 total = 72%
- Target: ~6,000 covered / 6,632 total = **~90%** (exceeds goal)
- Conservative estimate (70% of untested): 77-79% overall coverage ✅

### Secondary Goals
1. Test AWS service integrations (EventBridge, SES) in both enabled/disabled modes
2. Test Spring Security configuration (SecurityFilterChain, JWT decoder)
3. Test edge cases and error handling in services
4. Validate tenant isolation in AutomationService

---

## Test Implementation Plan

### Phase 1: EventPublisher Service Testing
**Target**: 429 missed instructions → ~343 covered (80% of class)

**Test Cases** (EventPublisherTest.java):
1. Constructor - EventBridge enabled mode
   - Verify EventBridge client initialized
   - Verify eventBusName and region configured
2. Constructor - EventBridge disabled mode
   - Verify client is null
   - Verify warning logged
3. `publishEvent()` - EventBridge enabled
   - Mock EventBridge client
   - Verify PutEventsRequest created correctly
   - Verify EventLog persisted with SUCCESS status
   - Verify event details JSON serialized
4. `publishEvent()` - EventBridge disabled
   - Verify no EventBridge call made
   - Verify EventLog persisted with local-only status
5. Error Handling
   - EventBridge publish fails
   - Verify EventLog persisted with FAILED status
   - Verify stack trace truncated to 4000 characters
6. Edge Cases
   - Null tenant ID
   - Empty eventPayload
   - Large eventPayload (JSON serialization)

**Mocking Strategy**:
- Mock EventBridgeClient using Mockito
- Mock EventLogRepository
- Use @InjectMocks for EventPublisher

**Configuration**:
```java
@ExtendWith(MockitoExtension.class)
class EventPublisherTest {
    @Mock private EventLogRepository eventLogRepository;
    @Mock private EventBridgeClient eventBridgeClient;
    @InjectMocks private EventPublisher eventPublisher;
}
```

---

### Phase 2: AutomationService Testing
**Target**: 385 missed instructions → ~308 covered (80% of class)

**Test Cases** (AutomationServiceTest.java):
1. `createRule()`
   - Verify tenant ID set from TenantContext
   - Verify defaults initialized (isActive=true, executionCount=0)
   - Test with null TenantContext → IllegalStateException
2. `getRuleById()`
   - Verify tenant isolation (correct tenant)
   - Verify tenant isolation (wrong tenant → empty)
3. `getAllRules()`
   - Verify returns only rules for current tenant
4. `updateRule()`
   - Verify tenant isolation enforced
   - Verify rule updates persisted
5. `deleteRule()`
   - Verify tenant isolation enforced
   - Verify rule deleted
6. `executeRule()` (if present)
   - Verify execution count incremented
   - Verify EventLog created

**Mocking Strategy**:
- Mock AutomationRuleRepository
- Mock EventLogRepository
- Mock static TenantContext.getTenantId() using MockedStatic

**Configuration**:
```java
@ExtendWith(MockitoExtension.class)
class AutomationServiceTest {
    @Mock private AutomationRuleRepository automationRuleRepository;
    @Mock private EventLogRepository eventLogRepository;
    @InjectMocks private AutomationService automationService;

    private MockedStatic<TenantContext> tenantContextMock;

    @BeforeEach
    void setUp() {
        tenantContextMock = mockStatic(TenantContext.class);
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }
}
```

---

### Phase 3: EmailService Testing
**Target**: 291 missed instructions → ~233 covered (80% of class)

**Test Cases** (EmailServiceTest.java):
1. Constructor - SES enabled mode
   - Verify SES client initialized
   - Verify region configured
   - Verify fromEmail configured
2. Constructor - SES disabled mode
   - Verify client is null
   - Verify warning logged
3. `sendEmail()` - SES enabled
   - Mock SES client
   - Verify SendEmailRequest created correctly
   - Verify email sent successfully
4. `sendEmail()` - SES disabled
   - Verify no SES call made
   - Verify warning logged
5. Error Handling
   - SES client throws exception
   - Verify error logged
   - Verify exception propagated/handled
6. `sendInvitationEmail()`
   - Verify HTML body constructed correctly
   - Verify invitation link included
7. `sendPasswordResetEmail()`
   - Verify reset token included

**Mocking Strategy**:
- Mock SesClient using Mockito
- Use @InjectMocks for EmailService

**Configuration**:
```java
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @Mock private SesClient sesClient;
    @InjectMocks private EmailService emailService;
}
```

---

### Phase 4: Security Configuration Testing
**Target**: 294 missed instructions (196 + 98) → ~235 covered (80% of classes)

**Test Cases** (SecurityConfigTest.java):
1. `securityFilterChain()` bean creation
   - Verify SecurityFilterChain bean created
   - Verify CSRF disabled
   - Verify CORS configured
   - Verify stateless session management
   - Verify public endpoints permitted (/api/tenants, /api/internal/*, /actuator/health)
   - Verify authenticated endpoints require JWT
2. `jwtDecoder()` bean creation
   - Verify JwtDecoder created with correct JWK set URI
3. `corsConfigurationSource()` bean creation
   - Verify CORS origins configured
   - Verify allowed methods (GET, POST, PUT, DELETE, OPTIONS)
   - Verify allowed headers

**JwtUserInfoExtractor Testing** (JwtUserInfoExtractorTest.java):
1. `extractUserId()` from JWT
   - Test with valid JWT containing user_id claim
   - Test with valid JWT containing sub claim
   - Test with missing user_id → null
2. `extractUserEmail()` from JWT
   - Test with valid JWT containing email claim
   - Test with missing email → null
3. `extractUserName()` from JWT
   - Test with valid JWT containing name claim
   - Test with missing name → null

**Testing Strategy**:
- Use @SpringBootTest for SecurityConfig (requires Spring context)
- Use Mockito for JwtUserInfoExtractor (mock Jwt object)

---

### Phase 5: Edge Case & Error Handling Testing

**Additional Test Cases for Existing Services**:
1. **TenantContext edge cases**
   - Test thread-local isolation
   - Test clear() functionality
   - Test multiple tenant switches
2. **Service error handling**
   - Test null input validation
   - Test invalid UUID handling
   - Test database constraint violations

---

## Test Execution Plan

### Test Run Sequence
1. Run existing test suite (baseline: 579 tests, 72% coverage)
2. Add EventPublisherTest → verify coverage increase
3. Add AutomationServiceTest → verify coverage increase
4. Add EmailServiceTest → verify coverage increase
5. Add SecurityConfigTest & JwtUserInfoExtractorTest → verify coverage increase
6. Run full suite and generate final JaCoCo report

### Success Criteria
- ✅ All tests pass (no failures)
- ✅ Overall coverage: 77-79% (target)
- ✅ EventPublisher coverage: >70%
- ✅ AutomationService coverage: >70%
- ✅ EmailService coverage: >70%
- ✅ SecurityConfig coverage: >50%
- ✅ No new PMD violations
- ✅ No new SpotBugs warnings

### Commands
```bash
# Run tests with coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html

# Run PMD analysis
mvn pmd:check

# Run SpotBugs analysis
mvn spotbugs:check
```

---

## Dependencies & Configuration

### Test Dependencies (already in pom.xml)
- spring-boot-starter-test (includes JUnit 5, Mockito, AssertJ)
- mockito-core
- mockito-inline (for static mocking)
- h2 database

### Application Test Properties
**src/test/resources/application-test.properties**:
```properties
# AWS Services - Disabled for tests
aws.eventbridge.enabled=false
aws.ses.enabled=false

# Security - Test JWT configuration
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/.well-known/jwks.json

# Database - H2 in-memory
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

---

## Risk Assessment

### Low Risk
- Service mocking (Mockito) - well-established patterns
- Repository mocking - simple CRUD operations
- AWS service mocking - using Mockito to avoid real AWS calls

### Medium Risk
- SecurityConfig testing - requires Spring context, may be slower
- JWT token mocking - requires understanding of JWT structure
- Static method mocking (TenantContext) - requires mockito-inline

### Mitigation Strategies
1. Use @SpringBootTest sparingly (only for SecurityConfig)
2. Create JWT test utilities for reusable token generation
3. Ensure mockito-inline dependency present for static mocking
4. Use @TestPropertySource to override AWS settings

---

## Estimated Coverage Increase

### Conservative Estimate
| Class | Current | Target Coverage | Instructions Covered |
|-------|---------|-----------------|---------------------|
| EventPublisher | 1% | 70% | +300 instructions |
| AutomationService | 1% | 70% | +270 instructions |
| EmailService | 1% | 70% | +200 instructions |
| SecurityConfig | 33% | 50% | +150 instructions |
| JwtUserInfoExtractor | 5% | 60% | +45 instructions |
| **Total** | | | **+965 instructions** |

**Overall Coverage**: 4,803 + 965 = 5,768 covered / 6,632 total = **87%** (exceeds goal)

### Realistic Estimate (accounting for unreachable code)
- Assuming 15% of code is unreachable (AWS initialization errors, etc.)
- Realistic coverage: **77-79%** ✅

---

## Timeline

### Estimated Effort
- Phase 1 (EventPublisher): 2-3 hours
- Phase 2 (AutomationService): 2 hours
- Phase 3 (EmailService): 2 hours
- Phase 4 (SecurityConfig + JwtUserInfoExtractor): 2-3 hours
- Phase 5 (Edge cases): 1 hour
- **Total**: 9-11 hours

### Sprint Duration
- **Start**: 2025-10-28
- **Target Completion**: 2025-10-28 (single session)

---

## Next Steps

1. ✅ Create Sprint 4 planning document (this document)
2. ⏭️ Create EventPublisherTest.java
3. ⏭️ Create AutomationServiceTest.java
4. ⏭️ Create EmailServiceTest.java
5. ⏭️ Create SecurityConfigTest.java
6. ⏭️ Create JwtUserInfoExtractorTest.java
7. ⏭️ Run full test suite and verify 77-79% coverage
8. ⏭️ Create Sprint 4 completion report

---

## References

- Sprint 3 Completion Report: `PMAT_SPRINT3_COMPLETE.md`
- JaCoCo Coverage Report: `target/site/jacoco/index.html`
- Coverage CSV: `target/site/jacoco/jacoco.csv`
