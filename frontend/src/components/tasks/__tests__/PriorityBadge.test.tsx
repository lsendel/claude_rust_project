import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import PriorityBadge from '../PriorityBadge';

/**
 * Tests for PriorityBadge component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~5, Cog~8
 */
describe('PriorityBadge', () => {
  it('renders LOW priority correctly', () => {
    render(<PriorityBadge priority="LOW" />);
    expect(screen.getByText('LOW')).toBeInTheDocument();
  });

  it('renders MEDIUM priority correctly', () => {
    render(<PriorityBadge priority="MEDIUM" />);
    expect(screen.getByText('MEDIUM')).toBeInTheDocument();
  });

  it('renders HIGH priority correctly', () => {
    render(<PriorityBadge priority="HIGH" />);
    expect(screen.getByText('HIGH')).toBeInTheDocument();
  });

  it('renders CRITICAL priority correctly', () => {
    render(<PriorityBadge priority="CRITICAL" />);
    expect(screen.getByText('CRITICAL')).toBeInTheDocument();
  });

  it('applies correct styles for LOW priority', () => {
    render(<PriorityBadge priority="LOW" />);
    const badge = screen.getByText('LOW');
    expect(badge).toHaveStyle({
      backgroundColor: '#dbeafe',
      color: '#1e40af',
    });
  });

  it('applies correct styles for MEDIUM priority', () => {
    render(<PriorityBadge priority="MEDIUM" />);
    const badge = screen.getByText('MEDIUM');
    expect(badge).toHaveStyle({
      backgroundColor: '#fef3c7',
      color: '#92400e',
    });
  });

  it('applies correct styles for HIGH priority', () => {
    render(<PriorityBadge priority="HIGH" />);
    const badge = screen.getByText('HIGH');
    expect(badge).toHaveStyle({
      backgroundColor: '#fed7aa',
      color: '#9a3412',
    });
  });

  it('applies correct styles for CRITICAL priority', () => {
    render(<PriorityBadge priority="CRITICAL" />);
    const badge = screen.getByText('CRITICAL');
    expect(badge).toHaveStyle({
      backgroundColor: '#fecaca',
      color: '#991b1b',
    });
  });
});
