import React from 'react';
import { AutomationRule } from '../../services/automationService';
import RuleForm from './RuleForm';

interface RuleModalProps {
  isOpen: boolean;
  isEditMode: boolean;
  formData: Partial<AutomationRule>;
  onSubmit: (e: React.FormEvent) => void;
  onClose: () => void;
  onChange: (field: keyof AutomationRule, value: any) => void;
}

/**
 * Modal component for displaying the rule creation/edit form.
 * PMAT Thresholds: Cyc≤8, Cog≤15, LOC≤200
 */
const RuleModal: React.FC<RuleModalProps> = ({
  isOpen,
  isEditMode,
  formData,
  onSubmit,
  onClose,
  onChange,
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
    maxWidth: '600px',
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
        <h2 style={headerStyle}>
          {isEditMode ? 'Edit Automation Rule' : 'Create Automation Rule'}
        </h2>
        <RuleForm
          formData={formData}
          isEditMode={isEditMode}
          onSubmit={onSubmit}
          onCancel={onClose}
          onChange={onChange}
        />
      </div>
    </div>
  );
};

export default RuleModal;
