package com.javaengineeringlab.backend.laboratorios.outbox;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payload do evento -- serializado como JSON dentro de
 * {@link OutboxEvento#getPayload()} no momento da escrita (mesma
 * transação local do Pedido) e desserializado pelo relay só na hora de
 * publicar de fato no Kafka.
 */
public record EventoPedidoCriado(
        UUID eventoId,
        Long pedidoId,
        String descricao,
        BigDecimal valor
) {
}
