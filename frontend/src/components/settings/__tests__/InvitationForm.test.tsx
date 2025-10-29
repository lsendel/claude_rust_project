import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import InvitationForm from '../InvitationForm';

/**
 * Tests for InvitationForm component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~5, Cog~8
 */
describe('InvitationForm', () => {
  const defaultProps = {
    email: 'test@example.com',
    role: 'EDITOR' as const,
    message: 'Welcome to the team!',
    inviting: false,
    onSubmit: vi.fn(),
    onEmailChange: vi.fn(),
    onRoleChange: vi.fn(),
    onMessageChange: vi.fn(),
  };

  it('renders all form fields', () => {
    render(<InvitationForm {...defaultProps} />);

    expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/role/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/custom message/i)).toBeInTheDocument();
  });

  it('renders all role options', () => {
    render(<InvitationForm {...defaultProps} />);

    expect(screen.getByText(/Viewer - Read-only access/i)).toBeInTheDocument();
    expect(screen.getByText(/Editor - Can create and modify/i)).toBeInTheDocument();
    expect(screen.getByText(/Administrator - Full access/i)).toBeInTheDocument();
  });

  it('calls onEmailChange when email changes', async () => {
    const user = userEvent.setup();
    const mockOnEmailChange = vi.fn();

    render(<InvitationForm {...defaultProps} onEmailChange={mockOnEmailChange} />);

    const emailInput = screen.getByLabelText(/email address/i);
    await user.clear(emailInput);
    await user.type(emailInput, 'new@example.com');

    expect(mockOnEmailChange).toHaveBeenCalled();
  });

  it('calls onRoleChange when role changes', async () => {
    const user = userEvent.setup();
    const mockOnRoleChange = vi.fn();

    render(<InvitationForm {...defaultProps} onRoleChange={mockOnRoleChange} />);

    const roleSelect = screen.getByLabelText(/role/i);
    await user.selectOptions(roleSelect, 'ADMINISTRATOR');

    expect(mockOnRoleChange).toHaveBeenCalledWith('ADMINISTRATOR');
  });

  it('calls onMessageChange when message changes', async () => {
    const user = userEvent.setup();
    const mockOnMessageChange = vi.fn();

    render(<InvitationForm {...defaultProps} onMessageChange={mockOnMessageChange} />);

    const messageTextarea = screen.getByLabelText(/custom message/i);
    await user.clear(messageTextarea);
    await user.type(messageTextarea, 'New message');

    expect(mockOnMessageChange).toHaveBeenCalled();
  });

  it('calls onSubmit when form is submitted', async () => {
    const user = userEvent.setup();
    const mockOnSubmit = vi.fn((e) => e.preventDefault());

    render(<InvitationForm {...defaultProps} onSubmit={mockOnSubmit} />);

    const submitButton = screen.getByText('Send Invitation');
    await user.click(submitButton);

    expect(mockOnSubmit).toHaveBeenCalledTimes(1);
  });

  it('displays "Send Invitation" button when not inviting', () => {
    render(<InvitationForm {...defaultProps} inviting={false} />);
    expect(screen.getByText('Send Invitation')).toBeInTheDocument();
  });

  it('displays "Sending Invitation..." button when inviting', () => {
    render(<InvitationForm {...defaultProps} inviting={true} />);
    expect(screen.getByText('Sending Invitation...')).toBeInTheDocument();
  });

  it('disables submit button when inviting', () => {
    render(<InvitationForm {...defaultProps} inviting={true} />);
    const submitButton = screen.getByText('Sending Invitation...');
    expect(submitButton).toBeDisabled();
  });

  it('reflects form data values in inputs', () => {
    render(<InvitationForm {...defaultProps} />);

    expect(screen.getByDisplayValue('test@example.com')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Editor - Can create and modify')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Welcome to the team!')).toBeInTheDocument();
  });

  it('marks email as required field', () => {
    render(<InvitationForm {...defaultProps} />);

    const emailInput = screen.getByLabelText(/email address/i);
    expect(emailInput).toBeRequired();
  });

  it('has email input type', () => {
    render(<InvitationForm {...defaultProps} />);

    const emailInput = screen.getByLabelText(/email address/i);
    expect(emailInput).toHaveAttribute('type', 'email');
  });

  it('displays placeholder for email input', () => {
    render(<InvitationForm {...defaultProps} />);

    const emailInput = screen.getByLabelText(/email address/i);
    expect(emailInput).toHaveAttribute('placeholder', 'user@example.com');
  });

  it('displays placeholder for message textarea', () => {
    render(<InvitationForm {...defaultProps} />);

    const messageTextarea = screen.getByLabelText(/custom message/i);
    expect(messageTextarea).toHaveAttribute('placeholder', 'Add a personal message to the invitation email...');
  });
});
