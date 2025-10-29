import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import EventLogRow from '../EventLogRow';
import { EventLog } from '../../../services/automationService';

/**
 * Tests for EventLogRow component.
 * PMAT: Ensures component meets thresholds (Cyc≤8, Cog≤15)
 * Component Complexity: Cyc~4, Cog~6
 */
describe('EventLogRow', () => {
  const mockLog: EventLog = {
    id: '1',
    eventType: 'USER_CREATED',
    actionType: 'SEND_EMAIL',
    status: 'SUCCESS',
    executionDurationMs: 150,
    createdAt: '2025-10-27T12:00:00Z',
    tenantId: 'tenant1',
  };

  it('renders event type', () => {
    render(
      <table>
        <tbody>
          <EventLogRow log={mockLog} />
        </tbody>
      </table>
    );
    expect(screen.getByText('USER_CREATED')).toBeInTheDocument();
  });

  it('renders action type when present', () => {
    render(
      <table>
        <tbody>
          <EventLogRow log={mockLog} />
        </tbody>
      </table>
    );
    expect(screen.getByText(/Action: SEND_EMAIL/i)).toBeInTheDocument();
  });

  it('does not render action type section when not present', () => {
    const logWithoutAction = { ...mockLog, actionType: undefined };
    render(
      <table>
        <tbody>
          <EventLogRow log={logWithoutAction} />
        </tbody>
      </table>
    );
    expect(screen.queryByText(/Action:/i)).not.toBeInTheDocument();
  });

  it('renders status badge', () => {
    render(
      <table>
        <tbody>
          <EventLogRow log={mockLog} />
        </tbody>
      </table>
    );
    expect(screen.getByText('SUCCESS')).toBeInTheDocument();
  });

  it('renders execution duration in milliseconds', () => {
    render(
      <table>
        <tbody>
          <EventLogRow log={mockLog} />
        </tbody>
      </table>
    );
    expect(screen.getByText('150ms')).toBeInTheDocument();
  });

  it('renders "-" when execution duration is not present', () => {
    const logWithoutDuration = { ...mockLog, executionDurationMs: undefined };
    render(
      <table>
        <tbody>
          <EventLogRow log={logWithoutDuration} />
        </tbody>
      </table>
    );
    expect(screen.getByText('-')).toBeInTheDocument();
  });

  it('renders formatted creation date', () => {
    render(
      <table>
        <tbody>
          <EventLogRow log={mockLog} />
        </tbody>
      </table>
    );
    const formattedDate = new Date('2025-10-27T12:00:00Z').toLocaleString();
    expect(screen.getByText(formattedDate)).toBeInTheDocument();
  });

  it('renders different status types', () => {
    const failedLog = { ...mockLog, status: 'FAILED' as const };
    render(
      <table>
        <tbody>
          <EventLogRow log={failedLog} />
        </tbody>
      </table>
    );
    expect(screen.getByText('FAILED')).toBeInTheDocument();
  });

  it('renders complete log data structure', () => {
    render(
      <table>
        <tbody>
          <EventLogRow log={mockLog} />
        </tbody>
      </table>
    );

    // Verify all main elements are present
    expect(screen.getByText('USER_CREATED')).toBeInTheDocument();
    expect(screen.getByText(/Action: SEND_EMAIL/i)).toBeInTheDocument();
    expect(screen.getByText('SUCCESS')).toBeInTheDocument();
    expect(screen.getByText('150ms')).toBeInTheDocument();
  });
});
