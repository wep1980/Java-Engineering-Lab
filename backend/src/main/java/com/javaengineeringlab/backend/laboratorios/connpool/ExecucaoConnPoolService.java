package com.javaengineeringlab.backend.laboratorios.connpool;

import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Orquestra o cenário de concorrência real do laboratório de Connection
 * Pool Exhaustion — ver
 * specs/labs/SPEC-LAB-CONN-POOL-001-connection-pool-exhaustion.md.
 *
 * <p>Os dois pools de demonstração são construídos diretamente aqui —
 * <strong>não</strong> como {@code @Bean HikariDataSource} avulsos — de
 * propósito: registrá-los como beans de tipo {@code DataSource}
 * quebrava {@code @ConditionalOnSingleCandidate(DataSource.class)} da
 * autoconfiguração do JPA (sem nenhum marcado {@code @Primary}, o Spring
 * deixa de encontrar um "único candidato" e desiste de criar o
 * {@code entityManagerFactory} silenciosamente, derrubando todos os
 * outros laboratórios). Construir os pools manualmente, a partir de
 * {@link JdbcConnectionDetails} (que não é do tipo {@code DataSource},
 * logo não conflita), evita esse problema por completo — ver ADR-0009.
 */
@Service
public class ExecucaoConnPoolService {

    static final int QUANTIDADE_REQUISICOES_CONCORRENTES = 10;
    static final long TRABALHO_LENTO_MS = 500;
    static final int TAMANHO_POOL_PEQUENO = 2;
    static final int TAMANHO_POOL_REDIMENSIONADO = 12;
    static final long TIMEOUT_CONEXAO_MS = 800;

    private final HikariDataSource poolPequeno;
    private final HikariDataSource poolRedimensionado;

    public ExecucaoConnPoolService(JdbcConnectionDetails detalhesConexao) {
        this.poolPequeno = criarPool(detalhesConexao, TAMANHO_POOL_PEQUENO, "pool-demonstracao-pequeno");
        this.poolRedimensionado = criarPool(detalhesConexao, TAMANHO_POOL_REDIMENSIONADO, "pool-demonstracao-redimensionado");
    }

    private static HikariDataSource criarPool(JdbcConnectionDetails detalhes, int tamanhoMaximo, String nomePool) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(detalhes.getJdbcUrl());
        dataSource.setUsername(detalhes.getUsername());
        dataSource.setPassword(detalhes.getPassword());
        dataSource.setMaximumPoolSize(tamanhoMaximo);
        dataSource.setConnectionTimeout(TIMEOUT_CONEXAO_MS);
        dataSource.setPoolName(nomePool);
        return dataSource;
    }

    @PreDestroy
    void encerrarPools() {
        poolPequeno.close();
        poolRedimensionado.close();
    }

    public ResultadoExecucaoLaboratorio executar(VarianteConnPool variante) {
        Instant inicio = Instant.now();

        HikariDataSource pool = variante == VarianteConnPool.POOL_REDIMENSIONADO
                ? poolRedimensionado
                : poolPequeno;
        boolean segurarConexaoDuranteTrabalhoLento = variante != VarianteConnPool.CONEXAO_CURTA;

        AtomicInteger sucessos = new AtomicInteger();
        AtomicInteger falhasPorTimeout = new AtomicInteger();

        dispararConcorrente(() -> executarOperacao(pool, segurarConexaoDuranteTrabalhoLento, sucessos, falhasPorTimeout));

        long duracaoMs = Duration.between(inicio, Instant.now()).toMillis();

        VarianteExecucao varianteExecucao = variante == VarianteConnPool.POOL_PEQUENO
                ? VarianteExecucao.PROBLEMATICO
                : VarianteExecucao.CORRIGIDO;

        Map<String, Object> metricas = Map.of(
                "tecnica", variante.name(),
                "tamanhoDoPool", pool.getMaximumPoolSize(),
                "quantidadeRequisicoesConcorrentes", QUANTIDADE_REQUISICOES_CONCORRENTES,
                "quantidadeSucesso", sucessos.get(),
                "quantidadeFalhasPorTimeout", falhasPorTimeout.get()
        );

        return new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(),
                "connection-pool-exhaustion",
                varianteExecucao,
                OrigemDados.REAL,
                inicio,
                duracaoMs,
                metricas
        );
    }

    private void executarOperacao(
            HikariDataSource pool,
            boolean segurarConexaoDuranteTrabalhoLento,
            AtomicInteger sucessos,
            AtomicInteger falhasPorTimeout
    ) {
        try {
            if (!segurarConexaoDuranteTrabalhoLento) {
                // técnica correta: trabalho lento ANTES de obter a conexão
                Thread.sleep(TRABALHO_LENTO_MS);
            }
            try (Connection conexao = pool.getConnection()) {
                if (segurarConexaoDuranteTrabalhoLento) {
                    // o erro que este laboratório demonstra: segurar a
                    // conexão durante um trabalho que não precisa dela
                    Thread.sleep(TRABALHO_LENTO_MS);
                }
                try (Statement consulta = conexao.createStatement()) {
                    consulta.execute("SELECT 1");
                }
            }
            sucessos.incrementAndGet();
        } catch (SQLTransientConnectionException esgotamentoDoPool) {
            falhasPorTimeout.incrementAndGet();
        } catch (SQLException erroInesperado) {
            throw new IllegalStateException("Falha inesperada de banco no laboratório de connection pool", erroInesperado);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Execução interrompida", e);
        }
    }

    /**
     * Dispara `QUANTIDADE_REQUISICOES_CONCORRENTES` execuções de
     * `operacao` em threads separadas, liberadas simultaneamente por uma
     * barreira de largada (CountDownLatch) — mesmo padrão de
     * ExecucaoRaceConditionService.
     */
    private void dispararConcorrente(Runnable operacao) {
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<?>> tarefas = new ArrayList<>();

        try (ExecutorService executor = Executors.newFixedThreadPool(QUANTIDADE_REQUISICOES_CONCORRENTES)) {
            for (int i = 0; i < QUANTIDADE_REQUISICOES_CONCORRENTES; i++) {
                tarefas.add(executor.submit(() -> {
                    largada.await();
                    operacao.run();
                    return null;
                }));
            }

            largada.countDown();

            for (Future<?> tarefa : tarefas) {
                tarefa.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Execução concorrente interrompida", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Falha em execução concorrente", e.getCause());
        }
    }
}
