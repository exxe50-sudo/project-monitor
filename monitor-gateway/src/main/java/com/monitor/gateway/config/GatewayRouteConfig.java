package com.monitor.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("monitor-server-api", r -> r
                        .path("/api/**")
                        .uri("http://localhost:8081"))
                .route("monitor-server-ws", r -> r
                        .path("/ws/**")
                        .uri("http://localhost:8081"))
                .build();
    }
}
