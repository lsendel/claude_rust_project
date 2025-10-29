import { apiClient } from './api';

export type ExecutionStatus = 'SUCCESS' | 'FAILED' | 'SKIPPED' | 'NO_RULES_MATCHED';

export interface AutomationRule {
  id?: string;
  tenantId?: string;
  name: string;
  eventType: string;
  actionType: string;
  conditions?: Record<string, any>;
  actionConfig: Record<string, any>;
  isActive?: boolean;
  executionCount?: number;
  lastExecutedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface EventLog {
  id: string;
  tenantId: string;
  automationRuleId?: string;
  eventType: string;
  actionType?: string;
  status: ExecutionStatus;
  eventPayload?: Record<string, any>;
  actionResult?: Record<string, any>;
  errorMessage?: string;
  errorStackTrace?: string;
  executionDurationMs?: number;
  createdAt: string;
  resourceId?: string;
  resourceType?: string;
}

export interface AutomationStats {
  totalRules: number;
  successfulExecutions: number;
  failedExecutions: number;
  skippedExecutions: number;
  noRulesMatched: number;
  averageExecutionDurationMs: number;
}

export const automationService = {
  // Automation Rules

  /**
   * Create a new automation rule.
   */
  async createRule(rule: AutomationRule): Promise<AutomationRule> {
    const response = await apiClient.post<AutomationRule>('/automations', rule);
    return response.data;
  },

  /**
   * Get all automation rules.
   */
  async getAllRules(activeOnly?: boolean): Promise<AutomationRule[]> {
    const response = await apiClient.get<AutomationRule[]>('/automations', {
      params: { activeOnly },
    });
    return response.data;
  },

  /**
   * Get a specific automation rule by ID.
   */
  async getRule(id: string): Promise<AutomationRule> {
    const response = await apiClient.get<AutomationRule>(`/automations/${id}`);
    return response.data;
  },

  /**
   * Get automation rules by event type.
   */
  async getRulesByEventType(eventType: string): Promise<AutomationRule[]> {
    const response = await apiClient.get<AutomationRule[]>('/automations/by-event-type', {
      params: { eventType },
    });
    return response.data;
  },

  /**
   * Get top executed automation rules.
   */
  async getTopExecutedRules(limit: number = 10): Promise<AutomationRule[]> {
    const response = await apiClient.get<AutomationRule[]>('/automations/top-executed', {
      params: { limit },
    });
    return response.data;
  },

  /**
   * Update an automation rule.
   */
  async updateRule(id: string, rule: Partial<AutomationRule>): Promise<AutomationRule> {
    const response = await apiClient.put<AutomationRule>(`/automations/${id}`, rule);
    return response.data;
  },

  /**
   * Toggle automation rule active status.
   */
  async toggleRuleStatus(id: string, isActive: boolean): Promise<AutomationRule> {
    const response = await apiClient.patch<AutomationRule>(`/automations/${id}/toggle`, {
      isActive,
    });
    return response.data;
  },

  /**
   * Delete an automation rule.
   */
  async deleteRule(id: string): Promise<void> {
    await apiClient.delete(`/automations/${id}`);
  },

  /**
   * Get count of automation rules.
   */
  async getRuleCount(): Promise<number> {
    const response = await apiClient.get<{ count: number }>('/automations/count');
    return response.data.count;
  },

  // Event Logs

  /**
   * Get recent event logs.
   */
  async getRecentLogs(limit: number = 50): Promise<EventLog[]> {
    const response = await apiClient.get<EventLog[]>('/automations/logs', {
      params: { limit },
    });
    return response.data;
  },

  /**
   * Get event logs for a specific automation rule.
   */
  async getLogsForRule(ruleId: string): Promise<EventLog[]> {
    const response = await apiClient.get<EventLog[]>(`/automations/${ruleId}/logs`);
    return response.data;
  },

  /**
   * Get failed event logs.
   */
  async getFailedLogs(): Promise<EventLog[]> {
    const response = await apiClient.get<EventLog[]>('/automations/logs/failed');
    return response.data;
  },

  /**
   * Get event logs within a date range.
   */
  async getLogsByDateRange(startDate: string, endDate: string): Promise<EventLog[]> {
    const response = await apiClient.get<EventLog[]>('/automations/logs/date-range', {
      params: { startDate, endDate },
    });
    return response.data;
  },

  // Statistics

  /**
   * Get automation statistics.
   */
  async getStats(): Promise<AutomationStats> {
    const response = await apiClient.get<AutomationStats>('/automations/stats');
    return response.data;
  },
};
