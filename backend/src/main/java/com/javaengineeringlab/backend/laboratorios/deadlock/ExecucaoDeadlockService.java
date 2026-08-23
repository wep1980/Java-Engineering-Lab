package com.javaengineeringlab.backend.laboratorios.deadlock;

import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
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
 * Orquestra o cenário de concorrência real do laboratório de Deadlock —
 * ver specs/labs/SPEC-LAB-DEADLOCK-001-deadlock.md.
 */
@Service
public class ExecucaoDeadlockService {

    static final BigDecimal VALOR_TRANSFERENCIA = BigDecimal.valueOf(50);
    static final long ATRASO_SIMULADO_MS = 300;

    private final SeedDadosDeadlock seedDadosDeadlock;
    private final ContaBancariaDeadlockRepository repositorio;
    private final TransferenciaDeadlockOperacoes transferenciaOperacoes;

    public ExecucaoDeadlockService(
            SeedDadosDeadlock seedDadosDeadlock,
            ContaBancariaDeadlockRepository repositorio,
            TransferenciaDeadlockOperacoes transferenciaOperacoes
    ) {
        this.seedDadosDeadlock = seedDadosDeadlock;
        this.repositorio = repositorio;
        this.transferenciaOperacoes = transferenciaOperacoes;
    }

    public ResultadoExecucaoLaboratorio executar(VarianteDeadlock variante) {
        Instant inicio = Instant.now();
        SeedDadosDeadlock.ContasDemonstracao contas = seedDadosDeadlock.reiniciarContas();

        AtomicInteger sucessos = new AtomicInteger();
        AtomicInteger deadlocksDetectados = new AtomicInteger();

        Runnable transferenciaAparaB = () -> executarTransferencia(
                variante, contas.idContaA(), contas.idContaB(), sucessos, deadlocksDetectados);
        Runnable transferenciaBparaA = () -> executarTransferencia(
                variante, contas.idContaB(), contas.idContaA(), sucessos, deadlocksDetectados);

        dispararConcorrente(List.of(transferenciaAparaB, transferenciaBparaA));

        long duracaoMs = Duration.between(inicio, Instant.now()).toMillis();

        ContaBancariaDeadlock contaA = repositorio.findById(contas.idContaA()).orElseThrow();
        ContaBancariaDeadlock contaB = repositorio.findById(contas.idContaB()).orElseThrow();

        VarianteExecucao varianteExecucao = variante == VarianteDeadlock.SEM_ORDEM_CONSISTENTE
                ? VarianteExecucao.PROBLEMATICO
                : VarianteExecucao.CORRIGIDO;

        Map<String, Object> metricas = Map.of(
                "tecnica", variante.name(),
                "quantidadeTransferenciasConcorrentes", 2,
                "quantidadeSucesso", sucessos.get(),
                "quantidadeDeadlocksDetectados", deadlocksDetectados.get(),
                "saldoContaA", contaA.getSaldo(),
                "saldoContaB", contaB.getSaldo()
        );

        return new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(),
                "deadlock",
                varianteExecucao,
                OrigemDados.REAL,
                inicio,
                duracaoMs,
                metricas
        );
    }

    private void executarTransferencia(
            VarianteDeadlock variante,
            Long origemId,
            Long destinoId,
            AtomicInteger sucessos,
            AtomicInteger deadlocksDetectados
    ) {
        try {
            if (variante == VarianteDeadlock.SEM_ORDEM_CONSISTENTE) {
                transferenciaOperacoes.transferirNaOrdemDada(origemId, destinoId, VALOR_TRANSFERENCIA, ATRASO_SIMULADO_MS);
            } else {
                transferenciaOperacoes.transferirEmOrdemConsistente(origemId, destinoId, VALOR_TRANSFERENCIA, ATRASO_SIMULADO_MS);
            }
            sucessos.incrementAndGet();
        } catch (CannotAcquireLockException deadlockDetectado) {
            deadlocksDetectados.incrementAndGet();
        }
    }

    /**
     * Dispara cada operação de `operacoes` em uma thread separada,
     * liberadas simultaneamente por uma barreira de largada
     * (CountDownLatch) — mesmo padrão de ExecucaoRaceConditionService,
     * mas com uma operação distinta por thread (não a repetição da
     * mesma).
     */
    private void dispararConcorrente(List<Runnable> operacoes) {
        CountDownLatch largada = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(operacoes.size())) {
            List<Future<Object>> tarefas = operacoes.stream()
                    .map(operacao -> executor.submit(() -> {
                        largada.await();
                        operacao.run();
                        return null;
                    }))
                    .toList();

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
