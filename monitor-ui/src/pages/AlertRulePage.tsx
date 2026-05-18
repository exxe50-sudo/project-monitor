import { useEffect, useState } from 'react'
import { api } from '@/hooks/useApi'
import { Bell, Plus } from 'lucide-react'
import { statusColor, statusLabel } from '@/lib/utils'
import { cn } from '@/lib/utils'

export default function AlertRulePage() {
  const [rules, setRules] = useState<any[]>([])
  const [templates, setTemplates] = useState<any[]>([])
  const [nodes, setNodes] = useState<any[]>([])
  const [showCreate, setShowCreate] = useState(false)
  const [form, setForm] = useState({ name: '', templateId: '', refType: 'HOST', refId: '' })

  useEffect(() => {
    api.getRules().then(setRules)
    api.getTemplates().then(setTemplates)
    api.getNodes().then(setNodes)
  }, [])

  const handleCreate = async () => {
    if (!form.name || !form.templateId) return
    await api.createRule(form)
    setShowCreate(false)
    setForm({ name: '', templateId: '', refType: 'HOST', refId: '' })
    api.getRules().then(setRules)
  }

  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold flex items-center gap-2"><Bell className="w-6 h-6 text-primary" /> 告警规则</h1>
          <p className="text-sm text-muted-foreground mt-1">将模板应用到具体节点或服务</p>
        </div>
        <button onClick={() => setShowCreate(true)}
          className="flex items-center gap-2 px-4 py-2 rounded-lg bg-gradient-to-r from-primary to-purple-600 text-white text-sm font-medium">
          <Plus className="w-4 h-4" /> 新建规则
        </button>
      </div>

      {/* Create modal */}
      {showCreate && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50" onClick={() => setShowCreate(false)}>
          <div className="monitor-card p-6 w-full max-w-md mx-4" onClick={(e) => e.stopPropagation()}>
            <h2 className="text-lg font-bold mb-4">新建告警规则</h2>
            <div className="space-y-3">
              <input value={form.name} onChange={(e) => setForm({...form, name: e.target.value})}
                placeholder="规则名称" className="w-full px-3 py-2 rounded-lg bg-secondary border border-border text-sm" />
              <select value={form.templateId} onChange={(e) => setForm({...form, templateId: e.target.value})}
                className="w-full px-3 py-2 rounded-lg bg-secondary border border-border text-sm">
                <option value="">选择模板</option>
                {templates.map((t: any) => <option key={t.id} value={t.id}>{t.name}</option>)}
              </select>
              <select value={form.refType} onChange={(e) => setForm({...form, refType: e.target.value})}
                className="w-full px-3 py-2 rounded-lg bg-secondary border border-border text-sm">
                <option value="HOST">节点</option>
                <option value="SERVICE">服务</option>
                <option value="PROJECT">项目</option>
              </select>
              <select value={form.refId} onChange={(e) => setForm({...form, refId: e.target.value})}
                className="w-full px-3 py-2 rounded-lg bg-secondary border border-border text-sm">
                <option value="">选择目标</option>
                {nodes.map((n: any) => <option key={n.id} value={n.id}>{n.hostname} ({n.ipAddress})</option>)}
              </select>
            </div>
            <div className="flex gap-2 mt-4 justify-end">
              <button onClick={() => setShowCreate(false)} className="px-4 py-2 rounded-lg border border-border text-sm">取消</button>
              <button onClick={handleCreate} className="px-4 py-2 rounded-lg bg-primary text-white text-sm font-medium">创建</button>
            </div>
          </div>
        </div>
      )}

      {/* Rules list */}
      <div className="space-y-2">
        {rules.map((r: any) => (
          <div key={r.id} className="monitor-card p-4 flex items-center gap-4">
            <div className={cn("w-2 h-2 rounded-full", r.enabled ? "bg-success" : "bg-offline")} />
            <div className="flex-1">
              <p className="font-medium text-sm">{r.name}</p>
              <p className="text-xs text-muted-foreground">
                目标: {r.refType}/{r.refId?.substring(0, 8) || 'ALL'} · {r.enabled ? '已启用' : '已禁用'}
              </p>
            </div>
          </div>
        ))}
        {rules.length === 0 && <p className="text-center py-12 text-muted-foreground">暂无告警规则</p>}
      </div>
    </div>
  )
}
