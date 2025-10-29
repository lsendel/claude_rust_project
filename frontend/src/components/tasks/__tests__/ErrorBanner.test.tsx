import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ErrorBanner from '../ErrorBanner';

/**
 * Tests for ErrorBanner component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~2, Cog~3
 */
describe('ErrorBanner', () => {
  it('renders error message correctly', () => {
    const mockOnClose = vi.fn();
    render(<ErrorBanner message="Test error message" onClose={mockOnClose} />);
    expect(screen.getByText('Test error message')).toBeInTheDocument();
  });

  it('renders close button', () => {
    const mockOnClose = vi.fn();
    render(<ErrorBanner message="Error" onClose={mockOnClose} />);
    expect(screen.getByRole('button')).toBeInTheDocument();
  });

  it('calls onClose when close button is clicked', async () => {
    const user = userEvent.setup();
    const mockOnClose = vi.fn();
    render(<ErrorBanner message="Error" onClose={mockOnClose} />);

    const closeButton = screen.getByRole('button');
    await user.click(closeButton);

    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  it('applies correct error styles', () => {
    const mockOnClose = vi.fn();
    render(<ErrorBanner message="Error" onClose={mockOnClose} />);

    const banner = screen.getByText('Error').parentElement;
    expect(banner).toHaveStyle({
      backgroundColor: '#fee2e2',
      color: '#991b1b',
    });
  });

  it('renders with different error messages', () => {
    const mockOnClose = vi.fn();
    const { rerender } = render(<ErrorBanner message="First error" onClose={mockOnClose} />);
    expect(screen.getByText('First error')).toBeInTheDocument();

    rerender(<ErrorBanner message="Second error" onClose={mockOnClose} />);
    expect(screen.getByText('Second error')).toBeInTheDocument();
    expect(screen.queryByText('First error')).not.toBeInTheDocument();
  });

  it('handles long error messages', () => {
    const mockOnClose = vi.fn();
    const longMessage = 'This is a very long error message that contains multiple words and should still be displayed correctly in the error banner component';
    render(<ErrorBanner message={longMessage} onClose={mockOnClose} />);
    expect(screen.getByText(longMessage)).toBeInTheDocument();
  });
});
