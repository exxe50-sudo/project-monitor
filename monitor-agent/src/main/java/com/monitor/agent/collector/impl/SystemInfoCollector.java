package com.monitor.agent.collector.impl;

import com.monitor.agent.collector.Collector;
import com.monitor.agent.grpc.proto.Metric;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统信息采集器 - 收集详细的系统硬件和软件信息
 */
public class SystemInfoCollector implements Collector {
    private final SystemInfo systemInfo;
    
    public SystemInfoCollector() {
        this.systemInfo = new SystemInfo();
    }
    
    @Override
    public List<Metric> collect() {
        // 系统信息采集通常在注册时执行，不用于常规指标采集
        return new ArrayList<>();
    }
    
    @Override
    public String name() {
        return "system-info";
    }
    
    /**
     * 获取详细的系统信息（用于注册时上报）
     */
    public com.monitor.agent.grpc.proto.SystemInfo getDetailedSystemInfo() {
        OperatingSystem os = systemInfo.getOperatingSystem();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        
        // 构建系统信息
        com.monitor.agent.grpc.proto.SystemInfo.Builder builder = 
                com.monitor.agent.grpc.proto.SystemInfo.newBuilder();
        
        // 基本信息
        builder.setHostname(os.getNetworkParams().getHostName())
               .setOsType(os.getFamily())
               .setOsVersion(os.getVersionInfo().getVersion())
               .setOsArch(System.getProperty("os.arch"))
               .setCpuCores(processor.getLogicalProcessorCount())
               .setTotalMemoryBytes(memory.getTotal())
               .setAgentVersion("1.0.0");
        
        // 磁盘信息
        long totalDisk = 0;
        FileSystem fileSystem = os.getFileSystem();
        List<OSFileStore> fileStores = fileSystem.getFileStores();
        for (OSFileStore store : fileStores) {
            totalDisk += store.getTotalSpace();
            
            builder.addDisks(com.monitor.agent.grpc.proto.DiskInfo.newBuilder()
                    .setMountPoint(store.getMount())
                    .setFilesystem(store.getType())
                    .setTotalBytes(store.getTotalSpace())
                    .setUsedBytes(store.getTotalSpace() - store.getUsableSpace())
                    .setAvailableBytes(store.getUsableSpace())
                    .build());
        }
        builder.setTotalDiskBytes(totalDisk);
        
        // 网络接口
        List<NetworkIF> networkIFs = systemInfo.getHardware().getNetworkIFs();
        for (NetworkIF netIF : networkIFs) {
            if (netIF.getBytesRecv() > 0 || netIF.getBytesSent() > 0) {
                builder.addNetworkInterfaces(netIF.getName());
            }
        }
        
        return builder.build();
    }
}
