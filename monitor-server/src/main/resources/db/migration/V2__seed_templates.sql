-- ============================================================
-- 预置告警模板数据 (Zabbix风格)
-- ============================================================

-- 1. Linux OS Basic 模板
INSERT INTO alert_templates (id, name, description, is_builtin, apply_scope, version, enabled)
VALUES ('a0000001-0000-0000-0000-000000000001', 'Linux OS Basic', 'Linux操作系统基础监控模板，包含CPU、内存、磁盘、网络、系统负载', TRUE, 'HOST/LINUX', 1, TRUE);

-- 监控项
INSERT INTO alert_template_items (id, template_id, item_key, item_name, metric_type, collect_interval, sort_order) VALUES
('b0000001-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000001', 'system.cpu.util[,idle]', 'CPU空闲率', 'CPU', 30, 1),
('b0000001-0000-0000-0000-000000000002', 'a0000001-0000-0000-0000-000000000001', 'system.cpu.util[,system]', 'CPU系统使用率', 'CPU', 30, 2),
('b0000001-0000-0000-0000-000000000003', 'a0000001-0000-0000-0000-000000000001', 'system.cpu.load[all,avg1]', 'CPU 1分钟负载', 'LOAD', 30, 3),
('b0000001-0000-0000-0000-000000000004', 'a0000001-0000-0000-0000-000000000001', 'system.cpu.load[all,avg5]', 'CPU 5分钟负载', 'LOAD', 30, 4),
('b0000001-0000-0000-0000-000000000005', 'a0000001-0000-0000-0000-000000000001', 'system.mem.used.pct', '内存使用率', 'MEMORY', 30, 5),
('b0000001-0000-0000-0000-000000000006', 'a0000001-0000-0000-0000-000000000001', 'system.mem.total', '总内存(MB)', 'MEMORY', 300, 6),
('b0000001-0000-0000-0000-000000000007', 'a0000001-0000-0000-0000-000000000001', 'system.swap.used.pct', 'Swap使用率', 'MEMORY', 60, 7),
('b0000001-0000-0000-0000-000000000008', 'a0000001-0000-0000-0000-000000000001', 'system.disk.used.pct[/]', '根分区使用率', 'DISK', 60, 8),
('b0000001-0000-0000-0000-000000000009', 'a0000001-0000-0000-0000-000000000001', 'system.disk.inode.pct[/]', '根分区inode使用率', 'DISK', 300, 9),
('b0000001-0000-0000-0000-000000000010', 'a0000001-0000-0000-0000-000000000001', 'system.net.if.in[eth0]', '网卡入流量(bps)', 'NETWORK', 30, 10),
('b0000001-0000-0000-0000-000000000011', 'a0000001-0000-0000-0000-000000000001', 'system.net.if.out[eth0]', '网卡出流量(bps)', 'NETWORK', 30, 11),
('b0000001-0000-0000-0000-000000000012', 'a0000001-0000-0000-0000-000000000001', 'system.net.tcp.established', 'TCP已建立连接数', 'NETWORK', 60, 12),
('b0000001-0000-0000-0000-000000000013', 'a0000001-0000-0000-0000-000000000001', 'system.proc.num[]', '进程总数', 'PROCESS', 60, 13);

-- 触发器
INSERT INTO alert_template_triggers (id, template_id, name, severity, expression, trigger_desc, sort_order) VALUES
('c0000001-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000001', 'CPU使用率过高(Warning)', 'WARNING', '{system.cpu.util[,idle].avg(5)} < 10', 'CPU空闲率在最近5分钟平均值低于10%，即CPU使用率超过90%', 1),
('c0000001-0000-0000-0000-000000000002', 'a0000001-0000-0000-0000-000000000001', 'CPU使用率过高(High)', 'HIGH', '{system.cpu.util[,idle].avg(5)} < 5', 'CPU空闲率在最近5分钟平均值低于5%，即CPU使用率超过95%', 2),
('c0000001-0000-0000-0000-000000000003', 'a0000001-0000-0000-0000-000000000001', '内存使用率过高(Warning)', 'WARNING', '{system.mem.used.pct.avg(5)} > 85', '内存使用率在最近5分钟平均值超过85%', 3),
('c0000001-0000-0000-0000-000000000004', 'a0000001-0000-0000-0000-000000000001', '磁盘使用率过高(Warning)', 'WARNING', '{system.disk.used.pct[/].last()} > 80', '根分区磁盘使用率超过80%', 4),
('c0000001-0000-0000-0000-000000000005', 'a0000001-0000-0000-0000-000000000001', '磁盘使用率过高(High)', 'HIGH', '{system.disk.used.pct[/].last()} > 95', '根分区磁盘使用率超过95%，即将耗尽', 5),
('c0000001-0000-0000-0000-000000000006', 'a0000001-0000-0000-0000-000000000001', '系统负载过高', 'AVERAGE', '{system.cpu.load[all,avg5].last()} > 5', '系统5分钟负载超过5', 6);

