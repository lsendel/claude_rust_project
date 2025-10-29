# Research & Technical Decisions: Multi-Tenant SaaS Platform

**Feature**: Multi-Tenant SaaS Platform (Project Management)
**Date**: 2025-10-26
**Status**: Completed

This document consolidates research findings and technical decisions for implementing the multi-tenant SaaS platform.

---

## 1. Multi-Tenant Data Isolation Strategy

### Decision: Discriminator Column (Tenant ID per table)

**Rationale**:
- **Simplicity**: Single database instance, single schema, tenant ID column in every table
- **Cost-effective**: No need for multiple databases or schemas, reduces RDS/DynamoDB costs
- **Query Performance**: Proper indexing on tenant_id enables efficient queries; add composite indexes (tenant_id, id) on all tables
- **Scalability**: Supports 1,000+ tenants in initial MVP; can shard later if needed
- **Proven Pattern**: Used successfully by Basecamp, Shopify, and other large SaaS platforms

**Alternatives Considered**:
- **Schema-per-tenant**: More isolation but operational complexity (migrations, backups), doesn't scale beyond ~100 tenants
- **Database-per-tenant**: Maximum isolation but cost-prohibitive ($50-100/month per tenant for RDS), operational nightmare at scale

**Implementation Details**:
- Add `tenant_id UUID NOT NULL` to all domain tables (Project, Task, User, etc.)
- Create composite primary keys: `(tenant_id, id)` or use `id` as PK with unique index on `(tenant_id, natural_key)`
- Row-Level Security (RLS) in PostgreSQL as defense-in-depth (optional but recommended)
- Application-level enforcement: Spring Security filter extracts tenant from JWT and sets ThreadLocal context
- Repository methods always filter by `tenant_id`: `findByTenantIdAndId(UUID tenantId, UUID id)`

---

## 2. AWS Cognito Configuration

### Decision: Custom JWT Claims + Tenant Context in Application

**Rationale**:
- **JWT Size Limits**: Keep JWT under 8KB by NOT embedding full tenant list in token
- **Security**: Store tenant ID in application session/context after URL-based tenant detection
- **Multi-Tenant Support**: User can belong to multiple tenants; tenant context determined by URL, not JWT
- **Flexibility**: Easy to add/remove tenants without re-issuing JWTs

**Cognito Setup**:
- **User Pool**: Single user pool for all tenants (not pool-per-tenant)
- **Social Providers**: Google, Facebook, GitHub OAuth2 via Cognito Hosted UI
- **Custom Attributes**: Add `custom:email_verified` and `custom:user_id` (UUID)
- **JWT Claims**: Standard claims only (sub, email, cognito:groups for roles)
- **Post-Confirmation Trigger**: Lambda function to create User record in database after sign-up

**Alternatives Considered**:
- **Tenant ID in JWT**: Would require re-authentication when switching tenants, bloats token size
- **Cognito User Pool per Tenant**: Operationally complex, 10,000 user pool limit per AWS account

**Implementation Details**:
- Spring Security `JwtAuthenticationConverter` extracts `sub` (Cognito user ID) from JWT
- Custom filter `TenantContextFilter` extracts tenant from URL (subdomain or path) and validates user membership
- `SecurityContextHolder` stores both user identity and tenant context
- Tenant membership validated on every request: check `user_tenants` join table

---

## 3. Tenant Identification from URL

### Decision: Subdomain-based with Wildcard SSL

**Rationale**:
- **Clean URLs**: `tenant1.platform.com` more professional than `platform.com/tenant1`
- **SSL Management**: Single wildcard certificate `*.platform.com` covers all tenants
- **CloudFront**: Supports wildcard CNAMEs, easy to configure
- **React Router**: No special routing needed, tenant extracted from `window.location.hostname`

**AWS Infrastructure**:
- **Route 53**: Wildcard DNS record `*.platform.com` → CloudFront distribution
- **CloudFront**: Single distribution with wildcard CNAMEs, forwards `Host` header to ALB
- **ALB**: Routes all subdomains to ECS/EKS backend, backend extracts tenant from `Host` header
- **ACM Certificate**: Wildcard SSL certificate for `*.platform.com`

**Alternatives Considered**:
- **Path-based routing** (`platform.com/tenant1`): Requires complex React Router configuration, harder to white-label later
- **Separate CloudFront per tenant**: Cost-prohibitive, operational complexity

**Implementation Details**:
- Spring Boot `TenantContextFilter`:
  ```java
  String host = request.getHeader("Host"); // e.g., "tenant1.platform.com"
  String subdomain = host.split("\\.")[0]; // "tenant1"
  Tenant tenant = tenantRepository.findBySubdomain(subdomain);
  TenantContext.setCurrentTenant(tenant.getId());
  ```
- React `useTenant()` hook extracts tenant from URL:
  ```typescript
  const subdomain = window.location.hostname.split('.')[0];
  ```
- Tenant creation validates subdomain uniqueness and DNS-safe format (alphanumeric + hyphens only)

---

## 4. Event-Driven Architecture

### Decision: Amazon EventBridge for domain events

