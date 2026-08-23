package com.javaengineeringlab.backend.laboratorios.n1;

import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Executa cada variante do laboratório de N+1 e mede o custo real em
 * número de statements SQL preparados pelo Hibernate (Statistics
 * .getPrepareStatementCount()) — instrumentação real, não estimada (ver
 * RNF-01 de specs/labs/SPEC-LAB-N1-001-n-mais-um-queries.md).
 */
@Service
public class ExecucaoN1Service {

    private final PedidoRepository pedidoRepository;
    private final Statistics estatisticasHibernate;

    public ExecucaoN1Service(PedidoRepository pedidoRepository, EntityManagerFactory entityManagerFactory) {
        this.pedidoRepository = pedidoRepository;
        this.estatisticasHibernate = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }

    @Transactional(readOnly = true)
    public ResultadoExecucaoLaboratorio executar(VarianteN1 variante) {
        long statementsAntes = estatisticasHibernate.getPrepareStatementCount();
        Instant inicio = Instant.now();

        int quantidadePedidos = switch (variante) {
            case PROBLEMATICO -> executarProblematico();
            case JOIN_FETCH -> pedidoRepository.buscarTodosComItensJoinFetch().size();
            case ENTITY_GRAPH -> pedidoRepository.buscarTodosComItensEntityGraph().size();
            case DTO_PROJECTION -> pedidoRepository.buscarResumoProjecao().size();
        };

        long duracaoMs = Duration.between(inicio, Instant.now()).toMillis();
        long quantidadeQueries = estatisticasHibernate.getPrepareStatementCount() - statementsAntes;

        VarianteExecucao varianteExecucao = variante == VarianteN1.PROBLEMATICO
                ? VarianteExecucao.PROBLEMATICO
                : VarianteExecucao.CORRIGIDO;

        Map<String, Object> metricas = Map.of(
                "tecnica", variante.name(),
                "quantidadeQueries", quantidadeQueries,
                "quantidadePedidos", quantidadePedidos
        );

        return new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(),
                "n1-queries",
                varianteExecucao,
                OrigemDados.REAL,
                inicio,
                duracaoMs,
                metricas
        );
    }

    private int executarProblematico() {
        List<Pedido> pedidos = pedidoRepository.buscarTodosSemItens();
        // Acessar a coleção lazy de cada pedido, um a um, dispara uma
        // consulta adicional por pedido -- o N+1.
        for (Pedido pedido : pedidos) {
            pedido.getItens().size(); // NOSONAR -- retorno ignorado de propósito: só força o lazy loading, é o próprio N+1 que este laboratório demonstra
        }
        return pedidos.size();
    }
}
