import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import ProgressBar from '../ProgressBar';

/**
 * Tests for ProgressBar component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~5, Cog~8
 */
describe('ProgressBar', () => {
  it('renders with 0% progress', () => {
    render(<ProgressBar progress={0} />);
    expect(screen.getByText('0%')).toBeInTheDocument();
  });

  it('renders with 50% progress', () => {
    render(<ProgressBar progress={50} />);
    expect(screen.getByText('50%')).toBeInTheDocument();
  });

  it('renders with 100% progress', () => {
    render(<ProgressBar progress={100} />);
    expect(screen.getByText('100%')).toBeInTheDocument();
  });

  it('hides percentage when showPercentage is false', () => {
    render(<ProgressBar progress={75} showPercentage={false} />);
    expect(screen.queryByText('75%')).not.toBeInTheDocument();
  });

  it('shows percentage by default', () => {
    render(<ProgressBar progress={60} />);
    expect(screen.getByText('60%')).toBeInTheDocument();
  });

  it('renders progress bar container', () => {
    const { container } = render(<ProgressBar progress={75} />);
    // Verify the progress bar structure exists
    const progressContainer = container.querySelector('div > div');
    expect(progressContainer).toBeInTheDocument();
  });

  it('displays different progress values correctly', () => {
    const { rerender } = render(<ProgressBar progress={25} />);
    expect(screen.getByText('25%')).toBeInTheDocument();

    rerender(<ProgressBar progress={75} />);
    expect(screen.getByText('75%')).toBeInTheDocument();
  });

  it('handles edge cases for progress values', () => {
    const { rerender } = render(<ProgressBar progress={0} />);
    expect(screen.getByText('0%')).toBeInTheDocument();

    rerender(<ProgressBar progress={100} />);
    expect(screen.getByText('100%')).toBeInTheDocument();
  });
});
