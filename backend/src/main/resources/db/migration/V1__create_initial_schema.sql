-- =====================================================
-- Flyway Migration V1: Create Initial Database Schema
-- =====================================================
-- Description: Creates all core tables for multi-tenant SaaS platform
-- Tables: tenants, users, user_tenants, projects, tasks
-- Author: Generated from JPA entities
-- Date: 2025-10-27
-- =====================================================

-- Enable UUID extension for PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- Table: tenants
-- Description: Multi-tenant organizations with subscription tiers
-- =====================================================
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    subdomain VARCHAR(63) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    subscription_tier VARCHAR(20) NOT NULL DEFAULT 'FREE',
    quota_limit INTEGER,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_subdomain_format CHECK (subdomain ~ '^[a-z0-9-]{3,63}$'),
    CONSTRAINT chk_subscription_tier CHECK (subscription_tier IN ('FREE', 'PRO', 'ENTERPRISE')),
    CONSTRAINT chk_quota_limit CHECK (quota_limit IS NULL OR quota_limit > 0)
);

-- Indexes for tenants table
CREATE UNIQUE INDEX idx_tenant_subdomain ON tenants(subdomain);
CREATE INDEX idx_tenant_created_at ON tenants(created_at);
CREATE INDEX idx_tenant_subscription_tier ON tenants(subscription_tier);

-- Comments for tenants table
COMMENT ON TABLE tenants IS 'Multi-tenant organizations with subscription tiers and quotas';
COMMENT ON COLUMN tenants.subdomain IS 'Unique subdomain identifier (e.g., acme for acme.platform.com)';
COMMENT ON COLUMN tenants.subscription_tier IS 'Subscription tier: FREE (50), PRO (1000), ENTERPRISE (unlimited)';
COMMENT ON COLUMN tenants.quota_limit IS 'Maximum number of projects + tasks (null = unlimited)';

-- =====================================================
-- Table: users
-- Description: User authentication with AWS Cognito integration
-- =====================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cognito_user_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$')
);

-- Indexes for users table
CREATE UNIQUE INDEX idx_user_cognito_id ON users(cognito_user_id);
CREATE UNIQUE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_created_at ON users(created_at);

-- Comments for users table
COMMENT ON TABLE users IS 'User authentication with AWS Cognito integration';
COMMENT ON COLUMN users.cognito_user_id IS 'AWS Cognito user identifier (sub claim from JWT)';
COMMENT ON COLUMN users.last_login_at IS 'Last successful login timestamp';

-- =====================================================
-- Table: user_tenants
-- Description: Many-to-many relationship between users and tenants with roles
-- =====================================================
CREATE TABLE user_tenants (
    user_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    invited_by UUID,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Primary key
    PRIMARY KEY (user_id, tenant_id),

    -- Foreign keys
    CONSTRAINT fk_user_tenant_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_tenant_tenant FOREIGN KEY (tenant_id)
        REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_tenant_inviter FOREIGN KEY (invited_by)
        REFERENCES users(id) ON DELETE SET NULL,

    -- Constraints
    CONSTRAINT chk_user_role CHECK (role IN ('ADMINISTRATOR', 'EDITOR', 'VIEWER'))
);

-- Indexes for user_tenants table
CREATE INDEX idx_user_tenant_tenant_id ON user_tenants(tenant_id);
CREATE INDEX idx_user_tenant_role ON user_tenants(tenant_id, role);
CREATE INDEX idx_user_tenant_joined_at ON user_tenants(joined_at);

-- Comments for user_tenants table
COMMENT ON TABLE user_tenants IS 'Many-to-many relationship between users and tenants with role-based access';
COMMENT ON COLUMN user_tenants.role IS 'User role within tenant: ADMINISTRATOR, EDITOR, or VIEWER';
COMMENT ON COLUMN user_tenants.invited_by IS 'User ID who invited this user to the tenant (null for self-registration)';

