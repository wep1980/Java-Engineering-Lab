package com.javaengineeringlab.backend.laboratorios.circuitbreaker;

/**
 * Simula o erro real que uma dependência externa fora do ar
 * retornaria (ex.: HTTP 503). Nunca chega até o cliente da API --
 * capturada dentro de {@link ExecucaoCircuitBreakerService} e contada
 * como falha real.
 */
public class DependenciaExternaIndisponivelException extends RuntimeException {

    public DependenciaExternaIndisponivelException() {
        super("Dependência externa simulada está indisponível (equivalente a um HTTP 503 sustentado)");
    }
}
