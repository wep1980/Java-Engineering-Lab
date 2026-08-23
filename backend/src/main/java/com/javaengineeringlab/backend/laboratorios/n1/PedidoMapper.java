package com.javaengineeringlab.backend.laboratorios.n1;

import java.util.List;

final class PedidoMapper {

    private PedidoMapper() {
    }

    static PedidoComItensResponse paraResponseComItens(Pedido pedido) {
        List<ItemPedidoResponse> itens = pedido.getItens().stream()
                .map(item -> new ItemPedidoResponse(item.getDescricao(), item.getQuantidade(), item.getPrecoUnitario()))
                .toList();
        return new PedidoComItensResponse(pedido.getId(), pedido.getStatus(), itens);
    }

    static PedidoResumoResponse paraResumoResponse(PedidoResumoProjecao projecao) {
        return new PedidoResumoResponse(projecao.id(), projecao.status(), projecao.quantidadeItens());
    }
}
