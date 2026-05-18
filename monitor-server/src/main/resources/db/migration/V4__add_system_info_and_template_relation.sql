-- 扩展 host_nodes 表，添加详细的系统信息字段
ALTER TABLE host_nodes ADD COLUMN IF NOT EXISTS os_arch VARCHAR(50);
ALTER TABLE host_nodes ADD COLUMN IF NOT EXISTS cpu_cores INTEGER;
ALTER TABLE host_nodes ADD COLUMN IF NOT EXISTS total_memory_bytes BIGINT;
ALTER TABLE host_nodes ADD COLUMN IF NOT EXISTS total_disk_bytes BIGINT;
ALTER TABLE host_nodes ADD COLUMN IF NOT EXISTS network_interfaces TEXT[];
ALTER TABLE host_nodes ADD COLUMN IF NOT EXISTS disk_info JSONB;

-- 创建节点-告警模板关联表
CREATE TABLE IF NOT EXISTS node_alert_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    node_id UUID NOT NULL REFERENCES host_nodes(id) ON DELETE CASCADE,
    template_id UUID NOT NULL REFERENCES alert_templates(id) ON DELETE CASCADE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    config JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(node_id, template_id)
);

CREATE INDEX idx_node_alert_templates_node ON node_alert_templates(node_id);
CREATE INDEX idx_node_alert_templates_template ON node_alert_templates(template_id);

-- 为内置模板添加注释
COMMENT ON TABLE node_alert_templates IS '节点与告警模板的关联配置表';
