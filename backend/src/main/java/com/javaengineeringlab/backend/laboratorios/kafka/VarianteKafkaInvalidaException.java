package com.javaengineeringlab.backend.laboratorios.kafka;

import com.javaengineeringlab.backend.plataforma.RequisicaoInvalidaException;

public class VarianteKafkaInvalidaException extends RequisicaoInvalidaException {

    public VarianteKafkaInvalidaException(String segmento) {
        super("Variante inválida para o laboratório de Kafka/idempotência: " + segmento
                + " (esperado: sem-idempotencia ou idempotente)");
    }
}