**Rationale**:
- **Schema Registry**: EventBridge has built-in schema discovery and versioning
- **Routing Flexibility**: Event buses with rules/patterns for routing to Lambda functions
- **Replay Capability**: Can replay events for debugging or reprocessing
- **AWS Integration**: Native integration with Lambda, SQS, SNS, Step Functions
- **Cost**: $1 per million events (negligible at MVP scale)

**Alternatives Considered**:
- **Amazon SQS**: Good for simple point-to-point messaging but lacks event routing and replay; would need multiple queues
- **Kafka/MSK**: Overkill for MVP, high operational complexity and cost ($150+/month minimum)

**Event Schema** (JSON):
```json
{
  "eventType": "project.created",
  "eventVersion": "1.0",
  "timestamp": "2025-10-26T12:00:00Z",
  "tenantId": "uuid",
  "userId": "uuid",
  "payload": {
    "projectId": "uuid",
    "name": "Project Name",
    "status": "active"
  }
}
```

**Implementation Details**:
- Spring Boot publishes events via `EventBridgeClient`:
  ```java
  PutEventsRequest request = PutEventsRequest.builder()
      .entries(PutEventsRequestEntry.builder()
          .source("saas-platform")
          .detailType("project.created")
          .detail(eventJson)
          .build())
      .build();
  eventBridgeClient.putEvents(request);
  ```
- EventBridge rules route events to Lambda functions based on tenant ID and event type
- Lambda functions consume events, execute automation rules, send notifications

---

## 5. Spring Boot + Lambda Integration

### Decision: Node.js Lambda Handlers (not Spring Cloud Function)

**Rationale**:
- **Cold Start**: Node.js Lambda cold start ~500ms vs. Java ~3-5 seconds (even with SnapStart)
- **Memory Efficiency**: Node.js Lambda uses 128-256MB vs. Java 512MB-1GB
- **Cost**: Lower memory = lower Lambda costs (charged by GB-second)
- **Simplicity**: Plain Lambda handlers easier to debug than Spring Cloud Function abstraction
- **Use Case**: Automation rules are short-lived, stateless tasks (send email, call webhook) - perfect for Node.js

**Alternatives Considered**:
- **Spring Cloud Function**: Code reuse with backend but cold start penalty unacceptable for user-facing automation
- **Java Lambda with SnapStart**: Reduces cold start but still 1-2 seconds, higher memory usage

**Implementation Details**:
- Lambda functions in `lambda-functions/automation-engine/src/index.js`:
  ```javascript
  exports.handler = async (event) => {
      const { tenantId, eventType, payload } = JSON.parse(event.detail);
      const rules = await getAutomationRules(tenantId, eventType);
      for (const rule of rules) {
          await executeAction(rule.action, payload);
      }
  };
  ```
- Share TypeScript types between backend and Lambda via npm package or code generation
- Lambda invocation timeout: 30 seconds (enforced in EventBridge rule)

---

## 6. Database Choice

### Decision: AWS RDS PostgreSQL (not DynamoDB)

**Rationale**:
- **Query Patterns**: Project management requires complex queries (search, filtering by date/assignee/status, joins between projects/tasks)
- **Relational Model**: Strong relationships between Tenant → Users → Projects → Tasks fit relational model
- **ACID Transactions**: Critical for quota enforcement (check count + insert must be atomic)
- **Tooling**: Mature ecosystem (Flyway migrations, pg_dump backups, pgAdmin)
- **Cost**: RDS ~$50-100/month for db.t3.medium, DynamoDB would be $100+/month with on-demand pricing for same workload

**Alternatives Considered**:
- **DynamoDB**: Better for key-value lookups and scale but poor for complex queries; would need denormalized GSIs and higher cost
- **Aurora Serverless**: Auto-scaling but cold start delays (10-30 seconds) unacceptable for user-facing API

**PostgreSQL Configuration**:
- **Instance Size**: db.t3.medium (2 vCPU, 4GB RAM) for MVP → db.r5.large (2 vCPU, 16GB RAM) for production
- **Storage**: 100GB SSD with auto-scaling to 500GB
- **Backups**: Automated daily snapshots with 7-day retention, point-in-time recovery enabled
- **Multi-AZ**: Enabled for production (99.95% SLA), disabled for dev to save costs
- **Connection Pooling**: HikariCP in Spring Boot (max 20 connections), consider RDS Proxy for high traffic

**Implementation Details**:
- Spring Data JPA with Hibernate for ORM
- Flyway for database migrations (`db/migration/V001__create_tenants_table.sql`)
- Indexes:
  - `CREATE INDEX idx_projects_tenant_id ON projects(tenant_id);`
  - `CREATE INDEX idx_tasks_project_tenant ON tasks(tenant_id, project_id);`
- Row-Level Security (RLS) for defense-in-depth (optional):
  ```sql
  CREATE POLICY tenant_isolation ON projects
      USING (tenant_id = current_setting('app.current_tenant')::UUID);
  ```

---

## 7. Frontend State Management

### Decision: React Context API (no Redux/Zustand)

