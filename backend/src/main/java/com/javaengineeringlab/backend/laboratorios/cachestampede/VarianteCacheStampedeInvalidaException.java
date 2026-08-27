package com.javaengineeringlab.backend.laboratorios.cachestampede;

import com.javaengineeringlab.backend.plataforma.RequisicaoInvalidaException;

public class VarianteCacheStampedeInvalidaException extends RequisicaoInvalidaException {

    public VarianteCacheStampedeInvalidaException(String segmento) {
        super("Variante inválida para o laboratório de cache stampede: " + segmento
                + " (esperado: sem-protecao ou com-protecao)");
    }
}
