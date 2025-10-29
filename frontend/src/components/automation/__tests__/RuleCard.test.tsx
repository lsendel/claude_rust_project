import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import RuleCard from '../RuleCard';
import { AutomationRule } from '../../../services/automationService';

/**
 * Tests for RuleCard component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~6, Cog~10
 */
describe('RuleCard', () => {
  const mockRule: AutomationRule = {
    id: 'rule1',
    name: 'Welcome Email Rule',
    eventType: 'USER_CREATED',
    actionType: 'SEND_EMAIL',
    isActive: true,
    executionCount: 42,
    lastExecutedAt: '2025-10-27T12:00:00Z',
    tenantId: 'tenant1',
  };

  const defaultProps = {
    rule: mockRule,
    onEdit: vi.fn(),
    onDelete: vi.fn(),
    onToggleStatus: vi.fn(),
  };

  it('renders rule name', () => {
    render(<RuleCard {...defaultProps} />);
    expect(screen.getByText('Welcome Email Rule')).toBeInTheDocument();
  });

  it('renders event type', () => {
    render(<RuleCard {...defaultProps} />);
    expect(screen.getByText(/USER_CREATED/i)).toBeInTheDocument();
  });

  it('renders action type', () => {
    render(<RuleCard {...defaultProps} />);
    expect(screen.getByText(/SEND_EMAIL/i)).toBeInTheDocument();
  });

  it('renders execution count', () => {
    render(<RuleCard {...defaultProps} />);
    expect(screen.getByText(/42/)).toBeInTheDocument();
  });

  it('renders last executed date when present', () => {
    render(<RuleCard {...defaultProps} />);
    const formattedDate = new Date('2025-10-27T12:00:00Z').toLocaleString();
    expect(screen.getByText(new RegExp(formattedDate))).toBeInTheDocument();
  });

  it('does not render last executed section when not present', () => {
    const ruleWithoutLastExecuted = { ...mockRule, lastExecutedAt: undefined };
    render(<RuleCard {...defaultProps} rule={ruleWithoutLastExecuted} />);
    expect(screen.queryByText(/Last Run:/i)).not.toBeInTheDocument();
  });

  it('displays "Active" button when rule is active', () => {
    render(<RuleCard {...defaultProps} />);
    expect(screen.getByText('Active')).toBeInTheDocument();
  });

  it('displays "Inactive" button when rule is inactive', () => {
    const inactiveRule = { ...mockRule, isActive: false };
    render(<RuleCard {...defaultProps} rule={inactiveRule} />);
    expect(screen.getByText('Inactive')).toBeInTheDocument();
  });

  it('renders edit button', () => {
    render(<RuleCard {...defaultProps} />);
    expect(screen.getByText('Edit')).toBeInTheDocument();
  });

  it('renders delete button', () => {
    render(<RuleCard {...defaultProps} />);
    expect(screen.getByText('Delete')).toBeInTheDocument();
  });

  it('calls onEdit when edit button is clicked', async () => {
    const user = userEvent.setup();
    const mockOnEdit = vi.fn();

    render(<RuleCard {...defaultProps} onEdit={mockOnEdit} />);

    const editButton = screen.getByText('Edit');
    await user.click(editButton);

    expect(mockOnEdit).toHaveBeenCalledWith(mockRule);
  });

  it('calls onDelete when delete button is clicked', async () => {
    const user = userEvent.setup();
    const mockOnDelete = vi.fn();

    render(<RuleCard {...defaultProps} onDelete={mockOnDelete} />);

    const deleteButton = screen.getByText('Delete');
    await user.click(deleteButton);

    expect(mockOnDelete).toHaveBeenCalledWith('rule1', 'Welcome Email Rule');
  });

  it('calls onToggleStatus when toggle button is clicked', async () => {
    const user = userEvent.setup();
    const mockOnToggleStatus = vi.fn();

    render(<RuleCard {...defaultProps} onToggleStatus={mockOnToggleStatus} />);

    const toggleButton = screen.getByText('Active');
    await user.click(toggleButton);

    expect(mockOnToggleStatus).toHaveBeenCalledWith('rule1', true);
  });

  it('displays 0 executions when count is not present', () => {
    const ruleWithoutCount = { ...mockRule, executionCount: undefined };
    render(<RuleCard {...defaultProps} rule={ruleWithoutCount} />);
    expect(screen.getByText(/0/)).toBeInTheDocument();
  });

  it('handles rule without isActive property', () => {
    const ruleWithoutActive = { ...mockRule, isActive: undefined };
    render(<RuleCard {...defaultProps} rule={ruleWithoutActive} />);
    expect(screen.getByText('Inactive')).toBeInTheDocument();
  });
});
