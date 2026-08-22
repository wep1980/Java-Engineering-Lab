package com.javaengineeringlab.backend.plataforma;

import java.time.Instant;

/**
 * Formato padrão de erro da API — ver docs/security.md. Nunca inclui
 * stack trace ou detalhes internos.
 */
public record ErroResposta(
        int codigo,
        String mensagem,
        Instant timestamp,
        String caminho,
        String correlationId
) {
}
