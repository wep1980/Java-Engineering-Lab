package com.javaengineeringlab.backend.assistente;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record PerguntaRequest(
        @NotBlank String pergunta,
        Map<String, Object> ultimoResultado
) {
}
