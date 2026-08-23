package com.javaengineeringlab.backend.assistente;

import java.util.Map;

/**
 * Contexto real repassado ao provedor de IA: identifica o laboratório e,
 * opcionalmente, o resultado da última execução exibida na tela do
 * usuário (o mesmo objeto do painel de execução) — ver
 * SPEC-JEL-006-engineering-ai-assistant.md, "Contexto vem do frontend".
 */
public record ContextoPergunta(
        String laboratorioId,
        String pergunta,
        Map<String, Object> ultimoResultado
) {
}
