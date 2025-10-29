import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { tenantService, TenantRegistrationRequest } from '../services/tenantService';
import { getErrorMessage } from '../services/api';

/**
 * Tenant sign-up page.
 * Allows users to register a new tenant organization.
 */
const SignUpPage: React.FC = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isSubdomainValid, setIsSubdomainValid] = useState<boolean | null>(null);

  const [formData, setFormData] = useState<TenantRegistrationRequest>({
    subdomain: '',
    name: '',
    description: '',
    ownerEmail: '',
    ownerName: '',
    subscriptionTier: 'FREE',
  });

  /**
   * Validate subdomain in real-time.
   */
  const validateSubdomain = async (subdomain: string) => {
    if (subdomain.length < 3) {
      setIsSubdomainValid(null);
      return;
    }

    const isValid = await tenantService.validateSubdomain(subdomain);
    setIsSubdomainValid(isValid);
  };

  /**
   * Handle form field changes.
   */
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));

    // Validate subdomain on change
    if (name === 'subdomain') {
      validateSubdomain(value);
    }
  };

  /**
   * Handle form submission.
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    try {
      const tenant = await tenantService.registerTenant(formData);
      console.log('Tenant registered successfully:', tenant);

      // Redirect to login page or tenant subdomain
      const tenantUrl = `${window.location.protocol}//${tenant.subdomain}.${window.location.host}`;
      window.location.href = tenantUrl + '/login';
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>Create Your Organization</h1>
        <p style={styles.subtitle}>
          Sign up for a free account and start managing your projects
        </p>

        {error && (
          <div style={styles.errorAlert}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} style={styles.form}>
          {/* Subdomain */}
          <div style={styles.formGroup}>
            <label htmlFor="subdomain" style={styles.label}>
              Subdomain *
            </label>
            <div style={styles.inputGroup}>
              <input
                type="text"
                id="subdomain"
                name="subdomain"
                value={formData.subdomain}
                onChange={handleChange}
                required
                minLength={3}
                maxLength={63}
                pattern="^[a-z0-9-]{3,63}$"
                style={styles.input}
                placeholder="your-company"
              />
              <span style={styles.inputSuffix}>.platform.com</span>
            </div>
            {isSubdomainValid === false && (
              <p style={styles.errorText}>This subdomain is not available</p>
            )}
            {isSubdomainValid === true && (
              <p style={styles.successText}>This subdomain is available</p>
            )}
            <p style={styles.helpText}>
              Lowercase letters, numbers, and hyphens only
            </p>
          </div>

          {/* Organization Name */}
          <div style={styles.formGroup}>
            <label htmlFor="name" style={styles.label}>
              Organization Name *
            </label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
              maxLength={255}
              style={styles.input}
              placeholder="Acme Corporation"
            />
          </div>

          {/* Description */}
          <div style={styles.formGroup}>
            <label htmlFor="description" style={styles.label}>
              Description
            </label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              maxLength={1000}
              rows={3}
              style={{ ...styles.input, ...styles.textarea }}
              placeholder="Tell us about your organization..."
            />
          </div>

          {/* Owner Email */}
          <div style={styles.formGroup}>
            <label htmlFor="ownerEmail" style={styles.label}>
              Your Email *
            </label>
            <input
              type="email"
              id="ownerEmail"
              name="ownerEmail"
              value={formData.ownerEmail}
              onChange={handleChange}
              required
              style={styles.input}
              placeholder="you@example.com"
            />
          </div>

          {/* Owner Name */}
          <div style={styles.formGroup}>
            <label htmlFor="ownerName" style={styles.label}>
              Your Name *
            </label>
            <input
              type="text"
              id="ownerName"
              name="ownerName"
              value={formData.ownerName}
              onChange={handleChange}
              required
              maxLength={255}
              style={styles.input}
              placeholder="John Doe"
            />
          </div>

          {/* Subscription Tier */}
          <div style={styles.formGroup}>
            <label htmlFor="subscriptionTier" style={styles.label}>
              Plan
            </label>
            <select
              id="subscriptionTier"
              name="subscriptionTier"
              value={formData.subscriptionTier}
              onChange={handleChange}
              style={styles.select}
            >
              <option value="FREE">Free (50 projects/tasks)</option>
              <option value="PRO">Pro (1,000 projects/tasks)</option>
              <option value="ENTERPRISE">Enterprise (Unlimited)</option>
            </select>
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            disabled={isLoading || isSubdomainValid === false}
            style={{
              ...styles.button,
              ...(isLoading || isSubdomainValid === false ? styles.buttonDisabled : {}),
            }}
          >
            {isLoading ? 'Creating...' : 'Create Organization'}
          </button>
        </form>

        <p style={styles.footer}>
          Already have an account?{' '}
          <a href="/login" style={styles.link}>
            Sign in
          </a>
        </p>
      </div>
    </div>
  );
};

/**
 * Inline styles for the component.
 */
const styles = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#f3f4f6',
    padding: '20px',
  },
  card: {
    backgroundColor: 'white',
    borderRadius: '8px',
    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
    padding: '40px',
    maxWidth: '500px',
    width: '100%',
  },
  title: {
    fontSize: '24px',
    fontWeight: 'bold',
    marginBottom: '8px',
    color: '#111827',
  },
  subtitle: {
    fontSize: '14px',
    color: '#6b7280',
    marginBottom: '24px',
  },
  form: {
    display: 'flex',
    flexDirection: 'column' as const,
    gap: '16px',
  },
  formGroup: {
    display: 'flex',
    flexDirection: 'column' as const,
  },
  label: {
    fontSize: '14px',
    fontWeight: '500',
    marginBottom: '4px',
    color: '#374151',
  },
  input: {
    padding: '10px 12px',
    borderRadius: '6px',
    border: '1px solid #d1d5db',
    fontSize: '14px',
    outline: 'none',
  },
  textarea: {
    resize: 'vertical' as const,
    fontFamily: 'inherit',
  },
  select: {
    padding: '10px 12px',
    borderRadius: '6px',
    border: '1px solid #d1d5db',
    fontSize: '14px',
    outline: 'none',
    backgroundColor: 'white',
  },
  inputGroup: {
    display: 'flex',
    alignItems: 'center',
  },
  inputSuffix: {
    marginLeft: '8px',
    fontSize: '14px',
    color: '#6b7280',
  },
  helpText: {
    fontSize: '12px',
    color: '#6b7280',
    marginTop: '4px',
  },
  successText: {
    fontSize: '12px',
    color: '#10b981',
    marginTop: '4px',
  },
  errorText: {
    fontSize: '12px',
    color: '#ef4444',
    marginTop: '4px',
  },
  errorAlert: {
    padding: '12px',
    backgroundColor: '#fee2e2',
    color: '#dc2626',
    borderRadius: '6px',
    marginBottom: '16px',
    fontSize: '14px',
  },
  button: {
    padding: '12px',
    backgroundColor: '#3b82f6',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    fontSize: '16px',
    fontWeight: '500',
    cursor: 'pointer',
    marginTop: '8px',
  },
  buttonDisabled: {
    backgroundColor: '#9ca3af',
    cursor: 'not-allowed',
  },
  footer: {
    marginTop: '24px',
    textAlign: 'center' as const,
    fontSize: '14px',
    color: '#6b7280',
  },
  link: {
    color: '#3b82f6',
    textDecoration: 'none',
    fontWeight: '500',
  },
};

export default SignUpPage;
