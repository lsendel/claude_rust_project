import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import RoleBadge from '../RoleBadge';

/**
 * Tests for RoleBadge component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~4, Cog~6
 */
describe('RoleBadge', () => {
  it('renders ADMINISTRATOR role correctly', () => {
    render(<RoleBadge role="ADMINISTRATOR" />);
    expect(screen.getByText('ADMINISTRATOR')).toBeInTheDocument();
  });

  it('renders EDITOR role correctly', () => {
    render(<RoleBadge role="EDITOR" />);
    expect(screen.getByText('EDITOR')).toBeInTheDocument();
  });

  it('renders VIEWER role correctly', () => {
    render(<RoleBadge role="VIEWER" />);
    expect(screen.getByText('VIEWER')).toBeInTheDocument();
  });

  it('applies correct styles for ADMINISTRATOR role', () => {
    render(<RoleBadge role="ADMINISTRATOR" />);
    const badge = screen.getByText('ADMINISTRATOR');
    expect(badge).toHaveStyle({
      backgroundColor: '#fee2e2',
      color: '#991b1b',
    });
  });

  it('applies correct styles for EDITOR role', () => {
    render(<RoleBadge role="EDITOR" />);
    const badge = screen.getByText('EDITOR');
    expect(badge).toHaveStyle({
      backgroundColor: '#dbeafe',
      color: '#1e40af',
    });
  });

  it('applies correct styles for VIEWER role', () => {
    render(<RoleBadge role="VIEWER" />);
    const badge = screen.getByText('VIEWER');
    expect(badge).toHaveStyle({
      backgroundColor: '#f3f4f6',
      color: '#4b5563',
    });
  });
});
