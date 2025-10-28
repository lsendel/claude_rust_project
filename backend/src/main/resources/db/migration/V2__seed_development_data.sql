-- =====================================================
-- Flyway Migration V2: Seed Development Data
-- =====================================================
-- Description: Seeds sample data for local development and testing
-- WARNING: This migration should ONLY be applied to development/test environments
-- Author: Generated for development purposes
-- Date: 2025-10-27
-- =====================================================

-- Check if we're in a development environment
-- This migration should be manually excluded in production

-- =====================================================
-- Sample Tenants
-- =====================================================

-- Demo tenant (FREE tier)
INSERT INTO tenants (id, subdomain, name, description, subscription_tier, quota_limit, is_active, created_at, updated_at)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'demo',
    'Demo Organization',
    'Sample organization for testing the platform',
    'FREE',
    50,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Acme Corporation (PRO tier)
INSERT INTO tenants (id, subdomain, name, description, subscription_tier, quota_limit, is_active, created_at, updated_at)
VALUES (
    '22222222-2222-2222-2222-222222222222',
    'acme',
    'Acme Corporation',
    'Professional tier organization with expanded quota',
    'PRO',
    1000,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Enterprise Corp (ENTERPRISE tier)
INSERT INTO tenants (id, subdomain, name, description, subscription_tier, quota_limit, is_active, created_at, updated_at)
VALUES (
    '33333333-3333-3333-3333-333333333333',
    'enterprise',
    'Enterprise Corporation',
    'Enterprise tier organization with unlimited resources',
    'ENTERPRISE',
    NULL,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- =====================================================
-- Sample Users
-- =====================================================

-- Admin user
INSERT INTO users (id, cognito_user_id, email, name, created_at, last_login_at)
VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'pending-admin-user',
    'admin@demo.com',
    'Admin User',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Regular user
INSERT INTO users (id, cognito_user_id, email, name, created_at, last_login_at)
VALUES (
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'pending-john-doe',
    'john@acme.com',
    'John Doe',
    CURRENT_TIMESTAMP,
    NULL
);

-- Editor user
INSERT INTO users (id, cognito_user_id, email, name, created_at, last_login_at)
VALUES (
    'cccccccc-cccc-cccc-cccc-cccccccccccc',
    'pending-jane-smith',
    'jane@acme.com',
    'Jane Smith',
    CURRENT_TIMESTAMP,
    NULL
);

-- Viewer user
INSERT INTO users (id, cognito_user_id, email, name, created_at, last_login_at)
VALUES (
    'dddddddd-dddd-dddd-dddd-dddddddddddd',
    'pending-bob-wilson',
    'bob@enterprise.com',
    'Bob Wilson',
    CURRENT_TIMESTAMP,
    NULL
);

-- =====================================================
-- Sample User-Tenant Relationships
-- =====================================================

-- Admin user is administrator of Demo tenant
INSERT INTO user_tenants (user_id, tenant_id, role, invited_by, joined_at)
VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    '11111111-1111-1111-1111-111111111111',
    'ADMINISTRATOR',
    NULL,
    CURRENT_TIMESTAMP
);

-- John Doe is administrator of Acme
INSERT INTO user_tenants (user_id, tenant_id, role, invited_by, joined_at)
VALUES (
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    '22222222-2222-2222-2222-222222222222',
    'ADMINISTRATOR',
    NULL,
    CURRENT_TIMESTAMP
);

-- Jane Smith is editor in Acme (invited by John)
INSERT INTO user_tenants (user_id, tenant_id, role, invited_by, joined_at)
VALUES (
    'cccccccc-cccc-cccc-cccc-cccccccccccc',
    '22222222-2222-2222-2222-222222222222',
    'EDITOR',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    CURRENT_TIMESTAMP
);

-- Bob Wilson is viewer in Enterprise
INSERT INTO user_tenants (user_id, tenant_id, role, invited_by, joined_at)
VALUES (
    'dddddddd-dddd-dddd-dddd-dddddddddddd',
    '33333333-3333-3333-3333-333333333333',
    'VIEWER',
    NULL,
    CURRENT_TIMESTAMP
);

-- =====================================================
-- Sample Projects
-- =====================================================

-- Project for Demo tenant
INSERT INTO projects (id, tenant_id, name, description, status, due_date, owner_id, progress_percentage, priority, created_at, updated_at)
VALUES (
    '11111111-aaaa-aaaa-aaaa-111111111111',
    '11111111-1111-1111-1111-111111111111',
    'Platform Evaluation',
    'Evaluate the SaaS platform features and capabilities',
    'ACTIVE',
    CURRENT_DATE + INTERVAL '30 days',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    25,
    'HIGH',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Project for Acme tenant
INSERT INTO projects (id, tenant_id, name, description, status, due_date, owner_id, progress_percentage, priority, created_at, updated_at)
VALUES (
    '22222222-aaaa-aaaa-aaaa-222222222222',
    '22222222-2222-2222-2222-222222222222',
    'Q4 Product Launch',
    'Launch new product line for Q4 2025',
    'PLANNING',
    CURRENT_DATE + INTERVAL '90 days',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    10,
    'CRITICAL',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO projects (id, tenant_id, name, description, status, due_date, owner_id, progress_percentage, priority, created_at, updated_at)
VALUES (
    '22222222-bbbb-bbbb-bbbb-222222222222',
    '22222222-2222-2222-2222-222222222222',
    'Website Redesign',
    'Redesign company website with modern UI/UX',
    'ACTIVE',
    CURRENT_DATE + INTERVAL '60 days',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    45,
    'MEDIUM',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Completed project for Acme
INSERT INTO projects (id, tenant_id, name, description, status, due_date, owner_id, progress_percentage, priority, created_at, updated_at)
VALUES (
    '22222222-cccc-cccc-cccc-222222222222',
    '22222222-2222-2222-2222-222222222222',
    'Office Setup',
    'Setup new office location and infrastructure',
    'COMPLETED',
    CURRENT_DATE - INTERVAL '10 days',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    100,
    'HIGH',
    CURRENT_TIMESTAMP - INTERVAL '90 days',
    CURRENT_TIMESTAMP - INTERVAL '10 days'
);

-- =====================================================
-- Sample Tasks
-- =====================================================

-- Tasks for "Platform Evaluation" project
INSERT INTO tasks (id, tenant_id, project_id, name, description, status, due_date, progress_percentage, priority, created_at, updated_at)
VALUES (
    '11111111-aaaa-aaaa-aaaa-111111111111',
    '11111111-1111-1111-1111-111111111111',
    '11111111-aaaa-aaaa-aaaa-111111111111',
    'Test tenant registration',
    'Verify subdomain validation and tenant creation flow',
    'COMPLETED',
    CURRENT_DATE + INTERVAL '5 days',
    100,
    'HIGH',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO tasks (id, tenant_id, project_id, name, description, status, due_date, progress_percentage, priority, created_at, updated_at)
VALUES (
    '11111111-bbbb-bbbb-bbbb-111111111111',
    '11111111-1111-1111-1111-111111111111',
    '11111111-aaaa-aaaa-aaaa-111111111111',
    'Test OAuth2 login',
    'Test login with Google, Facebook, and GitHub providers',
    'IN_PROGRESS',
    CURRENT_DATE + INTERVAL '7 days',
    60,
    'HIGH',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO tasks (id, tenant_id, project_id, name, description, status, due_date, progress_percentage, priority, created_at, updated_at)
VALUES (
    '11111111-cccc-cccc-cccc-111111111111',
    '11111111-1111-1111-1111-111111111111',
    '11111111-aaaa-aaaa-aaaa-111111111111',
    'Review dashboard features',
    'Review quota display and navigation features',
    'TODO',
    CURRENT_DATE + INTERVAL '10 days',
    0,
    'MEDIUM',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Tasks for "Q4 Product Launch" project
INSERT INTO tasks (id, tenant_id, project_id, name, description, status, due_date, progress_percentage, priority, created_at, updated_at)
VALUES (
    '22222222-aaaa-aaaa-aaaa-111111111111',
    '22222222-2222-2222-2222-222222222222',
    '22222222-aaaa-aaaa-aaaa-222222222222',
    'Market research',
    'Conduct market research for target demographics',
    'TODO',
    CURRENT_DATE + INTERVAL '20 days',
    0,
    'CRITICAL',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO tasks (id, tenant_id, project_id, name, description, status, due_date, progress_percentage, priority, created_at, updated_at)
VALUES (
    '22222222-bbbb-bbbb-bbbb-111111111111',
    '22222222-2222-2222-2222-222222222222',
    '22222222-aaaa-aaaa-aaaa-222222222222',
    'Create marketing materials',
    'Design brochures, website content, and social media assets',
    'TODO',
    CURRENT_DATE + INTERVAL '40 days',
    0,
    'HIGH',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Tasks for "Website Redesign" project
INSERT INTO tasks (id, tenant_id, project_id, name, description, status, due_date, progress_percentage, priority, created_at, updated_at)
VALUES (
    '22222222-cccc-cccc-cccc-111111111111',
    '22222222-2222-2222-2222-222222222222',
    '22222222-bbbb-bbbb-bbbb-222222222222',
    'Design mockups',
    'Create high-fidelity mockups in Figma',
    'COMPLETED',
    CURRENT_DATE - INTERVAL '5 days',
    100,
    'HIGH',
    CURRENT_TIMESTAMP - INTERVAL '30 days',
    CURRENT_TIMESTAMP - INTERVAL '5 days'
);

INSERT INTO tasks (id, tenant_id, project_id, name, description, status, due_date, progress_percentage, priority, created_at, updated_at)
VALUES (
    '22222222-dddd-dddd-dddd-111111111111',
    '22222222-2222-2222-2222-222222222222',
    '22222222-bbbb-bbbb-bbbb-222222222222',
    'Implement frontend',
    'Build responsive frontend with React',
    'IN_PROGRESS',
    CURRENT_DATE + INTERVAL '20 days',
    50,
    'HIGH',
    CURRENT_TIMESTAMP - INTERVAL '10 days',
    CURRENT_TIMESTAMP
);

INSERT INTO tasks (id, tenant_id, project_id, name, description, status, due_date, progress_percentage, priority, created_at, updated_at)
VALUES (
    '22222222-eeee-eeee-eeee-111111111111',
    '22222222-2222-2222-2222-222222222222',
    '22222222-bbbb-bbbb-bbbb-222222222222',
    'Content migration',
    'Migrate existing content to new structure',
    'TODO',
    CURRENT_DATE + INTERVAL '30 days',
    0,
    'MEDIUM',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO tasks (id, tenant_id, project_id, name, description, status, due_date, progress_percentage, priority, created_at, updated_at)
VALUES (
    '22222222-ffff-ffff-ffff-111111111111',
    '22222222-2222-2222-2222-222222222222',
    '22222222-bbbb-bbbb-bbbb-222222222222',
    'SEO optimization',
    'Implement SEO best practices and meta tags',
    'TODO',
    CURRENT_DATE + INTERVAL '45 days',
    0,
    'LOW',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Overdue task example
INSERT INTO tasks (id, tenant_id, project_id, name, description, status, due_date, progress_percentage, priority, created_at, updated_at)
VALUES (
    '22222222-0000-0000-0000-111111111111',
    '22222222-2222-2222-2222-222222222222',
    '22222222-bbbb-bbbb-bbbb-222222222222',
    'Browser testing',
    'Test website across different browsers and devices',
    'BLOCKED',
    CURRENT_DATE - INTERVAL '2 days',
    20,
    'CRITICAL',
    CURRENT_TIMESTAMP - INTERVAL '15 days',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
);

-- =====================================================
-- Verification Queries (for manual testing)
-- =====================================================

-- Verify tenant count
-- SELECT COUNT(*) as tenant_count FROM tenants;

-- Verify user count
-- SELECT COUNT(*) as user_count FROM users;

-- Verify relationships
-- SELECT t.name, u.name, ut.role
-- FROM user_tenants ut
-- JOIN tenants t ON ut.tenant_id = t.id
-- JOIN users u ON ut.user_id = u.id;

-- Verify project count per tenant
-- SELECT t.name, COUNT(p.id) as project_count
-- FROM tenants t
-- LEFT JOIN projects p ON t.id = p.tenant_id
-- GROUP BY t.id, t.name;

-- Verify task count per project
-- SELECT p.name, COUNT(t.id) as task_count, AVG(t.progress_percentage) as avg_progress
-- FROM projects p
-- LEFT JOIN tasks t ON p.id = t.project_id
-- GROUP BY p.id, p.name;

-- =====================================================
-- Migration Complete
-- =====================================================

COMMENT ON EXTENSION "uuid-ossp" IS 'Provides UUID generation functions';
