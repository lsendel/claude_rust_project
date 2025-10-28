-- =====================================================
-- Flyway Migration V3: Create Automation Tables
-- =====================================================
-- Description: Creates automation_rules and event_logs tables for Phase 6
-- Tables: automation_rules, event_logs
-- Author: Phase 6 Implementation
-- Date: 2025-10-27
-- =====================================================

-- =====================================================
-- Table: automation_rules
-- Description: Configurable automation rules triggered by domain events
-- =====================================================
CREATE TABLE automation_rules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    event_type VARCHAR(100) NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    conditions JSONB,
    action_config JSONB NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_by UUID,
    execution_count BIGINT NOT NULL DEFAULT 0,
    last_executed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraint
    CONSTRAINT fk_automation_rule_tenant FOREIGN KEY (tenant_id)
        REFERENCES tenants(id) ON DELETE CASCADE,

    -- Constraints
    CONSTRAINT chk_event_type_format CHECK (event_type ~ '^[a-z._]+$'),
    CONSTRAINT chk_action_type_format CHECK (action_type ~ '^[a-z_]+$'),
    CONSTRAINT chk_execution_count CHECK (execution_count >= 0)
);

-- Indexes for automation_rules table
CREATE INDEX idx_automation_rule_tenant_id ON automation_rules(tenant_id);
CREATE INDEX idx_automation_rule_event_type ON automation_rules(event_type);
CREATE INDEX idx_automation_rule_active ON automation_rules(is_active);
CREATE INDEX idx_automation_rule_tenant_event ON automation_rules(tenant_id, event_type, is_active);

-- Comments for automation_rules table
COMMENT ON TABLE automation_rules IS 'Configurable automation rules triggered by domain events';
COMMENT ON COLUMN automation_rules.event_type IS 'Event type that triggers this rule (e.g., task.status.changed)';
COMMENT ON COLUMN automation_rules.action_type IS 'Action to execute (e.g., send_email, call_webhook)';
COMMENT ON COLUMN automation_rules.conditions IS 'JSON conditions that must be met for rule to execute';
COMMENT ON COLUMN automation_rules.action_config IS 'JSON configuration for the action (e.g., email template, webhook URL)';
COMMENT ON COLUMN automation_rules.execution_count IS 'Number of times this rule has been executed';

-- =====================================================
-- Table: event_logs
-- Description: Audit trail for automation rule executions
-- =====================================================
CREATE TABLE event_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    automation_rule_id UUID,
    event_type VARCHAR(100) NOT NULL,
    action_type VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    event_payload JSONB,
    action_result JSONB,
    resource_id UUID,
    resource_type VARCHAR(50),
    error_message TEXT,
    error_stack_trace TEXT,
    execution_duration_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints
    CONSTRAINT fk_event_log_tenant FOREIGN KEY (tenant_id)
        REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_log_automation_rule FOREIGN KEY (automation_rule_id)
        REFERENCES automation_rules(id) ON DELETE SET NULL,

    -- Constraints
    CONSTRAINT chk_status CHECK (status IN ('SUCCESS', 'FAILED', 'SKIPPED', 'NO_RULES_MATCHED')),
    CONSTRAINT chk_execution_duration CHECK (execution_duration_ms IS NULL OR execution_duration_ms >= 0)
);

-- Indexes for event_logs table
CREATE INDEX idx_event_log_tenant_id ON event_logs(tenant_id);
CREATE INDEX idx_event_log_rule_id ON event_logs(automation_rule_id);
CREATE INDEX idx_event_log_event_type ON event_logs(event_type);
CREATE INDEX idx_event_log_created_at ON event_logs(created_at DESC);
CREATE INDEX idx_event_log_status ON event_logs(status);
CREATE INDEX idx_event_log_resource ON event_logs(resource_id, resource_type);
CREATE INDEX idx_event_log_tenant_created ON event_logs(tenant_id, created_at DESC);

-- Comments for event_logs table
COMMENT ON TABLE event_logs IS 'Audit trail for automation rule executions and domain events';
COMMENT ON COLUMN event_logs.automation_rule_id IS 'Automation rule that was executed (null if no rules matched)';
COMMENT ON COLUMN event_logs.status IS 'Execution status: SUCCESS, FAILED, SKIPPED, NO_RULES_MATCHED';
COMMENT ON COLUMN event_logs.event_payload IS 'JSON payload of the event that triggered automation';
COMMENT ON COLUMN event_logs.action_result IS 'JSON result returned from the action execution';
COMMENT ON COLUMN event_logs.resource_id IS 'ID of the resource that triggered the event (e.g., project ID, task ID)';
COMMENT ON COLUMN event_logs.resource_type IS 'Type of resource (e.g., project, task)';
COMMENT ON COLUMN event_logs.execution_duration_ms IS 'Time taken to execute automation in milliseconds';

-- =====================================================
-- Trigger: Update updated_at timestamp
-- =====================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_automation_rule_updated_at
    BEFORE UPDATE ON automation_rules
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- End of Migration V3
-- =====================================================
