import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import UserRow from '../UserRow';
import { User } from '../../../services/userService';

/**
 * Tests for UserRow component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~4, Cog~6
 */
describe('UserRow', () => {
  const mockUser: User = {
    userId: 'user1',
    name: 'John Doe',
    email: 'john@example.com',
    role: 'EDITOR',
    joinedAt: '2025-01-15',
    tenantId: 'tenant1',
  };

  const defaultProps = {
    user: mockUser,
    currentUserId: 'user2',
    onRemove: vi.fn(),
  };

  it('renders user name', () => {
    render(
      <table>
        <tbody>
          <UserRow {...defaultProps} />
        </tbody>
      </table>
    );
    expect(screen.getByText('John Doe')).toBeInTheDocument();
  });

  it('renders user email', () => {
    render(
      <table>
        <tbody>
          <UserRow {...defaultProps} />
        </tbody>
      </table>
    );
    expect(screen.getByText('john@example.com')).toBeInTheDocument();
  });

  it('renders role badge', () => {
    render(
      <table>
        <tbody>
          <UserRow {...defaultProps} />
        </tbody>
      </table>
    );
    expect(screen.getByText('EDITOR')).toBeInTheDocument();
  });

  it('renders formatted joined date', () => {
    render(
      <table>
        <tbody>
          <UserRow {...defaultProps} />
        </tbody>
      </table>
    );
    const formattedDate = new Date('2025-01-15').toLocaleDateString();
    expect(screen.getByText(formattedDate)).toBeInTheDocument();
  });

  it('renders remove button when user is not current user', () => {
    render(
      <table>
        <tbody>
          <UserRow {...defaultProps} currentUserId="user2" />
        </tbody>
      </table>
    );
    expect(screen.getByText('Remove')).toBeInTheDocument();
  });

  it('does not render remove button when user is current user', () => {
    render(
      <table>
        <tbody>
          <UserRow {...defaultProps} currentUserId="user1" />
        </tbody>
      </table>
    );
    expect(screen.queryByText('Remove')).not.toBeInTheDocument();
  });

  it('calls onRemove when remove button is clicked', async () => {
    const user = userEvent.setup();
    const mockOnRemove = vi.fn();

    render(
      <table>
        <tbody>
          <UserRow {...defaultProps} onRemove={mockOnRemove} />
        </tbody>
      </table>
    );

    const removeButton = screen.getByText('Remove');
    await user.click(removeButton);

    expect(mockOnRemove).toHaveBeenCalledWith('user1', 'john@example.com');
  });

  it('renders different role badges', () => {
    const adminUser = { ...mockUser, role: 'ADMINISTRATOR' as const };
    render(
      <table>
        <tbody>
          <UserRow {...defaultProps} user={adminUser} />
        </tbody>
      </table>
    );
    expect(screen.getByText('ADMINISTRATOR')).toBeInTheDocument();
  });

  it('renders complete user data structure', () => {
    render(
      <table>
        <tbody>
          <UserRow {...defaultProps} />
        </tbody>
      </table>
    );

    // Verify all main elements are present
    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('john@example.com')).toBeInTheDocument();
    expect(screen.getByText('EDITOR')).toBeInTheDocument();
    expect(screen.getByText('Remove')).toBeInTheDocument();
  });
});
