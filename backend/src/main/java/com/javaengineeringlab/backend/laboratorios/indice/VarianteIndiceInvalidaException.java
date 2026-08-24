package com.javaengineeringlab.backend.laboratorios.indice;

import com.javaengineeringlab.backend.plataforma.RequisicaoInvalidaException;

public class VarianteIndiceInvalidaException extends RequisicaoInvalidaException {

    public VarianteIndiceInvalidaException(String segmento) {
        super("Variante inválida para o laboratório de query sem índice: " + segmento
                + " (esperado: sem-indice ou com-indice)");
    }
}
