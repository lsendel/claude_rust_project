import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import AutomationPage from '../AutomationPage';
import { automationService } from '../../services/automationService';

/**
 * Integration tests for AutomationPage.
 * Tests full page workflows including rules management, tab switching, and event logs.
 * PMAT: Page complexity Cyc≤15, Cog≤30
 */

// Mock service
vi.mock('../../services/automationService');

// Mock contexts
vi.mock('../../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: { id: 'user1', name: 'Test User' },
  }),
}));

vi.mock('../../contexts/TenantContext', () => ({
  useTenant: () => ({
    tenant: { id: 'tenant1', name: 'Test Tenant' },
  }),
}));

describe('AutomationPage Integration Tests', () => {
  const mockRules = [
    {
      id: 'rule1',
      tenantId: 'tenant1',
      name: 'Send email on task completion',
      eventType: 'task.status.changed' as const,
      actionType: 'send_email' as const,
      conditions: {},
      actionConfig: {},
      isActive: true,
      executionCount: 5,
      lastExecutionAt: '2025-01-20T10:00:00Z',
      createdAt: '2025-01-01',
    },
    {
      id: 'rule2',
      tenantId: 'tenant1',
      name: 'Notify on high priority',
      eventType: 'task.created' as const,
      actionType: 'webhook' as const,
      conditions: {},
      actionConfig: {},
      isActive: false,
      executionCount: 2,
      createdAt: '2025-01-05',
    },
  ];

  const mockLogs = [
    {
      id: 'log1',
      ruleId: 'rule1',
      eventType: 'task.status.changed' as const,
      actionType: 'send_email' as const,
      status: 'SUCCESS' as const,
      executionDurationMs: 150,
      createdAt: '2025-01-20T10:00:00Z',
    },
    {
      id: 'log2',
      ruleId: 'rule2',
      eventType: 'task.created' as const,
      actionType: 'webhook' as const,
      status: 'FAILED' as const,
      executionDurationMs: 50,
      errorMessage: 'Network error',
      createdAt: '2025-01-20T09:00:00Z',
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(automationService.getAllRules).mockResolvedValue(mockRules);
    vi.mocked(automationService.getRecentLogs).mockResolvedValue(mockLogs);
  });

  describe('Page Rendering and Tabs', () => {
    it('renders page title and create button', async () => {
      render(<AutomationPage />);

      expect(screen.getByText('Automation Rules')).toBeInTheDocument();
      expect(screen.getByText('+ Create Rule')).toBeInTheDocument();
    });

    it('loads and displays rules on mount', async () => {
      render(<AutomationPage />);

      // Wait for loading to complete (Loading... should disappear)
      await waitFor(() => {
        expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
      }, { timeout: 3000 });

      // Then check for rules
      await waitFor(() => {
        expect(automationService.getAllRules).toHaveBeenCalled();
        expect(screen.getByText('Send email on task completion')).toBeInTheDocument();
      }, { timeout: 3000 });
    });

    it('displays loading state initially', () => {
      render(<AutomationPage />);
      expect(screen.getByText('Loading...')).toBeInTheDocument();
    });

    it('shows rules tab as active by default', async () => {
      render(<AutomationPage />);

      await waitFor(() => {
        expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
      }, { timeout: 3000 });

      await waitFor(() => {
        const rulesTab = screen.getByText(/Automation Rules \(2\)/i);
        expect(rulesTab).toBeInTheDocument();
      });
    });

    it('switches to logs tab when clicked', async () => {
      const user = userEvent.setup();
      render(<AutomationPage />);

      // Wait for page to load
      await waitFor(() => {
        expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
      }, { timeout: 3000 });

      const logsTab = screen.getByText(/Event Logs \(2\)/i);
      await user.click(logsTab);

      // Check that event logs table is displayed
      await waitFor(() => {
        expect(screen.getByText('Event Type')).toBeInTheDocument();
      });
    });

    it('displays empty state when no rules exist', async () => {
      vi.mocked(automationService.getAllRules).mockResolvedValue([]);

      render(<AutomationPage />);

      await waitFor(() => {
        expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
      }, { timeout: 3000 });

      await waitFor(() => {
        expect(screen.getByText('No automation rules yet')).toBeInTheDocument();
      });
    });
  });

  describe('Create Rule Workflow', () => {
    it('opens create modal when create button is clicked', async () => {
      const user = userEvent.setup();
      render(<AutomationPage />);

      await waitFor(() => {
        expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
      }, { timeout: 3000 });

      await user.click(screen.getByText('+ Create Rule'));

      expect(screen.getByText('Create Automation Rule')).toBeInTheDocument();
    });

    it('creates rule successfully and shows success message', async () => {
      const user = userEvent.setup();
      vi.mocked(automationService.createRule).mockResolvedValue({
        ...mockRules[0],
        id: 'rule3',
        name: 'New Rule',
      });

      render(<AutomationPage />);

      await waitFor(() => {
        expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
      }, { timeout: 3000 });

      await user.click(screen.getByText('+ Create Rule'));
      await waitFor(() => expect(screen.getByLabelText(/rule name/i)).toBeInTheDocument());

      const nameInput = screen.getByLabelText(/rule name/i);
      await user.type(nameInput, 'New Rule');

      const submitButtons = screen.getAllByText('Create Rule');
      await user.click(submitButtons[submitButtons.length - 1]);

      await waitFor(() => {
        expect(screen.getByText('Automation rule created successfully')).toBeInTheDocument();
      });
    });

    it('displays permission error when user lacks access', async () => {
      const user = userEvent.setup();
      vi.mocked(automationService.createRule).mockRejectedValue({
        response: { status: 403 },
      });

      render(<AutomationPage />);

      await waitFor(() => {
        expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
      }, { timeout: 3000 });

      await user.click(screen.getByText('+ Create Rule'));
      await waitFor(() => expect(screen.getByLabelText(/rule name/i)).toBeInTheDocument());

      const nameInput = screen.getByLabelText(/rule name/i);
      await user.type(nameInput, 'New Rule');

      const submitButtons = screen.getAllByText('Create Rule');
      await user.click(submitButtons[submitButtons.length - 1]);

      await waitFor(() => {
        expect(screen.getByText(/permission to create automation rules/i)).toBeInTheDocument();
      });
    });
  });

  describe('Edit Rule Workflow', () => {
    it('opens edit modal with prefilled data', async () => {
      const user = userEvent.setup();
      render(<AutomationPage />);

      await waitFor(() => {
        expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
      }, { timeout: 3000 });

      const editButtons = screen.getAllByText('Edit');
      await user.click(editButtons[0]);

      expect(screen.getByText('Edit Automation Rule')).toBeInTheDocument();
      expect(screen.getByDisplayValue('Send email on task completion')).toBeInTheDocument();
    });

    it('updates rule successfully', async () => {
      const user = userEvent.setup();
      vi.mocked(automationService.updateRule).mockResolvedValue(mockRules[0]);

      render(<AutomationPage />);

      await waitFor(() => {
        expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
      }, { timeout: 3000 });

      const editButtons = screen.getAllByText('Edit');
      await user.click(editButtons[0]);

      const nameInput = screen.getByDisplayValue('Send email on task completion');
      await user.clear(nameInput);
      await user.type(nameInput, 'Updated Rule');

      const updateButton = screen.getByText('Update Rule');
      await user.click(updateButton);

      await waitFor(() => {
        expect(screen.getByText('Automation rule updated successfully')).toBeInTheDocument();
      });
    });
  });

  describe('Toggle Rule Status', () => {
    it('toggles rule from active to inactive', async () => {
      const user = userEvent.setup();
      vi.mocked(automationService.toggleRuleStatus).mockResolvedValue(undefined);

      render(<AutomationPage />);

      await waitFor(() => {
        expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
      }, { timeout: 3000 });

      const activeButtons = screen.getAllByText('Active');
      await user.click(activeButtons[0]);

      await waitFor(() => {
        expect(automationService.toggleRuleStatus).toHaveBeenCalledWith('rule1', false);
        expect(screen.getByText('Automation rule disabled successfully')).toBeInTheDocument();
      });
    });

    it('displays permission error when toggling without access', async () => {
      const user = userEvent.setup();
      vi.mocked(automationService.toggleRuleStatus).mockRejectedValue({
        response: { status: 403 },
      });

      render(<AutomationPage />);

      await waitFor(() => {
        expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
      }, { timeout: 3000 });

      const activeButtons = screen.getAllByText('Active');
      await user.click(activeButtons[0]);

      await waitFor(() => {
        expect(screen.getByText(/permission to toggle automation rules/i)).toBeInTheDocument();
      });
    });
  });

  describe('Delete Rule Workflow', () => {
    it('deletes rule after confirmation', async () => {
      const user = userEvent.setup();
      vi.mocked(automationService.deleteRule).mockResolvedValue(undefined);
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);

      render(<AutomationPage />);

      await waitFor(() => {
        expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
      }, { timeout: 3000 });

      const deleteButtons = screen.getAllByText('Delete');
      await user.click(deleteButtons[0]);

      expect(confirmSpy).toHaveBeenCalled();

      await waitFor(() => {
        expect(automationService.deleteRule).toHaveBeenCalledWith('rule1');
        expect(screen.getByText('Automation rule deleted successfully')).toBeInTheDocument();
      });

      confirmSpy.mockRestore();
    });

    it('does not delete rule when confirmation is cancelled', async () => {
      const user = userEvent.setup();
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false);

      render(<AutomationPage />);

      await waitFor(() => {
        expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
      }, { timeout: 3000 });

      const deleteButtons = screen.getAllByText('Delete');
      await user.click(deleteButtons[0]);

      expect(confirmSpy).toHaveBeenCalled();
      expect(automationService.deleteRule).not.toHaveBeenCalled();

      confirmSpy.mockRestore();
    });
  });

  describe('Event Logs Tab', () => {
    it('displays event logs when logs tab is selected', async () => {
      const user = userEvent.setup();
      render(<AutomationPage />);

      await waitFor(() => {
        expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
      }, { timeout: 3000 });

      await user.click(screen.getByText(/Event Logs \(2\)/i));

      await waitFor(() => {
        expect(screen.getByText('Event Type')).toBeInTheDocument();
        expect(screen.getByText('task.status.changed')).toBeInTheDocument();
      });
    });

    it('loads logs on mount', async () => {
      render(<AutomationPage />);

      await waitFor(() => {
        expect(automationService.getRecentLogs).toHaveBeenCalledWith(50);
      });
    });
  });

  describe('Error Handling', () => {
    it('displays error when rules fail to load', async () => {
      vi.mocked(automationService.getAllRules).mockRejectedValue({
        response: { data: { message: 'Failed to load rules' } },
      });

      render(<AutomationPage />);

      await waitFor(() => {
        expect(screen.getByText('Failed to load rules')).toBeInTheDocument();
      });
    });

    it('closes error banner when close button is clicked', async () => {
      const user = userEvent.setup();
      vi.mocked(automationService.getAllRules).mockRejectedValue({
        response: { data: { message: 'Failed to load rules' } },
      });

      render(<AutomationPage />);

      await waitFor(() => {
        expect(screen.getByText('Failed to load rules')).toBeInTheDocument();
      });

      const closeButton = screen.getByText('×');
      await user.click(closeButton);

      expect(screen.queryByText('Failed to load rules')).not.toBeInTheDocument();
    });
  });
});
