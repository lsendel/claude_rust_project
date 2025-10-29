import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import TaskTable from '../TaskTable';
import { Task } from '../../../services/taskService';
import { Project } from '../../../services/projectService';

/**
 * Tests for TaskTable component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~5, Cog~8
 */
describe('TaskTable', () => {
  const mockProjects: Project[] = [
    { id: 'proj1', name: 'Project Alpha', description: '', createdAt: '', userId: 'user1' },
    { id: 'proj2', name: 'Project Beta', description: '', createdAt: '', userId: 'user1' },
  ];

  const mockTasks: Task[] = [
    {
      id: '1',
      name: 'Task 1',
      description: 'Description 1',
      status: 'TODO',
      priority: 'HIGH',
      progressPercentage: 0,
      dueDate: '2025-12-31',
      projectId: 'proj1',
      assigneeId: 'user1',
      createdAt: '2025-01-01',
    },
    {
      id: '2',
      name: 'Task 2',
      description: 'Description 2',
      status: 'IN_PROGRESS',
      priority: 'MEDIUM',
      progressPercentage: 50,
      dueDate: '2025-11-30',
      projectId: 'proj2',
      assigneeId: 'user1',
      createdAt: '2025-01-02',
    },
  ];

  const defaultProps = {
    tasks: mockTasks,
    projects: mockProjects,
    loading: false,
    onEdit: vi.fn(),
    onDelete: vi.fn(),
  };

  it('renders loading state', () => {
    render(<TaskTable {...defaultProps} loading={true} />);
    expect(screen.getByText('Loading tasks...')).toBeInTheDocument();
  });

  it('renders empty state when no tasks', () => {
    render(<TaskTable {...defaultProps} tasks={[]} />);
    expect(screen.getByText('No tasks found')).toBeInTheDocument();
    expect(screen.getByText('Create your first task to get started')).toBeInTheDocument();
  });

  it('does not render table when loading', () => {
    render(<TaskTable {...defaultProps} loading={true} />);
    expect(screen.queryByText('Task Name')).not.toBeInTheDocument();
  });

  it('does not render table when no tasks', () => {
    render(<TaskTable {...defaultProps} tasks={[]} />);
    expect(screen.queryByText('Task Name')).not.toBeInTheDocument();
  });

  it('renders table headers', () => {
    render(<TaskTable {...defaultProps} />);
    expect(screen.getByText('Task Name')).toBeInTheDocument();
    expect(screen.getByText('Project')).toBeInTheDocument();
    expect(screen.getByText('Status')).toBeInTheDocument();
    expect(screen.getByText('Priority')).toBeInTheDocument();
    expect(screen.getByText('Progress')).toBeInTheDocument();
    expect(screen.getByText('Due Date')).toBeInTheDocument();
    expect(screen.getByText('Actions')).toBeInTheDocument();
  });

  it('renders TaskRow for each task', () => {
    render(<TaskTable {...defaultProps} />);
    expect(screen.getByText('Task 1')).toBeInTheDocument();
    expect(screen.getByText('Task 2')).toBeInTheDocument();
  });

  it('displays correct project names', () => {
    render(<TaskTable {...defaultProps} />);
    expect(screen.getByText('Project Alpha')).toBeInTheDocument();
    expect(screen.getByText('Project Beta')).toBeInTheDocument();
  });

  it('displays "Unknown Project" for tasks with invalid project ID', () => {
    const tasksWithUnknownProject = [
      {
        ...mockTasks[0],
        projectId: 'invalid-project-id',
      },
    ];
    render(<TaskTable {...defaultProps} tasks={tasksWithUnknownProject} />);
    expect(screen.getByText('Unknown Project')).toBeInTheDocument();
  });

  it('renders all task statuses', () => {
    render(<TaskTable {...defaultProps} />);
    expect(screen.getByText('TODO')).toBeInTheDocument();
    expect(screen.getByText('IN PROGRESS')).toBeInTheDocument();
  });

  it('renders all task priorities', () => {
    render(<TaskTable {...defaultProps} />);
    expect(screen.getByText('HIGH')).toBeInTheDocument();
    expect(screen.getByText('MEDIUM')).toBeInTheDocument();
  });

  it('renders progress bars for all tasks', () => {
    render(<TaskTable {...defaultProps} />);
    expect(screen.getByText('0%')).toBeInTheDocument();
    expect(screen.getByText('50%')).toBeInTheDocument();
  });
});
