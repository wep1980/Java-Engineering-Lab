package com.javaengineeringlab.backend.laboratorios.kafka;

import java.util.Arrays;

public enum VarianteKafka {
    SEM_IDEMPOTENCIA("sem-idempotencia", ConsumidorPagamentoSemIdempotencia.TOPICO),
    IDEMPOTENTE("idempotente", ConsumidorPagamentoIdempotente.TOPICO);

    private final String segmentoUrl;
    private final String topico;

    VarianteKafka(String segmentoUrl, String topico) {
        this.segmentoUrl = segmentoUrl;
        this.topico = topico;
    }

    public String getTopico() {
        return topico;
    }

    static VarianteKafka apartirDoSegmentoUrl(String segmento) {
        return Arrays.stream(values())
                .filter(variante -> variante.segmentoUrl.equals(segmento))
                .findFirst()
                .orElseThrow(() -> new VarianteKafkaInvalidaException(segmento));
    }
}
