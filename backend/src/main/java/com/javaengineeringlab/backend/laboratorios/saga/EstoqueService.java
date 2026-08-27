package com.javaengineeringlab.backend.laboratorios.saga;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Etapa 1 da saga ("serviço" de estoque) e sua ação de compensação --
 * ver specs/labs/SPEC-LAB-SAGA-001-saga.md. Cada método é sua própria
 * transação local, real e commitada -- não existe uma transação
 * distribuída cobrindo as duas etapas da saga.
 */
@Service
public class EstoqueService {

    private final ReservaEstoqueRepository reservaEstoqueRepository;

    public EstoqueService(ReservaEstoqueRepository reservaEstoqueRepository) {
        this.reservaEstoqueRepository = reservaEstoqueRepository;
    }

    @Transactional
    public ReservaEstoque reservar(UUID pedidoId, int quantidade) {
        return reservaEstoqueRepository.save(new ReservaEstoque(pedidoId, quantidade));
    }

    /**
     * Ação de compensação real da etapa 1 -- desfaz a reserva quando
     * uma etapa posterior da saga falha.
     */
    @Transactional
    public void cancelarReserva(UUID pedidoId) {
        ReservaEstoque reserva = reservaEstoqueRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new IllegalStateException("Nenhuma reserva encontrada para o pedido " + pedidoId));
        reserva.cancelar();
        reservaEstoqueRepository.save(reserva);
    }
}
