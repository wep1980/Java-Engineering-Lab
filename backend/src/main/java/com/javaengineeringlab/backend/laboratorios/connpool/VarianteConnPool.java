package com.javaengineeringlab.backend.laboratorios.connpool;

import java.util.Arrays;

public enum VarianteConnPool {
    POOL_PEQUENO("pool-pequeno"),
    POOL_REDIMENSIONADO("pool-redimensionado"),
    CONEXAO_CURTA("conexao-curta");

    private final String segmentoUrl;

    VarianteConnPool(String segmentoUrl) {
        this.segmentoUrl = segmentoUrl;
    }

    static VarianteConnPool apartirDoSegmentoUrl(String segmento) {
        return Arrays.stream(values())
                .filter(variante -> variante.segmentoUrl.equals(segmento))
                .findFirst()
                .orElseThrow(() -> new VarianteConnPoolInvalidaException(segmento));
    }
}
