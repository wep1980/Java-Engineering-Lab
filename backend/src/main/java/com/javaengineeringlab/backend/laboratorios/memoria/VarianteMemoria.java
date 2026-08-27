package com.javaengineeringlab.backend.laboratorios.memoria;

import java.util.Arrays;

public enum VarianteMemoria {
    COM_VAZAMENTO("com-vazamento"),
    SEM_VAZAMENTO("sem-vazamento");

    private final String segmentoUrl;

    VarianteMemoria(String segmentoUrl) {
        this.segmentoUrl = segmentoUrl;
    }

    static VarianteMemoria apartirDoSegmentoUrl(String segmento) {
        return Arrays.stream(values())
                .filter(variante -> variante.segmentoUrl.equals(segmento))
                .findFirst()
                .orElseThrow(() -> new VarianteMemoriaInvalidaException(segmento));
    }
}
