package com.monitor.agent.collector.impl;

import com.monitor.agent.collector.Collector;
import com.monitor.agent.grpc.proto.Metric;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PortCollector implements Collector {
    private static final int[] DEFAULT_PORTS = {22, 80, 443, 3306, 5432, 6379, 8080, 9090};
    private static final int CONNECT_TIMEOUT = 2000;

    @Override
    public String name() { return "Port"; }

    @Override
    public List<Metric> collect() {
        List<Metric> metrics = new ArrayList<>();
        for (int port : DEFAULT_PORTS) {
            double reachable = checkPort(port) ? 1.0 : 0.0;
            metrics.add(Metric.newBuilder()
                    .setItemKey("system.net.tcp.port[" + port + "]")
                    .setValue(reachable).build());
        }
        return metrics;
    }

    private boolean checkPort(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), CONNECT_TIMEOUT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
