import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import MessageBanner from '../MessageBanner';

/**
 * Tests for MessageBanner component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~3, Cog~5
 */
describe('MessageBanner', () => {
  it('renders success message correctly', () => {
    const mockOnClose = vi.fn();
    render(<MessageBanner message="Operation successful" type="success" onClose={mockOnClose} />);
    expect(screen.getByText('Operation successful')).toBeInTheDocument();
  });

  it('renders error message correctly', () => {
    const mockOnClose = vi.fn();
    render(<MessageBanner message="Operation failed" type="error" onClose={mockOnClose} />);
    expect(screen.getByText('Operation failed')).toBeInTheDocument();
  });

  it('renders close button', () => {
    const mockOnClose = vi.fn();
    render(<MessageBanner message="Test" type="success" onClose={mockOnClose} />);
    expect(screen.getByRole('button')).toBeInTheDocument();
  });

  it('calls onClose when close button is clicked', async () => {
    const user = userEvent.setup();
    const mockOnClose = vi.fn();
    render(<MessageBanner message="Test" type="success" onClose={mockOnClose} />);

    const closeButton = screen.getByRole('button');
    await user.click(closeButton);

    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  it('applies success styles', () => {
    const mockOnClose = vi.fn();
    render(<MessageBanner message="Success" type="success" onClose={mockOnClose} />);

    const banner = screen.getByText('Success').parentElement;
    expect(banner).toHaveStyle({
      backgroundColor: '#d1fae5',
      color: '#065f46',
    });
  });

  it('applies error styles', () => {
    const mockOnClose = vi.fn();
    render(<MessageBanner message="Error" type="error" onClose={mockOnClose} />);

    const banner = screen.getByText('Error').parentElement;
    expect(banner).toHaveStyle({
      backgroundColor: '#fee2e2',
      color: '#991b1b',
    });
  });

  it('renders with different message types', () => {
    const mockOnClose = vi.fn();
    const { rerender } = render(<MessageBanner message="First message" type="success" onClose={mockOnClose} />);
    expect(screen.getByText('First message')).toBeInTheDocument();

    rerender(<MessageBanner message="Second message" type="error" onClose={mockOnClose} />);
    expect(screen.getByText('Second message')).toBeInTheDocument();
    expect(screen.queryByText('First message')).not.toBeInTheDocument();
  });

  it('handles long messages', () => {
    const mockOnClose = vi.fn();
    const longMessage = 'This is a very long message that contains multiple words and should still be displayed correctly in the message banner component regardless of the type';
    render(<MessageBanner message={longMessage} type="success" onClose={mockOnClose} />);
    expect(screen.getByText(longMessage)).toBeInTheDocument();
  });
});
