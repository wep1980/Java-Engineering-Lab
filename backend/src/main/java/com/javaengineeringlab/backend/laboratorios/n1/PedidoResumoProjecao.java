package com.javaengineeringlab.backend.laboratorios.n1;

/**
 * DTO Projection — uma das soluções para N+1. Como é construída
 * diretamente pela query (constructor expression JPQL), nunca carrega as
 * entidades gerenciadas nem a coleção de itens completa.
 */
public record PedidoResumoProjecao(
        Long id,
        StatusPedido status,
        Long quantidadeItens
) {
}
