package com.monitor.alerter.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitor.alerter.application.service.AlertEvaluationEngine;
import com.monitor.alerter.application.service.AlertEvaluationEngine.MetricPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricConsumer {
    private final AlertEvaluationEngine evaluationEngine;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "${app.alert.metrics-queue:metrics.queue}")
    public void onMetric(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            MetricPoint point = new MetricPoint(
                    node.get("nodeId").asText(),
                    node.get("itemKey").asText(),
                    node.get("value").asDouble()
            );
            evaluationEngine.evaluate(point);
        } catch (Exception e) {
            log.error("Failed to process metric message: {}", e.getMessage());
        }
    }
}
