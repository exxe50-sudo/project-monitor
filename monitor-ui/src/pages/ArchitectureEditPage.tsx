import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { api } from '@/hooks/useApi'
import { ArrowLeft, Plus, Trash2, GripVertical } from 'lucide-react'

export default function ArchitectureEditPage() {
  const { projectId } = useParams<{ projectId: string }>()
  const navigate = useNavigate()
  const [tree, setTree] = useState<any[]>([])
  const [selected, setSelected] = useState<any>(null)
  const [showAdd, setShowAdd] = useState(false)
  const [form, setForm] = useState({ label: '', nodeType: 'GROUP', refId: '' })

  useEffect(() => {
    if (projectId) api.getArchitectureTree(projectId).then(setTree)
  }, [projectId])

  const handleAdd = async () => {
    if (!projectId || !form.label) return
    const data: any = { label: form.label, nodeType: form.nodeType }
    if (form.nodeType === 'NODE') {
      data.refType = 'HOST'
      data.refId = form.refId || null
    }
    if (selected) data.parentId = selected.id
    await api.addArchNode(projectId, data)
    setShowAdd(false)
    setForm({ label: '', nodeType: 'GROUP', refId: '' })
    const t = await api.getArchitectureTree(projectId)
    setTree(t)
  }

  const renderNode = (node: any, level: number) => (
    <div key={node.id}>
      <div
        onClick={() => setSelected(node)}
        className={`flex items-center gap-2 px-3 py-2 rounded-lg cursor-pointer transition-all hover:bg-accent ${
          selected?.id === node.id ? 'bg-primary/20 border border-primary/50' : ''
        }`}
        style={{ paddingLeft: `${12 + level * 24}px` }}
      >
        <GripVertical className="w-4 h-4 text-muted-foreground" />
        <span className="text-xs px-2 py-0.5 rounded bg-secondary">{node.nodeType}</span>
        <span className="text-sm flex-1">{node.label}</span>
      </div>
      {node.children?.map((c: any) => renderNode(c, level + 1))}
    </div>
  )

  return (
    <div className="p-6">
      <div className="flex items-center gap-4 mb-6">
        <button onClick={() => navigate(`/projects/${projectId}`)} className="p-2 rounded-lg hover:bg-accent">
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold">编辑项目架构</h1>
          <p className="text-sm text-muted-foreground">管理项目的节点和服务架构</p>
        </div>
      </div>

      <div className="flex gap-6">
        {/* Tree */}
        <div className="flex-1 monitor-card p-4">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-semibold text-sm">架构树</h3>
            <button onClick={() => { setSelected(null); setShowAdd(true) }}
              className="flex items-center gap-1 px-3 py-1.5 rounded-lg bg-primary text-white text-xs font-medium hover:shadow-[0_0_12px_hsl(var(--primary)/0.4)] transition-all">
              <Plus className="w-3.5 h-3.5" /> 添加节点
            </button>
          </div>
          {tree.map((n) => renderNode(n, 0))}
        </div>

        {/* Add Form */}
        {showAdd && (
          <div className="w-80 monitor-card p-4 h-fit">
            <h3 className="font-semibold text-sm mb-3">添加节点</h3>
            <div className="space-y-3">
              <div>
                <label className="text-xs text-muted-foreground mb-1 block">标签名称</label>
                <input value={form.label} onChange={(e) => setForm({...form, label: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg bg-secondary border border-border text-sm focus:border-primary focus:outline-none" />
              </div>
              <div>
                <label className="text-xs text-muted-foreground mb-1 block">节点类型</label>
                <select value={form.nodeType} onChange={(e) => setForm({...form, nodeType: e.target.value})}
                  className="w-full px-3 py-2 rounded-lg bg-secondary border border-border text-sm focus:border-primary focus:outline-none">
                  <option value="GROUP">分组 (GROUP)</option>
                  <option value="NODE">主机节点 (NODE)</option>
                  <option value="SERVICE">服务 (SERVICE)</option>
                </select>
              </div>
              <div className="flex gap-2">
                <button onClick={() => setShowAdd(false)} className="flex-1 px-3 py-2 rounded-lg border border-border text-sm hover:bg-accent">取消</button>
                <button onClick={handleAdd} className="flex-1 px-3 py-2 rounded-lg bg-primary text-white text-sm font-medium">添加</button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
