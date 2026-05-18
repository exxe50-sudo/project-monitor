-- 创建 LTREE 扩展
CREATE EXTENSION IF NOT EXISTS ltree;

-- 项目表
CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

-- 主机节点表
CREATE TABLE host_nodes (
    id UUID PRIMARY KEY,
    hostname VARCHAR(200) NOT NULL,
    ip_address VARCHAR(45),
    os_type VARCHAR(50),
    os_version VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE',
    agent_version VARCHAR(20),
    labels JSONB,
    last_heartbeat TIMESTAMPTZ,
    ssh_config JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 监控服务表
CREATE TABLE monitored_services (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    service_type VARCHAR(30) NOT NULL,
    host_node_id UUID REFERENCES host_nodes(id),
    port INTEGER,
    endpoint_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'STOPPED',
    version VARCHAR(50),
    labels JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 架构节点表（自引用树 + ltree 物化路径）
CREATE TABLE architecture_nodes (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    parent_id UUID REFERENCES architecture_nodes(id) ON DELETE CASCADE,
    node_type VARCHAR(20) NOT NULL,
    label VARCHAR(200) NOT NULL,
    icon VARCHAR(100),
    ref_type VARCHAR(20),
    ref_id UUID,
    tree_path LTREE,
    sort_order INTEGER DEFAULT 0,
    style_config JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_arch_nodes_project ON architecture_nodes(project_id);
CREATE INDEX idx_arch_nodes_parent ON architecture_nodes(parent_id);
CREATE INDEX idx_arch_nodes_tree_path ON architecture_nodes USING GIST(tree_path);

-- 告警模板表
CREATE TABLE alert_templates (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    is_builtin BOOLEAN NOT NULL DEFAULT FALSE,
    apply_scope VARCHAR(50),
    version INTEGER DEFAULT 1,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 告警模板监控项
CREATE TABLE alert_template_items (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL REFERENCES alert_templates(id) ON DELETE CASCADE,
    item_key VARCHAR(200) NOT NULL,
    item_name VARCHAR(200) NOT NULL,
    metric_type VARCHAR(30),
    collect_interval INTEGER DEFAULT 30,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_template_items_tpl ON alert_template_items(template_id);

-- 告警模板触发器
CREATE TABLE alert_template_triggers (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL REFERENCES alert_templates(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    expression TEXT NOT NULL,
    recover_expr TEXT,
    trigger_desc TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_template_triggers_tpl ON alert_template_triggers(template_id);

-- 告警规则表
CREATE TABLE alert_rules (
    id UUID PRIMARY KEY,
    project_id UUID REFERENCES projects(id),
    template_id UUID REFERENCES alert_templates(id),
    name VARCHAR(200) NOT NULL,
    ref_type VARCHAR(20),
    ref_id UUID,
    trigger_conf JSONB,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    notify_channels JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_alert_rules_project ON alert_rules(project_id);

-- 告警事件表
CREATE TABLE alert_events (
    id UUID PRIMARY KEY,
    rule_id UUID REFERENCES alert_rules(id),
    severity VARCHAR(20) NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    trigger_name VARCHAR(200),
    trigger_expr TEXT,
    current_value DOUBLE PRECISION,
    ref_type VARCHAR(20),
    ref_id UUID,
    message TEXT,
    ack_status VARCHAR(20),
    ack_user_id UUID,
    ack_time TIMESTAMPTZ,
    ack_comment TEXT,
    triggered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMPTZ,
    duration_ms BIGINT,
    tags JSONB
);

CREATE INDEX idx_alert_events_triggered ON alert_events(triggered_at DESC);
CREATE INDEX idx_alert_events_rule ON alert_events(rule_id);
CREATE INDEX idx_alert_events_type ON alert_events(event_type);

-- 通知渠道表
CREATE TABLE notify_channels (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    channel_type VARCHAR(20) NOT NULL,
    config JSONB,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
