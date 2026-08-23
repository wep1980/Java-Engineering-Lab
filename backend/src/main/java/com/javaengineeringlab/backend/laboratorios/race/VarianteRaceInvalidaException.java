package com.javaengineeringlab.backend.laboratorios.race;

import com.javaengineeringlab.backend.plataforma.RequisicaoInvalidaException;

public class VarianteRaceInvalidaException extends RequisicaoInvalidaException {

    public VarianteRaceInvalidaException(String segmento) {
        super("Variante inválida para o laboratório de race condition: " + segmento
                + " (esperado: sem-controle, otimista ou pessimista)");
    }
}
