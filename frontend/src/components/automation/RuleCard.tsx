import React from 'react';
import { AutomationRule } from '../../services/automationService';

interface RuleCardProps {
  rule: AutomationRule;
  onEdit: (rule: AutomationRule) => void;
  onDelete: (id: string, name: string) => void;
  onToggleStatus: (id: string, isActive: boolean) => void;
}

/**
 * Card component for displaying an automation rule with actions.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const RuleCard: React.FC<RuleCardProps> = ({ rule, onEdit, onDelete, onToggleStatus }) => {
  const cardStyle: React.CSSProperties = {
    backgroundColor: 'white',
    borderRadius: '8px',
    padding: '24px',
    boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
    marginBottom: '16px',
  };

  const headerStyle: React.CSSProperties = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '12px',
  };

  const nameStyle: React.CSSProperties = {
    fontSize: '18px',
    fontWeight: '600',
    color: '#1f2937',
  };

  const actionsStyle: React.CSSProperties = {
    display: 'flex',
    gap: '8px',
  };

  const buttonStyle: React.CSSProperties = {
    padding: '6px 12px',
    border: '1px solid #d1d5db',
    borderRadius: '4px',
    backgroundColor: 'white',
    cursor: 'pointer',
    fontSize: '13px',
  };

  const toggleButtonStyle = (isActive: boolean): React.CSSProperties => ({
    ...buttonStyle,
    backgroundColor: isActive ? '#10b981' : '#6b7280',
    color: 'white',
    border: 'none',
  });

  const deleteButtonStyle: React.CSSProperties = {
    ...buttonStyle,
    color: '#dc2626',
    borderColor: '#dc2626',
  };

  const detailsStyle: React.CSSProperties = {
    fontSize: '14px',
    color: '#6b7280',
    marginBottom: '8px',
  };

  return (
    <div style={cardStyle}>
      <div style={headerStyle}>
        <div style={nameStyle}>{rule.name}</div>
        <div style={actionsStyle}>
          <button
            style={toggleButtonStyle(rule.isActive || false)}
            onClick={() => onToggleStatus(rule.id!, rule.isActive || false)}
          >
            {rule.isActive ? 'Active' : 'Inactive'}
          </button>
          <button style={buttonStyle} onClick={() => onEdit(rule)}>
            Edit
          </button>
          <button style={deleteButtonStyle} onClick={() => onDelete(rule.id!, rule.name)}>
            Delete
          </button>
        </div>
      </div>
      <div style={detailsStyle}>
        <strong>Event:</strong> {rule.eventType} → <strong>Action:</strong> {rule.actionType}
      </div>
      <div style={detailsStyle}>
        <strong>Executions:</strong> {rule.executionCount || 0}
        {rule.lastExecutedAt && (
          <>
            {' '}
            • <strong>Last Run:</strong> {new Date(rule.lastExecutedAt).toLocaleString()}
          </>
        )}
      </div>
    </div>
  );
};

export default RuleCard;
