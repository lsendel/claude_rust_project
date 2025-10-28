# Multi-Tenant SaaS Platform Development Guidelines

Auto-generated from feature plans. Last updated: 2025-10-27

## Project Overview

Multi-tenant project management SaaS platform with complete data isolation, OAuth2 authentication, and event-driven automation.

## Active Features

- **001-saas-platform**: Multi-tenant SaaS platform (In Progress)
  - Phase: Implementation (User Story 1 - MVP)
  - Status: Project structure setup

## Technology Stack

### Backend
- **Language**: Java 21 LTS
- **Framework**: Spring Boot 3.5.7 (web, data-jpa, security, oauth2)
- **Database**: PostgreSQL 15 (AWS RDS)
- **Build**: Maven 3.9+
- **Testing**: JUnit 5, Mockito, Testcontainers 1.21.3
- **Code Quality**: JaCoCo 0.8.14 (28% coverage), Maven Surefire 3.5.4
- **Dependencies**: AWS SDK 2.36.2

### Frontend
- **Language**: TypeScript 5.x
- **Framework**: React 18+, React Router 6+
- **Build**: Vite
- **Testing**: Jest, React Testing Library

### Infrastructure
- **Cloud**: AWS
- **IaC**: Terraform 1.5+
- **CI/CD**: GitHub Actions

## Project Structure

```text
backend/          # Spring Boot REST API
frontend/         # React TypeScript SPA
lambda-functions/ # Node.js serverless functions
infrastructure/   # Terraform AWS resources
specs/            # Feature specifications
```

## Development Commands

### Backend
```bash
cd backend
mvn clean install          # Build
mvn test                   # Run tests
mvn spring-boot:run        # Run locally
```

### Frontend
```bash
cd frontend
npm install                # Install dependencies
npm run dev                # Development server
npm test                   # Run tests
npm run build              # Production build
```

### Infrastructure
```bash
cd infrastructure/terraform
terraform init             # Initialize
terraform plan             # Preview changes
terraform apply            # Deploy to AWS
```

## Code Style

### Java/Spring Boot
- Follow Spring Boot conventions
- Use constructor injection for dependencies
- All endpoints protected by authentication
- Multi-tenant: Filter all queries by tenant_id
- REST naming: `/api/tenants/{id}/projects`

### TypeScript/React
- Functional components with hooks
- TypeScript strict mode enabled
- Props interfaces for all components
- Custom hooks for reusable logic
- Context API for global state (auth, tenant)

### General
- No compiler warnings in production
- Code formatting: Backend (Spring conventions), Frontend (Prettier)
- Linting: Backend (Checkstyle), Frontend (ESLint)

## Architecture Principles

### Multi-Tenancy (NON-NEGOTIABLE)
- All database tables include `tenant_id`
- Tenant context extracted from subdomain
- Zero cross-tenant data leakage tolerance
- Row-Level Security for defense-in-depth

### Security
- OAuth2 via AWS Cognito (Google/Facebook/GitHub)
- JWT tokens for stateless authentication
- RBAC with tenant-specific roles
- All API endpoints protected by default

### Testing
- TDD: Tests written first, then implementation
- 80% minimum code coverage
- Integration tests with Testcontainers
- Contract tests for API endpoints

## Constitution Compliance

This project follows the **pmatinit Project Constitution v1.0.0**:
- ✅ Test-First Development (NON-NEGOTIABLE)
- ✅ Independent User Stories (P1 → P2 → P3 priority)
- ✅ Multi-Tenant Isolation (zero cross-tenant leakage)
- ✅ API-First Design (OpenAPI 3.0 contracts)
- ✅ Observability & Debuggability (structured logging)
- ✅ Simplicity & YAGNI (no premature abstraction)
- ✅ Technology Consistency (Java/React/AWS stack)

See `.specify/memory/constitution.md` for complete governance.

## Recent Changes

- 2025-10-27: **Component Migration Complete** - Upgraded to Java 21 LTS, Spring Boot 3.5.7, Testcontainers 1.21.3, AWS SDK 2.36.2
- 2025-10-27: All 112 tests passing, coverage improved to 28%
- 2025-10-27: Repository transitioned from Rust calculator to SaaS platform
- 2025-10-27: Constitution v1.0.0 ratified
- 2025-10-26: Feature 001-saas-platform specification completed
- 2025-10-26: Technical plan and task breakdown generated

## Archive

The original **pmatinit Rust CLI calculator** has been archived to `archive/pmatinit-calculator/`. See archive README for details.

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
