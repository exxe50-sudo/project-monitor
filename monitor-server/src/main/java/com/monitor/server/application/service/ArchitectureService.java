package com.monitor.server.application.service;

import com.monitor.common.enums.ArchitectureNodeType;
import com.monitor.server.domain.architecture.ArchitectureNode;
import com.monitor.server.domain.architecture.ArchitectureNodeRepository;
import com.monitor.server.domain.node.HostNode;
import com.monitor.server.domain.node.HostNodeRepository;
import com.monitor.server.domain.service.MonitoredService;
import com.monitor.server.domain.service.MonitoredServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArchitectureService {
    private final ArchitectureNodeRepository archRepository;
    private final HostNodeRepository hostRepository;
    private final MonitoredServiceRepository serviceRepository;

    public List<ArchitectureNode> getProjectTree(UUID projectId) {
        List<ArchitectureNode> allNodes = archRepository.findByProjectIdOrderBySortOrder(projectId);
        List<ArchitectureNode> rootNodes = allNodes.stream()
                .filter(n -> n.getParentId() == null)
                .collect(Collectors.toList());

        Map<UUID, List<ArchitectureNode>> childrenMap = allNodes.stream()
                .filter(n -> n.getParentId() != null)
                .collect(Collectors.groupingBy(ArchitectureNode::getParentId));

        for (ArchitectureNode root : rootNodes) {
            buildTree(root, childrenMap);
        }
        return rootNodes;
    }

    private void buildTree(ArchitectureNode node, Map<UUID, List<ArchitectureNode>> childrenMap) {
        List<ArchitectureNode> children = childrenMap.getOrDefault(node.getId(), Collections.emptyList());
        children.sort(Comparator.comparing(ArchitectureNode::getSortOrder));
        node.setChildren(children);
        // Compute status
        if (node.getNodeType() == ArchitectureNodeType.NODE && node.getRefId() != null) {
            hostRepository.findById(node.getRefId()).ifPresent(h -> node.setStatus(h.getStatus().name()));
        } else if (node.getNodeType() == ArchitectureNodeType.SERVICE && node.getRefId() != null) {
            serviceRepository.findById(node.getRefId()).ifPresent(s -> node.setStatus(s.getStatus().name()));
        } else if (node.getNodeType() == ArchitectureNodeType.GROUP) {
            computeGroupStatus(node);
        }
        for (ArchitectureNode child : children) {
            buildTree(child, childrenMap);
        }
    }

    private void computeGroupStatus(ArchitectureNode node) {
        if (node.getChildren().isEmpty()) {
            node.setStatus("OK");
            return;
        }
        boolean hasError = false;
        boolean hasWarning = false;
        for (ArchitectureNode child : node.getChildren()) {
            String status = child.getStatus();
            if ("ERROR".equals(status) || "OFFLINE".equals(status) || "UNREACHABLE".equals(status)) {
                hasError = true;
            } else if ("WARNING".equals(status) || "STOPPED".equals(status)) {
                hasWarning = true;
            }
        }
        if (hasError) node.setStatus("ERROR");
        else if (hasWarning) node.setStatus("WARNING");
        else node.setStatus("OK");
    }

    @Transactional
    public ArchitectureNode addNode(ArchitectureNode node) {
        // 验证节点类型和引用
        validateNodeReference(node);
        node.setTreePath(computeTreePath(node));
        return archRepository.save(node);
    }
    
    /**
     * 验证节点引用是否存在
     */
    private void validateNodeReference(ArchitectureNode node) {
        if (node.getRefId() != null) {
            if (node.getNodeType() == ArchitectureNodeType.NODE) {
                if (!hostRepository.existsById(node.getRefId())) {
                    throw new RuntimeException("Host node not found: " + node.getRefId());
                }
            } else if (node.getNodeType() == ArchitectureNodeType.SERVICE) {
                if (!serviceRepository.existsById(node.getRefId())) {
                    throw new RuntimeException("Monitored service not found: " + node.getRefId());
                }
            }
        }
    }

    private String computeTreePath(ArchitectureNode node) {
        if (node.getParentId() == null) return node.getId().toString().substring(0, 8);
        ArchitectureNode parent = archRepository.findById(node.getParentId()).orElse(null);
        if (parent != null && parent.getTreePath() != null) {
            return parent.getTreePath() + "." + node.getId().toString().substring(0, 8);
        }
        return node.getId().toString().substring(0, 8);
    }

    @Transactional
    public ArchitectureNode updateNode(UUID id, ArchitectureNode updated) {
        ArchitectureNode node = archRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Node not found"));
        node.setLabel(updated.getLabel());
        node.setParentId(updated.getParentId());
        node.setSortOrder(updated.getSortOrder());
        node.setTreePath(computeTreePath(node));
        return archRepository.save(node);
    }

    @Transactional
    public void deleteNode(UUID id) {
        archRepository.deleteByParentId(id);
        archRepository.deleteById(id);
    }
}
