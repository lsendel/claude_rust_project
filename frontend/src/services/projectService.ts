import { apiClient } from './api';

/**
 * Project model matching backend entity.
 */
export interface Project {
  id?: string;
  tenantId?: string;
  name: string;
  description?: string;
  status: 'PLANNING' | 'ACTIVE' | 'ON_HOLD' | 'COMPLETED' | 'ARCHIVED';
  dueDate?: string;
  ownerId: string;
  progressPercentage?: number;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Project creation request.
 */
export interface CreateProjectRequest {
  name: string;
  description?: string;
  status?: 'PLANNING' | 'ACTIVE' | 'ON_HOLD' | 'COMPLETED' | 'ARCHIVED';
  dueDate?: string;
  ownerId: string;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
}

/**
 * Project update request.
 */
export interface UpdateProjectRequest {
  name?: string;
  description?: string;
  status?: 'PLANNING' | 'ACTIVE' | 'ON_HOLD' | 'COMPLETED' | 'ARCHIVED';
  dueDate?: string;
  ownerId?: string;
  progressPercentage?: number;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
}

/**
 * Project service for API operations.
 */
export const projectService = {
  /**
   * Get all projects for the current tenant.
   */
  async getAllProjects(params?: {
    status?: string;
    priority?: string;
    ownerId?: string;
    overdueOnly?: boolean;
    activeOnly?: boolean;
  }): Promise<Project[]> {
    const response = await apiClient.get<Project[]>('/projects', { params });
    return response.data;
  },

  /**
   * Get a specific project by ID.
   */
  async getProject(id: string): Promise<Project> {
    const response = await apiClient.get<Project>(`/projects/${id}`);
    return response.data;
  },

  /**
   * Create a new project.
   */
  async createProject(request: CreateProjectRequest): Promise<Project> {
    const response = await apiClient.post<Project>('/projects', request);
    return response.data;
  },

  /**
   * Update an existing project.
   */
  async updateProject(id: string, request: UpdateProjectRequest): Promise<Project> {
    const response = await apiClient.put<Project>(`/projects/${id}`, request);
    return response.data;
  },

  /**
   * Delete a project.
   */
  async deleteProject(id: string): Promise<void> {
    await apiClient.delete(`/projects/${id}`);
  },

  /**
   * Get project count.
   */
  async countProjects(activeOnly?: boolean): Promise<number> {
    const response = await apiClient.get<number>('/projects/count', {
      params: { activeOnly },
    });
    return response.data;
  },
};
