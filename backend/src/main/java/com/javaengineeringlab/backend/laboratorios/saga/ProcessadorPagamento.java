package com.javaengineeringlab.backend.laboratorios.saga;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Etapa 2 da saga ("serviço" de pagamento) -- sempre recusa nesta
 * demonstração, de propósito. Ver "Falha determinística na etapa de
 * pagamento" em specs/labs/SPEC-LAB-SAGA-001-saga.md.
 */
@Component
public class ProcessadorPagamento {

    public void cobrar(UUID pedidoId, BigDecimal valor) {
        throw new PagamentoRecusadoException();
    }
}
