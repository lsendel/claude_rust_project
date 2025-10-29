import React from 'react';
import { EventLog } from '../../services/automationService';
import EventLogRow from './EventLogRow';

interface EventLogTableProps {
  logs: EventLog[];
  loading: boolean;
}

/**
 * Table component for displaying event logs.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const EventLogTable: React.FC<EventLogTableProps> = ({ logs, loading }) => {
  const tableStyle: React.CSSProperties = {
    width: '100%',
    borderCollapse: 'collapse',
  };

  const thStyle: React.CSSProperties = {
    textAlign: 'left',
    padding: '12px',
    borderBottom: '2px solid #e5e7eb',
    fontSize: '12px',
    fontWeight: '600',
    color: '#6b7280',
    textTransform: 'uppercase',
  };

  const emptyStateStyle: React.CSSProperties = {
    textAlign: 'center',
    padding: '48px',
    color: '#6b7280',
  };

  if (loading) {
    return (
      <div style={emptyStateStyle}>
        Loading events...
      </div>
    );
  }

  if (logs.length === 0) {
    return (
      <div style={emptyStateStyle}>
        <p style={{ fontSize: '16px', marginBottom: '8px' }}>No event logs yet</p>
        <p style={{ fontSize: '14px' }}>
          Event logs will appear here when automation rules are triggered
        </p>
      </div>
    );
  }

  return (
    <div style={{ backgroundColor: 'white', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
      <table style={tableStyle}>
        <thead>
          <tr>
            <th style={thStyle}>Event Type</th>
            <th style={thStyle}>Status</th>
            <th style={thStyle}>Duration</th>
            <th style={thStyle}>Time</th>
          </tr>
        </thead>
        <tbody>
          {logs.map((log) => (
            <EventLogRow key={log.id} log={log} />
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default EventLogTable;
