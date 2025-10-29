import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthContext'
import { TenantProvider } from './contexts/TenantContext'
import SignUpPage from './pages/SignUpPage'
import LoginPage from './pages/LoginPage'
import Dashboard from './pages/Dashboard'
import ProjectsPage from './pages/ProjectsPage'
import TasksPage from './pages/TasksPage'
import SettingsPage from './pages/SettingsPage'
import AutomationPage from './pages/AutomationPage'
import OAuthCallbackPage from './pages/OAuthCallbackPage'
import './App.css'

function App() {
  return (
    <Router>
      <AuthProvider>
        <TenantProvider>
          <Routes>
            {/* Public routes */}
            <Route path="/" element={<HomePage />} />
            <Route path="/signup" element={<SignUpPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/oauth/callback" element={<OAuthCallbackPage />} />

            {/* Protected routes */}
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/projects" element={<ProjectsPage />} />
            <Route path="/tasks" element={<TasksPage />} />
            <Route path="/settings" element={<SettingsPage />} />
            <Route path="/automations" element={<AutomationPage />} />

            {/* Catch-all redirect */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </TenantProvider>
      </AuthProvider>
    </Router>
  )
}

/**
 * Home page component with links to sign up and login.
 */
const HomePage = () => (
  <div style={styles.container}>
    <div style={styles.hero}>
      <h1 style={styles.title}>Multi-Tenant SaaS Platform</h1>
      <p style={styles.subtitle}>
        Project Management & Team Collaboration Made Simple
      </p>
      <div style={styles.features}>
        <div style={styles.feature}>
          <h3>Multi-Tenant</h3>
          <p>Isolated data per organization with subdomain routing</p>
        </div>
        <div style={styles.feature}>
          <h3>OAuth2 Authentication</h3>
          <p>Sign in with Google, Facebook, or GitHub</p>
        </div>
        <div style={styles.feature}>
          <h3>Project Management</h3>
          <p>Organize projects and tasks with your team</p>
        </div>
      </div>
      <div style={styles.actions}>
        <a href="/signup" style={styles.primaryButton}>
          Create Organization
        </a>
        <a href="/login" style={styles.secondaryButton}>
          Sign In
        </a>
      </div>
    </div>
  </div>
)

const styles = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#f3f4f6',
    padding: '20px',
  },
  hero: {
    textAlign: 'center' as const,
    maxWidth: '800px',
  },
  title: {
    fontSize: '48px',
    fontWeight: 'bold',
    color: '#111827',
    marginBottom: '16px',
  },
  subtitle: {
    fontSize: '20px',
    color: '#6b7280',
    marginBottom: '48px',
  },
  features: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
    gap: '24px',
    marginBottom: '48px',
  },
  feature: {
    backgroundColor: 'white',
    padding: '24px',
    borderRadius: '8px',
    boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
  },
  actions: {
    display: 'flex',
    justifyContent: 'center',
    gap: '16px',
  },
  primaryButton: {
    padding: '12px 32px',
    backgroundColor: '#3b82f6',
    color: 'white',
    textDecoration: 'none',
    borderRadius: '6px',
    fontSize: '16px',
    fontWeight: '500',
  },
  secondaryButton: {
    padding: '12px 32px',
    backgroundColor: 'white',
    color: '#3b82f6',
    textDecoration: 'none',
    borderRadius: '6px',
    fontSize: '16px',
    fontWeight: '500',
    border: '2px solid #3b82f6',
  },
}

export default App
