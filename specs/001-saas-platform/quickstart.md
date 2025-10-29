# Quickstart Guide: Multi-Tenant SaaS Platform

**Feature**: Multi-Tenant SaaS Platform (Project Management)
**Date**: 2025-10-26

This guide helps developers set up the local development environment and understand the project structure.

---

## Prerequisites

- **Java 17+** (OpenJDK or Oracle JDK)
- **Node.js 18+** and npm/yarn
- **Docker Desktop** (for local PostgreSQL)
- **AWS CLI** (for Cognito configuration)
- **Git**
- **Maven 3.9+** or **Gradle 8+**

---

## Local Development Setup

### 1. Clone Repository

```bash
git clone https://github.com/your-org/saas-platform.git
cd saas-platform
git checkout 001-saas-platform
```

### 2. Start Local Database (Docker Compose)

Create `docker-compose.yml` in project root:

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: saas_platform
      POSTGRES_USER: dev_user
      POSTGRES_PASSWORD: dev_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

Start PostgreSQL:

```bash
docker-compose up -d
```

### 3. Configure AWS Cognito (Local Testing)

1. Create Cognito User Pool in AWS Console (us-east-1)
2. Configure social identity providers (Google/Facebook/GitHub)
3. Create App Client with Hosted UI enabled
4. Note down:
   - User Pool ID
   - Client ID
   - Client Secret
   - Hosted UI domain

Update `backend/src/main/resources/application-local.yml`:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          cognito:
            client-id: ${COGNITO_CLIENT_ID}
            client-secret: ${COGNITO_CLIENT_SECRET}
            scope: openid,profile,email
            redirect-uri: http://localhost:8080/oauth/callback
        provider:
          cognito:
            issuer-uri: https://cognito-idp.us-east-1.amazonaws.com/${COGNITO_USER_POOL_ID}
            user-name-attribute: sub
```

### 4. Run Backend (Spring Boot)

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Backend runs on http://localhost:8080

API Health Check: http://localhost:8080/actuator/health

### 5. Run Frontend (React + Vite)

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on http://localhost:3000

### 6. Test Authentication Flow

1. Navigate to http://localhost:3000
2. Click "Sign In"
3. Redirect to Cognito Hosted UI
4. Sign in with social provider
5. Redirect back to dashboard

---

## Project Structure

```
saas-platform/
├── backend/              # Spring Boot REST API
│   ├── src/main/java/
│   ├── src/test/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/             # React SPA
│   ├── src/
│   ├── tests/
│   ├── Dockerfile
│   └── package.json
├── lambda-functions/     # AWS Lambda functions
│   └── automation-engine/
├── infrastructure/       # Terraform/CDK
│   └── terraform/
├── specs/                # Spec-Kit documentation
│   └── 001-saas-platform/
├── docker-compose.yml    # Local development
└── README.md
```

---

## Running Tests

### Backend Tests (JUnit + Testcontainers)

```bash
cd backend
mvn test
```

Integration tests use Testcontainers to spin up PostgreSQL:

```java
@Testcontainers
@SpringBootTest
class ProjectServiceIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Test
    void createProject_shouldEnforceQuota() {
        // Test quota enforcement...
    }
}
```

### Frontend Tests (Jest + React Testing Library)

```bash
cd frontend
npm test
```

---

## Key Configuration Files

### Backend (`application.yml`)

```yaml
spring:
  application:
    name: saas-platform-api
  datasource:
    url: jdbc:postgresql://localhost:5432/saas_platform
    username: dev_user
    password: dev_password
  jpa:
    hibernate:
      ddl-auto: validate  # Use Flyway for migrations
    show-sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration
```

### Frontend (`.env.local`)

```
VITE_API_BASE_URL=http://localhost:8080/api
VITE_COGNITO_DOMAIN=https://your-cognito-domain.auth.us-east-1.amazoncognito.com
VITE_COGNITO_CLIENT_ID=your-client-id
VITE_REDIRECT_URI=http://localhost:3000/oauth/callback
```

---

## Deploying to AWS

### Prerequisites

- AWS account with appropriate IAM permissions
- Terraform installed
- AWS CLI configured

### Steps

1. **Provision Infrastructure**:

```bash
cd infrastructure/terraform
terraform init
terraform plan
terraform apply
```

2. **Build and Push Docker Images**:

```bash
# Backend
cd backend
docker build -t saas-platform-api:latest .
docker tag saas-platform-api:latest <ECR-REPO-URL>:latest
docker push <ECR-REPO-URL>:latest

# Frontend
cd frontend
npm run build
aws s3 sync dist/ s3://your-frontend-bucket/
aws cloudfront create-invalidation --distribution-id <DIST-ID> --paths "/*"
```

3. **Deploy Lambda Functions**:

```bash
cd lambda-functions/automation-engine
npm install
sam build
sam deploy --guided
```

---

## Troubleshooting

### Issue: Database connection fails

**Solution**: Ensure Docker container is running:
```bash
docker ps | grep postgres
docker logs <container-id>
```

### Issue: Cognito authentication fails

**Solution**: Verify callback URL matches in Cognito App Client settings.

### Issue: CORS errors in frontend

**Solution**: Add CORS configuration in Spring Boot:
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
```

---

## Next Steps

- ✅ Phase 0 & 1 Complete (Research, Data Model, Contracts, Quickstart)
- ⏳ Phase 2: Run `/speckit.tasks` to generate detailed task breakdown
- ⏳ Phase 3: Run `/speckit.implement` to begin implementation

---

## Resources

- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **React Docs**: https://react.dev
- **AWS Cognito Docs**: https://docs.aws.amazon.com/cognito/
- **PostgreSQL Docs**: https://www.postgresql.org/docs/
- **Testcontainers**: https://www.testcontainers.org/
