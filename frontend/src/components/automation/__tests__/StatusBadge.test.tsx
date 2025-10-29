import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import StatusBadge from '../StatusBadge';

/**
 * Tests for StatusBadge component (Automation).
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~5, Cog~8
 */
describe('StatusBadge (Automation)', () => {
  it('renders SUCCESS status correctly', () => {
    render(<StatusBadge status="SUCCESS" />);
    expect(screen.getByText('SUCCESS')).toBeInTheDocument();
  });

  it('renders FAILED status correctly', () => {
    render(<StatusBadge status="FAILED" />);
    expect(screen.getByText('FAILED')).toBeInTheDocument();
  });

  it('renders SKIPPED status correctly', () => {
    render(<StatusBadge status="SKIPPED" />);
    expect(screen.getByText('SKIPPED')).toBeInTheDocument();
  });

  it('renders NO_RULES_MATCHED status correctly', () => {
    render(<StatusBadge status="NO_RULES_MATCHED" />);
    expect(screen.getByText('NO_RULES_MATCHED')).toBeInTheDocument();
  });

  it('applies correct styles for SUCCESS status', () => {
    render(<StatusBadge status="SUCCESS" />);
    const badge = screen.getByText('SUCCESS');
    expect(badge).toHaveStyle({
      backgroundColor: '#d1fae5',
      color: '#065f46',
    });
  });

  it('applies correct styles for FAILED status', () => {
    render(<StatusBadge status="FAILED" />);
    const badge = screen.getByText('FAILED');
    expect(badge).toHaveStyle({
      backgroundColor: '#fee2e2',
      color: '#991b1b',
    });
  });

  it('applies correct styles for SKIPPED status', () => {
    render(<StatusBadge status="SKIPPED" />);
    const badge = screen.getByText('SKIPPED');
    expect(badge).toHaveStyle({
      backgroundColor: '#fef3c7',
      color: '#92400e',
    });
  });

  it('applies correct styles for NO_RULES_MATCHED status', () => {
    render(<StatusBadge status="NO_RULES_MATCHED" />);
    const badge = screen.getByText('NO_RULES_MATCHED');
    expect(badge).toHaveStyle({
      backgroundColor: '#f3f4f6',
      color: '#4b5563',
    });
  });
});
