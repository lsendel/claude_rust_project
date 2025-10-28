# Flyway Database Migrations

This directory contains Flyway migration scripts for the Multi-Tenant SaaS Platform database schema.

## Overview

Flyway is a database migration tool that tracks and applies schema changes in a versioned, repeatable manner. All migrations are automatically executed when the Spring Boot application starts.

## Migration Files

### V1__create_initial_schema.sql
**Description:** Creates the complete initial database schema
**Tables Created:**
- `tenants` - Multi-tenant organizations
- `users` - User authentication
- `user_tenants` - User-tenant relationships with roles
- `projects` - Project management
- `tasks` - Task work items

**Features:**
- UUID primary keys with automatic generation
- Foreign key constraints with appropriate cascade/restrict rules
- Check constraints for data validation
- Indexes for query performance
- Automatic timestamp updates via triggers
- Comprehensive table and column comments

### V2__seed_development_data.sql
**Description:** Seeds sample data for development and testing
**WARNING:** This migration should ONLY be applied to development/test environments, NOT production

**Sample Data:**
- 3 tenants (demo, acme, enterprise) with different subscription tiers
- 4 users with different roles
- User-tenant relationships demonstrating ADMINISTRATOR, EDITOR, VIEWER roles
- 4 projects in various states (PLANNING, ACTIVE, COMPLETED)
- 11 tasks demonstrating different statuses, priorities, and scenarios

## Migration Naming Convention

Flyway migrations follow the naming pattern: `V{version}__{description}.sql`

- `V` - Prefix indicating versioned migration
- `{version}` - Sequential version number (e.g., 1, 2, 3)
- `__` - Double underscore separator (required)
- `{description}` - Human-readable description using underscores

Examples:
- `V1__create_initial_schema.sql`
- `V2__seed_development_data.sql`
- `V3__add_automation_rules_table.sql`

## Running Migrations

### Automatic Execution
Migrations run automatically when the Spring Boot application starts. Spring Boot + Flyway configuration:

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

### Manual Execution via Maven
```bash
# Run all pending migrations
mvn flyway:migrate

# Show migration status
mvn flyway:info

# Validate applied migrations
mvn flyway:validate

# Repair migration history (if needed)
mvn flyway:repair

# Clean database (WARNING: Deletes all data)
mvn flyway:clean
```

### Docker PostgreSQL Setup
```bash
# Start PostgreSQL with Docker Compose
docker-compose up -d postgres

# Connect to database
docker exec -it saas_platform_postgres psql -U saas_user -d saas_platform

# View migration history
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

## Environment-Specific Migrations

### Development/Test Environments
Include seed data migration (V2):
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

### Production Environment
Exclude seed data migration:
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    ignore-migration-patterns: "*:pending,*:seed"
```

Or use Flyway callbacks to prevent V2 from running in production.

## Database Schema Verification

After migrations complete, verify the schema:

```sql
-- List all tables
\dt

-- View table structure
\d tenants
\d users
\d user_tenants
\d projects
\d tasks

-- Check indexes
\di

-- Check foreign keys
SELECT
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY';
```

## Sample Data Verification

After V2 migration, verify sample data:

```sql
-- View all tenants
SELECT subdomain, name, subscription_tier, quota_limit FROM tenants;

-- View users and their tenant memberships
SELECT
    u.name as user_name,
    u.email,
    t.name as tenant_name,
    ut.role
FROM users u
JOIN user_tenants ut ON u.id = ut.user_id
JOIN tenants t ON ut.tenant_id = t.id;

-- View projects by tenant
SELECT
    t.name as tenant,
    p.name as project,
    p.status,
    p.progress_percentage
FROM projects p
JOIN tenants t ON p.tenant_id = t.id
ORDER BY t.name, p.name;

-- View tasks by status
SELECT
    status,
    COUNT(*) as count
FROM tasks
GROUP BY status;

-- View overdue tasks
SELECT
    t.name as task_name,
    p.name as project_name,
    t.due_date,
    t.status
FROM tasks t
JOIN projects p ON t.project_id = p.id
WHERE t.due_date < CURRENT_DATE
  AND t.status != 'COMPLETED'
ORDER BY t.due_date;
```

## Rollback Strategy

Flyway does not support automatic rollback. For rollback:

1. **Create undo migrations** manually (e.g., `U1__undo_create_initial_schema.sql`)
2. **Restore from backup** if database backup exists
3. **Recreate database** from scratch if in development

Example undo migration:
```sql
-- U1__undo_create_initial_schema.sql
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS projects CASCADE;
DROP TABLE IF EXISTS user_tenants CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS tenants CASCADE;
DROP FUNCTION IF EXISTS update_updated_at_column();
```

## Troubleshooting

### Migration Fails: "checksum mismatch"
**Cause:** Migration file was modified after being applied
**Solution:**
```bash
# Option 1: Repair migration history (updates checksums)
mvn flyway:repair

# Option 2: Clean and re-migrate (DESTROYS ALL DATA)
mvn flyway:clean flyway:migrate
```

### Migration Fails: "constraint violation"
**Cause:** Data violates constraints or foreign keys
**Solution:**
1. Review migration SQL for constraint issues
2. Check foreign key order (tables must be created before references)
3. Verify sample data UUIDs match

### Application Fails: "relation does not exist"
**Cause:** Migrations haven't run or failed
**Solution:**
```bash
# Check migration status
mvn flyway:info

# Run pending migrations
mvn flyway:migrate

# Check Spring Boot logs for Flyway errors
```

### Want to Skip V2 in Production
**Solution:** Use Spring profiles

```yaml
# application-prod.yml
spring:
  flyway:
    locations: classpath:db/migration
    sql-migration-suffixes: .sql
    # Custom callback to skip V2
```

Or rename V2 to include environment suffix: `V2__seed_development_data.dev.sql`

## Adding New Migrations

1. Create new file with next version number: `V3__description.sql`
2. Write idempotent SQL (safe to run multiple times if possible)
3. Test migration on local PostgreSQL
4. Verify migration doesn't break existing data
5. Commit migration file to version control

Example:
```sql
-- V3__add_comments_table.sql
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    task_id UUID NOT NULL,
    user_id UUID NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comment_tenant FOREIGN KEY (tenant_id)
        REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_task FOREIGN KEY (task_id)
        REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_comment_task_id ON comments(task_id);
CREATE INDEX idx_comment_created_at ON comments(created_at);
```

## Best Practices

1. **Never modify applied migrations** - Create new migrations instead
2. **Use transactions** - Wrap DDL in transactions when possible
3. **Test migrations** - Always test on local/dev before production
4. **Keep migrations small** - One logical change per migration
5. **Add comments** - Document purpose and context
6. **Use constraints** - Enforce data integrity at database level
7. **Create indexes** - Add indexes for frequent queries
8. **Version control** - Always commit migration files
9. **Backup before production** - Take database backup before applying

## References

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Spring Boot Flyway Integration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

**Last Updated:** 2025-10-27
**Schema Version:** V2
**Database:** PostgreSQL 15
