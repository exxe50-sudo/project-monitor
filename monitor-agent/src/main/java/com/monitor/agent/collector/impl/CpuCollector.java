package com.monitor.agent.collector.impl;

import com.monitor.agent.collector.Collector;
import com.monitor.agent.grpc.proto.Metric;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.util.ArrayList;
import java.util.List;

@Component
public class CpuCollector implements Collector {
    private final SystemInfo si = new SystemInfo();
    private long[] prevTicks;

    @Override
    public String name() { return "CPU"; }

    @Override
    public List<Metric> collect() {
        CentralProcessor cpu = si.getHardware().getProcessor();
        long[] ticks = cpu.getSystemCpuLoadTicks();
        List<Metric> metrics = new ArrayList<>();

        if (prevTicks != null) {
            double idle = calculatePct(ticks, prevTicks, CentralProcessor.TickType.IDLE);
            double system = calculatePct(ticks, prevTicks, CentralProcessor.TickType.SYSTEM);
            double user = calculatePct(ticks, prevTicks, CentralProcessor.TickType.USER);

            metrics.add(Metric.newBuilder().setItemKey("system.cpu.util[,idle]").setValue(idle).build());
            metrics.add(Metric.newBuilder().setItemKey("system.cpu.util[,system]").setValue(system).build());
            metrics.add(Metric.newBuilder().setItemKey("system.cpu.util[,user]").setValue(user).build());
        }

        // 负载
        double[] loads = cpu.getSystemLoadAverage(3);
        if (loads[0] >= 0) {
            metrics.add(Metric.newBuilder().setItemKey("system.cpu.load[all,avg1]").setValue(loads[0]).build());
        }
        if (loads.length > 1 && loads[1] >= 0) {
            metrics.add(Metric.newBuilder().setItemKey("system.cpu.load[all,avg5]").setValue(loads[1]).build());
        }

        prevTicks = ticks;
        return metrics;
    }

    private double calculatePct(long[] current, long[] prev, CentralProcessor.TickType type) {
        long totalCur = 0, totalPrev = 0;
        for (int i = 0; i < current.length && i < prev.length; i++) {
            totalCur += current[i];
            totalPrev += prev[i];
        }
        long diff = totalCur - totalPrev;
        long typeDiff = current[type.getIndex()] - prev[type.getIndex()];
        if (diff == 0) return 0;
        return Math.round((double) typeDiff / diff * 10000.0) / 100.0;
    }
}
