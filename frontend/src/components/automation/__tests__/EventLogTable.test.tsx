import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import EventLogTable from '../EventLogTable';
import { EventLog } from '../../../services/automationService';

/**
 * Tests for EventLogTable component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~5, Cog~8
 */
describe('EventLogTable', () => {
  const mockLogs: EventLog[] = [
    {
      id: '1',
      eventType: 'USER_CREATED',
      actionType: 'SEND_EMAIL',
      status: 'SUCCESS',
      executionDurationMs: 150,
      createdAt: '2025-10-27T12:00:00Z',
      tenantId: 'tenant1',
    },
    {
      id: '2',
      eventType: 'TASK_CREATED',
      actionType: 'CALL_WEBHOOK',
      status: 'FAILED',
      executionDurationMs: 250,
      createdAt: '2025-10-27T13:00:00Z',
      tenantId: 'tenant1',
    },
  ];

  it('renders loading state', () => {
    render(<EventLogTable logs={[]} loading={true} />);
    expect(screen.getByText('Loading events...')).toBeInTheDocument();
  });

  it('renders empty state when no logs', () => {
    render(<EventLogTable logs={[]} loading={false} />);
    expect(screen.getByText('No event logs yet')).toBeInTheDocument();
    expect(screen.getByText(/Event logs will appear here when automation rules are triggered/i)).toBeInTheDocument();
  });

  it('does not render table when loading', () => {
    render(<EventLogTable logs={mockLogs} loading={true} />);
    expect(screen.queryByText('Event Type')).not.toBeInTheDocument();
  });

  it('does not render table when no logs', () => {
    render(<EventLogTable logs={[]} loading={false} />);
    expect(screen.queryByText('Event Type')).not.toBeInTheDocument();
  });

  it('renders table headers', () => {
    render(<EventLogTable logs={mockLogs} loading={false} />);
    expect(screen.getByText('Event Type')).toBeInTheDocument();
    expect(screen.getByText('Status')).toBeInTheDocument();
    expect(screen.getByText('Duration')).toBeInTheDocument();
    expect(screen.getByText('Time')).toBeInTheDocument();
  });

  it('renders EventLogRow for each log', () => {
    render(<EventLogTable logs={mockLogs} loading={false} />);
    expect(screen.getByText('USER_CREATED')).toBeInTheDocument();
    expect(screen.getByText('TASK_CREATED')).toBeInTheDocument();
  });

  it('renders status badges for all logs', () => {
    render(<EventLogTable logs={mockLogs} loading={false} />);
    expect(screen.getByText('SUCCESS')).toBeInTheDocument();
    expect(screen.getByText('FAILED')).toBeInTheDocument();
  });

  it('renders execution durations', () => {
    render(<EventLogTable logs={mockLogs} loading={false} />);
    expect(screen.getByText('150ms')).toBeInTheDocument();
    expect(screen.getByText('250ms')).toBeInTheDocument();
  });

  it('renders formatted timestamps', () => {
    render(<EventLogTable logs={mockLogs} loading={false} />);
    const date1 = new Date('2025-10-27T12:00:00Z').toLocaleString();
    const date2 = new Date('2025-10-27T13:00:00Z').toLocaleString();
    expect(screen.getByText(date1)).toBeInTheDocument();
    expect(screen.getByText(date2)).toBeInTheDocument();
  });
});
