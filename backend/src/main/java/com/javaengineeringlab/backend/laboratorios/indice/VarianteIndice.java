package com.javaengineeringlab.backend.laboratorios.indice;

import java.util.Arrays;

public enum VarianteIndice {
    SEM_INDICE("sem-indice"),
    COM_INDICE("com-indice");

    private final String segmentoUrl;

    VarianteIndice(String segmentoUrl) {
        this.segmentoUrl = segmentoUrl;
    }

    static VarianteIndice apartirDoSegmentoUrl(String segmento) {
        return Arrays.stream(values())
                .filter(variante -> variante.segmentoUrl.equals(segmento))
                .findFirst()
                .orElseThrow(() -> new VarianteIndiceInvalidaException(segmento));
    }
}
