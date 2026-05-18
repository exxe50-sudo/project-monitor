package com.monitor.alerter.domain.event;

import com.monitor.alerter.application.service.AlertEvaluationEngine.MetricPoint;
import com.monitor.alerter.application.service.ExpressionEvaluator.EvaluationContext;
import com.monitor.alerter.domain.rule.AlertRule;
import com.monitor.common.enums.AlertEventType;
import com.monitor.common.enums.Severity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AlertEventWriter {
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public UUID writeProblem(AlertRule rule, String triggerName, Severity severity, String expression, EvaluationContext ctx) {
        UUID eventId = UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO alert_events (id, rule_id, severity, event_type, trigger_name, trigger_expr, current_value, ref_type, ref_id, message, triggered_at, tags) " +
                "VALUES (?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?, ?::uuid, ?, ?, ?::jsonb)",
                eventId.toString(), rule.getId().toString(),
                severity.name(), AlertEventType.PROBLEM.name(),
                triggerName, expression, ctx.value(),
                rule.getRefType(), rule.getRefId() != null ? rule.getRefId().toString() : null,
                generateMessage(triggerName, ctx),
                Instant.now(),
                "{}"
        );
        return eventId;
    }

    @Transactional
    public void checkAndResolve(AlertRule rule, String triggerName, EvaluationContext ctx) {
        // 查询是否存在未恢复的告警
        // 简化实现：直接查询匹配的事件
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM alert_events WHERE rule_id = ?::uuid AND trigger_name = ? AND event_type = 'PROBLEM'",
                Integer.class, rule.getId().toString(), triggerName
        );

        if (count != null && count > 0) {
            jdbcTemplate.update(
                    "UPDATE alert_events SET event_type = ?, resolved_at = ?, duration_ms = EXTRACT(EPOCH FROM (? - triggered_at)) * 1000 " +
                    "WHERE rule_id = ?::uuid AND trigger_name = ? AND event_type = 'PROBLEM'",
                    AlertEventType.RESOLVED.name(), Instant.now(), Instant.now(),
                    rule.getId().toString(), triggerName
            );
        }
    }

    private String generateMessage(String triggerName, EvaluationContext ctx) {
        return String.format("触发器 [%s] 被触发, 当前值: %.2f, 监控项: %s, 节点: %s",
                triggerName, ctx.value(), ctx.itemKey(), ctx.nodeId());
    }
}
