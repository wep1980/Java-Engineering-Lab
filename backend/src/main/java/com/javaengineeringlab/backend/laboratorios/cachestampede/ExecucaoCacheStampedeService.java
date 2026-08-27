package com.javaengineeringlab.backend.laboratorios.cachestampede;

import com.javaengineeringlab.backend.plataforma.LaboratorioIndisponivelException;
import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
 * Orquestra o cenário de concorrência real do laboratório de Cache
 * Stampede -- ver specs/labs/SPEC-LAB-CACHESTAMPEDE-001-cache-stampede.md.
 *
 * <p>A correção usa um lock distribuído real no Redis
 * ({@code SETNX}/{@code SET ... NX PX} via
 * {@link org.springframework.data.redis.core.ValueOperations#setIfAbsent},
 * uma operação atômica real do Redis) -- não uma coordenação em
 * memória da própria JVM. Ver "Lock distribuído via Redis" na SPEC.
 */
@Service
public class ExecucaoCacheStampedeService {

    static final int QUANTIDADE_REQUISICOES_CONCORRENTES = 10;
    static final long TRABALHO_LENTO_MS = 500;
    private static final String PREFIXO_CHAVE = "cache-stampede:valor:";
    private static final String PREFIXO_LOCK = "cache-stampede:lock:";
    private static final Duration TTL_CACHE = Duration.ofSeconds(30);
    private static final Duration TTL_LOCK = Duration.ofSeconds(2);
    private static final long INTERVALO_POLL_MS = 50;
    private static final long TIMEOUT_ESPERA_CACHE_MS = 3000;

    private final StringRedisTemplate redisTemplate;

    public ExecucaoCacheStampedeService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public ResultadoExecucaoLaboratorio executar(VarianteCacheStampede variante) {
        Instant inicio = Instant.now();

        // Chave nova a cada execução -- garante cache frio real, sem
        // depender de esperar um TTL expirar.
        String chave = PREFIXO_CHAVE + UUID.randomUUID();
        AtomicInteger acessosAoRecursoLento = new AtomicInteger();

        try {
            dispararConcorrente(() -> {
                if (variante == VarianteCacheStampede.SEM_PROTECAO) {
                    buscarSemProtecao(chave, acessosAoRecursoLento);
                } else {
                    buscarComProtecao(chave, acessosAoRecursoLento);
                }
            });
        } catch (DataAccessException falhaRealDeConexao) {
            throw new LaboratorioIndisponivelException(
                    "Redis indisponível -- verifique se o profile 'cache' do docker-compose está no ar.");
        }

        long duracaoMs = Duration.between(inicio, Instant.now()).toMillis();

        VarianteExecucao varianteExecucao = variante == VarianteCacheStampede.SEM_PROTECAO
                ? VarianteExecucao.PROBLEMATICO
                : VarianteExecucao.CORRIGIDO;

        Map<String, Object> metricas = Map.of(
                "tecnica", variante.name(),
                "quantidadeRequisicoesConcorrentes", QUANTIDADE_REQUISICOES_CONCORRENTES,
                "quantidadeAcessosAoRecursoLentoReal", acessosAoRecursoLento.get()
        );

        return new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(),
                "cache-stampede",
                varianteExecucao,
                OrigemDados.REAL,
                inicio,
                duracaoMs,
                metricas
        );
    }

    /**
     * Sem nenhuma coordenação: toda requisição que encontra a chave
     * fria vai direto para o recurso lento.
     */
    private void buscarSemProtecao(String chave, AtomicInteger contador) {
        String valorEmCache = redisTemplate.opsForValue().get(chave);
        if (valorEmCache != null) {
            return;
        }
        String valorReal = buscarDoRecursoLento(contador);
        redisTemplate.opsForValue().set(chave, valorReal, TTL_CACHE);
    }

    /**
     * Disputa um lock distribuído real no Redis -- só quem consegue
     * acessa o recurso lento; as demais aguardam o cache ser
     * populado pela vencedora.
     */
    private void buscarComProtecao(String chave, AtomicInteger contador) {
        String valorEmCache = redisTemplate.opsForValue().get(chave);
        if (valorEmCache != null) {
            return;
        }

        String chaveLock = PREFIXO_LOCK + chave;
        Boolean adquiriuLock = redisTemplate.opsForValue().setIfAbsent(chaveLock, "1", TTL_LOCK);

        if (Boolean.TRUE.equals(adquiriuLock)) {
            String valorReal = buscarDoRecursoLento(contador);
            redisTemplate.opsForValue().set(chave, valorReal, TTL_CACHE);
            redisTemplate.delete(chaveLock);
        } else {
            aguardarCachePopulado(chave);
        }
    }

    private String buscarDoRecursoLento(AtomicInteger contador) {
        contador.incrementAndGet();
        try {
            Thread.sleep(TRABALHO_LENTO_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Execução interrompida", e);
        }
        return "valor-real-" + UUID.randomUUID();
    }

    private void aguardarCachePopulado(String chave) {
        long limite = System.currentTimeMillis() + TIMEOUT_ESPERA_CACHE_MS;
        while (System.currentTimeMillis() < limite) {
            if (redisTemplate.opsForValue().get(chave) != null) {
                return;
            }
            try {
                Thread.sleep(INTERVALO_POLL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Execução interrompida", e);
            }
        }
        throw new LaboratorioIndisponivelException(
                "Timeout aguardando o cache ser populado -- verifique se o profile 'cache' do docker-compose está no ar.");
    }

    /**
     * Dispara `QUANTIDADE_REQUISICOES_CONCORRENTES` execuções de
     * `operacao` em threads separadas, liberadas simultaneamente por
     * uma barreira de largada (CountDownLatch) -- mesmo padrão de
     * ExecucaoConnPoolService.
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
            if (e.getCause() instanceof DataAccessException dataAccessException) {
                throw dataAccessException;
            }
            throw new IllegalStateException("Falha em execução concorrente", e.getCause());
        }
    }
}
