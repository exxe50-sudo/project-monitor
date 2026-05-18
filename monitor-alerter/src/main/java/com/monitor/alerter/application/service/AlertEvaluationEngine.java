package com.monitor.alerter.application.service;

import com.monitor.alerter.application.service.ExpressionEvaluator.EvaluationContext;
import com.monitor.alerter.domain.rule.AlertRule;
import com.monitor.alerter.domain.rule.AlertRuleRepository;
import com.monitor.alerter.domain.event.AlertEventWriter;
import com.monitor.alerter.infrastructure.redis.SuppressionCache;
import com.monitor.common.enums.AlertEventType;
import com.monitor.common.enums.Severity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertEvaluationEngine {
    private final AlertRuleRepository ruleRepository;
    private final ExpressionEvaluator expressionEvaluator;
    private final AlertEventWriter eventWriter;
    private final SuppressionCache suppressionCache;
    private final NotificationDispatcher notificationDispatcher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void evaluate(MetricPoint point) {
        List<AlertRule> rules = ruleRepository.findEnabledRules(point.nodeId(), point.itemKey());
        if (rules.isEmpty()) return;

        EvaluationContext ctx = new EvaluationContext(point.nodeId(), point.itemKey(), point.value(), Instant.now());

        for (AlertRule rule : rules) {
            try {
                evaluateRule(rule, ctx);
            } catch (Exception e) {
                log.error("Error evaluating rule {}: {}", rule.getId(), e.getMessage());
            }
        }
    }

    private void evaluateRule(AlertRule rule, EvaluationContext ctx) {
        JsonNode triggerConf = rule.getTriggerConf();
        if (triggerConf == null || !triggerConf.isArray()) return;

        for (JsonNode triggerNode : triggerConf) {
            String name = triggerNode.get("name").asText();
            String severityStr = triggerNode.get("severity").asText();
            String expression = triggerNode.get("expression").asText();
            Severity severity = Severity.valueOf(severityStr);

            boolean triggered = expressionEvaluator.evaluate(expression, ctx);
            String suppressionKey = rule.getId() + ":" + rule.getRefType() + ":" + rule.getRefId() + ":" + name;

            if (triggered) {
                if (suppressionCache.isSuppressed(suppressionKey)) {
                    log.debug("Alert suppressed: {}", suppressionKey);
                    continue;
                }

                // 写入告警事件
                UUID eventId = eventWriter.writeProblem(rule, name, severity, expression, ctx);
                suppressionCache.suppress(suppressionKey);
                log.warn("ALERT TRIGGERED: rule={}, trigger={}, value={}", rule.getName(), name, ctx.value());

                // 发送通知
                notificationDispatcher.dispatch(rule, eventId, name, severity, ctx);
            } else {
                // 检查是否之前有告警需要恢复
                eventWriter.checkAndResolve(rule, name, ctx);
                suppressionCache.release(suppressionKey);
            }
        }
    }

    public record MetricPoint(String nodeId, String itemKey, double value) {}
}
