package com.monitor.server.interfaces.rest;

import com.monitor.common.dto.ApiResponse;
import com.monitor.server.domain.service.MonitoredService;
import com.monitor.server.domain.service.MonitoredServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class ServiceController {
    private final MonitoredServiceRepository serviceRepository;

    @GetMapping
    public ApiResponse<List<MonitoredService>> list() {
        return ApiResponse.success(serviceRepository.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<MonitoredService> get(@PathVariable UUID id) {
        return ApiResponse.success(serviceRepository.findById(id).orElse(null));
    }

    @PostMapping
    public ApiResponse<MonitoredService> create(@RequestBody MonitoredService service) {
        return ApiResponse.success(serviceRepository.save(service));
    }
}
