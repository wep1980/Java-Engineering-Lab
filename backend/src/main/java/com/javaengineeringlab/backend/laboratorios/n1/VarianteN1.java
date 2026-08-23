package com.javaengineeringlab.backend.laboratorios.n1;

import java.util.Arrays;

public enum VarianteN1 {
    PROBLEMATICO("problematico"),
    JOIN_FETCH("join-fetch"),
    ENTITY_GRAPH("entity-graph"),
    DTO_PROJECTION("dto-projection");

    private final String segmentoUrl;

    VarianteN1(String segmentoUrl) {
        this.segmentoUrl = segmentoUrl;
    }

    public String getSegmentoUrl() {
        return segmentoUrl;
    }

    static VarianteN1 apartirDoSegmentoUrl(String segmento) {
        return Arrays.stream(values())
                .filter(variante -> variante.segmentoUrl.equals(segmento))
                .findFirst()
                .orElseThrow(() -> new VarianteN1InvalidaException(segmento));
    }
}
