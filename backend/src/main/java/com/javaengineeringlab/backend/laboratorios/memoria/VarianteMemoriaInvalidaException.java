package com.javaengineeringlab.backend.laboratorios.memoria;

import com.javaengineeringlab.backend.plataforma.RequisicaoInvalidaException;

public class VarianteMemoriaInvalidaException extends RequisicaoInvalidaException {

    public VarianteMemoriaInvalidaException(String segmento) {
        super("Variante inválida para o laboratório de memory leak: " + segmento
                + " (esperado: com-vazamento ou sem-vazamento)");
    }
}
