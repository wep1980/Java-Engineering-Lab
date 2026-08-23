package com.javaengineeringlab.backend.laboratorios.race;

import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
 * Orquestra o cenário de concorrência real do laboratório de Race
 * Condition — ver specs/labs/SPEC-LAB-RACE-001-race-condition-lost-update.md.
 */
@Service
public class ExecucaoRaceConditionService {

    static final int QUANTIDADE_REQUISICOES_CONCORRENTES = 10;
    static final BigDecimal VALOR_POR_DEPOSITO = BigDecimal.valueOf(100);
    static final long ATRASO_SIMULADO_MS = 100;
    private static final int MAX_TENTATIVAS_OTIMISTA = 20;

    private final SeedDadosRace seedDadosRace;
    private final ContaBancariaRepository contaBancariaRepository;
    private final ContaBancariaVersionadaRepository contaBancariaVersionadaRepository;
    private final ContaBancariaSemControleOperacoes semControleOperacoes;
    private final ContaBancariaVersionadaOperacoes versionadaOperacoes;
    private final ContaBancariaPessimistaOperacoes pessimistaOperacoes;

    public ExecucaoRaceConditionService(
            SeedDadosRace seedDadosRace,
            ContaBancariaRepository contaBancariaRepository,
            ContaBancariaVersionadaRepository contaBancariaVersionadaRepository,
            ContaBancariaSemControleOperacoes semControleOperacoes,
            ContaBancariaVersionadaOperacoes versionadaOperacoes,
            ContaBancariaPessimistaOperacoes pessimistaOperacoes
    ) {
        this.seedDadosRace = seedDadosRace;
        this.contaBancariaRepository = contaBancariaRepository;
        this.contaBancariaVersionadaRepository = contaBancariaVersionadaRepository;
        this.semControleOperacoes = semControleOperacoes;
        this.versionadaOperacoes = versionadaOperacoes;
        this.pessimistaOperacoes = pessimistaOperacoes;
    }

    public ResultadoExecucaoLaboratorio executar(VarianteRace variante) {
        Instant inicio = Instant.now();
        BigDecimal saldoFinal;
        int conflitosDetectados = 0;

        switch (variante) {
            case SEM_CONTROLE -> {
                Long contaId = seedDadosRace.reiniciarContaSemVersionamento();
                dispararConcorrente(() -> semControleOperacoes.depositar(contaId, VALOR_POR_DEPOSITO, ATRASO_SIMULADO_MS));
                saldoFinal = contaBancariaRepository.findById(contaId).orElseThrow().getSaldo();
            }
            case OTIMISTA -> {
                Long contaId = seedDadosRace.reiniciarContaVersionada();
                AtomicInteger conflitos = new AtomicInteger();
                dispararConcorrente(() -> depositarComRetentativa(contaId, conflitos));
                conflitosDetectados = conflitos.get();
                saldoFinal = contaBancariaVersionadaRepository.findById(contaId).orElseThrow().getSaldo();
            }
            case PESSIMISTA -> {
                Long contaId = seedDadosRace.reiniciarContaSemVersionamento();
                dispararConcorrente(() -> pessimistaOperacoes.depositar(contaId, VALOR_POR_DEPOSITO, ATRASO_SIMULADO_MS));
                saldoFinal = contaBancariaRepository.findById(contaId).orElseThrow().getSaldo();
            }
            default -> throw new IllegalStateException("Variante não tratada: " + variante);
        }

        long duracaoMs = Duration.between(inicio, Instant.now()).toMillis();
        BigDecimal saldoEsperado = VALOR_POR_DEPOSITO.multiply(BigDecimal.valueOf(QUANTIDADE_REQUISICOES_CONCORRENTES));
        int atualizacoesPerdidas = saldoEsperado.subtract(saldoFinal)
                .divide(VALOR_POR_DEPOSITO, java.math.RoundingMode.UNNECESSARY)
                .intValue();

        VarianteExecucao varianteExecucao = variante == VarianteRace.SEM_CONTROLE
                ? VarianteExecucao.PROBLEMATICO
                : VarianteExecucao.CORRIGIDO;

        Map<String, Object> metricas = Map.of(
                "tecnica", variante.name(),
                "quantidadeRequisicoesConcorrentes", QUANTIDADE_REQUISICOES_CONCORRENTES,
                "saldoEsperado", saldoEsperado,
                "saldoFinal", saldoFinal,
                "atualizacoesPerdidas", atualizacoesPerdidas,
                "conflitosDetectadosERetentados", conflitosDetectados
        );

        return new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(),
                "race-condition",
                varianteExecucao,
                OrigemDados.REAL,
                inicio,
                duracaoMs,
                metricas
        );
    }

    private void depositarComRetentativa(Long contaId, AtomicInteger conflitosDetectados) {
        long atraso = ATRASO_SIMULADO_MS;
        for (int tentativa = 0; tentativa < MAX_TENTATIVAS_OTIMISTA; tentativa++) {
            try {
                versionadaOperacoes.depositar(contaId, VALOR_POR_DEPOSITO, atraso);
                return;
            } catch (ObjectOptimisticLockingFailureException conflito) {
                conflitosDetectados.incrementAndGet();
                atraso = 0; // retentativas não repetem a espera artificial
            }
        }
        throw new IllegalStateException(
                "Número máximo de tentativas excedido para depósito otimista na conta " + contaId);
    }

    /**
     * Dispara `QUANTIDADE_REQUISICOES_CONCORRENTES` execuções de
     * `operacao` em threads separadas, liberadas simultaneamente por uma
     * barreira de largada (CountDownLatch) — concorrência real, não
     * chamadas sequenciais.
     */
    private void dispararConcorrente(Runnable operacao) {
        ExecutorService executor = Executors.newFixedThreadPool(QUANTIDADE_REQUISICOES_CONCORRENTES);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<?>> tarefas = new ArrayList<>();

        try {
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
        } finally {
            executor.shutdown();
        }
    }
}
