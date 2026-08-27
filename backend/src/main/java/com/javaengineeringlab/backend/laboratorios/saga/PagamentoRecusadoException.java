package com.javaengineeringlab.backend.laboratorios.saga;

/**
 * Falha real e determinística da etapa 2 da saga -- nunca chega até o
 * cliente da API, capturada dentro de {@link ExecucaoSagaService} e
 * usada para decidir se a compensação da etapa 1 é executada. Ver
 * "Falha determinística na etapa de pagamento" em
 * specs/labs/SPEC-LAB-SAGA-001-saga.md.
 */
public class PagamentoRecusadoException extends RuntimeException {

    public PagamentoRecusadoException() {
        super("Pagamento recusado (cartão simulado sempre recusado nesta demonstração)");
    }
}
