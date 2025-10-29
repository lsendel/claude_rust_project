import React from 'react';
import { Project } from '../../services/projectService';
import { CreateTaskRequest } from '../../services/taskService';

type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'BLOCKED' | 'COMPLETED';
type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

interface TaskFormProps {
  formData: CreateTaskRequest;
  projects: Project[];
  isEditMode: boolean;
  progressPercentage?: number;
  onSubmit: (e: React.FormEvent) => void;
  onCancel: () => void;
  onChange: (field: keyof CreateTaskRequest, value: any) => void;
  onProgressChange?: (value: number) => void;
}

/**
 * Form component for creating and editing tasks.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const TaskForm: React.FC<TaskFormProps> = ({
  formData,
  projects,
  isEditMode,
  progressPercentage,
  onSubmit,
  onCancel,
  onChange,
  onProgressChange,
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

  const textareaStyle: React.CSSProperties = {
    ...inputStyle,
    minHeight: '80px',
    resize: 'vertical',
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

  const progressSliderStyle: React.CSSProperties = {
    width: '100%',
    marginTop: '8px',
  };

  return (
    <form onSubmit={onSubmit}>
      <div style={formGroupStyle}>
        <label style={labelStyle} htmlFor="projectId">
          Project *
        </label>
        <select
          id="projectId"
          style={inputStyle}
          value={formData.projectId}
          onChange={(e) => onChange('projectId', e.target.value)}
          required
        >
          <option value="">Select a project</option>
          {projects.map((project) => (
            <option key={project.id} value={project.id}>
              {project.name}
            </option>
          ))}
        </select>
      </div>

      <div style={formGroupStyle}>
        <label style={labelStyle} htmlFor="name">
          Task Name *
        </label>
        <input
          id="name"
          type="text"
          style={inputStyle}
          value={formData.name}
          onChange={(e) => onChange('name', e.target.value)}
          required
        />
      </div>

      <div style={formGroupStyle}>
        <label style={labelStyle} htmlFor="description">
          Description
        </label>
        <textarea
          id="description"
          style={textareaStyle}
          value={formData.description}
          onChange={(e) => onChange('description', e.target.value)}
        />
      </div>

      <div style={formGroupStyle}>
        <label style={labelStyle} htmlFor="status">
          Status
        </label>
        <select
          id="status"
          style={inputStyle}
          value={formData.status}
          onChange={(e) => onChange('status', e.target.value as TaskStatus)}
        >
          <option value="TODO">To Do</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="BLOCKED">Blocked</option>
          <option value="COMPLETED">Completed</option>
        </select>
      </div>

      <div style={formGroupStyle}>
        <label style={labelStyle} htmlFor="priority">
          Priority
        </label>
        <select
          id="priority"
          style={inputStyle}
          value={formData.priority}
          onChange={(e) => onChange('priority', e.target.value as Priority)}
        >
          <option value="LOW">Low</option>
          <option value="MEDIUM">Medium</option>
          <option value="HIGH">High</option>
          <option value="CRITICAL">Critical</option>
        </select>
      </div>

      {isEditMode && onProgressChange && (
        <div style={formGroupStyle}>
          <label style={labelStyle} htmlFor="progress">
            Progress: {progressPercentage || 0}%
          </label>
          <input
            id="progress"
            type="range"
            min="0"
            max="100"
            style={progressSliderStyle}
            value={progressPercentage || 0}
            onChange={(e) => onProgressChange(parseInt(e.target.value))}
          />
        </div>
      )}

      <div style={formGroupStyle}>
        <label style={labelStyle} htmlFor="dueDate">
          Due Date
        </label>
        <input
          id="dueDate"
          type="date"
          style={inputStyle}
          value={formData.dueDate}
          onChange={(e) => onChange('dueDate', e.target.value)}
        />
      </div>

      <div style={buttonContainerStyle}>
        <button type="button" style={cancelButtonStyle} onClick={onCancel}>
          Cancel
        </button>
        <button type="submit" style={submitButtonStyle}>
          {isEditMode ? 'Update Task' : 'Create Task'}
        </button>
      </div>
    </form>
  );
};

export default TaskForm;
