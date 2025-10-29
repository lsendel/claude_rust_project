# Data Model: Multi-Tenant SaaS Platform

**Feature**: Multi-Tenant SaaS Platform (Project Management)
**Date**: 2025-10-26
**Database**: PostgreSQL 15 on AWS RDS

This document defines the domain entities, relationships, and validation rules based on the feature specification and research decisions.

---

## Entity Relationship Diagram

```
Tenant (1) ─────< (N) User_Tenant (N) >───── (1) User
  │                                            │
  │ (1)                                        │ (1)
  │                                            │
  ├─< (N) Project                             │
  │    │ (1)                                   │
  │    ├─< (N) Task ───────────────< (N) Task_Assignee >─────┘
  │    │                  │ (dependencies)
  │    │                  │
  │    └─────< (N) ───────┘
  │
  └─< (N) Automation_Rule
```

---

## Core Entities

### 1. Tenant

Represents an organization/customer account with its subscription and quota information.

**Fields**:
- `id` UUID PRIMARY KEY
- `subdomain` VARCHAR(63) UNIQUE NOT NULL (DNS-safe: alphanumeric + hyphens)
- `name` VARCHAR(255) NOT NULL
- `subscription_tier` ENUM('FREE', 'PRO', 'ENTERPRISE') NOT NULL DEFAULT 'FREE'
- `quota_limit` INTEGER NOT NULL (50 for FREE, 1000 for PRO, NULL for ENTERPRISE)
- `created_at` TIMESTAMP NOT NULL DEFAULT NOW()
- `updated_at` TIMESTAMP NOT NULL DEFAULT NOW()

**Indexes**:
- PRIMARY KEY on `id`
- UNIQUE INDEX on `subdomain`

**Validation Rules**:
- Subdomain must match `^[a-z0-9-]{3,63}$` (lowercase alphanumeric + hyphens, 3-63 chars)
- Subdomain cannot be reserved words: `www`, `api`, `admin`, `app`, `platform`
- Quota limit must be >= current usage count

**State Transitions**:
- FREE → PRO → ENTERPRISE (upgrades)
- ENTERPRISE → PRO → FREE (downgrades, with grace period for quota violations)

---

### 2. User

Represents an individual with authentication credentials managed by AWS Cognito.

**Fields**:
- `id` UUID PRIMARY KEY
- `cognito_user_id` VARCHAR(255) UNIQUE NOT NULL (Cognito `sub` claim)
- `email` VARCHAR(255) UNIQUE NOT NULL
- `name` VARCHAR(255)
- `created_at` TIMESTAMP NOT NULL DEFAULT NOW()
- `last_login_at` TIMESTAMP

**Indexes**:
- PRIMARY KEY on `id`
- UNIQUE INDEX on `cognito_user_id`
- UNIQUE INDEX on `email`

**Validation Rules**:
- Email must be valid format (validated by Cognito)
- Cognito user ID immutable after creation

**Relationships**:
- User can belong to multiple Tenants (many-to-many via `user_tenants` table)

---

### 3. User_Tenant (Join Table)

Maps users to tenants with tenant-specific roles.

**Fields**:
- `user_id` UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE
- `tenant_id` UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE
- `role` ENUM('ADMINISTRATOR', 'EDITOR', 'VIEWER') NOT NULL
- `invited_by` UUID REFERENCES users(id) (NULL for initial admin)
- `joined_at` TIMESTAMP NOT NULL DEFAULT NOW()
- PRIMARY KEY (`user_id`, `tenant_id`)

**Indexes**:
- PRIMARY KEY on (`user_id`, `tenant_id`)
- INDEX on `tenant_id` (for listing users per tenant)

**Validation Rules**:
- At least one ADMINISTRATOR per tenant (enforced at application level)
- User cannot have duplicate roles in same tenant

---

### 4. Project

Core domain entity for project management within a tenant.

