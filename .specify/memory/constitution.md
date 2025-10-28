<!--
  Sync Impact Report - Constitution v1.0.1
  ========================================
  Version Change: 1.0.0 → 1.0.1 (Technology Stack Update)

  Modified Principles:
  - Technology Consistency (Principle VII): Updated backend versions to reflect Java 21 LTS migration
    - Java: 17+ → 21 LTS
    - Spring Boot: 3.x → 3.5.7
    - Added specific dependency versions (Testcontainers, AWS SDK, build tools)

  Added Sections: None
  Removed Sections: None

  Templates Requiring Updates:
  ✅ plan-template.md - Constitution Check section already generic, no version-specific references
  ✅ spec-template.md - No technology version references
  ✅ tasks-template.md - No technology version references
  ✅ checklist-template.md - No technology version references
  ✅ agent-file-template.md - No technology version references

  Follow-up TODOs: None

  Rationale for PATCH bump:
  - No governance changes
  - No new principles added
  - Only clarification of existing Technology Consistency principle
  - Updates concrete version numbers without changing principle semantics
-->

# pmatinit Project Constitution

## Core Principles

### I. Test-First Development (NON-NEGOTIABLE)

All new features and bug fixes MUST follow Test-Driven Development (TDD):

- Tests written FIRST → User/stakeholder approved → Tests FAIL → Then implement
- Red-Green-Refactor cycle strictly enforced
- No production code without corresponding tests
- Minimum 80% code coverage for new features
- Integration tests required for multi-component interactions

**Rationale**: TDD ensures correctness, prevents regressions, and serves as living documentation. The existing pmatinit CLI has 160+ tests demonstrating this commitment.

### II. Independent User Stories

Features MUST be decomposed into independently testable user stories:

- Each story delivers standalone value (can be MVP on its own)
- Stories prioritized P1 (critical) → P2 → P3 (nice-to-have)
- Each story must have explicit acceptance criteria
- Implementation order follows priority, not technical convenience
- Stories can be deployed/demonstrated independently

**Rationale**: Enables incremental delivery, early validation, parallel development, and flexible scope management.

### III. Multi-Tenant Isolation (for SaaS features)

All multi-tenant features MUST enforce strict data isolation:

- Tenant context extracted from URL (subdomain or path)
- All database queries MUST filter by `tenant_id`
- Zero cross-tenant data leakage tolerance
- Penetration testing required before production
- Row-Level Security (RLS) or equivalent as defense-in-depth

**Rationale**: Security breach in multi-tenant systems affects all customers. Complete isolation is non-negotiable.

### IV. API-First Design

All services MUST expose functionality via well-defined contracts:

- REST APIs documented in OpenAPI 3.0 specification
- Contract tests validate API compliance
- Versioning follows semantic versioning (MAJOR.MINOR.PATCH)
- Breaking changes require MAJOR version bump and migration path
- Support both JSON and human-readable formats where applicable

**Rationale**: Clear contracts enable parallel development, external integrations, and independent testing.

### V. Observability & Debuggability

All production code MUST be observable and debuggable:

- Structured logging for all critical operations (authentication, authorization, errors)
- Audit trail for security-sensitive actions
- Health check endpoints (`/health`, `/ready`) for monitoring
- Performance metrics exposed for load analysis
- Error messages MUST be actionable (not generic "something went wrong")

**Rationale**: Production issues must be diagnosable quickly. Poor observability leads to extended outages.

### VI. Simplicity & YAGNI

Code MUST start simple and grow only when needed:

- No abstractions until third use case emerges
- No frameworks/libraries without clear justification
- Configuration over code where feasible
- Reject complexity - simpler solutions preferred
- Avoid premature optimization

**Rationale**: Complexity is the enemy of maintainability. The best code is code you don't have to write.

### VII. Technology Consistency

Technology stack decisions MUST align across features:

- **Primary Language**: Rust (CLI tools, performance-critical components)
- **Backend**: Java 21 LTS with Spring Boot 3.5.7
  - Spring Framework 6.2.12 (via Spring Boot)
  - Spring Security 6.5.6
  - Spring Data 2025.0.5
- **Frontend**: React 18+ with TypeScript 5.x for web UIs
- **Infrastructure**: AWS with Infrastructure-as-Code (Terraform 1.5+)
- **Testing**:
  - Backend: JUnit 5, Mockito, Testcontainers 1.21.3
  - Frontend: Jest, React Testing Library
  - CLI: cargo test
