-- 内置 Linux 系统监控告警模板
INSERT INTO alert_templates (id, name, description, is_builtin, apply_scope, version, enabled) VALUES
('a1111111-1111-1111-1111-111111111111', 
 'Linux 系统监控模板', 
 '标准的 Linux 系统监控模板，包含 CPU、内存、磁盘、网络等核心指标',
 TRUE, 
 'OS_TYPE=Linux',
 1,
 TRUE)
ON CONFLICT (id) DO NOTHING;

-- Linux 模板的监控项
INSERT INTO alert_template_items (id, template_id, item_key, item_name, metric_type, collect_interval, enabled, sort_order) VALUES
-- CPU 相关
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'system.cpu.util[,idle]', 'CPU 空闲率', 'CPU', 5, TRUE, 1),
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'system.cpu.util[,system]', 'CPU 系统使用率', 'CPU', 5, TRUE, 2),
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'system.cpu.util[,user]', 'CPU 用户使用率', 'CPU', 5, TRUE, 3),
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'system.cpu.load[all,avg1]', 'CPU 1分钟负载', 'LOAD', 5, TRUE, 4),
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'system.cpu.load[all,avg5]', 'CPU 5分钟负载', 'LOAD', 5, TRUE, 5),
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'system.cpu.load[all,avg15]', 'CPU 15分钟负载', 'LOAD', 5, TRUE, 6),

-- 内存相关
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'system.mem.used.pct', '内存使用率', 'MEMORY', 5, TRUE, 10),
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'system.mem.total', '内存总量', 'MEMORY', 300, TRUE, 11),
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'system.mem.used', '内存使用量', 'MEMORY', 5, TRUE, 12),
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'system.mem.available', '内存可用量', 'MEMORY', 5, TRUE, 13),

-- 磁盘相关
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'system.disk.used.pct[/]', '根分区使用率', 'DISK', 10, TRUE, 20),
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'system.disk.total[/]', '根分区总容量', 'DISK', 300, TRUE, 21),
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'system.disk.used[/]', '根分区使用量', 'DISK', 10, TRUE, 22),
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'system.disk.available[/]', '根分区可用量', 'DISK', 10, TRUE, 23),

-- 网络相关
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'system.net.if.in[eth0]', '网络入站流量', 'NETWORK', 5, TRUE, 30),
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'system.net.if.out[eth0]', '网络出站流量', 'NETWORK', 5, TRUE, 31),

-- 进程相关
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'system.proc.num[]', '进程总数', 'PROCESS', 10, TRUE, 40)
ON CONFLICT (id) DO NOTHING;

-- Linux 模板的告警触发器
INSERT INTO alert_template_triggers (id, template_id, name, severity, expression, recover_expr, trigger_desc, enabled, sort_order) VALUES
-- CPU 告警
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 
 'CPU 使用率过高', 
 'WARNING',
 'system.cpu.util[,idle] < 20',
 'system.cpu.util[,idle] >= 30',
 'CPU 空闲率低于 20%，系统负载较高',
 TRUE, 1),

(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 
 'CPU 使用率严重过高', 
 'CRITICAL',
 'system.cpu.util[,idle] < 10',
 'system.cpu.util[,idle] >= 15',
 'CPU 空闲率低于 10%，系统负载严重',
 TRUE, 2),

(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 
 'CPU 负载过高',
 'WARNING',
 'system.cpu.load[all,avg15] > system.cpu.cores * 2',
 'system.cpu.load[all,avg15] <= system.cpu.cores * 1.5',
 'CPU 15分钟负载超过 CPU 核心数的 2 倍',
 TRUE, 3),

-- 内存告警
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 
 '内存使用率过高',
 'WARNING',
 'system.mem.used.pct > 85',
 'system.mem.used.pct <= 75',
 '内存使用率超过 85%',
 TRUE, 10),

(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 
 '内存使用率严重过高',
 'CRITICAL',
 'system.mem.used.pct > 95',
 'system.mem.used.pct <= 90',
 '内存使用率超过 95%，可能导致 OOM',
 TRUE, 11),

-- 磁盘告警
(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 
 '磁盘使用率过高',
 'WARNING',
 'system.disk.used.pct[/] > 85',
 'system.disk.used.pct[/] <= 75',
 '根分区使用率超过 85%',
 TRUE, 20),

(gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 
 '磁盘使用率严重过高',
 'CRITICAL',
 'system.disk.used.pct[/] > 95',
 'system.disk.used.pct[/] <= 90',
 '根分区使用率超过 95%，可能导致服务异常',
 TRUE, 21)
ON CONFLICT (id) DO NOTHING;
