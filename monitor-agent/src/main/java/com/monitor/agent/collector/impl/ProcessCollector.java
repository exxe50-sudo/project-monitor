package com.monitor.agent.collector.impl;

import com.monitor.agent.collector.Collector;
import com.monitor.agent.grpc.proto.Metric;
import oshi.SystemInfo;

import java.util.ArrayList;
import java.util.List;

public class ProcessCollector implements Collector {
    private final SystemInfo si = new SystemInfo();

    @Override
    public String name() { return "Process"; }

    @Override
    public List<Metric> collect() {
        List<Metric> metrics = new ArrayList<>();
        int count = si.getOperatingSystem().getProcessCount();
        metrics.add(Metric.newBuilder().setItemKey("system.proc.num[]").setValue(count).build());
        return metrics;
    }
}
