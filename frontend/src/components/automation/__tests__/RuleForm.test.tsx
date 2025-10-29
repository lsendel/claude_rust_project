import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import RuleForm from '../RuleForm';
import { AutomationRule } from '../../../services/automationService';

/**
 * Tests for RuleForm component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~5, Cog~8
 */
describe('RuleForm', () => {
  const mockFormData: Partial<AutomationRule> = {
    name: 'Test Rule',
    eventType: 'task.created',
    actionType: 'send_email',
  };

  const defaultProps = {
    formData: mockFormData,
    isEditMode: false,
    onSubmit: vi.fn(),
    onCancel: vi.fn(),
    onChange: vi.fn(),
  };

  it('renders all form fields', () => {
    render(<RuleForm {...defaultProps} />);

    expect(screen.getByText(/rule name/i)).toBeInTheDocument();
    expect(screen.getByText(/event type/i)).toBeInTheDocument();
    expect(screen.getByText(/action type/i)).toBeInTheDocument();
  });

  it('renders all event type options', () => {
    render(<RuleForm {...defaultProps} />);

    expect(screen.getByText('Task Status Changed')).toBeInTheDocument();
    expect(screen.getByText('Task Created')).toBeInTheDocument();
    expect(screen.getByText('Task Updated')).toBeInTheDocument();
    expect(screen.getByText('Task Deleted')).toBeInTheDocument();
    expect(screen.getByText('Project Created')).toBeInTheDocument();
    expect(screen.getByText('Project Updated')).toBeInTheDocument();
    expect(screen.getByText('Project Deleted')).toBeInTheDocument();
  });

  it('renders all action type options', () => {
    render(<RuleForm {...defaultProps} />);

    expect(screen.getByText('Send Email')).toBeInTheDocument();
    expect(screen.getByText('Call Webhook')).toBeInTheDocument();
    expect(screen.getByText('Create Task')).toBeInTheDocument();
    expect(screen.getByText('Send Notification')).toBeInTheDocument();
  });

  it('calls onChange when rule name changes', async () => {
    const user = userEvent.setup();
    const mockOnChange = vi.fn();

    render(<RuleForm {...defaultProps} onChange={mockOnChange} />);

    const nameInput = screen.getByDisplayValue('Test Rule');
    await user.clear(nameInput);
    await user.type(nameInput, 'New Rule Name');

    expect(mockOnChange).toHaveBeenCalledWith('name', expect.any(String));
  });

  it('calls onChange when event type changes', async () => {
    const user = userEvent.setup();
    const mockOnChange = vi.fn();

    render(<RuleForm {...defaultProps} onChange={mockOnChange} />);

    const eventTypeSelect = screen.getByDisplayValue('Task Created');
    await user.selectOptions(eventTypeSelect, 'task.status.changed');

    expect(mockOnChange).toHaveBeenCalledWith('eventType', 'task.status.changed');
  });

  it('calls onChange when action type changes', async () => {
    const user = userEvent.setup();
    const mockOnChange = vi.fn();

    render(<RuleForm {...defaultProps} onChange={mockOnChange} />);

    const actionTypeSelect = screen.getByDisplayValue('Send Email');
    await user.selectOptions(actionTypeSelect, 'call_webhook');

    expect(mockOnChange).toHaveBeenCalledWith('actionType', 'call_webhook');
  });

  it('calls onCancel when cancel button is clicked', async () => {
    const user = userEvent.setup();
    const mockOnCancel = vi.fn();

    render(<RuleForm {...defaultProps} onCancel={mockOnCancel} />);

    const cancelButton = screen.getByText('Cancel');
    await user.click(cancelButton);

    expect(mockOnCancel).toHaveBeenCalledTimes(1);
  });

  it('calls onSubmit when form is submitted', async () => {
    const user = userEvent.setup();
    const mockOnSubmit = vi.fn((e) => e.preventDefault());

    render(<RuleForm {...defaultProps} onSubmit={mockOnSubmit} />);

    const submitButton = screen.getByText('Create Rule');
    await user.click(submitButton);

    expect(mockOnSubmit).toHaveBeenCalledTimes(1);
  });

  it('displays "Create Rule" button in create mode', () => {
    render(<RuleForm {...defaultProps} isEditMode={false} />);
    expect(screen.getByText('Create Rule')).toBeInTheDocument();
  });

  it('displays "Update Rule" button in edit mode', () => {
    render(<RuleForm {...defaultProps} isEditMode={true} />);
    expect(screen.getByText('Update Rule')).toBeInTheDocument();
  });

  it('reflects form data values in inputs', () => {
    render(<RuleForm {...defaultProps} />);

    expect(screen.getByDisplayValue('Test Rule')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Task Created')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Send Email')).toBeInTheDocument();
  });

  it('marks rule name as required field', () => {
    render(<RuleForm {...defaultProps} />);

    const nameInput = screen.getByDisplayValue('Test Rule');
    expect(nameInput).toBeRequired();
  });

  it('handles empty form data', () => {
    render(<RuleForm {...defaultProps} formData={{}} />);

    const nameInput = screen.getByRole('textbox') as HTMLInputElement;
    expect(nameInput.value).toBe('');
  });

  it('uses default event type when not provided', () => {
    render(<RuleForm {...defaultProps} formData={{ name: 'Test' }} />);

    expect(screen.getByDisplayValue('Task Status Changed')).toBeInTheDocument();
  });

  it('uses default action type when not provided', () => {
    render(<RuleForm {...defaultProps} formData={{ name: 'Test' }} />);

    expect(screen.getByDisplayValue('Send Email')).toBeInTheDocument();
  });
});
