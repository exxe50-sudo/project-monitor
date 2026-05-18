package com.monitor.server.interfaces.rest;

import com.monitor.common.dto.ApiResponse;
import com.monitor.server.domain.alert.event.AlertEvent;
import com.monitor.server.domain.alert.event.AlertEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alert-events")
@RequiredArgsConstructor
public class AlertEventController {
    private final AlertEventRepository eventRepository;

    @GetMapping("/active")
    public ApiResponse<List<AlertEvent>> getActiveAlerts() {
        return ApiResponse.success(eventRepository.findActiveAlerts());
    }

    @GetMapping
    public ApiResponse<List<AlertEvent>> listByRef(@RequestParam UUID refId) {
        return ApiResponse.success(eventRepository.findByRefIdOrderByTriggeredAtDesc(refId));
    }

    @GetMapping("/{id}")
    public ApiResponse<AlertEvent> get(@PathVariable UUID id) {
        return ApiResponse.success(eventRepository.findById(id).orElse(null));
    }
}
