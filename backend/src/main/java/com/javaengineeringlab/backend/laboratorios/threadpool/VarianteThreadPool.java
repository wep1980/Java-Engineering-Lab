package com.javaengineeringlab.backend.laboratorios.threadpool;

import java.util.Arrays;

public enum VarianteThreadPool {
    FILA_ILIMITADA("fila-ilimitada"),
    FILA_LIMITADA("fila-limitada");

    private final String segmentoUrl;

    VarianteThreadPool(String segmentoUrl) {
        this.segmentoUrl = segmentoUrl;
    }

    static VarianteThreadPool apartirDoSegmentoUrl(String segmento) {
        return Arrays.stream(values())
                .filter(variante -> variante.segmentoUrl.equals(segmento))
                .findFirst()
                .orElseThrow(() -> new VarianteThreadPoolInvalidaException(segmento));
    }
}
