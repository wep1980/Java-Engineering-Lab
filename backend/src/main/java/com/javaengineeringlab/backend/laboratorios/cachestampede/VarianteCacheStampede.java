package com.javaengineeringlab.backend.laboratorios.cachestampede;

import java.util.Arrays;

public enum VarianteCacheStampede {
    SEM_PROTECAO("sem-protecao"),
    COM_PROTECAO("com-protecao");

    private final String segmentoUrl;

    VarianteCacheStampede(String segmentoUrl) {
        this.segmentoUrl = segmentoUrl;
    }

    static VarianteCacheStampede apartirDoSegmentoUrl(String segmento) {
        return Arrays.stream(values())
                .filter(variante -> variante.segmentoUrl.equals(segmento))
                .findFirst()
                .orElseThrow(() -> new VarianteCacheStampedeInvalidaException(segmento));
    }
}
