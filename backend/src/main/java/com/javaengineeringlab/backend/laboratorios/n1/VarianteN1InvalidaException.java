package com.javaengineeringlab.backend.laboratorios.n1;

import com.javaengineeringlab.backend.plataforma.RequisicaoInvalidaException;

public class VarianteN1InvalidaException extends RequisicaoInvalidaException {

    public VarianteN1InvalidaException(String segmento) {
        super("Variante inválida para o laboratório de N+1: " + segmento
                + " (esperado: problematico, join-fetch, entity-graph ou dto-projection)");
    }
}
