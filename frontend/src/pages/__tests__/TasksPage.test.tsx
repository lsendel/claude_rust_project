import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TasksPage from '../TasksPage';
import { taskService } from '../../services/taskService';
import { projectService } from '../../services/projectService';

/**
 * Integration tests for TasksPage.
 * Tests full page workflows including component interactions,
 * CRUD operations, and state management.
 * PMAT: Page complexity Cyc≤15, Cog≤30
 */

// Mock services
vi.mock('../../services/taskService');
vi.mock('../../services/projectService');

// Mock contexts
vi.mock('../../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: { id: 'user1', name: 'Test User', email: 'test@example.com' },
  }),
}));

vi.mock('../../contexts/TenantContext', () => ({
  useTenant: () => ({
    tenant: { id: 'tenant1', name: 'Test Tenant' },
  }),
}));

describe('TasksPage Integration Tests', () => {
  const mockProjects = [
    { id: 'proj1', name: 'Project 1', tenantId: 'tenant1', startDate: '2025-01-01', isActive: true },
    { id: 'proj2', name: 'Project 2', tenantId: 'tenant1', startDate: '2025-01-15', isActive: true },
  ];

  const mockTasks = [
    {
      id: 'task1',
      projectId: 'proj1',
      name: 'Task 1',
      description: 'Description 1',
      status: 'TODO' as const,
      priority: 'HIGH' as const,
      dueDate: '2025-12-31',
      progressPercentage: 0,
      createdAt: '2025-01-01',
    },
    {
      id: 'task2',
      projectId: 'proj2',
      name: 'Task 2',
      description: 'Description 2',
      status: 'IN_PROGRESS' as const,
      priority: 'MEDIUM' as const,
      dueDate: '2025-11-30',
      progressPercentage: 50,
      createdAt: '2025-01-02',
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();

    // Default mock implementations
    vi.mocked(projectService.getAllProjects).mockResolvedValue(mockProjects);
    vi.mocked(taskService.getAllTasks).mockResolvedValue(mockTasks);
  });

  describe('Page Rendering', () => {
    it('renders page title and create button', async () => {
      render(<TasksPage />);

      expect(screen.getByText('Tasks')).toBeInTheDocument();
      expect(screen.getByText('+ Create Task')).toBeInTheDocument();
    });

    it('loads and displays projects in filters', async () => {
      render(<TasksPage />);

      await waitFor(() => {
        expect(projectService.getAllProjects).toHaveBeenCalledWith({ activeOnly: true });
      });
    });

    it('loads and displays tasks on mount', async () => {
      render(<TasksPage />);

      await waitFor(() => {
        expect(screen.getByText('Task 1')).toBeInTheDocument();
        expect(screen.getByText('Task 2')).toBeInTheDocument();
      });
    });

    it('displays loading state initially', () => {
      render(<TasksPage />);

      expect(screen.getByText('Loading tasks...')).toBeInTheDocument();
    });

    it('renders all main components', async () => {
      render(<TasksPage />);

      await waitFor(() => {
        // TaskFilters should be present (check for filter options)
        expect(screen.getByText('All Projects')).toBeInTheDocument();
        // TaskTable headers should be present
        expect(screen.getByText('Task Name')).toBeInTheDocument();
      });
    });
  });

  describe('Create Task Workflow', () => {
    it('opens create modal when create button is clicked', async () => {
      const user = userEvent.setup();
      render(<TasksPage />);

      await waitFor(() => expect(screen.getByText('Task 1')).toBeInTheDocument());

      const createButton = screen.getByText('+ Create Task');
      await user.click(createButton);

      expect(screen.getByText('Create Task')).toBeInTheDocument();
    });

    it('creates task successfully and reloads list', async () => {
      const user = userEvent.setup();
      vi.mocked(taskService.createTask).mockResolvedValue({
        id: 'task3',
        projectId: 'proj1',
        name: 'New Task',
        status: 'TODO' as const,
        priority: 'MEDIUM' as const,
        progressPercentage: 0,
        createdAt: '2025-01-03',
      });

      render(<TasksPage />);

      await waitFor(() => expect(screen.getByText('Task 1')).toBeInTheDocument());

      // Open create modal
      await user.click(screen.getByText('+ Create Task'));

      // Wait for modal to open
      await waitFor(() => expect(screen.getByText('Create Task')).toBeInTheDocument());

      // Fill required fields
      const projectSelect = screen.getByLabelText(/project/i);
      await user.selectOptions(projectSelect, 'proj1');

      const nameInput = screen.getByLabelText(/task name/i);
      await user.type(nameInput, 'New Task');

      // Submit form
      const submitButton = screen.getByText('Create Task');
      await user.click(submitButton);

      // Verify service was called
      await waitFor(() => {
        expect(taskService.createTask).toHaveBeenCalledWith(
          expect.objectContaining({ name: 'New Task', projectId: 'proj1' })
        );
      });

      // Verify tasks were reloaded (getAllTasks called again)
      expect(taskService.getAllTasks).toHaveBeenCalledTimes(2);
    });

    it('displays error when create fails', async () => {
      const user = userEvent.setup();
      vi.mocked(taskService.createTask).mockRejectedValue({
        response: { data: { message: 'Failed to create task' } },
      });

      render(<TasksPage />);

      await waitFor(() => expect(screen.getByText('Task 1')).toBeInTheDocument());

      // Open modal and fill form
      await user.click(screen.getByText('+ Create Task'));

      await waitFor(() => expect(screen.getByLabelText(/project/i)).toBeInTheDocument());

      const projectSelect = screen.getByLabelText(/project/i);
      await user.selectOptions(projectSelect, 'proj1');

      const nameInput = screen.getByLabelText(/task name/i);
      await user.type(nameInput, 'New Task');

      // Submit
      const submitButtons = screen.getAllByText('Create Task');
      await user.click(submitButtons[submitButtons.length - 1]); // Click the button in the modal

      // Verify error is displayed
      await waitFor(() => {
        expect(screen.getByText('Failed to create task')).toBeInTheDocument();
      });
    });

    it('displays quota error when limit exceeded', async () => {
      const user = userEvent.setup();
      vi.mocked(taskService.createTask).mockRejectedValue({
        response: { status: 402, data: {} },
      });

      render(<TasksPage />);

      await waitFor(() => expect(screen.getByText('Task 1')).toBeInTheDocument());

      await user.click(screen.getByText('+ Create Task'));

      await waitFor(() => expect(screen.getByLabelText(/project/i)).toBeInTheDocument());

      const projectSelect = screen.getByLabelText(/project/i);
      await user.selectOptions(projectSelect, 'proj1');

      const nameInput = screen.getByLabelText(/task name/i);
      await user.type(nameInput, 'New Task');

      const submitButtons = screen.getAllByText('Create Task');
      await user.click(submitButtons[submitButtons.length - 1]);

      await waitFor(() => {
        expect(screen.getByText(/quota exceeded/i)).toBeInTheDocument();
      });
    });
  });

  describe('Edit Task Workflow', () => {
    it('opens edit modal when edit button is clicked', async () => {
      const user = userEvent.setup();
      render(<TasksPage />);

      await waitFor(() => expect(screen.getByText('Task 1')).toBeInTheDocument());

      const editButtons = screen.getAllByText('Edit');
      await user.click(editButtons[0]);

      expect(screen.getByText('Edit Task')).toBeInTheDocument();
      expect(screen.getByDisplayValue('Task 1')).toBeInTheDocument();
    });

    it('updates task successfully and reloads list', async () => {
      const user = userEvent.setup();
      vi.mocked(taskService.updateTask).mockResolvedValue(mockTasks[0]);

      render(<TasksPage />);

      await waitFor(() => expect(screen.getByText('Task 1')).toBeInTheDocument());

      // Open edit modal
      const editButtons = screen.getAllByText('Edit');
      await user.click(editButtons[0]);

      // Modify name
      const nameInput = screen.getByDisplayValue('Task 1');
      await user.clear(nameInput);
      await user.type(nameInput, 'Updated Task 1');

      // Submit
      const updateButton = screen.getByText('Update Task');
      await user.click(updateButton);

      // Verify service was called
      await waitFor(() => {
        expect(taskService.updateTask).toHaveBeenCalledWith(
          'task1',
          expect.objectContaining({ name: 'Updated Task 1' })
        );
      });

      // Verify tasks were reloaded
      expect(taskService.getAllTasks).toHaveBeenCalledTimes(2);
    });

    it('displays error when update fails', async () => {
      const user = userEvent.setup();
      vi.mocked(taskService.updateTask).mockRejectedValue({
        response: { data: { message: 'Failed to update task' } },
      });

      render(<TasksPage />);

      await waitFor(() => expect(screen.getByText('Task 1')).toBeInTheDocument());

      const editButtons = screen.getAllByText('Edit');
      await user.click(editButtons[0]);

      const nameInput = screen.getByDisplayValue('Task 1');
      await user.clear(nameInput);
      await user.type(nameInput, 'Updated Task');

      await user.click(screen.getByText('Update Task'));

      await waitFor(() => {
        expect(screen.getByText('Failed to update task')).toBeInTheDocument();
      });
    });
  });

  describe('Delete Task Workflow', () => {
    it('deletes task after confirmation', async () => {
      const user = userEvent.setup();
      vi.mocked(taskService.deleteTask).mockResolvedValue(undefined);

      // Mock window.confirm
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);

      render(<TasksPage />);

      await waitFor(() => expect(screen.getByText('Task 1')).toBeInTheDocument());

      const deleteButtons = screen.getAllByText('Delete');
      await user.click(deleteButtons[0]);

      expect(confirmSpy).toHaveBeenCalledWith('Are you sure you want to delete this task?');

      await waitFor(() => {
        expect(taskService.deleteTask).toHaveBeenCalledWith('task1');
      });

      // Verify tasks were reloaded
      expect(taskService.getAllTasks).toHaveBeenCalledTimes(2);

      confirmSpy.mockRestore();
    });

    it('does not delete task when confirmation is cancelled', async () => {
      const user = userEvent.setup();
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false);

      render(<TasksPage />);

      await waitFor(() => expect(screen.getByText('Task 1')).toBeInTheDocument());

      const deleteButtons = screen.getAllByText('Delete');
      await user.click(deleteButtons[0]);

      expect(confirmSpy).toHaveBeenCalled();
      expect(taskService.deleteTask).not.toHaveBeenCalled();

      confirmSpy.mockRestore();
    });

    it('displays error when delete fails', async () => {
      const user = userEvent.setup();
      vi.mocked(taskService.deleteTask).mockRejectedValue({
        response: { data: { message: 'Failed to delete task' } },
      });

      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);

      render(<TasksPage />);

      await waitFor(() => expect(screen.getByText('Task 1')).toBeInTheDocument());

      const deleteButtons = screen.getAllByText('Delete');
      await user.click(deleteButtons[0]);

      await waitFor(() => {
        expect(screen.getByText('Failed to delete task')).toBeInTheDocument();
      });

      confirmSpy.mockRestore();
    });
  });

  describe('Filter Integration', () => {
    it('reloads tasks when project filter changes', async () => {
      const user = userEvent.setup();
      render(<TasksPage />);

      await waitFor(() => expect(screen.getByText('Task 1')).toBeInTheDocument());

      // Initial load
      expect(taskService.getAllTasks).toHaveBeenCalledTimes(1);

      // Change project filter - get all comboboxes and select first one (projects)
      const selects = screen.getAllByRole('combobox');
      const projectSelect = selects[0];
      await user.selectOptions(projectSelect, 'proj1');

      // Verify tasks reloaded with filter
      await waitFor(() => {
        expect(taskService.getAllTasks).toHaveBeenCalledWith(
          expect.objectContaining({ projectId: 'proj1' })
        );
      });
    });

    it('reloads tasks when status filter changes', async () => {
      const user = userEvent.setup();
      render(<TasksPage />);

      await waitFor(() => expect(screen.getByText('Task 1')).toBeInTheDocument());

      // Get second select (status)
      const selects = screen.getAllByRole('combobox');
      const statusSelect = selects[1];
      await user.selectOptions(statusSelect, 'IN_PROGRESS');

      await waitFor(() => {
        expect(taskService.getAllTasks).toHaveBeenCalledWith(
          expect.objectContaining({ status: 'IN_PROGRESS' })
        );
      });
    });

    it('reloads tasks when priority filter changes', async () => {
      const user = userEvent.setup();
      render(<TasksPage />);

      await waitFor(() => expect(screen.getByText('Task 1')).toBeInTheDocument());

      // Get third select (priority)
      const selects = screen.getAllByRole('combobox');
      const prioritySelect = selects[2];
      await user.selectOptions(prioritySelect, 'HIGH');

      await waitFor(() => {
        expect(taskService.getAllTasks).toHaveBeenCalledWith(
          expect.objectContaining({ priority: 'HIGH' })
        );
      });
    });

    it('reloads tasks when overdue filter is toggled', async () => {
      const user = userEvent.setup();
      render(<TasksPage />);

      await waitFor(() => expect(screen.getByText('Task 1')).toBeInTheDocument());

      const overdueCheckbox = screen.getByLabelText(/overdue only/i);
      await user.click(overdueCheckbox);

      await waitFor(() => {
        expect(taskService.getAllTasks).toHaveBeenCalledWith(
          expect.objectContaining({ overdueOnly: true })
        );
      });
    });
  });

  describe('Error Handling', () => {
    it('displays error when tasks fail to load', async () => {
      vi.mocked(taskService.getAllTasks).mockRejectedValue({
        response: { data: { message: 'Failed to load tasks' } },
      });

      render(<TasksPage />);

      await waitFor(() => {
        expect(screen.getByText('Failed to load tasks')).toBeInTheDocument();
      });
    });

    it('closes error banner when close button is clicked', async () => {
      const user = userEvent.setup();
      vi.mocked(taskService.getAllTasks).mockRejectedValue({
        response: { data: { message: 'Failed to load tasks' } },
      });

      render(<TasksPage />);

      await waitFor(() => {
        expect(screen.getByText('Failed to load tasks')).toBeInTheDocument();
      });

      const closeButton = screen.getByText('×');
      await user.click(closeButton);

      expect(screen.queryByText('Failed to load tasks')).not.toBeInTheDocument();
    });
  });
});
