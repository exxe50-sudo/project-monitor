package com.monitor.alerter.application.service;

import com.monitor.alerter.application.service.ExpressionEvaluator.EvaluationContext;
import com.monitor.alerter.domain.rule.AlertRule;
import com.monitor.common.enums.Severity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class NotificationDispatcher {

    public void dispatch(AlertRule rule, UUID eventId, String triggerName, Severity severity, EvaluationContext ctx) {
        String message = String.format(
                "[%s] %s - %s: 值=%.2f | 节点=%s | 规则=%s | eventId=%s",
                severity, rule.getName(), triggerName, ctx.value(), ctx.nodeId(), rule.getName(), eventId
        );
        log.info("NOTIFICATION: {}", message);

        // 后续可扩展：读取 notifyChannels 配置，调用 Email、DingTalk、WeChat 等渠道
    }
}
