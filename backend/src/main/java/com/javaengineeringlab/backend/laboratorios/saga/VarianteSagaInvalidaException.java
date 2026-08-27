package com.javaengineeringlab.backend.laboratorios.saga;

import com.javaengineeringlab.backend.plataforma.RequisicaoInvalidaException;

public class VarianteSagaInvalidaException extends RequisicaoInvalidaException {

    public VarianteSagaInvalidaException(String segmento) {
        super("Variante inválida para o laboratório de saga: " + segmento
                + " (esperado: sem-compensacao ou com-compensacao)");
    }
}
