package com.javaengineeringlab.backend.assistente;

/**
 * Abstração de provedor de IA — ver SPEC-JEL-006-engineering-ai-assistant.md,
 * seção 30 do prompt mestre (evitar acoplamento a um único provedor).
 * Consumidores (controller) dependem só desta interface, nunca da
 * implementação concreta (hoje, Ollama).
 */
public interface AssistenteIA {

    String responder(ContextoPergunta contexto);
}
