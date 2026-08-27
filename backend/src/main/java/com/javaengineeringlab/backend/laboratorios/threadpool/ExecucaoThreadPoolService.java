package com.javaengineeringlab.backend.laboratorios.threadpool;

import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Orquestra as duas variantes do laboratório de Thread Pool Exhaustion
 * -- ver specs/labs/SPEC-LAB-THREADPOOL-001-thread-pool-exhaustion.md.
 *
 * <p>Os dois pools de demonstração são construídos diretamente aqui,
 * completamente isolados do pool de threads que atende requisições
 * HTTP do backend -- mesmo princípio de isolamento já validado em
 * {@code ExecucaoConnPoolService} (ver ADR-0009).
 */
@Service
public class ExecucaoThreadPoolService {

    static final int TAMANHO_POOL = 2;
    static final int CAPACIDADE_FILA_LIMITADA = 2;
    static final int QUANTIDADE_REQUISICOES = 10;
    static final long TRABALHO_LENTO_MS = 500;

    private final ExecutorService poolFilaIlimitada = Executors.newFixedThreadPool(TAMANHO_POOL);
    private final ThreadPoolExecutor poolFilaLimitada = new ThreadPoolExecutor(
            TAMANHO_POOL, TAMANHO_POOL, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(CAPACIDADE_FILA_LIMITADA));

    @PreDestroy
    void encerrarPools() {
        poolFilaIlimitada.shutdownNow();
        poolFilaLimitada.shutdownNow();
    }

    public ResultadoExecucaoLaboratorio executar(VarianteThreadPool variante) {
        Instant inicio = Instant.now();

        ExecutorService pool = variante == VarianteThreadPool.FILA_ILIMITADA
                ? poolFilaIlimitada
                : poolFilaLimitada;

        int aceitas = 0;
        int rejeitadas = 0;
        AtomicLong maiorEsperaNaFilaMs = new AtomicLong();
        List<Future<?>> tarefasAceitas = new ArrayList<>();

        for (int i = 0; i < QUANTIDADE_REQUISICOES; i++) {
            long momentoSubmissaoNanos = System.nanoTime();
            try {
                Future<?> tarefa = pool.submit(() -> {
                    long esperaMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - momentoSubmissaoNanos);
                    maiorEsperaNaFilaMs.updateAndGet(atual -> Math.max(atual, esperaMs));
                    try {
                        Thread.sleep(TRABALHO_LENTO_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                tarefasAceitas.add(tarefa);
                aceitas++;
            } catch (RejectedExecutionException rejeicaoReal) {
                rejeitadas++;
            }
        }

        aguardarConclusao(tarefasAceitas);

        long duracaoMs = Duration.between(inicio, Instant.now()).toMillis();

        VarianteExecucao varianteExecucao = variante == VarianteThreadPool.FILA_ILIMITADA
                ? VarianteExecucao.PROBLEMATICO
                : VarianteExecucao.CORRIGIDO;

        Map<String, Object> metricas = Map.of(
                "tecnica", variante.name(),
                "quantidadeRequisicoesConcorrentes", QUANTIDADE_REQUISICOES,
                "quantidadeAceitas", aceitas,
                "quantidadeRejeitadas", rejeitadas,
                "tempoMaximoEsperaNaFilaMs", maiorEsperaNaFilaMs.get()
        );

        return new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(),
                "thread-pool-exhaustion",
                varianteExecucao,
                OrigemDados.REAL,
                inicio,
                duracaoMs,
                metricas
        );
    }

    private void aguardarConclusao(List<Future<?>> tarefas) {
        try {
            for (Future<?> tarefa : tarefas) {
                tarefa.get(10, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Execução interrompida", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Falha em tarefa do pool de demonstração", e.getCause());
        } catch (java.util.concurrent.TimeoutException e) {
            throw new IllegalStateException("Tarefa do pool de demonstração não concluiu a tempo", e);
        }
    }
}
