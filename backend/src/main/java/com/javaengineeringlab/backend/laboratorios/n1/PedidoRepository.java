package com.javaengineeringlab.backend.laboratorios.n1;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /**
     * Variante problemática: retorna os pedidos sem os itens. Quem
     * acessar pedido.getItens() para cada um dispara uma consulta lazy
     * adicional por pedido — o N+1. Ver ExecucaoN1Service.
     */
    @Query("SELECT p FROM Pedido p")
    List<Pedido> buscarTodosSemItens();

    @Query("SELECT DISTINCT p FROM Pedido p JOIN FETCH p.itens")
    List<Pedido> buscarTodosComItensJoinFetch();

    @EntityGraph(attributePaths = "itens")
    @Query("SELECT p FROM Pedido p")
    List<Pedido> buscarTodosComItensEntityGraph();

    @Query("""
            SELECT new com.javaengineeringlab.backend.laboratorios.n1.PedidoResumoProjecao(
                p.id, p.status, COUNT(i)
            )
            FROM Pedido p LEFT JOIN p.itens i
            GROUP BY p.id, p.status
            ORDER BY p.id
            """)
    List<PedidoResumoProjecao> buscarResumoProjecao();
}
