package com.monitor.server.application.service;

import com.monitor.server.domain.alert.template.AlertTemplate;
import com.monitor.server.domain.alert.template.AlertTemplateRepository;
import com.monitor.server.domain.alert.template.TemplateItem;
import com.monitor.server.domain.alert.template.TemplateTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertTemplateService {
    private final AlertTemplateRepository templateRepository;

    public List<AlertTemplate> listAll() {
        return templateRepository.findAll();
    }

    public AlertTemplate getWithDetails(UUID id) {
        return templateRepository.findByIdWithDetails(id);
    }

    @Transactional
    public AlertTemplate create(AlertTemplate template) {
        if (template.getItems() != null) {
            for (TemplateItem item : template.getItems()) {
                item.setTemplateId(template.getId());
            }
        }
        if (template.getTriggers() != null) {
            for (TemplateTrigger trigger : template.getTriggers()) {
                trigger.setTemplateId(template.getId());
            }
        }
        return templateRepository.save(template);
    }

    @Transactional
    public void delete(UUID id) {
        templateRepository.deleteById(id);
    }
}
