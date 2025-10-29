import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TaskForm from '../TaskForm';
import { Project } from '../../../services/projectService';
import { CreateTaskRequest } from '../../../services/taskService';

/**
 * Tests for TaskForm component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~8, Cog~14
 */
describe('TaskForm', () => {
  const mockProjects: Project[] = [
    { id: 'proj1', name: 'Project Alpha', description: '', createdAt: '', userId: 'user1' },
    { id: 'proj2', name: 'Project Beta', description: '', createdAt: '', userId: 'user1' },
  ];

  const mockFormData: CreateTaskRequest = {
    name: 'Test Task',
    description: 'Test Description',
    status: 'TODO',
    priority: 'HIGH',
    projectId: 'proj1',
    dueDate: '2025-12-31',
    assigneeId: 'user1',
  };

  const defaultProps = {
    formData: mockFormData,
    projects: mockProjects,
    isEditMode: false,
    onSubmit: vi.fn(),
    onCancel: vi.fn(),
    onChange: vi.fn(),
  };

  it('renders all form fields', () => {
    render(<TaskForm {...defaultProps} />);

    expect(screen.getByLabelText(/project/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/task name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/description/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/status/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/priority/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/due date/i)).toBeInTheDocument();
  });

  it('renders project options from props', () => {
    render(<TaskForm {...defaultProps} />);

    expect(screen.getByText('Select a project')).toBeInTheDocument();
    expect(screen.getByText('Project Alpha')).toBeInTheDocument();
    expect(screen.getByText('Project Beta')).toBeInTheDocument();
  });

  it('renders all status options', () => {
    render(<TaskForm {...defaultProps} />);

    expect(screen.getByText('To Do')).toBeInTheDocument();
    expect(screen.getByText('In Progress')).toBeInTheDocument();
    expect(screen.getByText('Blocked')).toBeInTheDocument();
    expect(screen.getByText('Completed')).toBeInTheDocument();
  });

  it('renders all priority options', () => {
    render(<TaskForm {...defaultProps} />);

    expect(screen.getByText('Low')).toBeInTheDocument();
    expect(screen.getByText('Medium')).toBeInTheDocument();
    expect(screen.getByText('High')).toBeInTheDocument();
    expect(screen.getByText('Critical')).toBeInTheDocument();
  });

  it('calls onChange when task name changes', async () => {
    const user = userEvent.setup();
    const mockOnChange = vi.fn();

    render(<TaskForm {...defaultProps} onChange={mockOnChange} />);

    const nameInput = screen.getByLabelText(/task name/i);
    await user.clear(nameInput);
    await user.type(nameInput, 'New Task Name');

    expect(mockOnChange).toHaveBeenCalledWith('name', expect.any(String));
  });

  it('calls onChange when description changes', async () => {
    const user = userEvent.setup();
    const mockOnChange = vi.fn();

    render(<TaskForm {...defaultProps} onChange={mockOnChange} />);

    const descriptionInput = screen.getByLabelText(/description/i);
    await user.clear(descriptionInput);
    await user.type(descriptionInput, 'New Description');

    expect(mockOnChange).toHaveBeenCalledWith('description', expect.any(String));
  });

  it('calls onChange when project changes', async () => {
    const user = userEvent.setup();
    const mockOnChange = vi.fn();

    render(<TaskForm {...defaultProps} onChange={mockOnChange} />);

    const projectSelect = screen.getByLabelText(/project/i);
    await user.selectOptions(projectSelect, 'proj2');

    expect(mockOnChange).toHaveBeenCalledWith('projectId', 'proj2');
  });

  it('calls onChange when status changes', async () => {
    const user = userEvent.setup();
    const mockOnChange = vi.fn();

    render(<TaskForm {...defaultProps} onChange={mockOnChange} />);

    const statusSelect = screen.getByLabelText(/status/i);
    await user.selectOptions(statusSelect, 'IN_PROGRESS');

    expect(mockOnChange).toHaveBeenCalledWith('status', 'IN_PROGRESS');
  });

  it('calls onChange when priority changes', async () => {
    const user = userEvent.setup();
    const mockOnChange = vi.fn();

    render(<TaskForm {...defaultProps} onChange={mockOnChange} />);

    const prioritySelect = screen.getByLabelText(/priority/i);
    await user.selectOptions(prioritySelect, 'CRITICAL');

    expect(mockOnChange).toHaveBeenCalledWith('priority', 'CRITICAL');
  });

  it('calls onCancel when cancel button is clicked', async () => {
    const user = userEvent.setup();
    const mockOnCancel = vi.fn();

    render(<TaskForm {...defaultProps} onCancel={mockOnCancel} />);

    const cancelButton = screen.getByText('Cancel');
    await user.click(cancelButton);

    expect(mockOnCancel).toHaveBeenCalledTimes(1);
  });

  it('calls onSubmit when form is submitted', async () => {
    const user = userEvent.setup();
    const mockOnSubmit = vi.fn((e) => e.preventDefault());

    render(<TaskForm {...defaultProps} onSubmit={mockOnSubmit} />);

    const submitButton = screen.getByText('Create Task');
    await user.click(submitButton);

    expect(mockOnSubmit).toHaveBeenCalledTimes(1);
  });

  it('displays "Create Task" button in create mode', () => {
    render(<TaskForm {...defaultProps} isEditMode={false} />);
    expect(screen.getByText('Create Task')).toBeInTheDocument();
  });

  it('displays "Update Task" button in edit mode', () => {
    render(<TaskForm {...defaultProps} isEditMode={true} />);
    expect(screen.getByText('Update Task')).toBeInTheDocument();
  });

  it('shows progress slider in edit mode', () => {
    const mockOnProgressChange = vi.fn();
    render(
      <TaskForm
        {...defaultProps}
        isEditMode={true}
        progressPercentage={50}
        onProgressChange={mockOnProgressChange}
      />
    );

    expect(screen.getByLabelText(/progress/i)).toBeInTheDocument();
  });

  it('does not show progress slider in create mode', () => {
    render(<TaskForm {...defaultProps} isEditMode={false} />);
    expect(screen.queryByLabelText(/progress/i)).not.toBeInTheDocument();
  });

  it('calls onProgressChange when progress slider changes', () => {
    const mockOnProgressChange = vi.fn();

    render(
      <TaskForm
        {...defaultProps}
        isEditMode={true}
        progressPercentage={50}
        onProgressChange={mockOnProgressChange}
      />
    );

    const progressSlider = screen.getByLabelText(/progress/i) as HTMLInputElement;
    fireEvent.change(progressSlider, { target: { value: '75' } });

    expect(mockOnProgressChange).toHaveBeenCalledWith(75);
  });

  it('reflects form data values in inputs', () => {
    render(<TaskForm {...defaultProps} />);

    expect(screen.getByDisplayValue('Test Task')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Test Description')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Project Alpha')).toBeInTheDocument();
  });

  it('marks project and name as required fields', () => {
    render(<TaskForm {...defaultProps} />);

    const projectInput = screen.getByLabelText(/project/i);
    const nameInput = screen.getByLabelText(/task name/i);

    expect(projectInput).toBeRequired();
    expect(nameInput).toBeRequired();
  });
});
