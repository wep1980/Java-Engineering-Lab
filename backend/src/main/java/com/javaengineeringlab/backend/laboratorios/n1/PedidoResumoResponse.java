package com.javaengineeringlab.backend.laboratorios.n1;

public record PedidoResumoResponse(
        Long id,
        StatusPedido status,
        Long quantidadeItens
) {
}