**Fields**:
- `id` UUID PRIMARY KEY
- `tenant_id` UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE
- `name` VARCHAR(255) NOT NULL
- `description` TEXT
- `status` ENUM('PLANNING', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'ARCHIVED') NOT NULL DEFAULT 'PLANNING'
- `due_date` DATE
- `owner_id` UUID NOT NULL REFERENCES users(id)
- `progress_percentage` INTEGER DEFAULT 0 CHECK (progress_percentage >= 0 AND progress_percentage <= 100)
- `priority` ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM'
- `created_at` TIMESTAMP NOT NULL DEFAULT NOW()
- `updated_at` TIMESTAMP NOT NULL DEFAULT NOW()

**Indexes**:
- PRIMARY KEY on `id`
- INDEX on `tenant_id` (critical for multi-tenant isolation)
- INDEX on `(tenant_id, status)` (for filtering)
- INDEX on `(tenant_id, due_date)` (for date-based queries)

**Validation Rules**:
- Name cannot be empty (min 1 char, max 255)
- Progress must be 0-100
- Due date cannot be in the past for new projects
- Owner must be a member of the tenant

**State Transitions**:
- PLANNING → ACTIVE → COMPLETED
- Any status → ON_HOLD → previous status
- COMPLETED/ARCHIVED → cannot transition back (business rule)

---

### 5. Task

Sub-entity of Project, represents actionable work items.

**Fields**:
- `id` UUID PRIMARY KEY
- `tenant_id` UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE
- `project_id` UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE
- `name` VARCHAR(255) NOT NULL
- `description` TEXT
- `status` ENUM('TODO', 'IN_PROGRESS', 'BLOCKED', 'COMPLETED') NOT NULL DEFAULT 'TODO'
- `due_date` DATE
- `progress_percentage` INTEGER DEFAULT 0 CHECK (progress_percentage >= 0 AND progress_percentage <= 100)
- `priority` ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM'
- `created_at` TIMESTAMP NOT NULL DEFAULT NOW()
- `updated_at` TIMESTAMP NOT NULL DEFAULT NOW()

**Indexes**:
- PRIMARY KEY on `id`
- INDEX on `(tenant_id, project_id)` (for listing tasks per project)
- INDEX on `(tenant_id, status)` (for filtering)

**Validation Rules**:
- Name cannot be empty
- Progress must be 0-100
- Task project must belong to same tenant

---

### 6. Task_Assignee (Join Table)

Maps tasks to assigned users.

**Fields**:
- `task_id` UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE
- `user_id` UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE
- `assigned_at` TIMESTAMP NOT NULL DEFAULT NOW()
- PRIMARY KEY (`task_id`, `user_id`)

**Indexes**:
- PRIMARY KEY on (`task_id`, `user_id`)
- INDEX on `user_id` (for listing tasks per user)

**Validation Rules**:
- Assignee must be a member of the task's tenant

---

### 7. Task_Dependency (Self-Referencing Join Table)

Represents dependencies between tasks (task A blocks task B).

**Fields**:
- `blocking_task_id` UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE
- `blocked_task_id` UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE
- `created_at` TIMESTAMP NOT NULL DEFAULT NOW()
- PRIMARY KEY (`blocking_task_id`, `blocked_task_id`)

**Indexes**:
- PRIMARY KEY on (`blocking_task_id`, `blocked_task_id`)
- INDEX on `blocked_task_id` (for finding what blocks a task)

**Validation Rules**:
- Cannot create circular dependencies (checked at application level via graph traversal)
- Dependencies must be within same project

---

### 8. Automation_Rule

Tenant-specific automation rules triggered by domain events.

**Fields**:
- `id` UUID PRIMARY KEY
- `tenant_id` UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE
- `name` VARCHAR(255) NOT NULL
- `description` TEXT
- `event_type` VARCHAR(100) NOT NULL (e.g., 'project.created', 'task.status.changed')
- `conditions` JSONB (filter conditions, e.g., `{"status": "COMPLETED"}`)
- `action_type` ENUM('SEND_EMAIL', 'CALL_WEBHOOK', 'CREATE_TASK', 'SEND_NOTIFICATION') NOT NULL
- `action_config` JSONB NOT NULL (action-specific config, e.g., email template, webhook URL)
- `enabled` BOOLEAN NOT NULL DEFAULT TRUE
- `created_by` UUID NOT NULL REFERENCES users(id)
- `created_at` TIMESTAMP NOT NULL DEFAULT NOW()
- `updated_at` TIMESTAMP NOT NULL DEFAULT NOW()

