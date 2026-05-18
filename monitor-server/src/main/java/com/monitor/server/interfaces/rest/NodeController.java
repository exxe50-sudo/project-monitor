package com.monitor.server.interfaces.rest;

import com.monitor.common.dto.ApiResponse;
import com.monitor.server.domain.node.HostNode;
import com.monitor.server.domain.node.HostNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/nodes")
@RequiredArgsConstructor
public class NodeController {
    private final HostNodeRepository hostNodeRepository;

    @GetMapping
    public ApiResponse<List<HostNode>> list() {
        return ApiResponse.success(hostNodeRepository.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<HostNode> get(@PathVariable UUID id) {
        return ApiResponse.success(hostNodeRepository.findById(id).orElse(null));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        hostNodeRepository.deleteById(id);
        return ApiResponse.success(null);
    }
}
