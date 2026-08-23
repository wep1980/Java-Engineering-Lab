package com.javaengineeringlab.backend.laboratorios.race;

import java.util.Arrays;

public enum VarianteRace {
    SEM_CONTROLE("sem-controle"),
    OTIMISTA("otimista"),
    PESSIMISTA("pessimista");

    private final String segmentoUrl;

    VarianteRace(String segmentoUrl) {
        this.segmentoUrl = segmentoUrl;
    }

    static VarianteRace apartirDoSegmentoUrl(String segmento) {
        return Arrays.stream(values())
                .filter(variante -> variante.segmentoUrl.equals(segmento))
                .findFirst()
                .orElseThrow(() -> new VarianteRaceInvalidaException(segmento));
    }
}
