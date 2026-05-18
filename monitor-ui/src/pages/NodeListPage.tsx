import { useEffect, useState } from 'react'
import { api } from '@/hooks/useApi'
import { Server, Circle } from 'lucide-react'
import { statusColor, statusLabel, formatTime } from '@/lib/utils'
import { cn } from '@/lib/utils'

export default function NodeListPage() {
  const [nodes, setNodes] = useState<any[]>([])

  useEffect(() => { api.getNodes().then(setNodes) }, [])

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold flex items-center gap-2"><Server className="w-6 h-6 text-primary" /> 节点管理</h1>
        <p className="text-sm text-muted-foreground mt-1">已注册的监控节点</p>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {nodes.map((n: any) => {
          const color = statusColor(n.status)
          return (
            <div key={n.id} className="monitor-card p-5">
              <div className="flex items-center gap-3 mb-3">
                <div className={cn("w-10 h-10 rounded-xl flex items-center justify-center", {
                  'bg-success/20': color === 'success',
                  'bg-error/20': color === 'error',
                  'bg-offline/20': color === 'offline',
                })}>
                  <Server className={cn("w-5 h-5", {
                    'text-success': color === 'success',
                    'text-error': color === 'error',
                    'text-offline': color === 'offline',
                  })} />
                </div>
                <div>
                  <h3 className="font-bold text-sm">{n.hostname}</h3>
                  <p className="text-xs text-muted-foreground">{n.ipAddress}</p>
                </div>
                <span className={cn("ml-auto text-xs px-2 py-0.5 rounded-full flex items-center gap-1", {
                  'bg-success/10 text-success': color === 'success',
                  'bg-error/10 text-error': color === 'error',
                  'bg-warning/10 text-warning': color === 'warning',
                  'bg-offline/10 text-offline': color === 'offline',
                })}>
                  <Circle className={cn("w-1.5 h-1.5 fill-current", {
                    'animate-pulse-glow': color === 'success',
                    'animate-pulse-error': color === 'error',
                  })} />
                  {statusLabel(n.status)}
                </span>
              </div>
              <div className="text-xs text-muted-foreground space-y-0.5">
                <p>OS: {n.osType} {n.osVersion}</p>
                {n.agentVersion && <p>Agent: v{n.agentVersion}</p>}
                {n.lastHeartbeat && <p>最后心跳: {formatTime(n.lastHeartbeat)}</p>}
              </div>
            </div>
          )
        })}
        {nodes.length === 0 && (
          <div className="col-span-full text-center py-12 text-muted-foreground">
            <Server className="w-12 h-12 mx-auto mb-4 opacity-30" />
            <p>暂无注册节点</p>
            <p className="text-sm mt-1">启动 monitor-agent 后节点将自动注册</p>
          </div>
        )}
      </div>
    </div>
  )
}
