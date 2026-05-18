import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { api } from '@/hooks/useApi'
import { ArrowLeft, ShieldAlert, Gauge, Zap } from 'lucide-react'
import { statusColor, statusLabel } from '@/lib/utils'
import { cn } from '@/lib/utils'

export default function AlertTemplateEditPage() {
  const { templateId } = useParams<{ templateId: string }>()
  const navigate = useNavigate()
  const [template, setTemplate] = useState<any>(null)

  useEffect(() => {
    if (templateId) api.getTemplate(templateId).then(setTemplate)
  }, [templateId])

  if (!template) return null

  return (
    <div className="p-6">
      <div className="flex items-center gap-4 mb-6">
        <button onClick={() => navigate('/templates')} className="p-2 rounded-lg hover:bg-accent">
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold">{template.name}</h1>
          <p className="text-sm text-muted-foreground">{template.description}</p>
        </div>
      </div>

      {/* Items */}
      <div className="mb-6">
        <h2 className="text-sm font-semibold mb-3 flex items-center gap-2"><Gauge className="w-4 h-4 text-primary" /> 监控项 ({template.items?.length || 0})</h2>
        <div className="space-y-2">
          {template.items?.map((item: any) => (
            <div key={item.id} className="monitor-card p-3 flex items-center gap-3">
              <div className="w-8 h-8 rounded-lg bg-primary/20 flex items-center justify-center">
                <Gauge className="w-4 h-4 text-primary" />
              </div>
              <div className="flex-1">
                <p className="text-sm font-medium">{item.itemName}</p>
                <code className="text-xs text-muted-foreground">{item.itemKey} · 间隔 {item.collectInterval}s</code>
              </div>
              <span className="text-xs px-2 py-0.5 rounded bg-secondary">{item.metricType}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Triggers */}
      <div>
        <h2 className="text-sm font-semibold mb-3 flex items-center gap-2"><Zap className="w-4 h-4 text-warning" /> 触发器 ({template.triggers?.length || 0})</h2>
        <div className="space-y-2">
          {template.triggers?.map((trigger: any) => {
            const color = statusColor(trigger.severity)
            return (
              <div key={trigger.id} className="monitor-card p-3">
                <div className="flex items-center gap-2 mb-1">
                  <span className={cn("text-[10px] px-1.5 py-0.5 rounded font-medium", {
                    'bg-error/20 text-error': color === 'error',
                    'bg-warning/20 text-warning': color === 'warning',
                    'bg-success/20 text-success': color === 'success',
                  })}>
                    {statusLabel(trigger.severity)}
                  </span>
                  <span className="text-sm font-medium">{trigger.name}</span>
                </div>
                <code className="text-xs text-muted-foreground block mt-1">{trigger.expression}</code>
                {trigger.triggerDesc && <p className="text-xs text-muted-foreground mt-1">{trigger.triggerDesc}</p>}
              </div>
            )
          })}
        </div>
      </div>
    </div>
  )
}
