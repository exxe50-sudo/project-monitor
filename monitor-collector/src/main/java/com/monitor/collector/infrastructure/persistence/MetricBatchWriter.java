package com.monitor.collector.infrastructure.persistence;

import com.monitor.collector.application.service.MetricIngestionService.MetricPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricBatchWriter {
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void writeBatch(List<MetricPoint> points) {
        String sql = "INSERT INTO metrics_raw (ts, node_id, service_id, item_key, value) " +
                "VALUES (?, ?::uuid, ?::uuid, ?, ?) " +
                "ON CONFLICT DO NOTHING";

        jdbcTemplate.batchUpdate(sql, points, 500,
                (PreparedStatement ps, MetricPoint point) -> {
                    ps.setObject(1, point.ts());
                    ps.setString(2, point.nodeId());
                    ps.setString(3, point.serviceId());
                    ps.setString(4, point.itemKey());
                    ps.setDouble(5, point.value());
                });
    }
}
