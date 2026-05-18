-- 原始指标数据表（用于存储 agent 上报的指标）
CREATE TABLE IF NOT EXISTS metrics_raw (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ts TIMESTAMPTZ NOT NULL,
    node_id UUID NOT NULL,
    service_id UUID,
    item_key VARCHAR(200) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 创建索引以提高查询性能
CREATE INDEX idx_metrics_raw_ts ON metrics_raw(ts DESC);
CREATE INDEX idx_metrics_raw_node ON metrics_raw(node_id);
CREATE INDEX idx_metrics_raw_item ON metrics_raw(item_key);
CREATE INDEX idx_metrics_raw_node_ts ON metrics_raw(node_id, ts DESC);
