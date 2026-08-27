package com.javaengineeringlab.backend.laboratorios.saga;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Etapa 1 da saga de criação de pedido -- ver
 * specs/labs/SPEC-LAB-SAGA-001-saga.md. {@code pedidoId} é só um
 * identificador de correlação entre as etapas, não uma entidade
 * {@code Pedido} própria -- de propósito, ver "pedidoId como
 * correlação" na SPEC.
 */
@Entity
@Table(name = "reserva_estoque")
public class ReservaEstoque {

    @Id
    private UUID id;

    private UUID pedidoId;

    private int quantidade;

    @Enumerated(EnumType.STRING)
    private StatusReserva status;

    protected ReservaEstoque() {
    }

    public ReservaEstoque(UUID pedidoId, int quantidade) {
        this.id = UUID.randomUUID();
        this.pedidoId = pedidoId;
        this.quantidade = quantidade;
        this.status = StatusReserva.RESERVADA;
    }

    public void cancelar() {
        this.status = StatusReserva.CANCELADA;
    }

    public UUID getPedidoId() {
        return pedidoId;
    }

    public StatusReserva getStatus() {
        return status;
    }
}
