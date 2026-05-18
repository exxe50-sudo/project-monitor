import { useEffect, useState, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { api } from '@/hooks/useApi'
import { useWebSocket } from '@/hooks/useWebSocket'
import { useDashboardStore } from '@/stores/dashboardStore'
import { statusColor, statusLabel, formatTime } from '@/lib/utils'
import {
  Monitor, Server, Database, Globe, AlertTriangle, Activity,
  CheckCircle2, XCircle, AlertCircle, MinusCircle,
  ChevronRight, ChevronDown, RefreshCw, Settings
} from 'lucide-react'
import { cn } from '@/lib/utils'

// Node icon mapping
function NodeIcon({ type }: { type: string }) {
  switch (type) {
    case 'NODE': return <Server className="w-4 h-4" />
    case 'SERVICE': return <Database className="w-4 h-4" />
    case 'GROUP': return <Globe className="w-4 h-4" />
    default: return <Monitor className="w-4 h-4" />
  }
}

// Mini gauge component
function MiniGauge({ label, value, color }: { label: string; value: number; color: string }) {
  const barColor = value > 90 ? 'bg-error' : value > 70 ? 'bg-warning' : 'bg-success'
  return (
    <div className="flex items-center gap-1.5">
      <span className="text-[10px] text-muted-foreground w-7">{label}</span>
      <div className="flex-1 h-1.5 bg-secondary rounded-full overflow-hidden">
        <div className={cn("h-full rounded-full transition-all", barColor)} style={{ width: `${Math.min(value, 100)}%` }} />
      </div>
      <span className="text-[10px] text-foreground w-8 text-right">{value.toFixed(0)}%</span>
    </div>
  )
}

// Status badge
function StatusBadge({ status }: { status: string }) {
  const color = statusColor(status)
  const dotClass = `status-dot status-dot-${color === 'offline' ? 'offline' : color}`
  return (
    <span className={cn("flex items-center gap-1.5 text-xs px-2 py-0.5 rounded-full", {
      'bg-success/10 text-success': color === 'success',
      'bg-warning/10 text-warning': color === 'warning',
      'bg-error/10 text-error': color === 'error',
      'bg-offline/10 text-offline': color === 'offline',
    })}>
      <span className={dotClass} />{statusLabel(status)}
    </span>
  )
}

// Architecture tree node rendering
function TreeNode({ node, level = 0 }: { node: any; level?: number }) {
  const [expanded, setExpanded] = useState(true)
  const status = node.status || 'UNKNOWN'
  const hasChildren = node.children && node.children.length > 0
  const isGroup = node.nodeType === 'GROUP'
  const color = statusColor(status)

  return (
    <div>
      <div
        className={cn(
          "group flex items-center gap-2 px-3 py-2 rounded-lg cursor-pointer transition-all",
          "hover:bg-accent/50",
          level === 0 && "font-semibold"
        )}
        style={{ paddingLeft: `${12 + level * 20}px` }}
        onClick={() => hasChildren && setExpanded(!expanded)}
      >
        {hasChildren ? (
          expanded ? <ChevronDown className="w-3.5 h-3.5 text-muted-foreground" /> : <ChevronRight className="w-3.5 h-3.5 text-muted-foreground" />
        ) : (
          <span className="w-3.5" />
        )}
        <NodeIcon type={node.nodeType} />
        <span className="flex-1 text-sm truncate">{node.label}</span>
        <StatusBadge status={status} />
        {!isGroup && status === 'ONLINE' && node.nodeType === 'NODE' && (
          <div className="hidden group-hover:flex items-center gap-3 ml-1">
            <MiniGauge label="CPU" value={Math.random() * 40 + 20} color="success" />
            <MiniGauge label="MEM" value={Math.random() * 30 + 40} color="success" />
            <MiniGauge label="DISK" value={Math.random() * 20 + 30} color="success" />
          </div>
        )}
      </div>
      {expanded && hasChildren && (
        <div>
          {node.children.map((child: any) => (
            <TreeNode key={child.id} node={child} level={level + 1} />
          ))}
        </div>
      )}
    </div>
  )
}

// Alert item component
function AlertItem({ alert }: { alert: any }) {
  const severityColor = statusColor(alert.severity)
  const iconMap: Record<string, any> = {
    error: AlertCircle, warning: AlertTriangle, success: CheckCircle2, offline: MinusCircle,
  }
  const Icon = iconMap[severityColor] || AlertCircle

  return (
    <div className={cn(
      "flex items-start gap-3 p-3 rounded-lg border-l-2 animate-slide-alert transition-all",
      severityColor === 'error' ? "border-l-error bg-error/5" :
      severityColor === 'warning' ? "border-l-warning bg-warning/5" : "border-l-success bg-success/5"
    )}>
      <Icon className={cn("w-4 h-4 mt-0.5 flex-shrink-0", {
        'text-error': severityColor === 'error',
        'text-warning': severityColor === 'warning',
        'text-success': severityColor === 'success',
      })} />
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2">
          <span className="text-xs font-medium">{alert.triggerName || alert.message}</span>
          <span className={cn("text-[10px] px-1.5 py-0.5 rounded", {
            'bg-error/20 text-error': alert.severity === 'HIGH' || alert.severity === 'DISASTER',
            'bg-warning/20 text-warning': alert.severity === 'WARNING' || alert.severity === 'AVERAGE',
            'bg-success/20 text-success': alert.eventType === 'RESOLVED',
          })}>
            {statusLabel(alert.severity)}
          </span>
        </div>
        <p className="text-xs text-muted-foreground mt-0.5">{alert.message}</p>
        <p className="text-[10px] text-muted-foreground mt-1">{formatTime(alert.triggeredAt)}</p>
      </div>
    </div>
  )
}

export default function ProjectDashboardPage() {
  const { projectId } = useParams<{ projectId: string }>()
  const navigate = useNavigate()
  const store = useDashboardStore()
  const [treeData, setTreeData] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [showAlertPanel, setShowAlertPanel] = useState(true)

  const loadData = useCallback(async () => {
    if (!projectId) return
    try {
      const [tree, alerts] = await Promise.all([
        api.getArchitectureTree(projectId),
        api.getActiveAlerts(),
      ])
      setTreeData(tree || [])
      store.setSnapshot({ onlineCount: countStatus(tree, ['ONLINE', 'OK', 'RUNNING']), totalCount: countNodes(tree), alertCount: alerts?.length || 0 })
      setLoading(false)
    } catch { setLoading(false) }
  }, [projectId])

  useEffect(() => { loadData() }, [projectId])

  // WebSocket
  useWebSocket(
    projectId ? [
      `/topic/project/${projectId}/dashboard`,
      `/topic/project/${projectId}/status`,
      `/topic/project/${projectId}/alerts`,
    ] : [],
    useCallback((topic: string, body: any) => {
      if (topic.includes('/status') && body.type === 'NODE_STATUS_CHANGE') {
        store.updateNodeStatus(body.payload?.hostNodeId, body.payload?.newStatus)
      } else if (topic.includes('/alerts') && body.payload) {
        store.addAlert(body.payload)
      } else if (topic.includes('/dashboard') && body.payload) {
        store.setSnapshot(body.payload)
      }
    }, [])
  )

  if (loading) return <div className="flex items-center justify-center h-full"><RefreshCw className="w-8 h-8 animate-spin text-primary" /></div>

  return (
    <div className="flex h-full">
      {/* Main Content */}
      <div className="flex-1 p-6 overflow-auto">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold">{projectId?.substring(0, 8)} 态势监控</h1>
            <p className="text-sm text-muted-foreground mt-1">项目架构状态实时监控</p>
          </div>
          <div className="flex items-center gap-2">
            <button onClick={loadData} className="p-2 rounded-lg hover:bg-accent transition-all">
              <RefreshCw className="w-4 h-4" />
            </button>
            <button onClick={() => navigate(`/projects/${projectId}/architecture`)}
              className="flex items-center gap-2 px-4 py-2 rounded-lg bg-secondary hover:bg-accent text-sm transition-all">
              <Settings className="w-4 h-4" /> 编辑架构
            </button>
            <button onClick={() => setShowAlertPanel(!showAlertPanel)}
              className={cn("flex items-center gap-2 px-4 py-2 rounded-lg text-sm transition-all", showAlertPanel ? "bg-primary/20 text-primary" : "bg-secondary hover:bg-accent")}>
              <AlertTriangle className="w-4 h-4" /> 报警窗口
            </button>
          </div>
        </div>

        {/* Stats Row */}
        <div className="grid grid-cols-4 gap-4 mb-6">
          <div className="monitor-card p-4 flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-success/20 flex items-center justify-center">
              <CheckCircle2 className="w-5 h-5 text-success" />
            </div>
            <div>
              <p className="text-2xl font-bold">{store.onlineCount}</p>
              <p className="text-xs text-muted-foreground">在线节点</p>
            </div>
          </div>
          <div className="monitor-card p-4 flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-warning/20 flex items-center justify-center">
              <Server className="w-5 h-5 text-warning" />
            </div>
            <div>
              <p className="text-2xl font-bold">{store.totalCount}</p>
              <p className="text-xs text-muted-foreground">节点总数</p>
            </div>
          </div>
          <div className="monitor-card p-4 flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-error/20 flex items-center justify-center">
              <AlertCircle className="w-5 h-5 text-error" />
            </div>
            <div>
              <p className="text-2xl font-bold">{store.alertCount}</p>
              <p className="text-xs text-muted-foreground">活跃告警</p>
            </div>
          </div>
          <div className="monitor-card p-4 flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-primary/20 flex items-center justify-center">
              <Activity className="w-5 h-5 text-primary" />
            </div>
            <div>
              <p className="text-2xl font-bold">
                {store.totalCount > 0 ? Math.round(store.onlineCount / Math.max(store.totalCount, 1) * 100) : 0}%
              </p>
              <p className="text-xs text-muted-foreground">健康度</p>
            </div>
          </div>
        </div>

        {/* Architecture Tree */}
        <div className="monitor-card p-4">
          <h2 className="text-sm font-semibold mb-3 flex items-center gap-2">
            <Globe className="w-4 h-4 text-primary" /> 项目架构
          </h2>
          <div className="space-y-0.5">
            {treeData.map((node) => (
              <TreeNode key={node.id} node={node} />
            ))}
            {treeData.length === 0 && (
              <div className="text-center py-8 text-muted-foreground">
                <p>暂未配置架构，请点击"编辑架构"添加节点</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Alert Panel */}
      {showAlertPanel && (
        <aside className="w-80 border-l border-border bg-card/50 overflow-auto flex flex-col">
          <div className="p-4 border-b border-border sticky top-0 bg-card/80 backdrop-blur">
            <h3 className="font-semibold flex items-center gap-2 text-sm">
              <AlertTriangle className="w-4 h-4 text-error" /> 实时报警信息
            </h3>
            <p className="text-xs text-muted-foreground mt-1">自动接收最新告警通知</p>
          </div>
          <div className="flex-1 p-3 space-y-2">
            {store.alerts.length === 0 && (
              <div className="text-center py-12 text-muted-foreground">
                <CheckCircle2 className="w-8 h-8 mx-auto mb-2 text-success" />
                <p className="text-sm">暂无告警信息</p>
                <p className="text-xs">系统运行正常</p>
              </div>
            )}
            {store.alerts.map((alert: any, i: number) => (
              <AlertItem key={alert.eventId || i} alert={alert} />
            ))}
          </div>
        </aside>
      )}
    </div>
  )
}

// Helper functions
function countNodes(tree: any[]): number {
  let count = 0
  for (const node of tree) {
    if (node.nodeType === 'NODE' || node.nodeType === 'SERVICE') count++
    if (node.children) count += countNodes(node.children)
  }
  return count
}

function countStatus(tree: any[], statuses: string[]): number {
  let count = 0
  for (const node of tree) {
    if (statuses.includes(node.status)) count++
    if (node.children) count += countStatus(node.children, statuses)
  }
  return count
}
