import { type ClassValue, clsx } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatTime(ts: number | string): string {
  const d = new Date(ts)
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

export function formatPercent(v: number): string {
  return v.toFixed(1) + '%'
}

export function statusColor(status: string): string {
  switch (status) {
    case 'ONLINE': case 'OK': case 'RUNNING': return 'success'
    case 'WARNING': case 'AVERAGE': case 'STOPPED': return 'warning'
    case 'ERROR': case 'OFFLINE': case 'UNREACHABLE': case 'HIGH': case 'DISASTER': return 'error'
    default: return 'offline'
  }
}

export function statusLabel(status: string): string {
  const map: Record<string, string> = {
    ONLINE: '在线', OFFLINE: '离线', UNREACHABLE: '不可达',
    RUNNING: '运行中', STOPPED: '已停止', ERROR: '错误',
    OK: '正常', WARNING: '警告', AVERAGE: '一般', HIGH: '严重', DISASTER: '灾难',
    INFO: '信息', PROBLEM: '问题', RESOLVED: '已恢复', ACKNOWLEDGED: '已确认',
  }
  return map[status] || status
}
