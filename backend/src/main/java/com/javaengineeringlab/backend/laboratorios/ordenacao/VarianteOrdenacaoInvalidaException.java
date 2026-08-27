package com.javaengineeringlab.backend.laboratorios.ordenacao;

import com.javaengineeringlab.backend.plataforma.RequisicaoInvalidaException;

public class VarianteOrdenacaoInvalidaException extends RequisicaoInvalidaException {

    public VarianteOrdenacaoInvalidaException(String segmento) {
        super("Variante inválida para o laboratório de ordenação de eventos: " + segmento
                + " (esperado: sem-chave-particionamento ou com-chave-particionamento)");
    }
}
