# Implementation Plan: Multi-Tenant SaaS Platform

**Branch**: `001-saas-platform` | **Date**: 2025-10-26 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-saas-platform/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Build a multi-tenant project management SaaS platform with Spring Boot REST API backend, React responsive frontend, AWS Lambda for tenant-specific automation, and AWS infrastructure provisioning. The platform supports OAuth2 authentication via AWS Cognito, role-based access control, and three subscription tiers (Free: 50, Pro: 1,000, Enterprise: unlimited projects/tasks). Tenants are identified automatically via URL (subdomain or path), and users can belong to multiple tenants.

## Technical Context

**Language/Version**:
- Backend: Java 21 LTS (Spring Boot 3.5.7)
- Frontend: JavaScript/TypeScript (Node.js 18+, React 18+, TypeScript 5.x)
- Lambda Functions: Node.js 18

**Primary Dependencies**:
- Backend: Spring Boot 3.5.7 (Spring Framework 6.2.12, Spring Security 6.5.6, Spring Data 2025.0.5), Spring Cloud Function
- Frontend: React 18.2+, React Router 6+, Axios/Fetch API, Material-UI/Chakra UI/Bootstrap
- Build Tools: Maven (backend with Surefire 3.5.4, JaCoCo 0.8.14), npm/yarn (frontend with Vite)
- Containerization: Docker

**Storage**:
- Primary Database: AWS RDS (PostgreSQL 15)
- File Storage: Amazon S3 (for static assets, frontend hosting)
- CDN: Amazon CloudFront (frontend distribution)

**Testing**:
- Backend: JUnit 5, Spring Boot Test, Mockito, Testcontainers 1.21.3 (integration tests with real DB)
- Frontend: Jest, React Testing Library
- API: Postman/Newman or REST Assured for contract tests
- Coverage: JaCoCo 0.8.14 (target: 80% for new features, minimum 48% overall)

**Target Platform**:
- Backend: AWS ECS/EKS/App Runner (containerized Java application with auto-scaling)
- Frontend: AWS S3 + CloudFront (static site hosting)
- Functions: AWS Lambda (Node.js 18 runtime)
- Event Bus: AWS EventBridge (SDK 2.36.2)
- Email: AWS SES (SDK 2.36.2)
- Authentication: AWS Cognito User Pools

**Project Type**: Web application (separate backend API + frontend SPA)

**Performance Goals**:
- API response time: <2 seconds for 95% of CRUD requests under normal load
- Concurrent users: Support 10,000 authenticated users across 1,000 tenants
- Automation execution: Complete within 10 seconds for 95% of automation rules
- Cold start: Backend API <500ms, Lambda functions <3 seconds (Java) or <1 second (Node.js)

**Constraints**:
- Security: Complete multi-tenant data isolation, no cross-tenant data leakage
- Uptime: 99.9% excluding planned maintenance
- Compliance: Audit logging for all authentication/authorization events
- Scalability: Elastic scaling to handle 5x traffic spikes without manual intervention

**Scale/Scope**:
- Initial MVP: 100-1,000 tenants, 10,000-50,000 users
- Free tier: 50 projects/tasks per tenant
- Pro tier: 1,000 projects/tasks per tenant
- Enterprise tier: Unlimited projects/tasks
- Automation rules: Up to 30-second execution timeout per rule

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Status**: ✅ PASSED - Aligns with constitution v1.0.1

**Constitution Compliance Analysis**:
- ✅ **Test-First Development**: Plan includes comprehensive testing strategy (JUnit 5, Testcontainers 1.21.3, Mockito, Jest). Current backend: 164 tests passing, 48% coverage (target: 80% for new features).
- ✅ **Independent User Stories**: Spec.md defines 4 prioritized user stories (US1-US4) with independent test criteria
- ✅ **Multi-Tenant Isolation**: Architecture enforces discriminator column pattern, tenant context filters, RLS defense-in-depth
- ✅ **API-First Design**: Phase 1 includes OpenAPI 3.0 contract generation and contract testing
- ✅ **Observability**: Plan includes CloudWatch logging, X-Ray tracing, health checks, audit logging
- ✅ **Simplicity & YAGNI**: Architecture uses proven patterns (Spring Boot, React Context API vs Redux)
- ✅ **Technology Consistency**: Java 21 LTS with Spring Boot 3.5.7 backend, React 18+ with TypeScript 5.x frontend, AWS infrastructure, Testcontainers 1.21.3, JaCoCo 0.8.14 per constitution v1.0.1

**No Violations**: This feature aligns with all constitution principles.

## Project Structure

### Documentation (this feature)

```text
specs/001-saas-platform/
├── plan.md              # This file (/speckit.plan command output)
├── spec.md              # Feature specification (already created)
├── research.md          # Phase 0 output (to be created)
├── data-model.md        # Phase 1 output (to be created)
├── quickstart.md        # Phase 1 output (to be created)
├── contracts/           # Phase 1 output (API contracts, to be created)
│   ├── api-spec.yaml    # OpenAPI 3.0 specification
│   └── events-schema.json  # Event schema definitions
├── checklists/
│   └── requirements.md  # Quality checklist (already created)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created yet)
```

