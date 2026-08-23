package com.javaengineeringlab.backend.laboratorios.deadlock;

import com.javaengineeringlab.backend.plataforma.RequisicaoInvalidaException;

public class VarianteDeadlockInvalidaException extends RequisicaoInvalidaException {

    public VarianteDeadlockInvalidaException(String segmento) {
        super("Variante inválida para o laboratório de deadlock: " + segmento
                + " (esperado: sem-ordem-consistente ou ordem-consistente)");
    }
}
