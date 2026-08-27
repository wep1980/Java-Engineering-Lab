package com.javaengineeringlab.backend.laboratorios.saga;

import java.util.Arrays;

public enum VarianteSaga {
    SEM_COMPENSACAO("sem-compensacao"),
    COM_COMPENSACAO("com-compensacao");

    private final String segmentoUrl;

    VarianteSaga(String segmentoUrl) {
        this.segmentoUrl = segmentoUrl;
    }

    static VarianteSaga apartirDoSegmentoUrl(String segmento) {
        return Arrays.stream(values())
                .filter(variante -> variante.segmentoUrl.equals(segmento))
                .findFirst()
                .orElseThrow(() -> new VarianteSagaInvalidaException(segmento));
    }
}
