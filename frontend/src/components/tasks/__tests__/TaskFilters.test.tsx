import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TaskFilters from '../TaskFilters';
import { Project } from '../../../services/projectService';

/**
 * Tests for TaskFilters component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~6, Cog~10
 */
describe('TaskFilters', () => {
  const mockProjects: Project[] = [
    { id: '1', name: 'Project Alpha', description: '', createdAt: '', userId: '1' },
    { id: '2', name: 'Project Beta', description: '', createdAt: '', userId: '1' },
    { id: '3', name: 'Project Gamma', description: '', createdAt: '', userId: '1' },
  ];

  const defaultProps = {
    projects: mockProjects,
    projectFilter: '',
    statusFilter: '' as const,
    priorityFilter: '' as const,
    showOverdueOnly: false,
    onProjectFilterChange: vi.fn(),
    onStatusFilterChange: vi.fn(),
    onPriorityFilterChange: vi.fn(),
    onShowOverdueChange: vi.fn(),
  };

  it('renders all filter controls', () => {
    render(<TaskFilters {...defaultProps} />);

    expect(screen.getByText('All Projects')).toBeInTheDocument();
    expect(screen.getByText('All Statuses')).toBeInTheDocument();
    expect(screen.getByText('All Priorities')).toBeInTheDocument();
    expect(screen.getByLabelText('Overdue Only')).toBeInTheDocument();
  });

  it('displays project options from props', () => {
    render(<TaskFilters {...defaultProps} />);

    expect(screen.getByText('Project Alpha')).toBeInTheDocument();
    expect(screen.getByText('Project Beta')).toBeInTheDocument();
    expect(screen.getByText('Project Gamma')).toBeInTheDocument();
  });

  it('displays all status options', () => {
    render(<TaskFilters {...defaultProps} />);

    expect(screen.getByText('All Statuses')).toBeInTheDocument();
    expect(screen.getByText('To Do')).toBeInTheDocument();
    expect(screen.getByText('In Progress')).toBeInTheDocument();
    expect(screen.getByText('Blocked')).toBeInTheDocument();
    expect(screen.getByText('Completed')).toBeInTheDocument();
  });

  it('displays all priority options', () => {
    render(<TaskFilters {...defaultProps} />);

    expect(screen.getByText('All Priorities')).toBeInTheDocument();
    expect(screen.getByText('Low')).toBeInTheDocument();
    expect(screen.getByText('Medium')).toBeInTheDocument();
    expect(screen.getByText('High')).toBeInTheDocument();
    expect(screen.getByText('Critical')).toBeInTheDocument();
  });

  it('calls onProjectFilterChange when project filter changes', async () => {
    const user = userEvent.setup();
    const mockOnChange = vi.fn();

    render(<TaskFilters {...defaultProps} onProjectFilterChange={mockOnChange} />);

    const projectSelect = screen.getByDisplayValue('All Projects');
    await user.selectOptions(projectSelect, '1');

    expect(mockOnChange).toHaveBeenCalledWith('1');
  });

  it('calls onStatusFilterChange when status filter changes', async () => {
    const user = userEvent.setup();
    const mockOnChange = vi.fn();

    render(<TaskFilters {...defaultProps} onStatusFilterChange={mockOnChange} />);

    const statusSelect = screen.getByDisplayValue('All Statuses');
    await user.selectOptions(statusSelect, 'TODO');

    expect(mockOnChange).toHaveBeenCalledWith('TODO');
  });

  it('calls onPriorityFilterChange when priority filter changes', async () => {
    const user = userEvent.setup();
    const mockOnChange = vi.fn();

    render(<TaskFilters {...defaultProps} onPriorityFilterChange={mockOnChange} />);

    const prioritySelect = screen.getByDisplayValue('All Priorities');
    await user.selectOptions(prioritySelect, 'HIGH');

    expect(mockOnChange).toHaveBeenCalledWith('HIGH');
  });

  it('calls onShowOverdueChange when checkbox is clicked', async () => {
    const user = userEvent.setup();
    const mockOnChange = vi.fn();

    render(<TaskFilters {...defaultProps} onShowOverdueChange={mockOnChange} />);

    const checkbox = screen.getByLabelText('Overdue Only');
    await user.click(checkbox);

    expect(mockOnChange).toHaveBeenCalledWith(true);
  });

  it('reflects selected project filter value', () => {
    render(<TaskFilters {...defaultProps} projectFilter="2" />);

    const projectSelect = screen.getByDisplayValue('Project Beta');
    expect(projectSelect).toBeInTheDocument();
  });

  it('reflects selected status filter value', () => {
    render(<TaskFilters {...defaultProps} statusFilter="IN_PROGRESS" />);

    const statusSelect = screen.getByDisplayValue('In Progress');
    expect(statusSelect).toBeInTheDocument();
  });

  it('reflects selected priority filter value', () => {
    render(<TaskFilters {...defaultProps} priorityFilter="CRITICAL" />);

    const prioritySelect = screen.getByDisplayValue('Critical');
    expect(prioritySelect).toBeInTheDocument();
  });

  it('reflects checkbox checked state', () => {
    render(<TaskFilters {...defaultProps} showOverdueOnly={true} />);

    const checkbox = screen.getByLabelText('Overdue Only') as HTMLInputElement;
    expect(checkbox.checked).toBe(true);
  });

  it('handles empty projects list', () => {
    render(<TaskFilters {...defaultProps} projects={[]} />);

    expect(screen.getByText('All Projects')).toBeInTheDocument();
    // Should only have "All Projects" option
    const projectSelect = screen.getByDisplayValue('All Projects') as HTMLSelectElement;
    expect(projectSelect.options.length).toBe(1);
  });
});
