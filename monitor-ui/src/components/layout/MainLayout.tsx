import { useState } from 'react'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'
import { cn } from '@/lib/utils'
import {
  LayoutDashboard, FolderKanban, Bell, ShieldAlert,
  Monitor, Server, Settings, LogOut, Menu, X
} from 'lucide-react'

const navItems = [
  { path: '/projects', label: '项目列表', icon: FolderKanban },
  { path: '/templates', label: '告警模板', icon: ShieldAlert },
  { path: '/rules', label: '告警规则', icon: Bell },
  { path: '/nodes', label: '节点管理', icon: Server },
]

export default function MainLayout() {
  const navigate = useNavigate()
  const location = useLocation()
  const { username, logout } = useAuthStore()
  const [sidebarOpen, setSidebarOpen] = useState(true)

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="flex h-screen bg-background">
      {/* Sidebar */}
      <aside className={cn(
        "flex flex-col border-r border-border transition-all duration-300",
        "bg-card",
        sidebarOpen ? "w-56" : "w-16"
      )}>
        {/* Logo */}
        <div className="flex items-center gap-2 p-4 border-b border-border h-14">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary to-purple-600 flex items-center justify-center flex-shrink-0">
            <Monitor className="w-5 h-5 text-white" />
          </div>
          {sidebarOpen && <span className="font-bold text-sm">Service Monitor</span>}
          <button onClick={() => setSidebarOpen(!sidebarOpen)} className="ml-auto text-muted-foreground hover:text-foreground">
            {sidebarOpen ? <X className="w-4 h-4" /> : <Menu className="w-4 h-4" />}
          </button>
        </div>

        {/* Nav */}
        <nav className="flex-1 p-2 space-y-1">
          {navItems.map((item) => {
            const active = location.pathname.startsWith(item.path)
            return (
              <button
                key={item.path}
                onClick={() => navigate(item.path)}
                className={cn(
                  "flex items-center gap-3 w-full px-3 py-2.5 rounded-lg text-sm transition-all",
                  active
                    ? "bg-primary/20 text-primary font-medium shadow-[0_0_12px_hsl(var(--primary)/0.15)]"
                    : "text-muted-foreground hover:bg-accent hover:text-foreground"
                )}
              >
                <item.icon className="w-5 h-5 flex-shrink-0" />
                {sidebarOpen && <span>{item.label}</span>}
              </button>
            )
          })}
        </nav>

        {/* User */}
        <div className="p-3 border-t border-border">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-full bg-primary/30 flex items-center justify-center text-xs font-bold">
              {username?.charAt(0)?.toUpperCase() || 'A'}
            </div>
            {sidebarOpen && (
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium truncate">{username}</p>
                <button onClick={handleLogout} className="text-xs text-muted-foreground hover:text-error flex items-center gap-1">
                  <LogOut className="w-3 h-3" /> 退出
                </button>
              </div>
            )}
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  )
}