-- =====================================================
-- Table: projects
-- Description: Project management units within tenants
-- =====================================================
CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNING',
    due_date DATE,
    owner_id UUID NOT NULL,
    progress_percentage INTEGER NOT NULL DEFAULT 0,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_project_tenant FOREIGN KEY (tenant_id)
        REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_owner FOREIGN KEY (owner_id)
        REFERENCES users(id) ON DELETE RESTRICT,

    -- Constraints
    CONSTRAINT chk_project_status CHECK (status IN ('PLANNING', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'ARCHIVED')),
    CONSTRAINT chk_project_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_project_progress CHECK (progress_percentage >= 0 AND progress_percentage <= 100)
);

-- Indexes for projects table
CREATE INDEX idx_project_tenant_id ON projects(tenant_id);
CREATE INDEX idx_project_tenant_status ON projects(tenant_id, status);
CREATE INDEX idx_project_tenant_due_date ON projects(tenant_id, due_date);
CREATE INDEX idx_project_owner_id ON projects(owner_id);
CREATE INDEX idx_project_priority ON projects(priority);
CREATE INDEX idx_project_created_at ON projects(created_at);

-- Comments for projects table
COMMENT ON TABLE projects IS 'Project management units with multi-tenant isolation';
COMMENT ON COLUMN projects.tenant_id IS 'Tenant ID for multi-tenant data isolation';
COMMENT ON COLUMN projects.status IS 'Project lifecycle status';
COMMENT ON COLUMN projects.owner_id IS 'User ID who owns/manages this project';
COMMENT ON COLUMN projects.progress_percentage IS 'Overall project progress (0-100)';

-- =====================================================
-- Table: tasks
-- Description: Task work items belonging to projects
-- =====================================================
CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    project_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    due_date DATE,
    progress_percentage INTEGER NOT NULL DEFAULT 0,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_task_tenant FOREIGN KEY (tenant_id)
        REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_project FOREIGN KEY (project_id)
        REFERENCES projects(id) ON DELETE CASCADE,

    -- Constraints
    CONSTRAINT chk_task_status CHECK (status IN ('TODO', 'IN_PROGRESS', 'BLOCKED', 'COMPLETED')),
    CONSTRAINT chk_task_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_task_progress CHECK (progress_percentage >= 0 AND progress_percentage <= 100)
);

-- Indexes for tasks table
CREATE INDEX idx_task_tenant_id ON tasks(tenant_id);
CREATE INDEX idx_task_tenant_project ON tasks(tenant_id, project_id);
CREATE INDEX idx_task_tenant_status ON tasks(tenant_id, status);
CREATE INDEX idx_task_project_status ON tasks(project_id, status);
CREATE INDEX idx_task_priority ON tasks(priority);
CREATE INDEX idx_task_due_date ON tasks(due_date);
CREATE INDEX idx_task_created_at ON tasks(created_at);

-- Comments for tasks table
COMMENT ON TABLE tasks IS 'Task work items belonging to projects with multi-tenant isolation';
COMMENT ON COLUMN tasks.tenant_id IS 'Tenant ID for multi-tenant data isolation';
COMMENT ON COLUMN tasks.project_id IS 'Parent project ID';
COMMENT ON COLUMN tasks.status IS 'Task status: TODO, IN_PROGRESS, BLOCKED, or COMPLETED';
COMMENT ON COLUMN tasks.progress_percentage IS 'Task completion progress (0-100)';

-- =====================================================
-- Triggers: Automatic timestamp updates
-- =====================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for tenants table
CREATE TRIGGER trg_tenants_updated_at
    BEFORE UPDATE ON tenants
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for projects table
CREATE TRIGGER trg_projects_updated_at
    BEFORE UPDATE ON projects
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for tasks table
CREATE TRIGGER trg_tasks_updated_at
    BEFORE UPDATE ON tasks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- Initial Data: Reserved subdomains
-- =====================================================
-- Note: Reserved subdomain enforcement is handled in application layer
-- This is just for documentation purposes

COMMENT ON COLUMN tenants.subdomain IS
    'Reserved subdomains: www, api, admin, app, mail, email, support, help, docs, blog, status, cdn, assets, static, ftp, smtp, pop, imap, webmail, portal, dashboard';

-- =====================================================
-- Migration Complete
-- =====================================================
