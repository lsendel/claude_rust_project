import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import SettingsPage from '../SettingsPage';
import { userService } from '../../services/userService';

/**
 * Integration tests for SettingsPage.
 * Tests full page workflows including user management and invitations.
 * PMAT: Page complexity Cyc≤15, Cog≤30
 */

// Mock service
vi.mock('../../services/userService');

// Mock contexts
vi.mock('../../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: { id: 'user1', name: 'Admin User', email: 'admin@example.com' },
  }),
}));

vi.mock('../../contexts/TenantContext', () => ({
  useTenant: () => ({
    tenant: { id: 'tenant1', name: 'Test Tenant' },
  }),
}));

describe('SettingsPage Integration Tests', () => {
  const mockUsers = [
    {
      userId: 'user1',
      name: 'Admin User',
      email: 'admin@example.com',
      role: 'ADMINISTRATOR' as const,
      joinedAt: '2025-01-01',
      tenantId: 'tenant1',
    },
    {
      userId: 'user2',
      name: 'Editor User',
      email: 'editor@example.com',
      role: 'EDITOR' as const,
      joinedAt: '2025-01-15',
      tenantId: 'tenant1',
    },
    {
      userId: 'user3',
      name: 'Viewer User',
      email: 'viewer@example.com',
      role: 'VIEWER' as const,
      joinedAt: '2025-01-20',
      tenantId: 'tenant1',
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(userService.listUsers).mockResolvedValue(mockUsers);
  });

  describe('Page Rendering', () => {
    it('renders page title and sections', async () => {
      render(<SettingsPage />);

      expect(screen.getByText('Settings')).toBeInTheDocument();
      expect(screen.getByText('Invite Team Member')).toBeInTheDocument();
      expect(screen.getByText('Team Members')).toBeInTheDocument();
    });

    it('loads and displays team members on mount', async () => {
      render(<SettingsPage />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
        expect(screen.getByText('Editor User')).toBeInTheDocument();
        expect(screen.getByText('Viewer User')).toBeInTheDocument();
      });
    });

    it('displays loading state initially', () => {
      render(<SettingsPage />);
      expect(screen.getByText('Loading team members...')).toBeInTheDocument();
    });

    it('renders invitation form with all fields', async () => {
      render(<SettingsPage />);

      await waitFor(() => {
        expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/role/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/custom message/i)).toBeInTheDocument();
        expect(screen.getByText('Send Invitation')).toBeInTheDocument();
      });
    });

    it('calls listUsers with correct tenant ID', async () => {
      render(<SettingsPage />);

      await waitFor(() => {
        expect(userService.listUsers).toHaveBeenCalledWith('tenant1');
      });
    });
  });

  describe('Invite User Workflow', () => {
    it('invites user successfully and shows success message', async () => {
      const user = userEvent.setup();
      vi.mocked(userService.inviteUser).mockResolvedValue({
        userId: 'user4',
        email: 'newuser@example.com',
        role: 'EDITOR' as const,
        emailSent: true,
      });

      render(<SettingsPage />);

      await waitFor(() => expect(screen.getByText('Admin User')).toBeInTheDocument());

      // Fill invitation form
      const emailInput = screen.getByLabelText(/email address/i);
      await user.type(emailInput, 'newuser@example.com');

      const roleSelect = screen.getByLabelText(/role/i);
      await user.selectOptions(roleSelect, 'EDITOR');

      const messageTextarea = screen.getByLabelText(/custom message/i);
      await user.type(messageTextarea, 'Welcome to the team!');

      // Submit form
      const submitButton = screen.getByText('Send Invitation');
      await user.click(submitButton);

      // Verify service was called
      await waitFor(() => {
        expect(userService.inviteUser).toHaveBeenCalledWith('tenant1', {
          email: 'newuser@example.com',
          role: 'EDITOR',
          message: 'Welcome to the team!',
        });
      });

      // Verify success message
      await waitFor(() => {
        expect(screen.getByText('Invitation sent to newuser@example.com')).toBeInTheDocument();
      });

      // Verify form is reset
      expect(emailInput).toHaveValue('');
      expect(messageTextarea).toHaveValue('');
    });

    it('shows appropriate message when email not sent', async () => {
      const user = userEvent.setup();
      vi.mocked(userService.inviteUser).mockResolvedValue({
        userId: 'user4',
        email: 'newuser@example.com',
        role: 'VIEWER' as const,
        emailSent: false,
      });

      render(<SettingsPage />);

      await waitFor(() => expect(screen.getByText('Admin User')).toBeInTheDocument());

      const emailInput = screen.getByLabelText(/email address/i);
      await user.type(emailInput, 'newuser@example.com');

      await user.click(screen.getByText('Send Invitation'));

      await waitFor(() => {
        expect(screen.getByText(/added to team \(email not sent\)/i)).toBeInTheDocument();
      });
    });

    it('displays permission error when user lacks access', async () => {
      const user = userEvent.setup();
      vi.mocked(userService.inviteUser).mockRejectedValue({
        response: { status: 403 },
      });

      render(<SettingsPage />);

      await waitFor(() => expect(screen.getByText('Admin User')).toBeInTheDocument());

      const emailInput = screen.getByLabelText(/email address/i);
      await user.type(emailInput, 'newuser@example.com');

      await user.click(screen.getByText('Send Invitation'));

      await waitFor(() => {
        expect(screen.getByText(/permission to invite users/i)).toBeInTheDocument();
      });
    });

    it('displays error when invitation fails', async () => {
      const user = userEvent.setup();
      vi.mocked(userService.inviteUser).mockRejectedValue({
        response: { data: { message: 'Email already exists' } },
      });

      render(<SettingsPage />);

      await waitFor(() => expect(screen.getByText('Admin User')).toBeInTheDocument());

      const emailInput = screen.getByLabelText(/email address/i);
      await user.type(emailInput, 'existing@example.com');

      await user.click(screen.getByText('Send Invitation'));

      await waitFor(() => {
        expect(screen.getByText('Email already exists')).toBeInTheDocument();
      });
    });

    it('shows inviting state during submission', async () => {
      const user = userEvent.setup();
      vi.mocked(userService.inviteUser).mockImplementation(
        () => new Promise((resolve) => setTimeout(resolve, 100))
      );

      render(<SettingsPage />);

      await waitFor(() => expect(screen.getByText('Admin User')).toBeInTheDocument());

      const emailInput = screen.getByLabelText(/email address/i);
      await user.type(emailInput, 'newuser@example.com');

      const submitButton = screen.getByText('Send Invitation');
      await user.click(submitButton);

      // Should show "Sending Invitation..." during submission
      expect(screen.getByText('Sending Invitation...')).toBeInTheDocument();
      expect(screen.getByText('Sending Invitation...')).toBeDisabled();
    });

    it('reloads user list after successful invitation', async () => {
      const user = userEvent.setup();
      vi.mocked(userService.inviteUser).mockResolvedValue({
        userId: 'user4',
        email: 'newuser@example.com',
        role: 'VIEWER' as const,
        emailSent: true,
      });

      render(<SettingsPage />);

      await waitFor(() => expect(screen.getByText('Admin User')).toBeInTheDocument());

      // Initial load
      expect(userService.listUsers).toHaveBeenCalledTimes(1);

      const emailInput = screen.getByLabelText(/email address/i);
      await user.type(emailInput, 'newuser@example.com');
      await user.click(screen.getByText('Send Invitation'));

      // Should reload after invitation
      await waitFor(() => {
        expect(userService.listUsers).toHaveBeenCalledTimes(2);
      });
    });
  });

  describe('Remove User Workflow', () => {
    it('removes user after confirmation', async () => {
      const user = userEvent.setup();
      vi.mocked(userService.removeUser).mockResolvedValue(undefined);
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);

      render(<SettingsPage />);

      await waitFor(() => expect(screen.getByText('Editor User')).toBeInTheDocument());

      // Find remove buttons (current user won't have one)
      const removeButtons = screen.getAllByText('Remove');
      await user.click(removeButtons[0]);

      expect(confirmSpy).toHaveBeenCalledWith(
        expect.stringContaining('editor@example.com')
      );

      await waitFor(() => {
        expect(userService.removeUser).toHaveBeenCalledWith('tenant1', 'user2');
        expect(screen.getByText(/editor@example.com has been removed/i)).toBeInTheDocument();
      });

      confirmSpy.mockRestore();
    });

    it('does not remove user when confirmation is cancelled', async () => {
      const user = userEvent.setup();
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false);

      render(<SettingsPage />);

      await waitFor(() => expect(screen.getByText('Editor User')).toBeInTheDocument());

      const removeButtons = screen.getAllByText('Remove');
      await user.click(removeButtons[0]);

      expect(confirmSpy).toHaveBeenCalled();
      expect(userService.removeUser).not.toHaveBeenCalled();

      confirmSpy.mockRestore();
    });

    it('displays permission error when user lacks access to remove', async () => {
      const user = userEvent.setup();
      vi.mocked(userService.removeUser).mockRejectedValue({
        response: { status: 403 },
      });
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);

      render(<SettingsPage />);

      await waitFor(() => expect(screen.getByText('Editor User')).toBeInTheDocument());

      const removeButtons = screen.getAllByText('Remove');
      await user.click(removeButtons[0]);

      await waitFor(() => {
        expect(screen.getByText(/permission to remove users/i)).toBeInTheDocument();
      });

      confirmSpy.mockRestore();
    });

    it('displays error when remove fails', async () => {
      const user = userEvent.setup();
      vi.mocked(userService.removeUser).mockRejectedValue({
        response: { data: { message: 'Cannot remove last admin' } },
      });
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);

      render(<SettingsPage />);

      await waitFor(() => expect(screen.getByText('Editor User')).toBeInTheDocument());

      const removeButtons = screen.getAllByText('Remove');
      await user.click(removeButtons[0]);

      await waitFor(() => {
        expect(screen.getByText('Cannot remove last admin')).toBeInTheDocument();
      });

      confirmSpy.mockRestore();
    });

    it('reloads user list after successful removal', async () => {
      const user = userEvent.setup();
      vi.mocked(userService.removeUser).mockResolvedValue(undefined);
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);

      render(<SettingsPage />);

      await waitFor(() => expect(screen.getByText('Editor User')).toBeInTheDocument());

      // Initial load
      expect(userService.listUsers).toHaveBeenCalledTimes(1);

      const removeButtons = screen.getAllByText('Remove');
      await user.click(removeButtons[0]);

      // Should reload after removal
      await waitFor(() => {
        expect(userService.listUsers).toHaveBeenCalledTimes(2);
      });

      confirmSpy.mockRestore();
    });

    it('does not show remove button for current user', async () => {
      render(<SettingsPage />);

      await waitFor(() => expect(screen.getByText('Admin User')).toBeInTheDocument());

      // Should have 2 remove buttons (for user2 and user3, but not user1 who is current)
      const removeButtons = screen.getAllByText('Remove');
      expect(removeButtons).toHaveLength(2);
    });
  });

  describe('Error Handling', () => {
    it('displays error when users fail to load', async () => {
      vi.mocked(userService.listUsers).mockRejectedValue({
        response: { data: { message: 'Failed to load users' } },
      });

      render(<SettingsPage />);

      await waitFor(() => {
        expect(screen.getByText('Failed to load users')).toBeInTheDocument();
      });
    });

    it('closes error banner when close button is clicked', async () => {
      const user = userEvent.setup();
      vi.mocked(userService.listUsers).mockRejectedValue({
        response: { data: { message: 'Failed to load users' } },
      });

      render(<SettingsPage />);

      await waitFor(() => {
        expect(screen.getByText('Failed to load users')).toBeInTheDocument();
      });

      const closeButton = screen.getByText('×');
      await user.click(closeButton);

      expect(screen.queryByText('Failed to load users')).not.toBeInTheDocument();
    });

    it('closes success banner when close button is clicked', async () => {
      const user = userEvent.setup();
      vi.mocked(userService.inviteUser).mockResolvedValue({
        userId: 'user4',
        email: 'newuser@example.com',
        role: 'VIEWER' as const,
        emailSent: true,
      });

      render(<SettingsPage />);

      await waitFor(() => expect(screen.getByText('Admin User')).toBeInTheDocument());

      const emailInput = screen.getByLabelText(/email address/i);
      await user.type(emailInput, 'newuser@example.com');
      await user.click(screen.getByText('Send Invitation'));

      await waitFor(() => {
        expect(screen.getByText(/Invitation sent/i)).toBeInTheDocument();
      });

      const closeButton = screen.getByText('×');
      await user.click(closeButton);

      expect(screen.queryByText(/Invitation sent/i)).not.toBeInTheDocument();
    });
  });

  describe('User Display', () => {
    it('displays all user roles correctly', async () => {
      render(<SettingsPage />);

      await waitFor(() => {
        expect(screen.getByText('ADMINISTRATOR')).toBeInTheDocument();
        expect(screen.getByText('EDITOR')).toBeInTheDocument();
        expect(screen.getByText('VIEWER')).toBeInTheDocument();
      });
    });

    it('displays all user emails', async () => {
      render(<SettingsPage />);

      await waitFor(() => {
        expect(screen.getByText('admin@example.com')).toBeInTheDocument();
        expect(screen.getByText('editor@example.com')).toBeInTheDocument();
        expect(screen.getByText('viewer@example.com')).toBeInTheDocument();
      });
    });

    it('displays empty state when no users', async () => {
      vi.mocked(userService.listUsers).mockResolvedValue([]);

      render(<SettingsPage />);

      await waitFor(() => {
        expect(screen.getByText('No team members yet. Invite someone to get started!')).toBeInTheDocument();
      });
    });
  });
});
