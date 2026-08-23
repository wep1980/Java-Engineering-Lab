package com.javaengineeringlab.backend.laboratorios.deadlock;

import java.util.Arrays;

public enum VarianteDeadlock {
    SEM_ORDEM_CONSISTENTE("sem-ordem-consistente"),
    ORDEM_CONSISTENTE("ordem-consistente");

    private final String segmentoUrl;

    VarianteDeadlock(String segmentoUrl) {
        this.segmentoUrl = segmentoUrl;
    }

    static VarianteDeadlock apartirDoSegmentoUrl(String segmento) {
        return Arrays.stream(values())
                .filter(variante -> variante.segmentoUrl.equals(segmento))
                .findFirst()
                .orElseThrow(() -> new VarianteDeadlockInvalidaException(segmento));
    }
}
