package com.javaengineeringlab.backend.laboratorios.kafka;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payload do evento publicado no Kafka. O mesmo eventoId é publicado
 * duas vezes deliberadamente pelo laboratório, para reproduzir uma
 * entrega duplicada real — ver
 * specs/labs/SPEC-LAB-KAFKA-IDEMP-001-mensagem-duplicada-idempotencia.md.
 */
public record EventoPagamentoConfirmado(
        UUID eventoId,
        Long carteiraId,
        BigDecimal valor
) {
}
