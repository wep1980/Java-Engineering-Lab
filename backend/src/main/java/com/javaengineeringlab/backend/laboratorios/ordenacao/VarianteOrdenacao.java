package com.javaengineeringlab.backend.laboratorios.ordenacao;

import java.util.Arrays;

public enum VarianteOrdenacao {
    SEM_CHAVE_PARTICIONAMENTO("sem-chave-particionamento"),
    COM_CHAVE_PARTICIONAMENTO("com-chave-particionamento");

    private final String segmentoUrl;

    VarianteOrdenacao(String segmentoUrl) {
        this.segmentoUrl = segmentoUrl;
    }

    static VarianteOrdenacao apartirDoSegmentoUrl(String segmento) {
        return Arrays.stream(values())
                .filter(variante -> variante.segmentoUrl.equals(segmento))
                .findFirst()
                .orElseThrow(() -> new VarianteOrdenacaoInvalidaException(segmento));
    }
}
