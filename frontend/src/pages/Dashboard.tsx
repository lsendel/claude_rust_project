import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useTenant } from '../contexts/TenantContext';
import { tenantService, TenantUsageResponse } from '../services/tenantService';
import { automationService, EventLog } from '../services/automationService';

/**
 * Dashboard page showing tenant information and quota usage.
 * Protected route - requires authentication.
 */
const Dashboard: React.FC = () => {
  const { user, logout } = useAuth();
  const { tenant } = useTenant();
  const [usage, setUsage] = useState<TenantUsageResponse | null>(null);
  const [loadingUsage, setLoadingUsage] = useState(true);
  const [recentLogs, setRecentLogs] = useState<EventLog[]>([]);
  const [loadingLogs, setLoadingLogs] = useState(false);

  useEffect(() => {
    if (tenant?.id) {
      loadUsage();
      loadAutomationLogs();
    }
  }, [tenant?.id]);

  const loadUsage = async () => {
    if (!tenant?.id) return;

    try {
      setLoadingUsage(true);
      const usageData = await tenantService.getTenantUsage(tenant.id);
      setUsage(usageData);
    } catch (error) {
      console.error('Failed to load usage data:', error);
    } finally {
      setLoadingUsage(false);
    }
  };

  const loadAutomationLogs = async () => {
    try {
      setLoadingLogs(true);
      const logs = await automationService.getRecentLogs(10);
      setRecentLogs(logs);
    } catch (error) {
      console.error('Failed to load automation logs:', error);
    } finally {
      setLoadingLogs(false);
    }
  };

  if (!tenant || !user) {
    return <div>Loading...</div>;
  }

  const usagePercentage = usage?.usagePercentage || 0;

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <div style={styles.headerContent}>
          <h1 style={styles.logo}>{tenant.name}</h1>
          <div style={styles.headerRight}>
            <span style={styles.userName}>{user.name}</span>
            <button onClick={logout} style={styles.logoutButton}>
              Logout
            </button>
          </div>
        </div>
      </header>

      <main style={styles.main}>
        <div style={styles.welcomeCard}>
          <h2 style={styles.welcomeTitle}>Welcome back, {user.name}!</h2>
          <p style={styles.welcomeSubtitle}>
            Here's an overview of your organization
          </p>
        </div>

        <div style={styles.grid}>
          {/* Tenant Info Card */}
          <div style={styles.card}>
            <h3 style={styles.cardTitle}>Organization Details</h3>
            <div style={styles.cardContent}>
              <div style={styles.infoRow}>
                <span style={styles.infoLabel}>Name:</span>
                <span style={styles.infoValue}>{tenant.name}</span>
              </div>
              <div style={styles.infoRow}>
                <span style={styles.infoLabel}>Subdomain:</span>
                <span style={styles.infoValue}>{tenant.subdomain}</span>
              </div>
              <div style={styles.infoRow}>
                <span style={styles.infoLabel}>Plan:</span>
                <span style={styles.badge}>{tenant.subscriptionTier}</span>
              </div>
              <div style={styles.infoRow}>
                <span style={styles.infoLabel}>Status:</span>
                <span style={{
                  ...styles.badge,
                  backgroundColor: tenant.isActive ? '#d1fae5' : '#fee2e2',
                  color: tenant.isActive ? '#065f46' : '#991b1b',
                }}>
                  {tenant.isActive ? 'Active' : 'Inactive'}
                </span>
              </div>
              {tenant.description && (
                <div style={styles.description}>
                  <p style={styles.infoLabel}>Description:</p>
                  <p style={styles.descriptionText}>{tenant.description}</p>
                </div>
              )}
            </div>
          </div>

          {/* Quota Usage Card */}
          <div style={styles.card}>
            <h3 style={styles.cardTitle}>Usage & Quota</h3>
            <div style={styles.cardContent}>
              {loadingUsage ? (
                <div style={{ textAlign: 'center', padding: '24px', color: '#6b7280' }}>
                  Loading usage...
                </div>
              ) : usage ? (
                <>
                  <div style={styles.usageStats}>
                    <div style={styles.usageNumber}>
                      {usage.totalUsage}
                      <span style={styles.usageTotal}>
                        {usage.quotaLimit ? ` / ${usage.quotaLimit}` : ' (Unlimited)'}
                      </span>
                    </div>
                    <div style={styles.usageLabel}>
                      Projects + Tasks
                    </div>
                  </div>

                  {/* Usage Breakdown */}
                  <div style={styles.usageBreakdown}>
                    <div style={styles.breakdownRow}>
                      <span style={styles.breakdownLabel}>Projects:</span>
                      <span style={styles.breakdownValue}>{usage.projectCount}</span>
                    </div>
                    <div style={styles.breakdownRow}>
                      <span style={styles.breakdownLabel}>Tasks:</span>
                      <span style={styles.breakdownValue}>{usage.taskCount}</span>
                    </div>
                  </div>

                  {usage.quotaLimit && (
                    <>
                      <div style={styles.progressBar}>
                        <div
                          style={{
                            ...styles.progressFill,
                            width: `${Math.min(usagePercentage, 100)}%`,
                            backgroundColor:
                              usagePercentage >= 100 ? '#ef4444' :
                              usagePercentage >= 80 ? '#f59e0b' :
                              '#10b981',
                          }}
                        />
                      </div>
                      <p style={styles.usageDescription}>
                        {usagePercentage.toFixed(1)}% of your quota used
                      </p>

                      {usage.quotaExceeded && (
                        <div style={styles.errorAlert}>
                          ⚠️ Quota exceeded! Upgrade your plan to create more projects and tasks.
                        </div>
                      )}

                      {usage.nearingQuota && !usage.quotaExceeded && (
                        <div style={styles.warningAlert}>
                          You're approaching your quota limit. Consider upgrading your plan.
                        </div>
                      )}

                      {(usage.nearingQuota || usage.quotaExceeded) && (
                        <button
                          style={styles.upgradeButton}
                          onClick={() => alert('Upgrade functionality coming soon!')}
                          onMouseOver={(e) => (e.currentTarget.style.backgroundColor = '#1d4ed8')}
                          onMouseOut={(e) => (e.currentTarget.style.backgroundColor = '#2563eb')}
                        >
                          Upgrade Plan
                        </button>
                      )}
                    </>
                  )}

                  {!usage.quotaLimit && (
                    <p style={styles.unlimitedText}>
                      ✓ Unlimited usage on Enterprise plan
                    </p>
                  )}
                </>
              ) : (
                <div style={{ textAlign: 'center', padding: '24px', color: '#ef4444' }}>
                  Failed to load usage data
                </div>
              )}
            </div>
          </div>

          {/* Quick Actions Card */}
          <div style={styles.card}>
            <h3 style={styles.cardTitle}>Quick Actions</h3>
            <div style={styles.cardContent}>
              <button style={styles.actionButton}>
                Create New Project
              </button>
              <button style={styles.actionButton}>
                Invite Team Members
              </button>
              <button style={styles.actionButton}>
                View All Projects
              </button>
              <button style={styles.actionButton}>
                Manage Settings
              </button>
            </div>
          </div>

          {/* Recent Automation Logs Card */}
          <div style={styles.card}>
            <h3 style={styles.cardTitle}>Recent Automation Events</h3>
            <div style={styles.cardContent}>
              {loadingLogs ? (
                <div style={{ textAlign: 'center', padding: '24px', color: '#6b7280' }}>
                  Loading events...
                </div>
              ) : recentLogs.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '24px', color: '#6b7280' }}>
                  <p style={{ fontSize: '14px', marginBottom: '8px' }}>No automation events yet</p>
                  <p style={{ fontSize: '13px' }}>
                    <a href="/automations" style={{ color: '#2563eb', textDecoration: 'none' }}>
                      Create automation rules
                    </a>
                  </p>
                </div>
              ) : (
                <>
                  {recentLogs.slice(0, 5).map((log) => (
                    <div key={log.id} style={styles.logEntry}>
                      <div style={styles.logHeader}>
                        <span style={styles.logEventType}>{log.eventType}</span>
                        <span
                          style={{
                            ...styles.logStatusBadge,
                            backgroundColor:
                              log.status === 'SUCCESS' ? '#d1fae5' :
                              log.status === 'FAILED' ? '#fee2e2' :
                              log.status === 'SKIPPED' ? '#fef3c7' :
                              '#f3f4f6',
                            color:
                              log.status === 'SUCCESS' ? '#065f46' :
                              log.status === 'FAILED' ? '#991b1b' :
                              log.status === 'SKIPPED' ? '#92400e' :
                              '#4b5563',
                          }}
                        >
                          {log.status}
                        </span>
                      </div>
                      {log.actionType && (
                        <div style={styles.logAction}>Action: {log.actionType}</div>
                      )}
                      <div style={styles.logTime}>
                        {new Date(log.createdAt).toLocaleString()}
                      </div>
                    </div>
                  ))}
                  <a
                    href="/automations"
                    style={{
                      ...styles.actionButton,
                      marginTop: '12px',
                      textDecoration: 'none',
                      display: 'inline-block',
                      textAlign: 'center',
                    }}
                  >
                    View All Events
                  </a>
                </>
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

/**
 * Inline styles for the component.
 */
const styles = {
  container: {
    minHeight: '100vh',
    backgroundColor: '#f3f4f6',
  },
  header: {
    backgroundColor: 'white',
    borderBottom: '1px solid #e5e7eb',
    padding: '16px 0',
  },
  headerContent: {
    maxWidth: '1200px',
    margin: '0 auto',
    padding: '0 20px',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  logo: {
    fontSize: '20px',
    fontWeight: 'bold',
    color: '#111827',
  },
  headerRight: {
    display: 'flex',
    alignItems: 'center',
    gap: '16px',
  },
  userName: {
    fontSize: '14px',
    color: '#6b7280',
  },
  logoutButton: {
    padding: '8px 16px',
    backgroundColor: '#ef4444',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    fontSize: '14px',
    fontWeight: '500',
    cursor: 'pointer',
  },
  main: {
    maxWidth: '1200px',
    margin: '0 auto',
    padding: '40px 20px',
  },
  welcomeCard: {
    backgroundColor: 'white',
    borderRadius: '8px',
    padding: '24px',
    marginBottom: '24px',
  },
  welcomeTitle: {
    fontSize: '24px',
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: '8px',
  },
  welcomeSubtitle: {
    fontSize: '14px',
    color: '#6b7280',
  },
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
    gap: '24px',
  },
  card: {
    backgroundColor: 'white',
    borderRadius: '8px',
    padding: '24px',
    boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
  },
  cardTitle: {
    fontSize: '18px',
    fontWeight: '600',
    color: '#111827',
    marginBottom: '16px',
  },
  cardContent: {
    display: 'flex',
    flexDirection: 'column' as const,
    gap: '12px',
  },
  infoRow: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  infoLabel: {
    fontSize: '14px',
    color: '#6b7280',
    fontWeight: '500',
  },
  infoValue: {
    fontSize: '14px',
    color: '#111827',
  },
  badge: {
    padding: '4px 12px',
    backgroundColor: '#dbeafe',
    color: '#1e40af',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: '500',
  },
  description: {
    marginTop: '8px',
  },
  descriptionText: {
    fontSize: '14px',
    color: '#374151',
    marginTop: '4px',
  },
  usageStats: {
    textAlign: 'center' as const,
    padding: '16px 0',
  },
  usageNumber: {
    fontSize: '36px',
    fontWeight: 'bold',
    color: '#111827',
  },
  usageTotal: {
    fontSize: '18px',
    color: '#6b7280',
  },
  usageLabel: {
    fontSize: '14px',
    color: '#6b7280',
    marginTop: '4px',
  },
  progressBar: {
    width: '100%',
    height: '8px',
    backgroundColor: '#e5e7eb',
    borderRadius: '4px',
    overflow: 'hidden',
  },
  progressFill: {
    height: '100%',
    transition: 'width 0.3s ease',
  },
  usageDescription: {
    fontSize: '12px',
    color: '#6b7280',
    textAlign: 'center' as const,
  },
  warningAlert: {
    padding: '12px',
    backgroundColor: '#fef3c7',
    color: '#92400e',
    borderRadius: '6px',
    fontSize: '14px',
  },
  errorAlert: {
    padding: '12px',
    backgroundColor: '#fee2e2',
    color: '#991b1b',
    borderRadius: '6px',
    fontSize: '14px',
    fontWeight: '500',
  },
  unlimitedText: {
    fontSize: '14px',
    color: '#10b981',
    textAlign: 'center' as const,
    fontWeight: '500',
  },
  usageBreakdown: {
    backgroundColor: '#f9fafb',
    padding: '12px',
    borderRadius: '6px',
    display: 'flex',
    flexDirection: 'column' as const,
    gap: '8px',
  },
  breakdownRow: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  breakdownLabel: {
    fontSize: '14px',
    color: '#6b7280',
    fontWeight: '500',
  },
  breakdownValue: {
    fontSize: '16px',
    color: '#111827',
    fontWeight: '600',
  },
  upgradeButton: {
    width: '100%',
    padding: '12px',
    backgroundColor: '#2563eb',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    fontSize: '14px',
    fontWeight: '500',
    cursor: 'pointer',
    marginTop: '8px',
  },
  actionButton: {
    width: '100%',
    padding: '12px',
    backgroundColor: '#3b82f6',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    fontSize: '14px',
    fontWeight: '500',
    cursor: 'pointer',
    textAlign: 'left' as const,
  },
  logEntry: {
    padding: '12px',
    backgroundColor: '#f9fafb',
    borderRadius: '6px',
    marginBottom: '8px',
  },
  logHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '4px',
  },
  logEventType: {
    fontSize: '14px',
    fontWeight: '500',
    color: '#111827',
  },
  logStatusBadge: {
    padding: '2px 8px',
    borderRadius: '8px',
    fontSize: '11px',
    fontWeight: '500',
  },
  logAction: {
    fontSize: '13px',
    color: '#6b7280',
    marginBottom: '4px',
  },
  logTime: {
    fontSize: '12px',
    color: '#9ca3af',
  },
};

export default Dashboard;
