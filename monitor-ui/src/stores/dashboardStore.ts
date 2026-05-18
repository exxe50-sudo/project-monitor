import { create } from 'zustand'

interface DashboardState {
  onlineCount: number
  totalCount: number
  alertCount: number
  architectureTree: any[]
  alerts: any[]
  setSnapshot: (data: any) => void
  addAlert: (alert: any) => void
  updateNodeStatus: (nodeId: string, status: string) => void
}

export const useDashboardStore = create<DashboardState>((set, get) => ({
  onlineCount: 0,
  totalCount: 0,
  alertCount: 0,
  architectureTree: [],
  alerts: [],
  setSnapshot: (data) => set({
    onlineCount: data.onlineCount || 0,
    totalCount: data.totalCount || 0,
    alertCount: data.alertCount || 0,
  }),
  addAlert: (alert) => set((state) => ({
    alerts: [alert, ...state.alerts].slice(0, 50),
    alertCount: state.alertCount + 1,
  })),
  updateNodeStatus: (nodeId, status) => {
    // recursive status update in architecture tree
    const updateNode = (nodes: any[]): any[] =>
      nodes.map((n: any) => {
        if (n.id === nodeId || n.refId === nodeId) {
          return { ...n, status }
        }
        if (n.children?.length) {
          return { ...n, children: updateNode(n.children) }
        }
        return n
      })
    set((state) => ({ architectureTree: updateNode(state.architectureTree) }))
  },
}))
