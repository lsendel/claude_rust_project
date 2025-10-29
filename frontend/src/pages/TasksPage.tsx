import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useTenant } from '../contexts/TenantContext';
import { taskService, Task, CreateTaskRequest, UpdateTaskRequest } from '../services/taskService';
import { projectService, Project } from '../services/projectService';
import TaskFilters from '../components/tasks/TaskFilters';
import TaskTable from '../components/tasks/TaskTable';
import TaskModal from '../components/tasks/TaskModal';
import ErrorBanner from '../components/tasks/ErrorBanner';

type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'BLOCKED' | 'COMPLETED';
type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

/**
 * Tasks page component - Main container for task management.
 * PMAT Thresholds: Cyc≤15, Cog≤30, LOC≤400
 */
const TasksPage: React.FC = () => {
  const { user } = useAuth();
  const { tenant: currentTenant } = useTenant();

  // State
  const [tasks, setTasks] = useState<Task[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filters
  const [projectFilter, setProjectFilter] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<TaskStatus | ''>('');
  const [priorityFilter, setPriorityFilter] = useState<Priority | ''>('');
  const [showOverdueOnly, setShowOverdueOnly] = useState(false);

  // Modals
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);

  // Form state
  const [formData, setFormData] = useState<CreateTaskRequest>({
    projectId: '',
    name: '',
    description: '',
    status: 'TODO',
    dueDate: '',
    priority: 'MEDIUM',
  });

  // Load projects for dropdown
  useEffect(() => {
    const loadProjects = async () => {
      try {
        const data = await projectService.getAllProjects({ activeOnly: true });
        setProjects(data);
      } catch (err: any) {
        console.error('Failed to load projects:', err);
      }
    };
    loadProjects();
  }, []);

  // Load tasks when filters change
  useEffect(() => {
    loadTasks();
  }, [projectFilter, statusFilter, priorityFilter, showOverdueOnly]);

  const loadTasks = async () => {
    if (!user || !currentTenant) return;

    setLoading(true);
    setError(null);

    try {
      const params: any = {};
      if (projectFilter) params.projectId = projectFilter;
      if (statusFilter) params.status = statusFilter;
      if (priorityFilter) params.priority = priorityFilter;
      if (showOverdueOnly) params.overdueOnly = true;

      const data = await taskService.getAllTasks(params);
      setTasks(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load tasks');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    try {
      await taskService.createTask(formData);
      setShowCreateModal(false);
      resetForm();
      loadTasks();
    } catch (err: any) {
      if (err.response?.status === 402) {
        setError('Quota exceeded. Please upgrade your subscription to create more tasks.');
      } else {
        setError(err.response?.data?.message || 'Failed to create task');
      }
    }
  };

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingTask?.id) return;

    setError(null);

    try {
      const updateRequest: UpdateTaskRequest = {
        name: formData.name,
        description: formData.description || undefined,
        status: formData.status,
        dueDate: formData.dueDate || undefined,
        priority: formData.priority,
        progressPercentage: editingTask.progressPercentage,
      };

      await taskService.updateTask(editingTask.id, updateRequest);
      setShowEditModal(false);
      setEditingTask(null);
      resetForm();
      loadTasks();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update task');
    }
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('Are you sure you want to delete this task?')) return;

    setError(null);

    try {
      await taskService.deleteTask(id);
      loadTasks();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete task');
    }
  };

  const openEditModal = (task: Task) => {
    setEditingTask(task);
    setFormData({
      projectId: task.projectId,
      name: task.name,
      description: task.description || '',
      status: task.status,
      dueDate: task.dueDate || '',
      priority: task.priority,
    });
    setShowEditModal(true);
  };

  const openCreateModal = () => {
    resetForm();
    setShowCreateModal(true);
  };

  const closeCreateModal = () => {
    setShowCreateModal(false);
    resetForm();
  };

  const closeEditModal = () => {
    setShowEditModal(false);
    setEditingTask(null);
    resetForm();
  };

  const resetForm = () => {
    setFormData({
      projectId: '',
      name: '',
      description: '',
      status: 'TODO',
      dueDate: '',
      priority: 'MEDIUM',
    });
  };

  const handleFormChange = (field: keyof CreateTaskRequest, value: any) => {
    setFormData({ ...formData, [field]: value });
  };

  const handleProgressChange = (value: number) => {
    if (editingTask) {
      setEditingTask({ ...editingTask, progressPercentage: value });
    }
  };

  const containerStyle: React.CSSProperties = {
    padding: '24px',
    maxWidth: '1400px',
    margin: '0 auto',
  };

  const headerStyle: React.CSSProperties = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '24px',
  };

  const titleStyle: React.CSSProperties = {
    fontSize: '28px',
    fontWeight: 'bold',
    color: '#1f2937',
  };

  const buttonStyle: React.CSSProperties = {
    padding: '10px 20px',
    backgroundColor: '#2563eb',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: '500',
  };

  return (
    <div style={containerStyle}>
      <div style={headerStyle}>
        <h1 style={titleStyle}>Tasks</h1>
        <button
          style={buttonStyle}
          onClick={openCreateModal}
          onMouseOver={(e) => (e.currentTarget.style.backgroundColor = '#1d4ed8')}
          onMouseOut={(e) => (e.currentTarget.style.backgroundColor = '#2563eb')}
        >
          + Create Task
        </button>
      </div>

      {error && <ErrorBanner message={error} onClose={() => setError(null)} />}

      <TaskFilters
        projects={projects}
        projectFilter={projectFilter}
        statusFilter={statusFilter}
        priorityFilter={priorityFilter}
        showOverdueOnly={showOverdueOnly}
        onProjectFilterChange={setProjectFilter}
        onStatusFilterChange={setStatusFilter}
        onPriorityFilterChange={setPriorityFilter}
        onShowOverdueChange={setShowOverdueOnly}
      />

      <TaskTable
        tasks={tasks}
        projects={projects}
        loading={loading}
        onEdit={openEditModal}
        onDelete={handleDelete}
      />

      <TaskModal
        isOpen={showCreateModal}
        isEditMode={false}
        formData={formData}
        projects={projects}
        onSubmit={handleCreate}
        onClose={closeCreateModal}
        onChange={handleFormChange}
      />

      <TaskModal
        isOpen={showEditModal}
        isEditMode={true}
        formData={formData}
        projects={projects}
        progressPercentage={editingTask?.progressPercentage}
        onSubmit={handleUpdate}
        onClose={closeEditModal}
        onChange={handleFormChange}
        onProgressChange={handleProgressChange}
      />
    </div>
  );
};

export default TasksPage;
