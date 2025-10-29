import { apiClient } from './api';

/**
 * Task model matching backend entity.
 */
export interface Task {
  id?: string;
  tenantId?: string;
  projectId: string;
  name: string;
  description?: string;
  status: 'TODO' | 'IN_PROGRESS' | 'BLOCKED' | 'COMPLETED';
  dueDate?: string;
  progressPercentage?: number;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Task creation request.
 */
export interface CreateTaskRequest {
  projectId: string;
  name: string;
  description?: string;
  status?: 'TODO' | 'IN_PROGRESS' | 'BLOCKED' | 'COMPLETED';
  dueDate?: string;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
}

/**
 * Task update request.
 */
export interface UpdateTaskRequest {
  name?: string;
  description?: string;
  status?: 'TODO' | 'IN_PROGRESS' | 'BLOCKED' | 'COMPLETED';
  dueDate?: string;
  progressPercentage?: number;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
}

/**
 * Task service for API operations.
 */
export const taskService = {
  /**
   * Get all tasks for the current tenant.
   */
  async getAllTasks(params?: {
    projectId?: string;
    status?: string;
    priority?: string;
    overdueOnly?: boolean;
  }): Promise<Task[]> {
    const response = await apiClient.get<Task[]>('/tasks', { params });
    return response.data;
  },

  /**
   * Get a specific task by ID.
   */
  async getTask(id: string): Promise<Task> {
    const response = await apiClient.get<Task>(`/tasks/${id}`);
    return response.data;
  },

  /**
   * Create a new task.
   */
  async createTask(request: CreateTaskRequest): Promise<Task> {
    const response = await apiClient.post<Task>('/tasks', request);
    return response.data;
  },

  /**
   * Update an existing task.
   */
  async updateTask(id: string, request: UpdateTaskRequest): Promise<Task> {
    const response = await apiClient.put<Task>(`/tasks/${id}`, request);
    return response.data;
  },

  /**
   * Delete a task.
   */
  async deleteTask(id: string): Promise<void> {
    await apiClient.delete(`/tasks/${id}`);
  },

  /**
   * Get task count.
   */
  async countTasks(projectId?: string): Promise<number> {
    const response = await apiClient.get<number>('/tasks/count', {
      params: { projectId },
    });
    return response.data;
  },

  /**
   * Get average progress for a project's tasks.
   */
  async getAverageProgress(projectId: string): Promise<number> {
    const response = await apiClient.get<number>('/tasks/progress/average', {
      params: { projectId },
    });
    return response.data;
  },
};
