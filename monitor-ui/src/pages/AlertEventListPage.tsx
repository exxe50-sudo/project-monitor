import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { api } from '@/hooks/useApi'
import { AlertTriangle, CheckCircle2, ArrowLeft } from 'lucide-react'
import { statusColor, statusLabel, formatTime } from '@/lib/utils'
import { cn } from '@/lib/utils'

export default function AlertEventListPage() {
  const { projectId } = useParams<{ projectId: string }>()
  const [events, setEvents] = useState<any[]>([])
  const [filter, setFilter] = useState('all')

  useEffect(() => {
    api.getActiveAlerts().then(setEvents)
  }, [])

  const filtered = filter === 'all' ? events : events.filter((e: any) => e.eventType === filter || e.severity === filter)

  const FilterBtn = ({ label, val }: { label: string; val: string }) => (
    <button onClick={() => setFilter(val)}
      className={cn("px-3 py-1.5 rounded-lg text-xs font-medium transition-all",
        filter === val ? "bg-primary text-white" : "bg-secondary hover:bg-accent")}
    >{label}</button>
  )

  return (
    <div className="p-6">
      <div className="flex items-center gap-4 mb-4">
        <div>
          <h1 className="text-xl font-bold flex items-center gap-2"><AlertTriangle className="w-5 h-5 text-error" /> 报警记录</h1>
        </div>
      </div>
      <div className="flex gap-2 mb-4">
        <FilterBtn label="全部" val="all" />
        <FilterBtn label="问题" val="PROBLEM" />
        <FilterBtn label="已恢复" val="RESOLVED" />
        <FilterBtn label="严重" val="HIGH" />
        <FilterBtn label="灾难" val="DISASTER" />
      </div>
      <div className="space-y-2">
        {filtered.map((e: any) => {
          const color = statusColor(e.severity)
          return (
            <div key={e.id} className="monitor-card p-4 flex items-start gap-3">
              <div className={cn("w-2 h-2 rounded-full mt-1.5", {
                'bg-error': color === 'error', 'bg-warning': color === 'warning',
                'bg-success': color === 'success',
              })} />
              <div className="flex-1">
                <div className="flex items-center gap-2">
                  <span className="text-sm font-medium">{e.triggerName}</span>
                  <span className={cn("text-[10px] px-1.5 py-0.5 rounded", {
                    'bg-error/20 text-error': e.severity === 'HIGH' || e.severity === 'DISASTER',
                    'bg-warning/20 text-warning': e.severity === 'WARNING',
                    'bg-success/20 text-success': e.eventType === 'RESOLVED',
                  })}>{statusLabel(e.severity)}</span>
                  <span className="text-xs text-muted-foreground">{statusLabel(e.eventType)}</span>
                </div>
                <p className="text-xs text-muted-foreground mt-1">{e.message}</p>
                <div className="flex items-center gap-3 mt-1 text-[10px] text-muted-foreground">
                  <span>{formatTime(e.triggeredAt)}</span>
                  {e.durationMs && <span>持续 {Math.round(e.durationMs / 1000)}s</span>}
                </div>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