- **Build Tools**:
  - Backend: Maven with Surefire 3.5.4, JaCoCo 0.8.14
  - Frontend: Vite
- **AWS Services**:
  - Authentication: AWS Cognito
  - Events: EventBridge (SDK 2.36.2)
  - Email: SES (SDK 2.36.2)
  - Database: PostgreSQL 15 (RDS)
  - Serverless: Lambda (Node.js 18)

**Rationale**: Consistency reduces cognitive load, enables code reuse, and simplifies onboarding. Version specifications ensure reproducible builds and security compliance.

## Multi-Tenancy & Security

### Authentication & Authorization

- OAuth2 via AWS Cognito for user authentication
- JWT tokens for stateless authentication
- Role-Based Access Control (RBAC) with tenant-specific roles
- All API endpoints protected by authentication by default
- Principle of least privilege enforced

### Data Isolation

- Discriminator column pattern (tenant_id in every table) for RDS/relational databases
- Application-level enforcement via Spring Security filters
- Database-level enforcement via Row-Level Security (optional but recommended)
- No shared data structures across tenants
- Separate tenant contexts in ThreadLocal or equivalent

### Security Audit Requirements

- All authentication attempts logged
- All authorization failures logged
- Security logs retained per subscription tier (90 days Free, 1 year Pro, 7 years Enterprise)
- Automated security scanning in CI/CD pipeline
- Penetration testing before production deployment

## Quality Standards

### Code Quality Gates

- All code MUST pass linting/formatting (`cargo fmt`, `cargo clippy`, Spring Boot conventions)
- No compiler warnings in production builds
- Code complexity limits: max cyclomatic complexity 10 per function
- Code reviews required for all changes (minimum 1 approval)
- Automated CI/CD pipeline runs tests on every commit

### Documentation Requirements

- Public APIs documented in OpenAPI/Swagger
- README.md with quick start guide for each component
- Inline code comments for non-obvious logic only
- Architecture Decision Records (ADRs) for major technical decisions
- User-facing features documented in `docs/` directory

### Performance Requirements

- API response time: <2 seconds for 95th percentile under normal load
- Cold start time: <100ms for CLI tools, <3 seconds for serverless functions
- Frontend bundle size: <500KB gzipped
- Database queries: <100ms for single-record lookups
- Elastic scaling: handle 5x traffic spikes without manual intervention

### Deployment Requirements

- Zero-downtime deployments via blue-green or rolling updates
- Automated rollback on deployment failure
- Database migrations reversible or forward-compatible
- Feature flags for risky changes
- Monitoring/alerting configured before production

## Governance

### Constitution Authority

- This constitution supersedes all other development practices
- All PRs/code reviews MUST verify compliance with constitution principles
- Violations must be explicitly justified in PR description with "Complexity Tracking" table
- Constitution amendments require approval from project maintainers

### Amendment Process

- Amendments proposed via PR to this file
- MAJOR version bump: Backward-incompatible governance changes (e.g., removing NON-NEGOTIABLE principle)
- MINOR version bump: New principle added or existing principle materially expanded
- PATCH version bump: Clarifications, typo fixes, non-semantic refinements
- All amendments require migration plan for existing code

### Version Semantics

- **MAJOR**: Breaking changes to governance (e.g., removing TDD requirement)
- **MINOR**: New principles or constraints (e.g., adding performance requirements)
- **PATCH**: Clarifications without semantic changes (e.g., fixing typos, adding examples)

### Compliance Verification

- Pre-commit hooks enforce formatting/linting
- CI/CD pipeline enforces test coverage thresholds
- Manual code review verifies architecture compliance
- Quarterly constitution review to assess effectiveness
- Violations tracked in GitHub issues with `constitution-violation` label

### Complexity Justification

When violating constitution principles (e.g., adding complexity, skipping tests), use this format in plan.md:

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Adding 4th microservice | Tenant-specific automation requires isolation | Monolithic Lambda hits 15-minute timeout |
| Repository pattern | 50+ database queries need centralized caching | Direct DAO access causes N+1 query explosion |

**Version**: 1.0.1 | **Ratified**: 2025-10-27 | **Last Amended**: 2025-10-28