**Rationale**:
- **Simplicity**: Context API sufficient for auth state and tenant context (2 global states)
- **Bundle Size**: Zero external dependencies, reduces bundle size by ~50KB (gzip)
- **Learning Curve**: Team familiar with Context API, no Redux boilerplate
- **Performance**: Minimal re-renders with `useMemo` and context splitting (AuthContext, TenantContext separate)

**Alternatives Considered**:
- **Redux**: Overkill for 2 global states, adds 45KB (gzip) to bundle, steep learning curve
- **Zustand**: Lighter than Redux (3KB) but unnecessary for simple use cases

**Context Structure**:
```typescript
// AuthContext.tsx
interface AuthState {
    user: User | null;
    isAuthenticated: boolean;
    login: () => void;
    logout: () => void;
}

// TenantContext.tsx
interface TenantState {
    tenant: Tenant | null;
    subdomain: string;
    quotaUsage: QuotaUsage | null;
}
```

**Implementation Details**:
- `AuthContext` manages user authentication state (Cognito tokens in HTTP-only cookie or localStorage)
- `TenantContext` extracts tenant from URL and fetches tenant details/quota
- Components consume contexts via `useAuth()` and `useTenant()` hooks
- API client (Axios) includes tenant context in all requests: `axios.defaults.headers.common['X-Tenant-ID'] = tenantId;`

---

## 8. Subscription Tier Enforcement

### Decision: Application-level quota checks (not API Gateway throttling)

**Rationale**:
- **User Experience**: Application can show detailed error messages ("You've used 50/50 projects. Upgrade to Pro for 1,000 projects.")
- **Flexibility**: Easy to adjust quotas per tenant or grant exceptions (support overrides)
- **Granularity**: Enforce different quotas for different entity types (projects vs. tasks)
- **Cost**: API Gateway throttling charges $1 per million requests; application-level is free

**Alternatives Considered**:
- **API Gateway throttling**: Coarse-grained (rate limits only), poor error messages ("429 Too Many Requests"), no entity-specific limits

**Implementation Details**:
- Spring Boot service method checks quota before create:
  ```java
  @Transactional
  public Project createProject(UUID tenantId, ProjectRequest request) {
      Tenant tenant = tenantRepository.findById(tenantId);
      long currentCount = projectRepository.countByTenantId(tenantId);

      if (currentCount >= tenant.getQuotaLimit()) {
          throw new QuotaExceededException(
              "Project limit reached. Upgrade to create more projects.",
              tenant.getSubscriptionTier(),
              currentCount,
              tenant.getQuotaLimit()
          );
      }

      // Create project...
  }
  ```
- Frontend shows quota usage in header: "Projects: 45/50 (Free)" with upgrade button
- `@ControllerAdvice` translates `QuotaExceededException` to 402 Payment Required with JSON body:
  ```json
  {
    "error": "QUOTA_EXCEEDED",
    "message": "You've reached your limit of 50 projects.",
    "currentUsage": 50,
    "quotaLimit": 50,
    "currentTier": "FREE",
    "upgradeUrl": "/billing/upgrade"
  }
  ```

---

## Summary of Decisions

| Area | Decision | Rationale |
|------|----------|-----------|
| **Multi-Tenancy** | Discriminator column (tenant_id per table) | Simple, cost-effective, scales to 1,000+ tenants |
| **Authentication** | AWS Cognito + Custom JWT claims | Security, multi-tenant support, social providers |
| **Tenant Routing** | Subdomain-based (`tenant1.platform.com`) | Professional URLs, wildcard SSL, easy CloudFront config |
| **Events** | Amazon EventBridge | Schema registry, routing, replay capability |
| **Lambda Runtime** | Node.js (not Java) | Fast cold start (<500ms), low memory, low cost |
| **Database** | AWS RDS PostgreSQL | Complex queries, ACID transactions, relational model |
| **Frontend State** | React Context API | Simple, zero dependencies, sufficient for 2 global states |
| **Quota Enforcement** | Application-level checks | Detailed error messages, flexible, granular control |

---

## Technology Stack Summary

**Backend (Spring Boot 3.x)**:
- Language: Java 17
- Framework: Spring Boot 3.2.x (web, data-jpa, security, oauth2)
- Database: PostgreSQL 15 on AWS RDS
- Testing: JUnit 5, Testcontainers
- Build: Maven 3.9+

**Frontend (React 18)**:
- Language: TypeScript 5.x
- Framework: React 18.2+, React Router 6+
- UI Library: Material-UI (or Chakra UI/Bootstrap)
- HTTP Client: Axios
- Build: Vite (faster than Create React App)

**Lambda Functions**:
- Runtime: Node.js 18 (AWS Lambda)
- Language: TypeScript
- Framework: None (plain handlers)
- Testing: Jest

**Infrastructure (Terraform)**:
- IaC: Terraform 1.5+
- Cloud: AWS (us-east-1 initially)
- CI/CD: GitHub Actions
- Monitoring: AWS CloudWatch, X-Ray

---

## Next Phase

All research tasks completed. Ready to proceed to **Phase 1: Design & Contracts**.
