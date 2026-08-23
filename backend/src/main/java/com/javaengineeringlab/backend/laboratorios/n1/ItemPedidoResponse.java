package com.javaengineeringlab.backend.laboratorios.n1;

import java.math.BigDecimal;

public record ItemPedidoResponse(
        String descricao,
        Integer quantidade,
        BigDecimal precoUnitario
) {
}
