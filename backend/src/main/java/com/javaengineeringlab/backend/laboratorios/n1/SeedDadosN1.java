package com.javaengineeringlab.backend.laboratorios.n1;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Massa de dados determinística para o laboratório de N+1 — mesma
 * quantidade de pedidos/itens em toda execução, para permitir comparação
 * justa entre variantes (specs/labs/SPEC-LAB-N1-001-n-mais-um-queries.md, RF-04).
 */
@Component
public class SeedDadosN1 {

    static final int QUANTIDADE_PEDIDOS = 50;
    static final int ITENS_POR_PEDIDO = 3;

    private final PedidoRepository pedidoRepository;

    public SeedDadosN1(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public void garantirDadosDemonstracao() {
        if (pedidoRepository.count() > 0) {
            return;
        }

        Instant referencia = Instant.parse("2026-01-01T00:00:00Z");
        StatusPedido[] statusDisponiveis = StatusPedido.values();

        for (int i = 0; i < QUANTIDADE_PEDIDOS; i++) {
            StatusPedido status = statusDisponiveis[i % statusDisponiveis.length];
            Pedido pedido = new Pedido(status, referencia.plus(i, ChronoUnit.HOURS));

            for (int j = 0; j < ITENS_POR_PEDIDO; j++) {
                pedido.adicionarItem(new ItemPedido(
                        "Item " + (j + 1) + " do pedido " + (i + 1),
                        j + 1,
                        BigDecimal.valueOf(10 + j * 5.5)
                ));
            }

            pedidoRepository.save(pedido);
        }
    }
}
