import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TaskModal from '../TaskModal';
import { Project } from '../../../services/projectService';
import { CreateTaskRequest } from '../../../services/taskService';

/**
 * Tests for TaskModal component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~4, Cog~6
 */
describe('TaskModal', () => {
  const mockProjects: Project[] = [
    { id: 'proj1', name: 'Project Alpha', description: '', createdAt: '', userId: 'user1' },
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
    isOpen: true,
    isEditMode: false,
    formData: mockFormData,
    projects: mockProjects,
    onSubmit: vi.fn(),
    onClose: vi.fn(),
    onChange: vi.fn(),
  };

  it('renders nothing when isOpen is false', () => {
    const { container } = render(<TaskModal {...defaultProps} isOpen={false} />);
    expect(container.firstChild).toBeNull();
  });

  it('renders modal when isOpen is true', () => {
    render(<TaskModal {...defaultProps} isOpen={true} />);
    expect(screen.getByText('Create New Task')).toBeInTheDocument();
  });

  it('displays "Create New Task" header in create mode', () => {
    render(<TaskModal {...defaultProps} isEditMode={false} />);
    expect(screen.getByText('Create New Task')).toBeInTheDocument();
  });

  it('displays "Edit Task" header in edit mode', () => {
    render(<TaskModal {...defaultProps} isEditMode={true} />);
    expect(screen.getByText('Edit Task')).toBeInTheDocument();
  });

  it('renders TaskForm component with all fields', () => {
    render(<TaskModal {...defaultProps} />);

    // Verify form fields are present
    expect(screen.getByLabelText(/project/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/task name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/description/i)).toBeInTheDocument();
  });

  it('calls onClose when overlay is clicked', async () => {
    const user = userEvent.setup();
    const mockOnClose = vi.fn();

    render(<TaskModal {...defaultProps} onClose={mockOnClose} />);

    // Click on overlay (the backdrop)
    const overlay = screen.getByText('Create New Task').parentElement?.parentElement;
    if (overlay) {
      await user.click(overlay);
      expect(mockOnClose).toHaveBeenCalledTimes(1);
    }
  });

  it('does not call onClose when modal content is clicked', async () => {
    const user = userEvent.setup();
    const mockOnClose = vi.fn();

    render(<TaskModal {...defaultProps} onClose={mockOnClose} />);

    // Click on modal content (the header)
    const header = screen.getByText('Create New Task');
    await user.click(header);

    // onClose should not be called
    expect(mockOnClose).not.toHaveBeenCalled();
  });

  it('passes correct props to TaskForm', () => {
    render(<TaskModal {...defaultProps} />);

    // Verify form has correct data
    expect(screen.getByDisplayValue('Test Task')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Test Description')).toBeInTheDocument();
  });

  it('renders with progress percentage in edit mode', () => {
    render(
      <TaskModal
        {...defaultProps}
        isEditMode={true}
        progressPercentage={75}
        onProgressChange={vi.fn()}
      />
    );

    expect(screen.getByText('Edit Task')).toBeInTheDocument();
    expect(screen.getByLabelText(/progress/i)).toBeInTheDocument();
  });
});
