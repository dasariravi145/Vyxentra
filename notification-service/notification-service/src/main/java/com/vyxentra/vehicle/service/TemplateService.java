package com.vyxentra.vehicle.service;



import com.vyxentra.vehicle.entity.NotificationTemplate;

import java.util.List;
import java.util.Map;

public interface TemplateService {

    NotificationTemplate createTemplate(NotificationTemplate template);

    NotificationTemplate updateTemplate(String templateId, NotificationTemplate template);

    NotificationTemplate getTemplate(String templateId);

    NotificationTemplate getTemplateByNameAndChannel(String name, String channel);

    List<NotificationTemplate> getAllTemplates(String channel);

    void deleteTemplate(String templateId);

    String renderTemplate(String templateContent, Map<String, Object> data);

    Map<String, Object> validateTemplate(String templateContent);
}
