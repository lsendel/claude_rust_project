import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import RuleModal from '../RuleModal';
import { AutomationRule } from '../../../services/automationService';

/**
 * Tests for RuleModal component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~4, Cog~6
 */
describe('RuleModal', () => {
  const mockFormData: Partial<AutomationRule> = {
    name: 'Test Rule',
    eventType: 'task.created',
    actionType: 'send_email',
  };

  const defaultProps = {
    isOpen: true,
    isEditMode: false,
    formData: mockFormData,
    onSubmit: vi.fn(),
    onClose: vi.fn(),
    onChange: vi.fn(),
  };

  it('renders nothing when isOpen is false', () => {
    const { container } = render(<RuleModal {...defaultProps} isOpen={false} />);
    expect(container.firstChild).toBeNull();
  });

  it('renders modal when isOpen is true', () => {
    render(<RuleModal {...defaultProps} isOpen={true} />);
    expect(screen.getByText('Create Automation Rule')).toBeInTheDocument();
  });

  it('displays "Create Automation Rule" header in create mode', () => {
    render(<RuleModal {...defaultProps} isEditMode={false} />);
    expect(screen.getByText('Create Automation Rule')).toBeInTheDocument();
  });

  it('displays "Edit Automation Rule" header in edit mode', () => {
    render(<RuleModal {...defaultProps} isEditMode={true} />);
    expect(screen.getByText('Edit Automation Rule')).toBeInTheDocument();
  });

  it('renders RuleForm component with all fields', () => {
    render(<RuleModal {...defaultProps} />);

    // Verify form fields are present
    expect(screen.getByText(/rule name/i)).toBeInTheDocument();
    expect(screen.getByText(/event type/i)).toBeInTheDocument();
    expect(screen.getByText(/action type/i)).toBeInTheDocument();
  });

  it('calls onClose when overlay is clicked', async () => {
    const user = userEvent.setup();
    const mockOnClose = vi.fn();

    render(<RuleModal {...defaultProps} onClose={mockOnClose} />);

    // Click on overlay (the backdrop)
    const overlay = screen.getByText('Create Automation Rule').parentElement?.parentElement;
    if (overlay) {
      await user.click(overlay);
      expect(mockOnClose).toHaveBeenCalledTimes(1);
    }
  });

  it('does not call onClose when modal content is clicked', async () => {
    const user = userEvent.setup();
    const mockOnClose = vi.fn();

    render(<RuleModal {...defaultProps} onClose={mockOnClose} />);

    // Click on modal content (the header)
    const header = screen.getByText('Create Automation Rule');
    await user.click(header);

    // onClose should not be called
    expect(mockOnClose).not.toHaveBeenCalled();
  });

  it('passes correct props to RuleForm', () => {
    render(<RuleModal {...defaultProps} />);

    // Verify form has correct data
    expect(screen.getByDisplayValue('Test Rule')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Task Created')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Send Email')).toBeInTheDocument();
  });
});
