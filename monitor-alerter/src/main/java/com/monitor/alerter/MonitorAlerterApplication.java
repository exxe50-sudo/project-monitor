package com.monitor.alerter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MonitorAlerterApplication {
    public static void main(String[] args) {
        SpringApplication.run(MonitorAlerterApplication.class, args);
    }
}
