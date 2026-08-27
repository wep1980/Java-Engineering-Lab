package com.javaengineeringlab.backend.plataforma;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Catálogo de laboratórios. Lista fixa em memória — sem persistência
 * nesta fase (ver RNF-03 de SPEC-JEL-003). Cada laboratório novo entra
 * aqui quando sua SPEC (specs/labs/) for aprovada.
 */
@Service
public class CatalogoLaboratoriosService {

    private static final List<LaboratorioResumo> LABORATORIOS = List.of(
            new LaboratorioResumo(
                    "n1-queries",
                    "N+1 Queries",
                    "Demonstrar o problema de N+1 consultas com JPA/Hibernate e suas soluções (JOIN FETCH, EntityGraph, DTO Projection).",
                    StatusLaboratorio.DISPONIVEL
            ),
            new LaboratorioResumo(
                    "race-condition",
                    "Race Condition / Lost Update",
                    "Demonstrar a perda silenciosa de atualizações sob concorrência real, e as soluções com Optimistic Locking (@Version) e Pessimistic Locking.",
                    StatusLaboratorio.DISPONIVEL
            ),
            new LaboratorioResumo(
                    "kafka-idempotencia",
                    "Kafka / Mensagem Duplicada / Idempotência",
                    "Demonstrar entrega duplicada real no Kafka e a diferença entre semântica de entrega, processamento idempotente e efeito de negócio.",
                    StatusLaboratorio.DISPONIVEL
            ),
            new LaboratorioResumo(
                    "connection-pool-exhaustion",
                    "Connection Pool Exhaustion",
                    "Demonstrar o esgotamento do pool de conexões sob concorrência real, e por que reduzir o tempo de retenção da conexão costuma ser mais eficaz que apenas aumentar o tamanho do pool.",
                    StatusLaboratorio.DISPONIVEL
            ),
            new LaboratorioResumo(
                    "deadlock",
                    "Deadlock",
                    "Demonstrar um deadlock real de banco de dados por ordem de aquisição de locks inconsistente, e a correção por ordenação consistente.",
                    StatusLaboratorio.DISPONIVEL
            ),
            new LaboratorioResumo(
                    "query-sem-indice",
                    "Query sem índice",
                    "Demonstrar a diferença real de plano de execução (Seq Scan vs. Index Scan) e de tempo entre uma busca sem índice e com índice, usando EXPLAIN ANALYZE real do PostgreSQL.",
                    StatusLaboratorio.DISPONIVEL
            ),
            new LaboratorioResumo(
                    "circuit-breaker",
                    "Circuit Breaker",
                    "Demonstrar a falha em cascata contra uma dependência externa instável e como um circuit breaker real (Resilience4j) interrompe chamadas fadadas ao fracasso em vez de deixar cada requisição pagar o custo total da falha.",
                    StatusLaboratorio.DISPONIVEL
            )
    );

    public List<LaboratorioResumo> listar() {
        return LABORATORIOS;
    }

    public LaboratorioResumo buscarPorId(String id) {
        return LABORATORIOS.stream()
                .filter(laboratorio -> laboratorio.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new LaboratorioNaoEncontradoException(id));
    }
}
