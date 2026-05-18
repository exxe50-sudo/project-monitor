import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import MainLayout from './components/layout/MainLayout'
import LoginPage from './pages/LoginPage'
import ProjectListPage from './pages/ProjectListPage'
import ProjectDashboardPage from './pages/ProjectDashboardPage'
import ArchitectureEditPage from './pages/ArchitectureEditPage'
import AlertTemplateListPage from './pages/AlertTemplateListPage'
import AlertTemplateEditPage from './pages/AlertTemplateEditPage'
import AlertRulePage from './pages/AlertRulePage'
import AlertEventListPage from './pages/AlertEventListPage'
import NodeListPage from './pages/NodeListPage'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<MainLayout />}>
          <Route index element={<Navigate to="/projects" replace />} />
          <Route path="projects" element={<ProjectListPage />} />
          <Route path="projects/:projectId" element={<ProjectDashboardPage />} />
          <Route path="projects/:projectId/architecture" element={<ArchitectureEditPage />} />
          <Route path="projects/:projectId/alerts" element={<AlertEventListPage />} />
          <Route path="templates" element={<AlertTemplateListPage />} />
          <Route path="templates/:templateId" element={<AlertTemplateEditPage />} />
          <Route path="rules" element={<AlertRulePage />} />
          <Route path="nodes" element={<NodeListPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
