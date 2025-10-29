# Entity Specification for Multi-Tenant SaaS Platform

## Requirements

Generate JPA entity classes for a multi-tenant SaaS platform with the following requirements:

### 1. Tenant Entity
**Purpose**: Represents an organization/customer account
**Package**: `com.platform.saas.model`

**Fields**:
- `id`: UUID (primary key, auto-generated)
- `subdomain`: String (unique, 3-63 chars, alphanumeric + hyphens)
- `name`: String (not null, max 255 chars)
- `subscriptionTier`: Enum (FREE, PRO, ENTERPRISE, default FREE)
- `quotaLimit`: Integer (50 for FREE, 1000 for PRO, null for ENTERPRISE)
- `createdAt`: LocalDateTime (not null, auto-set on creation)
- `updatedAt`: LocalDateTime (not null, auto-update)

**Constraints**:
- Subdomain must match pattern: ^[a-z0-9-]{3,63}$
- Reserved subdomains: www, api, admin, app, platform

### 2. User Entity
**Purpose**: Represents an individual with authentication credentials
**Package**: `com.platform.saas.model`

**Fields**:
- `id`: UUID (primary key, auto-generated)
- `cognitoUserId`: String (unique, not null, max 255 chars)
- `email`: String (unique, not null, max 255 chars, email format)
- `name`: String (max 255 chars)
- `createdAt`: LocalDateTime (not null, auto-set)
- `lastLoginAt`: LocalDateTime (nullable)

**Relationships**:
- Many-to-many with Tenant (via UserTenant join table)

### 3. UserTenant Entity (Join Table)
**Purpose**: Maps users to tenants with tenant-specific roles
**Package**: `com.platform.saas.model`

**Fields**:
- `userId`: UUID (FK to User, part of composite key)
- `tenantId`: UUID (FK to Tenant, part of composite key)
- `role`: Enum (ADMINISTRATOR, EDITOR, VIEWER, not null)
- `invitedBy`: UUID (FK to User, nullable)
- `joinedAt`: LocalDateTime (not null, auto-set)

**Composite Key**: (userId, tenantId)

### 4. Project Entity
**Purpose**: Core domain entity for project management
**Package**: `com.platform.saas.model`

**Fields**:
- `id`: UUID (primary key, auto-generated)
- `tenantId`: UUID (FK to Tenant, not null, indexed)
- `name`: String (not null, max 255 chars)
- `description`: Text (nullable)
- `status`: Enum (PLANNING, ACTIVE, ON_HOLD, COMPLETED, ARCHIVED, default PLANNING)
- `dueDate`: LocalDate (nullable)
- `ownerId`: UUID (FK to User, not null)
- `progressPercentage`: Integer (0-100, default 0)
- `priority`: Enum (LOW, MEDIUM, HIGH, CRITICAL, default MEDIUM)
- `createdAt`: LocalDateTime (not null, auto-set)
- `updatedAt`: LocalDateTime (not null, auto-update)

**Indexes**:
- (tenant_id, status)
- (tenant_id, due_date)

### 5. Task Entity
**Purpose**: Sub-entity of Project for actionable work items
**Package**: `com.platform.saas.model`

**Fields**:
- `id`: UUID (primary key, auto-generated)
- `tenantId`: UUID (FK to Tenant, not null, indexed)
- `projectId`: UUID (FK to Project, not null)
- `name`: String (not null, max 255 chars)
- `description`: Text (nullable)
- `status`: Enum (TODO, IN_PROGRESS, BLOCKED, COMPLETED, default TODO)
- `dueDate`: LocalDate (nullable)
- `progressPercentage`: Integer (0-100, default 0)
- `priority`: Enum (LOW, MEDIUM, HIGH, CRITICAL, default MEDIUM)
- `createdAt`: LocalDateTime (not null, auto-set)
- `updatedAt`: LocalDateTime (not null, auto-update)

**Relationships**:
- Many-to-many with User via TaskAssignee (assignees)
- Self-referencing many-to-many via TaskDependency (dependencies)

**Indexes**:
- (tenant_id, project_id)
- (tenant_id, status)

## Quality Standards

- Use Lombok annotations (@Data, @Entity, @Table, @NoArgsConstructor, @AllArgsConstructor)
- All entities must have proper JPA annotations
- All timestamp fields use @CreatedDate and @LastModifiedDate with @EntityListeners(AuditingEntityListener.class)
- All enums defined as separate enum classes
- Validation annotations (@NotNull, @Size, @Email, @Pattern) on fields
- Proper equals() and hashCode() based on id field only
- toString() excludes circular references