### Source Code (repository root)

```text
# Option 2: Web application (frontend + backend)

backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/platform/saas/
│   │   │       ├── config/          # Security, Cognito, JPA config
│   │   │       ├── controller/      # REST API endpoints
│   │   │       ├── service/         # Business logic
│   │   │       ├── repository/      # Data access (JPA or DynamoDB)
│   │   │       ├── model/           # Domain entities (Tenant, User, Project, Task, Role)
│   │   │       ├── security/        # JWT converter, tenant context
│   │   │       ├── events/          # Event publishers for EventBridge/SQS
│   │   │       └── exception/       # Exception handlers (@ControllerAdvice)
│   │   └── resources/
│   │       ├── application.yml      # Spring Boot configuration
│   │       └── db/migration/        # Flyway/Liquibase migrations
│   └── test/
│       ├── java/
│       │   └── com/platform/saas/
│       │       ├── integration/     # Integration tests with Testcontainers
│       │       └── unit/            # Unit tests for services/controllers
│       └── resources/
│           └── application-test.yml
├── Dockerfile
├── pom.xml (or build.gradle)
└── README.md

frontend/
├── public/
│   └── index.html
├── src/
│   ├── components/          # Reusable React components
│   │   ├── auth/            # OAuth2 login flow components
│   │   ├── layout/          # Header, sidebar, footer
│   │   └── common/          # Buttons, forms, modals
│   ├── pages/               # Page-level components
│   │   ├── Dashboard.tsx
│   │   ├── Projects.tsx
│   │   ├── Tasks.tsx
│   │   └── Settings.tsx
│   ├── services/            # API client (Axios)
│   │   ├── api.ts           # Base API configuration
│   │   ├── authService.ts   # OAuth2/Cognito integration
│   │   └── projectService.ts
│   ├── hooks/               # Custom React hooks
│   │   ├── useAuth.ts       # Authentication state
│   │   └── useTenant.ts     # Tenant context from URL
│   ├── context/             # React Context providers
│   │   └── AuthContext.tsx
│   ├── App.tsx
│   └── index.tsx
├── tests/
│   ├── components/
│   └── integration/
├── package.json
├── tsconfig.json
├── Dockerfile
└── README.md

lambda-functions/
├── automation-engine/       # Executes tenant-specific automation rules
│   ├── src/
│   │   └── handler.java (or index.js)
│   ├── pom.xml (or package.json)
│   └── template.yaml        # SAM template
├── cognito-triggers/        # Post-confirmation, pre-signup hooks
│   └── src/
│       └── handler.java (or index.js)
└── shared/                  # Shared libraries/utilities

infrastructure/
├── terraform/               # Or AWS CDK/SAM
│   ├── main.tf
│   ├── vpc.tf
│   ├── cognito.tf
│   ├── rds.tf (or dynamodb.tf)
│   ├── ecs.tf (or eks.tf)
│   ├── s3-cloudfront.tf
│   ├── lambda.tf
│   ├── eventbridge.tf (or sqs.tf)
│   └── variables.tf
└── README.md

.github/
└── workflows/
    ├── backend-ci.yml       # Build/test Spring Boot
    ├── frontend-ci.yml      # Build/test React
    ├── lambda-ci.yml        # Build/test Lambda functions
    └── deploy.yml           # Deploy to AWS (IaC + containers)
```

**Structure Decision**: Web application structure selected due to separate frontend (React SPA) and backend (Spring Boot REST API). Lambda functions are isolated in `lambda-functions/` for serverless automation. Infrastructure-as-code is in `infrastructure/` using Terraform/CDK. This structure supports independent deployment and scaling of each component.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

*N/A - No constitution violations detected. All technology stack decisions align with constitution v1.0.1.*

## Phase 0: Research & Decisions

**Prerequisites**: Technical Context filled
**Output**: `research.md`

### Research Tasks

1. **Multi-Tenant Data Isolation Strategy**
   - Investigate: Discriminator column vs. schema-per-tenant vs. database-per-tenant
   - Decision needed: Which approach for RDS/PostgreSQL or DynamoDB?
   - Factors: Cost, complexity, query performance, tenant limits

2. **AWS Cognito Configuration**
   - Research: Hosted UI customization, social provider setup (Google/Facebook/GitHub)
   - Decision needed: JWT claim structure for tenant context
   - Factors: Token size, security, multi-tenant support

3. **Tenant Identification from URL**
   - Investigate: Subdomain routing vs. path-based routing in AWS
   - Decision needed: ALB routing rules, CloudFront behavior, React Router configuration
   - Factors: Cost, complexity, SSL certificate management

4. **Event-Driven Architecture**
   - Research: EventBridge vs. SQS for domain events
   - Decision needed: Which service for automation triggers?
   - Factors: Cost, latency, ordering guarantees, retry logic

5. **Spring Boot + Lambda Integration**
   - Investigate: Spring Cloud Function vs. plain Lambda handlers
   - Decision needed: Java runtime performance vs. Node.js cold start times
   - Factors: Cold start latency, memory usage, developer experience

