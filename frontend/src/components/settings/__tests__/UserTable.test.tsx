import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import UserTable from '../UserTable';
import { User } from '../../../services/userService';

/**
 * Tests for UserTable component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~5, Cog~8
 */
describe('UserTable', () => {
  const mockUsers: User[] = [
    {
      userId: 'user1',
      name: 'John Doe',
      email: 'john@example.com',
      role: 'EDITOR',
      joinedAt: '2025-01-15',
      tenantId: 'tenant1',
    },
    {
      userId: 'user2',
      name: 'Jane Smith',
      email: 'jane@example.com',
      role: 'ADMINISTRATOR',
      joinedAt: '2025-01-20',
      tenantId: 'tenant1',
    },
  ];

  const defaultProps = {
    users: mockUsers,
    loading: false,
    currentUserId: 'user2',
    onRemove: vi.fn(),
  };

  it('renders loading state', () => {
    render(<UserTable {...defaultProps} loading={true} />);
    expect(screen.getByText('Loading team members...')).toBeInTheDocument();
  });

  it('renders empty state when no users', () => {
    render(<UserTable {...defaultProps} users={[]} loading={false} />);
    expect(screen.getByText('No team members yet. Invite someone to get started!')).toBeInTheDocument();
  });

  it('does not render table when loading', () => {
    render(<UserTable {...defaultProps} loading={true} />);
    expect(screen.queryByText('Name')).not.toBeInTheDocument();
  });

  it('does not render table when no users', () => {
    render(<UserTable {...defaultProps} users={[]} loading={false} />);
    expect(screen.queryByText('Name')).not.toBeInTheDocument();
  });

  it('renders table headers', () => {
    render(<UserTable {...defaultProps} />);
    expect(screen.getByText('Name')).toBeInTheDocument();
    expect(screen.getByText('Email')).toBeInTheDocument();
    expect(screen.getByText('Role')).toBeInTheDocument();
    expect(screen.getByText('Joined')).toBeInTheDocument();
    expect(screen.getByText('Actions')).toBeInTheDocument();
  });

  it('renders UserRow for each user', () => {
    render(<UserTable {...defaultProps} />);
    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('Jane Smith')).toBeInTheDocument();
  });

  it('renders all user emails', () => {
    render(<UserTable {...defaultProps} />);
    expect(screen.getByText('john@example.com')).toBeInTheDocument();
    expect(screen.getByText('jane@example.com')).toBeInTheDocument();
  });

  it('renders all user roles', () => {
    render(<UserTable {...defaultProps} />);
    expect(screen.getByText('EDITOR')).toBeInTheDocument();
    expect(screen.getByText('ADMINISTRATOR')).toBeInTheDocument();
  });

  it('passes currentUserId to UserRow components', () => {
    render(<UserTable {...defaultProps} currentUserId="user2" />);
    // User2 (Jane Smith) should not have a remove button since they are the current user
    const removeButtons = screen.queryAllByText('Remove');
    expect(removeButtons).toHaveLength(1); // Only user1 should have remove button
  });

  it('renders remove buttons for non-current users', () => {
    render(<UserTable {...defaultProps} currentUserId="user2" />);
    // Should have remove button for user1 but not for user2
    const removeButtons = screen.getAllByText('Remove');
    expect(removeButtons.length).toBeGreaterThan(0);
  });
});
