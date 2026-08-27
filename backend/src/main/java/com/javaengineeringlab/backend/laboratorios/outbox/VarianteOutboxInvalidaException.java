package com.javaengineeringlab.backend.laboratorios.outbox;

import com.javaengineeringlab.backend.plataforma.RequisicaoInvalidaException;

public class VarianteOutboxInvalidaException extends RequisicaoInvalidaException {

    public VarianteOutboxInvalidaException(String segmento) {
        super("Variante inválida para o laboratório de transactional outbox: " + segmento
                + " (esperado: sem-outbox ou com-outbox)");
    }
}
