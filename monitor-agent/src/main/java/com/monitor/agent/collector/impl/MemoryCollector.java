package com.monitor.agent.collector.impl;

import com.monitor.agent.collector.Collector;
import com.monitor.agent.grpc.proto.Metric;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;

import java.util.ArrayList;
import java.util.List;

@Component
public class MemoryCollector implements Collector {
    private final SystemInfo si = new SystemInfo();

    @Override
    public String name() { return "Memory"; }

    @Override
    public List<Metric> collect() {
        var mem = si.getHardware().getMemory();
        List<Metric> metrics = new ArrayList<>();

        long total = mem.getTotal() / (1024 * 1024);
        long available = mem.getAvailable() / (1024 * 1024);
        double usedPct = Math.round((1.0 - (double) available / total) * 10000.0) / 100.0;

        metrics.add(Metric.newBuilder().setItemKey("system.mem.total").setValue(total).build());
        metrics.add(Metric.newBuilder().setItemKey("system.mem.available").setValue(available).build());
        metrics.add(Metric.newBuilder().setItemKey("system.mem.used.pct").setValue(usedPct).build());

        // Swap
        var swap = mem.getVirtualMemory();
        double swapUsedPct = Math.round((1.0 - (double) swap.getSwapFree() / swap.getSwapTotal()) * 10000.0) / 100.0;
        metrics.add(Metric.newBuilder().setItemKey("system.swap.used.pct").setValue(swapUsedPct).build());

        return metrics;
    }
}
