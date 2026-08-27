package com.javaengineeringlab.backend.laboratorios.circuitbreaker;

import com.javaengineeringlab.backend.plataforma.RequisicaoInvalidaException;

public class VarianteCircuitBreakerInvalidaException extends RequisicaoInvalidaException {

    public VarianteCircuitBreakerInvalidaException(String segmento) {
        super("Variante inválida para o laboratório de circuit breaker: " + segmento
                + " (esperado: sem-circuit-breaker ou com-circuit-breaker)");
    }
}
