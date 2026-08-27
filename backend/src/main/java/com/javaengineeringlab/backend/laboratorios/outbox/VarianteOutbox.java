package com.javaengineeringlab.backend.laboratorios.outbox;

import java.util.Arrays;

public enum VarianteOutbox {
    SEM_OUTBOX("sem-outbox"),
    COM_OUTBOX("com-outbox");

    private final String segmentoUrl;

    VarianteOutbox(String segmentoUrl) {
        this.segmentoUrl = segmentoUrl;
    }

    static VarianteOutbox apartirDoSegmentoUrl(String segmento) {
        return Arrays.stream(values())
                .filter(variante -> variante.segmentoUrl.equals(segmento))
                .findFirst()
                .orElseThrow(() -> new VarianteOutboxInvalidaException(segmento));
    }
}
