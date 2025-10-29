import React from 'react';
import { Project } from '../../services/projectService';

type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'BLOCKED' | 'COMPLETED';
type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

interface TaskFiltersProps {
  projects: Project[];
  projectFilter: string;
  statusFilter: TaskStatus | '';
  priorityFilter: Priority | '';
  showOverdueOnly: boolean;
  onProjectFilterChange: (value: string) => void;
  onStatusFilterChange: (value: TaskStatus | '') => void;
  onPriorityFilterChange: (value: Priority | '') => void;
  onShowOverdueChange: (value: boolean) => void;
}

/**
 * Filter controls component for tasks page.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const TaskFilters: React.FC<TaskFiltersProps> = ({
  projects,
  projectFilter,
  statusFilter,
  priorityFilter,
  showOverdueOnly,
  onProjectFilterChange,
  onStatusFilterChange,
  onPriorityFilterChange,
  onShowOverdueChange,
}) => {
  const filtersStyle: React.CSSProperties = {
    display: 'flex',
    gap: '16px',
    marginBottom: '24px',
    flexWrap: 'wrap',
    alignItems: 'center',
  };

  const selectStyle: React.CSSProperties = {
    padding: '8px 12px',
    border: '1px solid #d1d5db',
    borderRadius: '6px',
    fontSize: '14px',
    backgroundColor: 'white',
  };

  const checkboxContainerStyle: React.CSSProperties = {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
  };

  return (
    <div style={filtersStyle}>
      <select
        value={projectFilter}
        onChange={(e) => onProjectFilterChange(e.target.value)}
        style={selectStyle}
      >
        <option value="">All Projects</option>
        {projects.map((project) => (
          <option key={project.id} value={project.id}>
            {project.name}
          </option>
        ))}
      </select>

      <select
        value={statusFilter}
        onChange={(e) => onStatusFilterChange(e.target.value as TaskStatus | '')}
        style={selectStyle}
      >
        <option value="">All Statuses</option>
        <option value="TODO">To Do</option>
        <option value="IN_PROGRESS">In Progress</option>
        <option value="BLOCKED">Blocked</option>
        <option value="COMPLETED">Completed</option>
      </select>

      <select
        value={priorityFilter}
        onChange={(e) => onPriorityFilterChange(e.target.value as Priority | '')}
        style={selectStyle}
      >
        <option value="">All Priorities</option>
        <option value="LOW">Low</option>
        <option value="MEDIUM">Medium</option>
        <option value="HIGH">High</option>
        <option value="CRITICAL">Critical</option>
      </select>

      <div style={checkboxContainerStyle}>
        <input
          type="checkbox"
          id="overdueOnly"
          checked={showOverdueOnly}
          onChange={(e) => onShowOverdueChange(e.target.checked)}
        />
        <label htmlFor="overdueOnly" style={{ fontSize: '14px', cursor: 'pointer' }}>
          Overdue Only
        </label>
      </div>
    </div>
  );
};

export default TaskFilters;
