package com.catjard.solicitudes.jira;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Propiedades de la integracion con Jira Cloud (prefijo 'jira.' en application.properties).
@ConfigurationProperties(prefix = "jira")
public record JiraProperties(
        boolean enabled,
        String baseUrl,
        String email,
        String apiToken,
        String projectKey,
        String cambiosProjectKey,
        String issueType
) {}
