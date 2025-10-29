import React from 'react';
import { AutomationRule } from '../../services/automationService';

interface RuleFormProps {
  formData: Partial<AutomationRule>;
  isEditMode: boolean;
  onSubmit: (e: React.FormEvent) => void;
  onCancel: () => void;
  onChange: (field: keyof AutomationRule, value: any) => void;
}

/**
 * Form component for creating and editing automation rules.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const RuleForm: React.FC<RuleFormProps> = ({
  formData,
  isEditMode,
  onSubmit,
  onCancel,
  onChange,
}) => {
  const formGroupStyle: React.CSSProperties = {
    marginBottom: '16px',
  };

  const labelStyle: React.CSSProperties = {
    display: 'block',
    marginBottom: '6px',
    fontSize: '14px',
    fontWeight: '500',
    color: '#374151',
  };

  const inputStyle: React.CSSProperties = {
    width: '100%',
    padding: '8px 12px',
    border: '1px solid #d1d5db',
    borderRadius: '6px',
    fontSize: '14px',
  };

  const buttonContainerStyle: React.CSSProperties = {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: '12px',
    marginTop: '20px',
  };

  const cancelButtonStyle: React.CSSProperties = {
    padding: '8px 16px',
    border: '1px solid #d1d5db',
    borderRadius: '6px',
    backgroundColor: 'white',
    cursor: 'pointer',
    fontSize: '14px',
  };

  const submitButtonStyle: React.CSSProperties = {
    padding: '8px 16px',
    backgroundColor: '#2563eb',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: '500',
  };

  return (
    <form onSubmit={onSubmit}>
      <div style={formGroupStyle}>
        <label style={labelStyle}>Rule Name *</label>
        <input
          type="text"
          style={inputStyle}
          value={formData.name || ''}
          onChange={(e) => onChange('name', e.target.value)}
          required
        />
      </div>

      <div style={formGroupStyle}>
        <label style={labelStyle}>Event Type *</label>
        <select
          style={inputStyle}
          value={formData.eventType || 'task.status.changed'}
          onChange={(e) => onChange('eventType', e.target.value)}
        >
          <option value="task.status.changed">Task Status Changed</option>
          <option value="task.created">Task Created</option>
          <option value="task.updated">Task Updated</option>
          <option value="task.deleted">Task Deleted</option>
          <option value="project.created">Project Created</option>
          <option value="project.updated">Project Updated</option>
          <option value="project.deleted">Project Deleted</option>
        </select>
      </div>

      <div style={formGroupStyle}>
        <label style={labelStyle}>Action Type *</label>
        <select
          style={inputStyle}
          value={formData.actionType || 'send_email'}
          onChange={(e) => onChange('actionType', e.target.value)}
        >
          <option value="send_email">Send Email</option>
          <option value="call_webhook">Call Webhook</option>
          <option value="create_task">Create Task</option>
          <option value="send_notification">Send Notification</option>
        </select>
      </div>

      <div style={buttonContainerStyle}>
        <button type="button" style={cancelButtonStyle} onClick={onCancel}>
          Cancel
        </button>
        <button type="submit" style={submitButtonStyle}>
          {isEditMode ? 'Update Rule' : 'Create Rule'}
        </button>
      </div>
    </form>
  );
};

export default RuleForm;
