package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.entity.NotificationTemplate;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.repository.NotificationTemplateRepository;
import com.vyxentra.vehicle.template.TemplateEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final TemplateEngine templateEngine;

    @Override
    @Transactional
    public NotificationTemplate createTemplate(NotificationTemplate template) {

        log.info("Creating notification template: {}", template.getName());

        String templateName = template.getName();

        templateRepository.findByNameAndChannel(templateName, template.getChannel())
                .ifPresent(t -> {
                    throw new BusinessException(
                            ErrorCode.VALIDATION_ERROR,
                            "Template already exists with name: " + templateName
                    );
                });

        templateEngine.validateTemplate(template.getTemplateContent());

        template.setIsActive(true);
        template = templateRepository.save(template);

        log.info("Template created with ID: {}", template.getId());

        return template;
    }

    @Override
    @Transactional
    public NotificationTemplate updateTemplate(String templateId, NotificationTemplate template) {
        log.info("Updating template: {}", templateId);

        NotificationTemplate existing = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template", templateId));

        // Check name uniqueness if changed
        if (!existing.getName().equals(template.getName())) {
            templateRepository.findByNameAndChannel(template.getName(), template.getChannel())
                    .ifPresent(t -> {
                        throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                                "Template already exists with name: " + template.getName());
                    });
        }

        // Validate template syntax
        templateEngine.validateTemplate(template.getTemplateContent());

        existing.setName(template.getName());
        existing.setSubject(template.getSubject());
        existing.setTemplateContent(template.getTemplateContent());
        existing.setVariables(template.getVariables());
        existing.setIsActive(template.getIsActive());

        existing = templateRepository.save(existing);

        log.info("Template updated: {}", templateId);

        return existing;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationTemplate getTemplate(String templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template", templateId));
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationTemplate getTemplateByNameAndChannel(String name, String channel) {
        return templateRepository.findByNameAndChannelAndIsActiveTrue(name, channel)
                .orElseThrow(() -> new ResourceNotFoundException("Template", "name", name));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplate> getAllTemplates(String channel) {
        if (channel != null) {
            return templateRepository.findAll().stream()
                    .filter(t -> t.getChannel().equals(channel))
                    .toList();
        }
        return templateRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteTemplate(String templateId) {
        log.info("Deleting template: {}", templateId);

        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template", templateId));

        // Soft delete
        template.setIsActive(false);
        templateRepository.save(template);

        log.info("Template deactivated: {}", templateId);
    }

    @Override
    public String renderTemplate(String templateContent, Map<String, Object> data) {
        return templateEngine.processTemplate(templateContent, data);
    }

    @Override
    public Map<String, Object> validateTemplate(String templateContent) {
        return templateEngine.validateTemplate(templateContent);
    }
}
