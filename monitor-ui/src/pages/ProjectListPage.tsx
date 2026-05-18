import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '@/hooks/useApi'
import { Plus, FolderKanban, Trash2, ArrowRight } from 'lucide-react'
import { formatTime } from '@/lib/utils'

export default function ProjectListPage() {
  const [projects, setProjects] = useState<any[]>([])
  const [showCreate, setShowCreate] = useState(false)
  const [newName, setNewName] = useState('')
  const [newDesc, setNewDesc] = useState('')
  const navigate = useNavigate()

  const load = async () => {
    try { setProjects(await api.getProjects()) } catch {}
  }

  useEffect(() => { load() }, [])

  const handleCreate = async () => {
    if (!newName.trim()) return
    await api.createProject({ name: newName, description: newDesc })
    setNewName('')
    setNewDesc('')
    setShowCreate(false)
    load()
  }

  const handleDelete = async (id: string) => {
    await api.deleteProject(id)
    load()
  }

  return (
    <div className="p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold">项目列表</h1>
          <p className="text-sm text-muted-foreground mt-1">管理监控项目</p>
        </div>
        <button
          onClick={() => setShowCreate(true)}
          className="flex items-center gap-2 px-4 py-2 rounded-lg bg-gradient-to-r from-primary to-purple-600 text-white text-sm font-medium hover:shadow-[0_0_20px_hsl(var(--primary)/0.4)] transition-all"
        >
          <Plus className="w-4 h-4" /> 新建项目
        </button>
      </div>

      {/* Create Modal */}
      {showCreate && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50" onClick={() => setShowCreate(false)}>
          <div className="monitor-card p-6 w-full max-w-md mx-4" onClick={(e) => e.stopPropagation()}>
            <h2 className="text-lg font-bold mb-4">新建项目</h2>
            <input
              type="text" value={newName} onChange={(e) => setNewName(e.target.value)}
              placeholder="项目名称" className="w-full px-3 py-2 rounded-lg bg-secondary border border-border text-foreground mb-3 focus:outline-none focus:border-primary"
            />
            <textarea
              value={newDesc} onChange={(e) => setNewDesc(e.target.value)}
              placeholder="项目描述（可选）" className="w-full px-3 py-2 rounded-lg bg-secondary border border-border text-foreground mb-4 focus:outline-none focus:border-primary resize-none h-20"
            />
            <div className="flex gap-2 justify-end">
              <button onClick={() => setShowCreate(false)} className="px-4 py-2 rounded-lg border border-border text-sm hover:bg-accent transition-all">取消</button>
              <button onClick={handleCreate} className="px-4 py-2 rounded-lg bg-primary text-white text-sm font-medium hover:shadow-[0_0_12px_hsl(var(--primary)/0.4)] transition-all">创建</button>
            </div>
          </div>
        </div>
      )}

      {/* Project Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {projects.map((p) => (
          <div key={p.id} className="monitor-card p-5 group cursor-pointer animate-fade-in" onClick={() => navigate(`/projects/${p.id}`)}>
            <div className="flex items-start justify-between mb-3">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-primary to-purple-600 flex items-center justify-center">
                  <FolderKanban className="w-5 h-5 text-white" />
                </div>
                <div>
                  <h3 className="font-bold">{p.name}</h3>
                  <p className="text-xs text-muted-foreground">{p.description || '暂无描述'}</p>
                </div>
              </div>
              <button
                onClick={(e) => { e.stopPropagation(); handleDelete(p.id) }}
                className="opacity-0 group-hover:opacity-100 text-muted-foreground hover:text-error transition-all"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
            <div className="flex items-center justify-between text-xs text-muted-foreground">
              <span>创建于 {formatTime(p.createdAt)}</span>
              <span className="flex items-center gap-1 text-primary group-hover:gap-2 transition-all">
                进入监控 <ArrowRight className="w-3 h-3" />
              </span>
            </div>
          </div>
        ))}

        {projects.length === 0 && (
          <div className="col-span-full text-center py-16 text-muted-foreground">
            <FolderKanban className="w-16 h-16 mx-auto mb-4 opacity-30" />
            <p className="text-lg">暂无项目</p>
            <p className="text-sm mt-1">点击"新建项目"开始监控</p>
          </div>
        )}
      </div>
    </div>
  )
}
