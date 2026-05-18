import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '@/hooks/useApi'
import { ShieldAlert, BookOpen, Layers } from 'lucide-react'

export default function AlertTemplateListPage() {
  const [templates, setTemplates] = useState<any[]>([])
  const navigate = useNavigate()

  useEffect(() => { api.getTemplates().then(setTemplates) }, [])

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold flex items-center gap-2">
          <ShieldAlert className="w-6 h-6 text-primary" /> 告警模板
        </h1>
        <p className="text-sm text-muted-foreground mt-1">Zabbix风格预置监控模板</p>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {templates.map((t) => (
          <div key={t.id} onClick={() => navigate(`/templates/${t.id}`)}
            className="monitor-card p-5 cursor-pointer group">
            <div className="flex items-start gap-3">
              <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${t.isBuiltin ? 'bg-primary/20' : 'bg-purple-600/20'}`}>
                {t.isBuiltin ? <BookOpen className="w-5 h-5 text-primary" /> : <Layers className="w-5 h-5 text-purple-400" />}
              </div>
              <div className="flex-1">
                <div className="flex items-center gap-2">
                  <h3 className="font-bold">{t.name}</h3>
                  {t.isBuiltin && <span className="text-[10px] px-1.5 py-0.5 rounded bg-primary/20 text-primary">内置</span>}
                </div>
                <p className="text-xs text-muted-foreground mt-1">{t.description}</p>
                <div className="flex items-center gap-3 mt-2 text-xs text-muted-foreground">
                  <span>适用范围: {t.applyScope}</span>
                  <span>v{t.version}</span>
                  <span>{t.items?.length || 0} 监控项</span>
                  <span>{t.triggers?.length || 0} 触发器</span>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
