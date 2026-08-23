package com.javaengineeringlab.backend.laboratorios.n1;

import java.util.List;

public record PedidoComItensResponse(
        Long id,
        StatusPedido status,
        List<ItemPedidoResponse> itens
) {
}
