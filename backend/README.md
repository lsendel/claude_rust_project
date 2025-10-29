# Multi-Tenant SaaS Platform - Backend

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Test Coverage](https://img.shields.io/badge/coverage-89%25-brightgreen)]()
[![Tests](https://img.shields.io/badge/tests-650%20passing-brightgreen)]()
[![Java](https://img.shields.io/badge/Java-21-orange)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen)]()

A production-ready, multi-tenant SaaS platform backend with enterprise-grade features including user management, project tracking, task management, automation rules, and event-driven architecture.

---

## 🎯 Project Overview

This is a **Spring Boot 3.4** backend application that provides:

- **Multi-tenant architecture** with complete data isolation
- **OAuth2/JWT authentication** with AWS Cognito integration
- **RESTful API** for project and task management
- **Event-driven automation** with AWS EventBridge
- **Email notifications** via AWS SES
- **Real-time updates** and audit logging
- **Comprehensive test coverage** (89% instruction coverage)

---

## 📊 Test Coverage

The project has **excellent test coverage** with 650 tests covering all critical paths:

```
┌─────────────────────────────────────────────────────┐
│  Test Coverage Dashboard                            │
├─────────────────────────────────────────────────────┤
│  Overall Coverage:           89%  ████████░░        │
│  Service Layer:              98%  █████████░        │
│  Controller Layer:           97%  █████████░        │
│  Security Layer:             82%  ████████░░        │
│  DTO Layer:                 100%  ██████████        │
│  Exception Layer:           100%  ██████████        │
├─────────────────────────────────────────────────────┤
│  Total Tests:                650                    │
│  Test Failures:                0  ✅                │
│  Build Time:              ~13 sec  ⚡                │
└─────────────────────────────────────────────────────┘
```

### Test Suites

| Test Suite | Tests | Coverage | Type |
|------------|-------|----------|------|
| Repository Tests | 60 | Integration | @DataJpaTest with H2 |
| Service Tests | 576 | 98% | Unit tests with Mockito |
| Security Tests | 14 | 82% | JWT/OAuth2 testing |

**Run tests:**
```bash
mvn test                      # Run all tests
mvn test jacoco:report       # Generate coverage report
open target/site/jacoco/index.html  # View coverage
```

📚 **Testing Documentation**: See [TESTING_ACHIEVEMENT_SUMMARY.md](TESTING_ACHIEVEMENT_SUMMARY.md) for comprehensive testing details.

---

## 🏗️ Architecture

### Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 3.4.0 |
| **Database** | PostgreSQL | 15+ |
| **Migrations** | Flyway | 10.21.0 |
| **Security** | Spring Security + OAuth2 | 6.4 |
| **Testing** | JUnit 5 + Mockito | 5.11.3 |
| **Coverage** | JaCoCo | 0.8.14 |
| **AWS SDK** | EventBridge, SES | 2.29.15 |
| **Build Tool** | Maven | 3.9+ |

### Package Structure

```
com.platform.saas
├── config/              # Spring configuration
│   └── SecurityConfig.java
├── controller/          # REST controllers (97% coverage)
│   ├── TenantController.java
│   ├── UserController.java
│   ├── ProjectController.java
│   ├── TaskController.java
│   └── AutomationController.java
├── model/              # JPA entities (68% coverage)
│   ├── Tenant.java
│   ├── User.java
│   ├── Project.java
│   ├── Task.java
│   └── AutomationRule.java
├── repository/         # Spring Data repositories
│   ├── TenantRepository.java
│   ├── UserRepository.java
│   ├── ProjectRepository.java
│   └── TaskRepository.java
├── service/           # Business logic (98% coverage)
│   ├── TenantService.java
│   ├── UserService.java
│   ├── ProjectService.java
│   ├── TaskService.java
│   ├── AutomationService.java
│   ├── EventPublisher.java
│   └── EmailService.java
├── security/          # Security & JWT (82% coverage)
│   ├── TenantContext.java
│   ├── TenantContextFilter.java
│   ├── JwtUserInfoExtractor.java
│   └── SecurityConfig.java
├── dto/              # Data transfer objects (100% coverage)
└── exception/        # Custom exceptions (100% coverage)
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven 3.9+**
- **PostgreSQL 15+** (or use Docker)
- **AWS Account** (optional, for EventBridge/SES)

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd backend
   ```

2. **Configure database**
   ```bash
   # Using Docker (recommended)
   docker run -d \
     --name postgres-dev \
     -e POSTGRES_DB=saas_platform \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 \
     postgres:15
   ```

3. **Configure application properties**
   ```bash
   cp src/main/resources/application.properties.example \
      src/main/resources/application.properties
   ```

   Edit `application.properties`:
   ```properties
   # Database
   spring.datasource.url=jdbc:postgresql://localhost:5432/saas_platform
   spring.datasource.username=postgres
   spring.datasource.password=postgres

   # OAuth2/JWT (AWS Cognito)
   spring.security.oauth2.resourceserver.jwt.issuer-uri=https://cognito-idp.{region}.amazonaws.com/{userPoolId}
   spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json

   # AWS Services (optional - can be disabled)
   aws.eventbridge.enabled=false
   aws.ses.enabled=false
   ```

4. **Run database migrations**
   ```bash
   mvn flyway:migrate
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

6. **Verify it's running**
   ```bash
   curl http://localhost:8080/actuator/health
   # Should return: {"status":"UP"}
   ```

---

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run tests with coverage report
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Test Types

**1. Repository Tests** (60 tests, ~2 seconds)
```bash
mvn test -Dtest="*RepositoryTest"
```
- Integration tests with H2 in-memory database
- Tests custom @Query methods
- Validates multi-tenant isolation

**2. Service Tests** (576 tests, ~8 seconds)
```bash
mvn test -Dtest="*ServiceTest"
```
- Unit tests with Mockito
- Covers business logic thoroughly
- Tests AWS service integrations (mocked)

**3. Security Tests** (14 tests, <1 second)
```bash
mvn test -Dtest="*SecurityTest"
```
- JWT token extraction
- User synchronization
- Tenant context management

### Writing New Tests

Follow the established patterns:

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("MyService Tests")
class MyServiceTest {

    @Mock
    private MyRepository repository;

    @InjectMocks
    private MyService service;

    @Test
    @DisplayName("Should do something when condition is met")
    void methodName_scenario_expectedOutcome() {
        // Given (Arrange)
        MyEntity entity = new MyEntity();
        when(repository.findById(any())).thenReturn(Optional.of(entity));

        // When (Act)
        MyEntity result = service.doSomething(id);

        // Then (Assert)
        assertThat(result).isNotNull();
        verify(repository).findById(id);
    }
}
```

📚 **Testing Guide**: See [PMAT_SPRINT4_COMPLETE.md](PMAT_SPRINT4_COMPLETE.md) for testing patterns and best practices.

---

## 🔐 Security

### Multi-Tenant Architecture

The application enforces **strict tenant isolation** at multiple layers:

1. **TenantContextFilter**: Extracts tenant ID from JWT token or request header
2. **TenantContext**: Thread-local storage for current tenant ID
3. **Repository Layer**: All queries filtered by tenant ID
4. **Service Layer**: Validates tenant access for all operations

```java
@Service
public class MyService {
    public Entity getEntity(UUID entityId) {
        UUID tenantId = TenantContext.getTenantId();
        // Automatically filtered by tenant ID
        return repository.findByIdAndTenantId(entityId, tenantId)
            .orElseThrow(() -> new EntityNotFoundException());
    }
}
```

### Authentication

- **OAuth2/JWT** with AWS Cognito
- **Stateless** session management
- **Automatic user sync** from JWT claims
- **Role-based access control**

### Security Headers

```yaml
CSRF: Disabled (using JWT tokens)
CORS: Configured for frontend origins
Session: Stateless (SessionCreationPolicy.STATELESS)
JWT: RS256 algorithm with JWK validation
```

---

## 📡 API Endpoints

### Public Endpoints (No Authentication)

```
POST   /api/tenants                    # Register new tenant
GET    /api/tenants/subdomain/{name}  # Lookup tenant by subdomain
GET    /api/tenants/validate-subdomain # Validate subdomain availability
GET    /actuator/health                # Health check
```

### Authenticated Endpoints (Requires JWT)

**Tenants**
```
GET    /api/tenants/{id}              # Get tenant details
PUT    /api/tenants/{id}              # Update tenant
GET    /api/tenants/{id}/usage        # Get usage statistics
```

**Users**
```
GET    /api/users                     # List users (tenant-scoped)
GET    /api/users/{id}                # Get user details
PUT    /api/users/{id}                # Update user
POST   /api/users/invite              # Invite user to tenant
```

**Projects**
```
GET    /api/projects                  # List projects (tenant-scoped)
POST   /api/projects                  # Create project
GET    /api/projects/{id}             # Get project details
PUT    /api/projects/{id}             # Update project
DELETE /api/projects/{id}             # Delete project
```

**Tasks**
```
GET    /api/projects/{id}/tasks       # List project tasks
POST   /api/projects/{id}/tasks       # Create task
GET    /api/tasks/{id}                # Get task details
PUT    /api/tasks/{id}                # Update task
DELETE /api/tasks/{id}                # Delete task
```

**Automation**
```
GET    /api/automation/rules          # List automation rules
POST   /api/automation/rules          # Create rule
GET    /api/automation/rules/{id}     # Get rule details
PUT    /api/automation/rules/{id}     # Update rule
DELETE /api/automation/rules/{id}     # Delete rule
GET    /api/automation/logs           # View event logs
```

---

## 🔧 Configuration

### Application Properties

Key configuration properties:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/saas_platform
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Flyway Migrations
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true

# OAuth2/JWT
spring.security.oauth2.resourceserver.jwt.issuer-uri=${COGNITO_ISSUER_URI}
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${COGNITO_JWK_SET_URI}

# CORS
app.cors.allowed-origins=http://localhost:3000,https://app.example.com

# AWS EventBridge
aws.eventbridge.enabled=false
aws.eventbridge.event-bus-name=saas-platform-events
aws.eventbridge.region=us-east-1

# AWS SES
aws.ses.enabled=false
aws.ses.from-email=noreply@example.com
aws.ses.region=us-east-1

# Logging
logging.level.com.platform.saas=INFO
```

### Environment Variables

Required environment variables for production:

```bash
# Database
DATABASE_URL=jdbc:postgresql://prod-db:5432/saas_platform
DATABASE_USERNAME=prod_user
DATABASE_PASSWORD=<secure-password>

# AWS Cognito
COGNITO_ISSUER_URI=https://cognito-idp.us-east-1.amazonaws.com/<pool-id>
COGNITO_JWK_SET_URI=https://cognito-idp.us-east-1.amazonaws.com/<pool-id>/.well-known/jwks.json

# AWS Services
AWS_EVENTBRIDGE_ENABLED=true
AWS_SES_ENABLED=true
AWS_REGION=us-east-1

# CORS
CORS_ALLOWED_ORIGINS=https://app.example.com,https://www.example.com
```

---

## 📦 Build & Deployment

### Maven Build

```bash
# Clean build
mvn clean package

# Skip tests (not recommended)
mvn clean package -DskipTests

# Build with specific profile
mvn clean package -P production
```

### Docker Build

```dockerfile
FROM eclipse-temurin:21-jre-alpine
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
docker build -t saas-platform-backend .
docker run -p 8080:8080 saas-platform-backend
```

### Production Checklist

- [ ] Database migrations tested
- [ ] Environment variables configured
- [ ] AWS credentials configured (if using EventBridge/SES)
- [ ] CORS origins updated
- [ ] Health check endpoint accessible
- [ ] Logging configured (CloudWatch, etc.)
- [ ] Monitoring/alerting set up
- [ ] SSL/TLS certificates configured
- [ ] Rate limiting configured (API Gateway/load balancer)

---

## 📊 Database Schema

### Core Entities

**Tenants** - Organization accounts
```sql
tenants (id, subdomain, name, subscription_tier, created_at)
```

**Users** - User accounts (multi-tenant)
```sql
users (id, cognito_user_id, email, name, created_at)
user_tenants (user_id, tenant_id, role, joined_at)
```

**Projects** - Project management
```sql
projects (id, tenant_id, name, status, priority, owner_id, due_date)
```

**Tasks** - Task tracking
```sql
tasks (id, tenant_id, project_id, name, status, priority, assignee_id, due_date)
task_dependencies (task_id, depends_on_task_id)
```

**Automation** - Event-driven automation
```sql
automation_rules (id, tenant_id, event_type, action_type, conditions, is_active)
event_logs (id, tenant_id, event_type, status, execution_duration_ms)
```

### Migrations

Flyway migrations in `src/main/resources/db/migration/`:
```
V1__initial_schema.sql          # Core tables
V2__add_automation.sql          # Automation tables
V3__add_indexes.sql             # Performance indexes
V4__add_audit_fields.sql        # Created/updated timestamps
```

Run migrations:
```bash
mvn flyway:migrate              # Apply migrations
mvn flyway:info                 # Check migration status
mvn flyway:validate             # Validate migrations
```

---

## 🐛 Troubleshooting

### Common Issues

**1. Database connection failed**
```
Error: Connection to localhost:5432 refused
```
**Solution**: Ensure PostgreSQL is running
```bash
docker ps  # Check if postgres container is running
docker logs postgres-dev  # Check container logs
```

**2. Flyway migration failed**
```
Error: Flyway found non-empty schema without schema history table
```
**Solution**: Baseline existing database
```bash
mvn flyway:baseline
mvn flyway:migrate
```

**3. JWT validation failed**
```
Error: Invalid JWT signature
```
**Solution**: Verify JWK set URI is correct
```properties
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=<correct-uri>
```

**4. Tests failing**
```
Error: H2 database initialization failed
```
**Solution**: Check test properties
```properties
# src/test/resources/application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

---

## 📚 Documentation

### Available Documentation

| Document | Description |
|----------|-------------|
| [TESTING_ACHIEVEMENT_SUMMARY.md](TESTING_ACHIEVEMENT_SUMMARY.md) | Comprehensive testing overview |
| [PMAT_SPRINT3_COMPLETE.md](PMAT_SPRINT3_COMPLETE.md) | Repository testing report |
| [PMAT_SPRINT4_PLAN.md](PMAT_SPRINT4_PLAN.md) | Service testing plan |
| [PMAT_SPRINT4_COMPLETE.md](PMAT_SPRINT4_COMPLETE.md) | Service testing report |
| [PMAT_SPRINT4.5_SUMMARY.md](PMAT_SPRINT4.5_SUMMARY.md) | Security testing summary |
| [entity-spec.md](entity-spec.md) | Entity relationship documentation |

---

## 🤝 Contributing

### Development Workflow

1. **Create feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make changes and write tests**
   - Follow existing code patterns
   - Maintain >85% test coverage
   - Write descriptive test names

3. **Run tests**
   ```bash
   mvn clean test jacoco:report
   ```

4. **Commit changes**
   ```bash
   git add .
   git commit -m "Add feature: description"
   ```

5. **Push and create PR**
   ```bash
   git push origin feature/your-feature-name
   ```

### Code Style

- **Java**: Follow Google Java Style Guide
- **Tests**: Use AAA pattern (Arrange-Act-Assert)
- **Naming**: Descriptive test names with `@DisplayName`
- **Coverage**: Maintain >85% instruction coverage

---

## 📄 License

This project is licensed under dual license:
- Apache License 2.0
- MIT License

Choose the license that best suits your needs.

---

## 🎯 Roadmap

### Completed ✅
- [x] Multi-tenant architecture
- [x] OAuth2/JWT authentication
- [x] RESTful API
- [x] Event-driven automation
- [x] Comprehensive test suite (89% coverage)
- [x] Database migrations
- [x] AWS EventBridge integration
- [x] AWS SES integration

### Planned 🚀
- [ ] WebSocket support for real-time updates
- [ ] GraphQL API
- [ ] Rate limiting and throttling
- [ ] Audit log viewer
- [ ] Advanced reporting and analytics
- [ ] Export/import functionality
- [ ] Bulk operations API

---

## 📞 Support

For questions or issues:
- **Issues**: [GitHub Issues](https://github.com/yourorg/saas-platform/issues)
- **Documentation**: See `/docs` folder
- **Email**: support@example.com

---

**Built with ❤️ using Spring Boot 3.4**

*Last updated: 2025-10-28*
