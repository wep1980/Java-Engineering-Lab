package com.javaengineeringlab.backend.laboratorios.circuitbreaker;

import org.springframework.stereotype.Component;

/**
 * Representa uma dependência externa completamente fora do ar --
 * toda chamada espera uma latência de rede real simulada
 * ({@code Thread.sleep}, mesma técnica já usada em
 * {@code SPEC-LAB-RACE-001} e {@code SPEC-LAB-CONN-POOL-001}) e então
 * falha. Ver "Dependência sempre indisponível, não intermitente" em
 * specs/labs/SPEC-LAB-CIRCUITBREAKER-001-circuit-breaker.md.
 */
@Component
public class DependenciaExternaInstavel {

    static final long LATENCIA_MS = 300;

    public String chamar() {
        try {
            Thread.sleep(LATENCIA_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Execução interrompida", e);
        }
        throw new DependenciaExternaIndisponivelException();
    }
}
