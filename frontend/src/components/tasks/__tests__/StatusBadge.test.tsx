import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import StatusBadge from '../StatusBadge';

/**
 * Tests for StatusBadge component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~5, Cog~8
 */
describe('StatusBadge', () => {
  it('renders TODO status correctly', () => {
    render(<StatusBadge status="TODO" />);
    expect(screen.getByText('TODO')).toBeInTheDocument();
  });

  it('renders IN_PROGRESS status correctly with formatted text', () => {
    render(<StatusBadge status="IN_PROGRESS" />);
    expect(screen.getByText('IN PROGRESS')).toBeInTheDocument();
  });

  it('renders BLOCKED status correctly', () => {
    render(<StatusBadge status="BLOCKED" />);
    expect(screen.getByText('BLOCKED')).toBeInTheDocument();
  });

  it('renders COMPLETED status correctly', () => {
    render(<StatusBadge status="COMPLETED" />);
    expect(screen.getByText('COMPLETED')).toBeInTheDocument();
  });

  it('applies correct styles for TODO status', () => {
    render(<StatusBadge status="TODO" />);
    const badge = screen.getByText('TODO');
    expect(badge).toHaveStyle({
      backgroundColor: '#e5e7eb',
      color: '#374151',
    });
  });

  it('applies correct styles for IN_PROGRESS status', () => {
    render(<StatusBadge status="IN_PROGRESS" />);
    const badge = screen.getByText('IN PROGRESS');
    expect(badge).toHaveStyle({
      backgroundColor: '#dbeafe',
      color: '#1e40af',
    });
  });

  it('applies correct styles for BLOCKED status', () => {
    render(<StatusBadge status="BLOCKED" />);
    const badge = screen.getByText('BLOCKED');
    expect(badge).toHaveStyle({
      backgroundColor: '#fecaca',
      color: '#991b1b',
    });
  });

  it('applies correct styles for COMPLETED status', () => {
    render(<StatusBadge status="COMPLETED" />);
    const badge = screen.getByText('COMPLETED');
    expect(badge).toHaveStyle({
      backgroundColor: '#d1fae5',
      color: '#065f46',
    });
  });
});
