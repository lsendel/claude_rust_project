import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useTenant } from '../contexts/TenantContext';
import {
  automationService,
  AutomationRule,
  EventLog,
} from '../services/automationService';
import RuleCard from '../components/automation/RuleCard';
import RuleModal from '../components/automation/RuleModal';
import EventLogTable from '../components/automation/EventLogTable';
import AlertBanner from '../components/automation/AlertBanner';

/**
 * Automation page for managing automation rules and viewing event logs.
 * Refactored to meet PMAT thresholds: Cyc≤15, Cog≤30, LOC≤500
 */
const AutomationPage: React.FC = () => {
  const { user } = useAuth();
  const { tenant } = useTenant();

  // State
  const [rules, setRules] = useState<AutomationRule[]>([]);
  const [logs, setLogs] = useState<EventLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingLogs, setLoadingLogs] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'rules' | 'logs'>('rules');
  const [showModal, setShowModal] = useState(false);
  const [editingRule, setEditingRule] = useState<AutomationRule | null>(null);
  const [formData, setFormData] = useState<Partial<AutomationRule>>({
    name: '',
    eventType: 'task.status.changed',
    actionType: 'send_email',
    conditions: {},
    actionConfig: {},
    isActive: true,
  });

  // Load data on mount
  useEffect(() => {
    if (user && tenant) {
      loadRules();
      loadLogs();
    }
  }, [user, tenant]);

  const loadRules = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await automationService.getAllRules();
      setRules(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load automation rules');
    } finally {
      setLoading(false);
    }
  };

  const loadLogs = async () => {
    try {
      setLoadingLogs(true);
      const data = await automationService.getRecentLogs(50);
      setLogs(data);
    } catch (err: any) {
      console.error('Failed to load event logs:', err);
    } finally {
      setLoadingLogs(false);
    }
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    try {
      await automationService.createRule(formData as AutomationRule);
      setSuccess('Automation rule created successfully');
      setShowModal(false);
      resetForm();
      loadRules();
    } catch (err: any) {
      if (err.response?.status === 403) {
        setError('You do not have permission to create automation rules. Only administrators can manage automations.');
      } else {
        setError(err.response?.data?.message || 'Failed to create automation rule');
      }
    }
  };

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingRule?.id) return;

    setError(null);
    setSuccess(null);

    try {
      await automationService.updateRule(editingRule.id, formData);
      setSuccess('Automation rule updated successfully');
      setShowModal(false);
      setEditingRule(null);
      resetForm();
      loadRules();
    } catch (err: any) {
      if (err.response?.status === 403) {
        setError('You do not have permission to update automation rules.');
      } else {
        setError(err.response?.data?.message || 'Failed to update automation rule');
      }
    }
  };

  const handleToggleStatus = async (ruleId: string, isActive: boolean) => {
    setError(null);
    setSuccess(null);

    try {
      await automationService.toggleRuleStatus(ruleId, !isActive);
      setSuccess(`Automation rule ${!isActive ? 'enabled' : 'disabled'} successfully`);
      loadRules();
    } catch (err: any) {
      if (err.response?.status === 403) {
        setError('You do not have permission to toggle automation rules.');
      } else {
        setError(err.response?.data?.message || 'Failed to toggle automation rule');
      }
    }
  };

  const handleDelete = async (id: string, name: string) => {
    if (!window.confirm(`Are you sure you want to delete the automation rule "${name}"?`)) return;

    setError(null);
    setSuccess(null);

    try {
      await automationService.deleteRule(id);
      setSuccess('Automation rule deleted successfully');
      loadRules();
    } catch (err: any) {
      if (err.response?.status === 403) {
        setError('You do not have permission to delete automation rules.');
      } else {
        setError(err.response?.data?.message || 'Failed to delete automation rule');
      }
    }
  };

  const openEditModal = (rule: AutomationRule) => {
    setEditingRule(rule);
    setFormData({
      name: rule.name,
      eventType: rule.eventType,
      actionType: rule.actionType,
      conditions: rule.conditions || {},
      actionConfig: rule.actionConfig,
      isActive: rule.isActive,
    });
    setShowModal(true);
  };

  const openCreateModal = () => {
    resetForm();
    setEditingRule(null);
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
    setEditingRule(null);
    resetForm();
  };

  const resetForm = () => {
    setFormData({
      name: '',
      eventType: 'task.status.changed',
      actionType: 'send_email',
      conditions: {},
      actionConfig: {},
      isActive: true,
    });
  };

  const handleFormChange = (field: keyof AutomationRule, value: any) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = editingRule ? handleUpdate : handleCreate;

  // Styles
  const containerStyle: React.CSSProperties = {
    padding: '24px',
    maxWidth: '1400px',
    margin: '0 auto',
  };

  const headerStyle: React.CSSProperties = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '24px',
  };

  const titleStyle: React.CSSProperties = {
    fontSize: '28px',
    fontWeight: 'bold',
    color: '#1f2937',
  };

  const createButtonStyle: React.CSSProperties = {
    padding: '10px 20px',
    backgroundColor: '#2563eb',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: '500',
  };

  const tabsStyle: React.CSSProperties = {
    display: 'flex',
    gap: '8px',
    marginBottom: '24px',
    borderBottom: '2px solid #e5e7eb',
  };

  const tabStyle = (isActive: boolean): React.CSSProperties => ({
    padding: '12px 24px',
    backgroundColor: 'transparent',
    border: 'none',
    borderBottom: isActive ? '2px solid #2563eb' : 'none',
    color: isActive ? '#2563eb' : '#6b7280',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: '500',
    marginBottom: '-2px',
  });

  const emptyStateStyle: React.CSSProperties = {
    textAlign: 'center',
    padding: '48px',
    color: '#6b7280',
  };

  return (
    <div style={containerStyle}>
      <div style={headerStyle}>
        <h1 style={titleStyle}>Automation Rules</h1>
        {user && (
          <button style={createButtonStyle} onClick={openCreateModal}>
            + Create Rule
          </button>
        )}
      </div>

      {error && <AlertBanner message={error} type="error" onClose={() => setError(null)} />}
      {success && <AlertBanner message={success} type="success" onClose={() => setSuccess(null)} />}

      <div style={tabsStyle}>
        <button style={tabStyle(activeTab === 'rules')} onClick={() => setActiveTab('rules')}>
          Automation Rules ({rules.length})
        </button>
        <button style={tabStyle(activeTab === 'logs')} onClick={() => setActiveTab('logs')}>
          Event Logs ({logs.length})
        </button>
      </div>

      {loading ? (
        <div style={emptyStateStyle}>Loading...</div>
      ) : activeTab === 'rules' ? (
        rules.length === 0 ? (
          <div style={emptyStateStyle}>
            <p style={{ fontSize: '16px', marginBottom: '8px' }}>No automation rules yet</p>
            <p style={{ fontSize: '14px' }}>Create your first automation rule to get started</p>
          </div>
        ) : (
          <div>
            {rules.map((rule) => (
              <RuleCard
                key={rule.id}
                rule={rule}
                onEdit={openEditModal}
                onDelete={handleDelete}
                onToggleStatus={handleToggleStatus}
              />
            ))}
          </div>
        )
      ) : (
        <EventLogTable logs={logs} loading={loadingLogs} />
      )}

      <RuleModal
        isOpen={showModal}
        isEditMode={!!editingRule}
        formData={formData}
        onSubmit={handleSubmit}
        onClose={closeModal}
        onChange={handleFormChange}
      />
    </div>
  );
};

export default AutomationPage;
