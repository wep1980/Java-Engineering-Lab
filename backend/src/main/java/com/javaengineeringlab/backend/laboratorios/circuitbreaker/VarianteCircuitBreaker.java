package com.javaengineeringlab.backend.laboratorios.circuitbreaker;

import java.util.Arrays;

public enum VarianteCircuitBreaker {
    SEM_CIRCUIT_BREAKER("sem-circuit-breaker"),
    COM_CIRCUIT_BREAKER("com-circuit-breaker");

    private final String segmentoUrl;

    VarianteCircuitBreaker(String segmentoUrl) {
        this.segmentoUrl = segmentoUrl;
    }

    static VarianteCircuitBreaker apartirDoSegmentoUrl(String segmento) {
        return Arrays.stream(values())
                .filter(variante -> variante.segmentoUrl.equals(segmento))
                .findFirst()
                .orElseThrow(() -> new VarianteCircuitBreakerInvalidaException(segmento));
    }
}
