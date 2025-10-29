import React from 'react';
import { EventLog } from '../../services/automationService';
import StatusBadge from './StatusBadge';

interface EventLogRowProps {
  log: EventLog;
}

/**
 * Table row component for displaying an event log entry.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const EventLogRow: React.FC<EventLogRowProps> = ({ log }) => {
  const tdStyle: React.CSSProperties = {
    padding: '16px 12px',
    borderBottom: '1px solid #f3f4f6',
  };

  return (
    <tr>
      <td style={tdStyle}>
        <span style={{ fontWeight: '500', color: '#1f2937' }}>{log.eventType}</span>
        {log.actionType && (
          <div style={{ fontSize: '13px', color: '#6b7280', marginTop: '4px' }}>
            Action: {log.actionType}
          </div>
        )}
      </td>
      <td style={tdStyle}>
        <StatusBadge status={log.status} />
      </td>
      <td style={tdStyle}>{log.executionDurationMs ? `${log.executionDurationMs}ms` : '-'}</td>
      <td style={tdStyle}>{new Date(log.createdAt).toLocaleString()}</td>
    </tr>
  );
};

export default EventLogRow;
