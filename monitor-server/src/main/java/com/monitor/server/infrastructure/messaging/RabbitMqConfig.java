package com.monitor.server.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String METRICS_EXCHANGE = "monitor.metrics";
    public static final String ALERTS_EXCHANGE = "monitor.alerts";
    public static final String EVENTS_EXCHANGE = "monitor.events";
    public static final String METRICS_QUEUE = "metrics.queue";
    public static final String ALERTS_QUEUE = "alerts.queue";
    public static final String EVENTS_QUEUE = "events.queue";

    @Bean
    public TopicExchange metricsExchange() {
        return new TopicExchange(METRICS_EXCHANGE);
    }

    @Bean
    public TopicExchange alertsExchange() {
        return new TopicExchange(ALERTS_EXCHANGE);
    }

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE);
    }

    @Bean
    public Queue metricsQueue() {
        return QueueBuilder.durable(METRICS_QUEUE).build();
    }

    @Bean
    public Queue alertsQueue() {
        return QueueBuilder.durable(ALERTS_QUEUE).build();
    }

    @Bean
    public Queue eventsQueue() {
        return QueueBuilder.durable(EVENTS_QUEUE).build();
    }

    @Bean
    public Binding metricsBinding(Queue metricsQueue, TopicExchange metricsExchange) {
        return BindingBuilder.bind(metricsQueue).to(metricsExchange).with("metric.#");
    }

    @Bean
    public Binding alertsBinding(Queue alertsQueue, TopicExchange alertsExchange) {
        return BindingBuilder.bind(alertsQueue).to(alertsExchange).with("alert.#");
    }

    @Bean
    public Binding eventsBinding(Queue eventsQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(eventsQueue).to(eventsExchange).with("event.#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
