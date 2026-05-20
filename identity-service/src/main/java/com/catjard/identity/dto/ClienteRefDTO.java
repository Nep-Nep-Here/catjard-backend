package com.catjard.identity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Respuesta de crm-service: solo nos interesa el id del cliente CRM creado/reusado.
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClienteRefDTO(Long id) {}
