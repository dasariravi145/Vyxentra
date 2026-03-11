package com.vyxentra.vehicle.template;


import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateEngine {

    private final SpringTemplateEngine templateEngine;

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    public String processTemplate(String template, Object data) {
        if (template == null) {
            return null;
        }

        Context context = new Context();
        if (data instanceof Map) {
            ((Map<?, ?>) data).forEach((key, value) -> {
                if (key != null) {
                    context.setVariable(key.toString(), value);
                }
            });
        }

        return templateEngine.process(template, context);
    }

    public Map<String, Object> validateTemplate(String template) {
        // Extract all variables from template
        Set<String> variables = extractVariables(template);

        return Map.of(
                "valid", true,
                "variables", variables
        );
    }

    private Set<String> extractVariables(String template) {
        Set<String> variables = new java.util.HashSet<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(template);

        while (matcher.find()) {
            variables.add(matcher.group(1).trim());
        }

        return variables;
    }
}
