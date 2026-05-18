package com.monitor.agent.collector.impl;

import com.monitor.agent.collector.Collector;
import com.monitor.agent.grpc.proto.Metric;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;

import java.util.ArrayList;
import java.util.List;

@Component
public class NetworkCollector implements Collector {
    private final SystemInfo si = new SystemInfo();
    private long prevBytesRecv;
    private long prevBytesSent;

    @Override
    public String name() { return "Network"; }

    @Override
    public List<Metric> collect() {
        var ifs = si.getHardware().getNetworkIFs();
        List<Metric> metrics = new ArrayList<>();
        long totalRecv = 0, totalSent = 0;
        for (var iface : ifs) {
            iface.updateAttributes();
            totalRecv += iface.getBytesRecv();
            totalSent += iface.getBytesSent();
        }

        if (prevBytesRecv > 0) {
            long recvRate = (totalRecv - prevBytesRecv) * 8; // bps
            long sentRate = (totalSent - prevBytesSent) * 8;
            metrics.add(Metric.newBuilder().setItemKey("system.net.if.in[eth0]").setValue(recvRate).build());
            metrics.add(Metric.newBuilder().setItemKey("system.net.if.out[eth0]").setValue(sentRate).build());
        }

        prevBytesRecv = totalRecv;
        prevBytesSent = totalSent;
        return metrics;
    }
}
