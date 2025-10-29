import React from 'react';
import { Project } from '../../services/projectService';
import { CreateTaskRequest } from '../../services/taskService';
import TaskForm from './TaskForm';

interface TaskModalProps {
  isOpen: boolean;
  isEditMode: boolean;
  formData: CreateTaskRequest;
  projects: Project[];
  progressPercentage?: number;
  onSubmit: (e: React.FormEvent) => void;
  onClose: () => void;
  onChange: (field: keyof CreateTaskRequest, value: any) => void;
  onProgressChange?: (value: number) => void;
}

/**
 * Modal component for displaying task creation/edit form.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const TaskModal: React.FC<TaskModalProps> = ({
  isOpen,
  isEditMode,
  formData,
  projects,
  progressPercentage,
  onSubmit,
  onClose,
  onChange,
  onProgressChange,
}) => {
  if (!isOpen) return null;

  const overlayStyle: React.CSSProperties = {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.5)',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 1000,
  };

  const modalStyle: React.CSSProperties = {
    backgroundColor: 'white',
    padding: '24px',
    borderRadius: '8px',
    width: '90%',
    maxWidth: '500px',
    maxHeight: '90vh',
    overflow: 'auto',
  };

  const headerStyle: React.CSSProperties = {
    fontSize: '20px',
    fontWeight: 'bold',
    marginBottom: '20px',
    color: '#1f2937',
  };

  const handleOverlayClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    <div style={overlayStyle} onClick={handleOverlayClick}>
      <div style={modalStyle} onClick={(e) => e.stopPropagation()}>
        <h2 style={headerStyle}>{isEditMode ? 'Edit Task' : 'Create New Task'}</h2>
        <TaskForm
          formData={formData}
          projects={projects}
          isEditMode={isEditMode}
          progressPercentage={progressPercentage}
          onSubmit={onSubmit}
          onCancel={onClose}
          onChange={onChange}
          onProgressChange={onProgressChange}
        />
      </div>
    </div>
  );
};

export default TaskModal;