6. **Database Choice**
   - Research: RDS PostgreSQL vs. DynamoDB for multi-tenant workloads
   - Decision needed: Which provides better cost/performance for this use case?
   - Factors: Query patterns, scalability, cost, operational overhead

7. **Frontend State Management**
   - Investigate: Context API vs. Redux vs. Zustand
   - Decision needed: Which for tenant context and auth state?
   - Factors: Bundle size, learning curve, team experience

8. **Subscription Tier Enforcement**
   - Research: Application-level quota checks vs. API Gateway throttling
   - Decision needed: Where to enforce quota limits?
   - Factors: User experience (error messages), cost, flexibility

## Phase 1: Design & Contracts

**Prerequisites**: `research.md` complete
**Output**: `data-model.md`, `/contracts/*`, `quickstart.md`, updated agent context

### Data Model Design

Extract from spec entities:
- Tenant (organization, subdomain, subscription tier, quota usage)
- User (authentication, tenant memberships, roles per tenant)
- Role (permissions, tenant-scoped)
- Project (name, description, status, due date, tenant-scoped)
- Task (parent project, assignees, progress, priority, dependencies, tenant-scoped)
- AutomationRule (tenant-scoped, event triggers, actions)
- Event (domain events for EventBridge/SQS)

### API Contracts

Generate OpenAPI 3.0 specification for REST endpoints:

**Authentication**:
- GET `/oauth2/authorization/{provider}` - Initiate OAuth2 flow
- GET `/oauth/callback` - Handle OAuth2 redirect

**Tenant Management**:
- POST `/api/tenants` - Create new tenant (sign-up)
- GET `/api/tenants/{id}` - Get tenant details
- GET `/api/tenants/{id}/usage` - Get quota usage

**User Management**:
- POST `/api/tenants/{id}/users/invite` - Invite user to tenant
- GET `/api/tenants/{id}/users` - List tenant users
- DELETE `/api/tenants/{id}/users/{userId}` - Remove user from tenant

**Project Management**:
- POST `/api/tenants/{id}/projects` - Create project
- GET `/api/tenants/{id}/projects` - List projects (with filtering/search)
- GET `/api/tenants/{id}/projects/{projectId}` - Get project details
- PUT `/api/tenants/{id}/projects/{projectId}` - Update project
- DELETE `/api/tenants/{id}/projects/{projectId}` - Delete project

**Task Management**:
- POST `/api/tenants/{id}/projects/{projectId}/tasks` - Create task
- GET `/api/tenants/{id}/projects/{projectId}/tasks` - List tasks
- PUT `/api/tenants/{id}/projects/{projectId}/tasks/{taskId}` - Update task
- DELETE `/api/tenants/{id}/projects/{projectId}/tasks/{taskId}` - Delete task

**Automation Rules**:
- POST `/api/tenants/{id}/automations` - Create automation rule
- GET `/api/tenants/{id}/automations` - List automation rules
- PUT `/api/tenants/{id}/automations/{ruleId}` - Update rule
- DELETE `/api/tenants/{id}/automations/{ruleId}` - Delete rule

### Event Schema

Define event schemas for EventBridge/SQS:
- `tenant.created`
- `project.created`, `project.updated`, `project.deleted`
- `task.created`, `task.updated`, `task.deleted`
- `user.invited`, `user.added`, `user.removed`

### Quickstart Guide

Developer onboarding documentation:
- Local development setup (Docker Compose for PostgreSQL/DynamoDB)
- Running backend (Spring Boot) locally
- Running frontend (React) locally
- Configuring AWS Cognito for local testing
- Running tests (JUnit, Jest)
- Deploying to AWS (IaC commands)

## Phase 2: Task Breakdown

**Prerequisites**: Phase 1 complete
**Command**: Use `/speckit.tasks` (separate command, not part of this plan)
**Output**: `tasks.md`

Tasks will be generated based on:
- Infrastructure setup (VPC, Cognito, RDS/DynamoDB, ECS/EKS, S3/CloudFront)
- Backend implementation (Spring Boot API, security, services, repositories)
- Frontend implementation (React components, OAuth2 integration, API client)
- Lambda functions (automation engine, Cognito triggers)
- CI/CD pipelines (GitHub Actions workflows)
- Testing (unit, integration, contract tests)
- Deployment (IaC provisioning, container deployment)

## Next Steps

1. ✅ Complete Phase 0: Generate `research.md` to resolve all technical decisions
2. ⏳ Complete Phase 1: Generate `data-model.md`, API contracts, quickstart guide
3. ⏳ Run `/speckit.tasks` to generate detailed task breakdown
4. ⏳ Run `/speckit.implement` to begin implementation

## Notes

- This is the initial feature for the project, establishing architecture patterns for future features
- Multi-tenancy and security are foundational - must be implemented correctly from the start
- Consider creating a project constitution after MVP to codify established patterns
- Infrastructure-as-code is critical for reproducible deployments across environments
- Testcontainers recommended for integration tests to avoid mocking DB/AWS services