**Indexes**:
- PRIMARY KEY on `id`
- INDEX on `(tenant_id, event_type, enabled)` (for finding applicable rules)

**Validation Rules**:
- Event type must match supported event schema
- Action config must be valid JSON for the action type
- Only tenant ADMINISTRATOR can create/edit rules

---

### 9. Event_Log (Audit Trail)

Logs all domain events for auditing and debugging.

**Fields**:
- `id` UUID PRIMARY KEY
- `tenant_id` UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE
- `event_type` VARCHAR(100) NOT NULL
- `entity_type` VARCHAR(50) NOT NULL (e.g., 'PROJECT', 'TASK', 'USER')
- `entity_id` UUID NOT NULL
- `user_id` UUID REFERENCES users(id) (NULL for system events)
- `event_data` JSONB NOT NULL (full event payload)
- `created_at` TIMESTAMP NOT NULL DEFAULT NOW()

**Indexes**:
- PRIMARY KEY on `id`
- INDEX on `(tenant_id, created_at DESC)` (for time-based queries)
- INDEX on `(tenant_id, entity_type, entity_id)` (for entity history)

**Validation Rules**:
- Event data must be valid JSON
- Retention policy: 90 days for free tier, 1 year for Pro, 7 years for Enterprise

---

## Data Integrity Rules

### Multi-Tenant Isolation
- **All queries MUST filter by `tenant_id`** (enforced in Spring Data repositories)
- Row-Level Security (RLS) as defense-in-depth (optional):
  ```sql
  ALTER TABLE projects ENABLE ROW LEVEL SECURITY;
  CREATE POLICY tenant_isolation ON projects
      USING (tenant_id = current_setting('app.current_tenant')::UUID);
  ```

### Quota Enforcement
- Before inserting Project or Task, check:
  ```sql
  SELECT COUNT(*) FROM projects WHERE tenant_id = ?
  AND status NOT IN ('ARCHIVED', 'COMPLETED');
  ```
- If count >= tenant quota_limit, reject with `QUOTA_EXCEEDED` error

### Cascading Deletes
- Deleting Tenant → cascades to all Projects, Tasks, User_Tenant, Automation_Rules, Event_Logs
- Deleting Project → cascades to all Tasks, Task_Assignees
- Deleting User → does NOT cascade (set foreign keys to NULL or prevent deletion if user owns projects)

---

## Database Migrations (Flyway)

**Initial Migration** (`V001__create_schema.sql`):
```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE subscription_tier AS ENUM ('FREE', 'PRO', 'ENTERPRISE');
CREATE TYPE user_role AS ENUM ('ADMINISTRATOR', 'EDITOR', 'VIEWER');
CREATE TYPE project_status AS ENUM ('PLANNING', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'ARCHIVED');
CREATE TYPE task_status AS ENUM ('TODO', 'IN_PROGRESS', 'BLOCKED', 'COMPLETED');
CREATE TYPE priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');
CREATE TYPE automation_action AS ENUM ('SEND_EMAIL', 'CALL_WEBHOOK', 'CREATE_TASK', 'SEND_NOTIFICATION');

-- Tenants table
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    subdomain VARCHAR(63) UNIQUE NOT NULL CHECK (subdomain ~ '^[a-z0-9-]{3,63}$'),
    name VARCHAR(255) NOT NULL,
    subscription_tier subscription_tier NOT NULL DEFAULT 'FREE',
    quota_limit INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- (Additional CREATE TABLE statements...)
```

---

## Next Steps

- ✅ Data model defined
- ⏳ Generate OpenAPI contracts (Phase 1 continued)
- ⏳ Create quickstart guide (Phase 1 continued)
