package com.monitor.agent.collector.impl;

import com.monitor.agent.collector.Collector;
import com.monitor.agent.grpc.proto.Metric;
import oshi.SystemInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DiskCollector implements Collector {
    private final SystemInfo si = new SystemInfo();

    @Override
    public String name() { return "Disk"; }

    @Override
    public List<Metric> collect() {
        List<Metric> metrics = new ArrayList<>();
        File[] roots = File.listRoots();
        for (File root : roots) {
            long total = root.getTotalSpace();
            long free = root.getFreeSpace();
            double usedPct = Math.round((1.0 - (double) free / total) * 10000.0) / 100.0;
            String mountPoint = root.getPath().replace("\\", "/");
            if (mountPoint.endsWith(":")) mountPoint = mountPoint.substring(0, mountPoint.length() - 1);
            if (mountPoint.isEmpty()) mountPoint = "/";
            metrics.add(Metric.newBuilder()
                    .setItemKey("system.disk.used.pct[" + mountPoint + "]")
                    .setValue(usedPct).build());
        }
        return metrics;
    }
}