-- 2. Linux Port Check 模板
INSERT INTO alert_templates (id, name, description, is_builtin, apply_scope, version, enabled)
VALUES ('a0000001-0000-0000-0000-000000000002', 'Linux Port Check', 'TCP端口连通性检测模板', TRUE, 'HOST/LINUX', 1, TRUE);

INSERT INTO alert_template_items (id, template_id, item_key, item_name, metric_type, collect_interval, sort_order) VALUES
('b0000002-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000002', 'system.net.tcp.port[22]', 'SSH端口(22)可达性', 'PORT', 30, 1),
('b0000002-0000-0000-0000-000000000002', 'a0000001-0000-0000-0000-000000000002', 'system.net.tcp.port[80]', 'HTTP端口(80)可达性', 'PORT', 30, 2),
('b0000002-0000-0000-0000-000000000003', 'a0000001-0000-0000-0000-000000000002', 'system.net.tcp.port[443]', 'HTTPS端口(443)可达性', 'PORT', 30, 3);

INSERT INTO alert_template_triggers (id, template_id, name, severity, expression, trigger_desc, sort_order) VALUES
('c0000002-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000002', '端口不可达', 'HIGH', '{system.net.tcp.port[*].last()} = 0', 'TCP端口连接失败', 1);

-- 3. PostgreSQL Monitor 模板
INSERT INTO alert_templates (id, name, description, is_builtin, apply_scope, version, enabled)
VALUES ('a0000001-0000-0000-0000-000000000003', 'PostgreSQL Monitor', 'PostgreSQL数据库监控模板', TRUE, 'SERVICE_POSTGRESQL', 1, TRUE);

INSERT INTO alert_template_items (id, template_id, item_key, item_name, metric_type, collect_interval, sort_order) VALUES
('b0000003-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000003', 'pgsql.connections.active', '活跃连接数', 'CUSTOM', 30, 1),
('b0000003-0000-0000-0000-000000000002', 'a0000001-0000-0000-0000-000000000003', 'pgsql.connections.total', '总连接数', 'CUSTOM', 30, 2),
('b0000003-0000-0000-0000-000000000003', 'a0000001-0000-0000-0000-000000000003', 'pgsql.replication.lag', '复制延迟(秒)', 'CUSTOM', 60, 3);

INSERT INTO alert_template_triggers (id, template_id, name, severity, expression, trigger_desc, sort_order) VALUES
('c0000003-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000003', '数据库连接数过高', 'WARNING', '{pgsql.connections.active.avg(5)} > 80', '活跃连接数超过80', 1),
('c0000003-0000-0000-0000-000000000002', 'a0000001-0000-0000-0000-000000000003', '复制延迟过高', 'HIGH', '{pgsql.replication.lag.last()} > 10', '主从复制延迟超过10秒', 2);

-- 4. Nginx Monitor 模板
INSERT INTO alert_templates (id, name, description, is_builtin, apply_scope, version, enabled)
VALUES ('a0000001-0000-0000-0000-000000000004', 'Nginx Monitor', 'Nginx Web服务器监控模板', TRUE, 'SERVICE_NGINX', 1, TRUE);

INSERT INTO alert_template_items (id, template_id, item_key, item_name, metric_type, collect_interval, sort_order) VALUES
('b0000004-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000004', 'nginx.requests.total', '请求总数', 'CUSTOM', 30, 1),
('b0000004-0000-0000-0000-000000000002', 'a0000001-0000-0000-0000-000000000004', 'nginx.connections.active', '活跃连接数', 'CUSTOM', 30, 2),
('b0000004-0000-0000-0000-000000000003', 'a0000001-0000-0000-0000-000000000004', 'nginx.status.5xx', '5xx错误数', 'CUSTOM', 30, 3);

INSERT INTO alert_template_triggers (id, template_id, name, severity, expression, trigger_desc, sort_order) VALUES
('c0000004-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000004', 'Nginx 5xx错误过多', 'HIGH', '{nginx.status.5xx.avg(5)} > 5', '最近5分钟平均5xx错误超过5个', 1),
('c0000004-0000-0000-0000-000000000002', 'a0000001-0000-0000-0000-000000000004', 'Nginx连接数过高', 'WARNING', '{nginx.connections.active.last()} > 1000', '活跃连接数超过1000', 2);
