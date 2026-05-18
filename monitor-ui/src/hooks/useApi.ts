import { useAuthStore } from '@/stores/authStore'

const BASE = '/api/v1'

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const token = useAuthStore.getState().token
  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = `Bearer ${token}`

  const res = await fetch(BASE + path, { ...options, headers })
  if (!res.ok) {
    if (res.status === 401) {
      useAuthStore.getState().logout()
      window.location.href = '/login'
    }
    throw new Error(`API error: ${res.status}`)
  }
  const json = await res.json()
  return json.data
}

export const api = {
  getProjects: () => request<any[]>('/projects'),
  getProject: (id: string) => request<any>(`/projects/${id}`),
  createProject: (data: any) => request<any>('/projects', { method: 'POST', body: JSON.stringify(data) }),
  deleteProject: (id: string) => request<void>(`/projects/${id}`, { method: 'DELETE' }),

  getArchitectureTree: (projectId: string) => request<any[]>(`/projects/${projectId}/architecture/tree`),
  addArchNode: (projectId: string, data: any) =>
    request<any>(`/projects/${projectId}/architecture/nodes`, { method: 'POST', body: JSON.stringify(data) }),

  getNodes: () => request<any[]>('/nodes'),
  getNode: (id: string) => request<any>(`/nodes/${id}`),

  getTemplates: () => request<any[]>('/templates'),
  getTemplate: (id: string) => request<any>(`/templates/${id}`),
  createTemplate: (data: any) => request<any>('/templates', { method: 'POST', body: JSON.stringify(data) }),

  getRules: (projectId?: string) => request<any[]>(projectId ? `/rules?projectId=${projectId}` : '/rules'),
  createRule: (data: any) => request<any>('/rules', { method: 'POST', body: JSON.stringify(data) }),

  getActiveAlerts: () => request<any[]>('/alert-events/active'),
  getAlertsByRef: (refId: string) => request<any[]>(`/alert-events?refId=${refId}`),

  getServices: () => request<any[]>('/services'),

  login: (username: string, password: string) =>
    request<{ token: string; username: string }>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    }),
}
