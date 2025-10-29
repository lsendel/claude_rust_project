import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TaskRow from '../TaskRow';
import { Task } from '../../../services/taskService';

/**
 * Tests for TaskRow component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~7, Cog~12
 */
describe('TaskRow', () => {
  const mockTask: Task = {
    id: '1',
    name: 'Test Task',
    description: 'Test task description',
    status: 'TODO',
    priority: 'HIGH',
    progressPercentage: 50,
    dueDate: '2025-12-31',
    projectId: 'proj1',
    assigneeId: 'user1',
    createdAt: '2025-01-01',
  };

  const defaultProps = {
    task: mockTask,
    projectName: 'Test Project',
    onEdit: vi.fn(),
    onDelete: vi.fn(),
  };

  it('renders task name', () => {
    render(
      <table>
        <tbody>
          <TaskRow {...defaultProps} />
        </tbody>
      </table>
    );
    expect(screen.getByText('Test Task')).toBeInTheDocument();
  });

  it('renders task description when present', () => {
    render(
      <table>
        <tbody>
          <TaskRow {...defaultProps} />
        </tbody>
      </table>
    );
    expect(screen.getByText('Test task description')).toBeInTheDocument();
  });

  it('does not render description section when description is empty', () => {
    const taskWithoutDescription = { ...mockTask, description: '' };
    render(
      <table>
        <tbody>
          <TaskRow {...defaultProps} task={taskWithoutDescription} />
        </tbody>
      </table>
    );
    expect(screen.queryByText('Test task description')).not.toBeInTheDocument();
  });

  it('renders project name', () => {
    render(
      <table>
        <tbody>
          <TaskRow {...defaultProps} />
        </tbody>
      </table>
    );
    expect(screen.getByText('Test Project')).toBeInTheDocument();
  });

  it('renders status badge', () => {
    render(
      <table>
        <tbody>
          <TaskRow {...defaultProps} />
        </tbody>
      </table>
    );
    expect(screen.getByText('TODO')).toBeInTheDocument();
  });

  it('renders priority badge', () => {
    render(
      <table>
        <tbody>
          <TaskRow {...defaultProps} />
        </tbody>
      </table>
    );
    expect(screen.getByText('HIGH')).toBeInTheDocument();
  });

  it('renders progress bar', () => {
    render(
      <table>
        <tbody>
          <TaskRow {...defaultProps} />
        </tbody>
      </table>
    );
    expect(screen.getByText('50%')).toBeInTheDocument();
  });

  it('renders formatted due date', () => {
    render(
      <table>
        <tbody>
          <TaskRow {...defaultProps} />
        </tbody>
      </table>
    );
    // Check for date in format like "12/31/2025"
    const date = new Date('2025-12-31').toLocaleDateString();
    expect(screen.getByText(date)).toBeInTheDocument();
  });

  it('renders "-" when no due date', () => {
    const taskWithoutDueDate = { ...mockTask, dueDate: undefined };
    render(
      <table>
        <tbody>
          <TaskRow {...defaultProps} task={taskWithoutDueDate} />
        </tbody>
      </table>
    );
    expect(screen.getByText('-')).toBeInTheDocument();
  });

  it('shows overdue badge for overdue tasks', () => {
    const overdueTask = { ...mockTask, dueDate: '2020-01-01', status: 'TODO' as const };
    render(
      <table>
        <tbody>
          <TaskRow {...defaultProps} task={overdueTask} />
        </tbody>
      </table>
    );
    expect(screen.getByText('Overdue')).toBeInTheDocument();
  });

  it('does not show overdue badge for completed tasks', () => {
    const overdueCompletedTask = { ...mockTask, dueDate: '2020-01-01', status: 'COMPLETED' as const };
    render(
      <table>
        <tbody>
          <TaskRow {...defaultProps} task={overdueCompletedTask} />
        </tbody>
      </table>
    );
    expect(screen.queryByText('Overdue')).not.toBeInTheDocument();
  });

  it('does not show overdue badge when no due date', () => {
    const taskWithoutDueDate = { ...mockTask, dueDate: undefined };
    render(
      <table>
        <tbody>
          <TaskRow {...defaultProps} task={taskWithoutDueDate} />
        </tbody>
      </table>
    );
    expect(screen.queryByText('Overdue')).not.toBeInTheDocument();
  });

  it('calls onEdit when edit button is clicked', async () => {
    const user = userEvent.setup();
    const mockOnEdit = vi.fn();

    render(
      <table>
        <tbody>
          <TaskRow {...defaultProps} onEdit={mockOnEdit} />
        </tbody>
      </table>
    );

    const editButton = screen.getByText('Edit');
    await user.click(editButton);

    expect(mockOnEdit).toHaveBeenCalledWith(mockTask);
  });

  it('calls onDelete when delete button is clicked', async () => {
    const user = userEvent.setup();
    const mockOnDelete = vi.fn();

    render(
      <table>
        <tbody>
          <TaskRow {...defaultProps} onDelete={mockOnDelete} />
        </tbody>
      </table>
    );

    const deleteButton = screen.getByText('Delete');
    await user.click(deleteButton);

    expect(mockOnDelete).toHaveBeenCalledWith('1');
  });

  it('renders edit and delete buttons', () => {
    render(
      <table>
        <tbody>
          <TaskRow {...defaultProps} />
        </tbody>
      </table>
    );

    expect(screen.getByText('Edit')).toBeInTheDocument();
    expect(screen.getByText('Delete')).toBeInTheDocument();
  });
});
