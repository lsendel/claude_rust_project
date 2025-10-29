import React from 'react';
import { Task } from '../../services/taskService';
import StatusBadge from './StatusBadge';
import PriorityBadge from './PriorityBadge';
import ProgressBar from './ProgressBar';

interface TaskRowProps {
  task: Task;
  projectName: string;
  onEdit: (task: Task) => void;
  onDelete: (id: string) => void;
}

/**
 * Table row component for displaying a single task.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const TaskRow: React.FC<TaskRowProps> = ({ task, projectName, onEdit, onDelete }) => {
  const isOverdue = (dueDate?: string): boolean => {
    if (!dueDate) return false;
    return new Date(dueDate) < new Date();
  };

  const tdStyle: React.CSSProperties = {
    padding: '16px',
    borderBottom: '1px solid #f3f4f6',
  };

  const overdueBadgeStyle: React.CSSProperties = {
    display: 'inline-block',
    padding: '4px 12px',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: '500',
    backgroundColor: '#fecaca',
    color: '#991b1b',
    marginLeft: '8px',
  };

  const actionButtonStyle: React.CSSProperties = {
    padding: '6px 12px',
    marginRight: '8px',
    border: '1px solid #d1d5db',
    borderRadius: '4px',
    backgroundColor: 'white',
    cursor: 'pointer',
    fontSize: '13px',
  };

  const deleteButtonStyle: React.CSSProperties = {
    ...actionButtonStyle,
    color: '#dc2626',
    borderColor: '#dc2626',
  };

  return (
    <tr>
      <td style={tdStyle}>
        <div style={{ fontWeight: '500', color: '#1f2937' }}>
          {task.name}
          {isOverdue(task.dueDate) && task.status !== 'COMPLETED' && (
            <span style={overdueBadgeStyle}>Overdue</span>
          )}
        </div>
        {task.description && (
          <div style={{ fontSize: '13px', color: '#6b7280', marginTop: '4px' }}>
            {task.description}
          </div>
        )}
      </td>
      <td style={tdStyle}>
        <span style={{ fontSize: '14px', color: '#4b5563' }}>{projectName}</span>
      </td>
      <td style={tdStyle}>
        <StatusBadge status={task.status} />
      </td>
      <td style={tdStyle}>
        <PriorityBadge priority={task.priority} />
      </td>
      <td style={tdStyle}>
        <ProgressBar progress={task.progressPercentage || 0} />
      </td>
      <td style={tdStyle}>
        {task.dueDate ? (
          <span
            style={{
              fontSize: '14px',
              color: isOverdue(task.dueDate) && task.status !== 'COMPLETED' ? '#dc2626' : '#4b5563',
            }}
          >
            {new Date(task.dueDate).toLocaleDateString()}
          </span>
        ) : (
          <span style={{ fontSize: '14px', color: '#9ca3af' }}>-</span>
        )}
      </td>
      <td style={tdStyle}>
        <button
          style={actionButtonStyle}
          onClick={() => onEdit(task)}
          onMouseOver={(e) => (e.currentTarget.style.backgroundColor = '#f3f4f6')}
          onMouseOut={(e) => (e.currentTarget.style.backgroundColor = 'white')}
        >
          Edit
        </button>
        <button
          style={deleteButtonStyle}
          onClick={() => task.id && onDelete(task.id)}
          onMouseOver={(e) => (e.currentTarget.style.backgroundColor = '#fef2f2')}
          onMouseOut={(e) => (e.currentTarget.style.backgroundColor = 'white')}
        >
          Delete
        </button>
      </td>
    </tr>
  );
};

export default TaskRow;
