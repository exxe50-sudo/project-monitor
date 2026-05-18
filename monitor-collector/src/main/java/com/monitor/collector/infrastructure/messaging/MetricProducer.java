package com.monitor.collector.infrastructure.messaging;

import com.monitor.collector.application.service.MetricIngestionService.MetricPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricProducer {
    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "monitor.metrics";

    public void send(MetricPoint point) {
        String routingKey = "metric." + point.itemKey().replaceAll("[^a-zA-Z0-9.]", ".");
        rabbitTemplate.convertAndSend(EXCHANGE, routingKey, point);
    }
}
