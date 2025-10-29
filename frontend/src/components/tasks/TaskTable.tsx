import React from 'react';
import { Task } from '../../services/taskService';
import { Project } from '../../services/projectService';
import TaskRow from './TaskRow';

interface TaskTableProps {
  tasks: Task[];
  projects: Project[];
  loading: boolean;
  onEdit: (task: Task) => void;
  onDelete: (id: string) => void;
}

/**
 * Table component for displaying tasks list.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const TaskTable: React.FC<TaskTableProps> = ({ tasks, projects, loading, onEdit, onDelete }) => {
  const getProjectName = (projectId: string): string => {
    const project = projects.find((p) => p.id === projectId);
    return project?.name || 'Unknown Project';
  };

  const tableStyle: React.CSSProperties = {
    width: '100%',
    backgroundColor: 'white',
    borderRadius: '8px',
    boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
    overflow: 'hidden',
  };

  const tableHeaderStyle: React.CSSProperties = {
    backgroundColor: '#f9fafb',
    borderBottom: '1px solid #e5e7eb',
  };

  const thStyle: React.CSSProperties = {
    padding: '12px 16px',
    textAlign: 'left',
    fontSize: '12px',
    fontWeight: '600',
    color: '#6b7280',
    textTransform: 'uppercase',
  };

  const emptyStateStyle: React.CSSProperties = {
    textAlign: 'center',
    padding: '48px 24px',
    color: '#6b7280',
  };

  const loadingStyle: React.CSSProperties = {
    textAlign: 'center',
    padding: '48px',
    color: '#6b7280',
  };

  if (loading) {
    return <div style={loadingStyle}>Loading tasks...</div>;
  }

  if (tasks.length === 0) {
    return (
      <div style={emptyStateStyle}>
        <p style={{ fontSize: '16px', marginBottom: '8px' }}>No tasks found</p>
        <p style={{ fontSize: '14px' }}>Create your first task to get started</p>
      </div>
    );
  }

  return (
    <div style={tableStyle}>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead style={tableHeaderStyle}>
          <tr>
            <th style={thStyle}>Task Name</th>
            <th style={thStyle}>Project</th>
            <th style={thStyle}>Status</th>
            <th style={thStyle}>Priority</th>
            <th style={thStyle}>Progress</th>
            <th style={thStyle}>Due Date</th>
            <th style={thStyle}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {tasks.map((task) => (
            <TaskRow
              key={task.id}
              task={task}
              projectName={getProjectName(task.projectId)}
              onEdit={onEdit}
              onDelete={onDelete}
            />
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default TaskTable;
