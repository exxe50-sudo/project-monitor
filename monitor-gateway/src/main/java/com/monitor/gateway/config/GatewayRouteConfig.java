package com.monitor.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Value("${spring.cloud.gateway.routes[0].uri:http://monitor-server:8081}")
    private String serverUri;

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("monitor-server-api", r -> r
                        .path("/api/**")
                        .uri(serverUri))
                .route("monitor-server-ws", r -> r
                        .path("/ws/**")
                        .uri(serverUri))
                .build();
    }
}
