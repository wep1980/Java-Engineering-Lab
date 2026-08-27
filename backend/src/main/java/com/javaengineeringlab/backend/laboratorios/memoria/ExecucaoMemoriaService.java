package com.javaengineeringlab.backend.laboratorios.memoria;

import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Orquestra as duas variantes do laboratório de Memory Leak -- ver
 * specs/labs/SPEC-LAB-MEMLEAK-001-memory-leak.md. Mede heap real via
 * {@link MemoryMXBean}, nunca provoca um OutOfMemoryError de verdade
 * -- ver "Decisão deliberada de segurança" na SPEC.
 */
@Service
public class ExecucaoMemoriaService {

    static final int QUANTIDADE_ENTRADAS = 200;
    static final int TAMANHO_BYTES_POR_ENTRADA = 100_000; // ~20 MB no total
    private static final long LIMIAR_VAZAMENTO_BYTES = (long) QUANTIDADE_ENTRADAS * TAMANHO_BYTES_POR_ENTRADA / 2;

    private final CacheComVazamento cacheComVazamento;
    private final CacheSemVazamento cacheSemVazamento;
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    public ExecucaoMemoriaService(CacheComVazamento cacheComVazamento, CacheSemVazamento cacheSemVazamento) {
        this.cacheComVazamento = cacheComVazamento;
        this.cacheSemVazamento = cacheSemVazamento;
    }

    public ResultadoExecucaoLaboratorio executar(VarianteMemoria variante) {
        Instant inicio = Instant.now();

        // GC também antes de medir a linha de base -- achado real: sem
        // isso, heapAntesBytes inclui lixo geral da JVM ainda não
        // coletado (execuções de teste anteriores, overhead do
        // framework, etc.), e esse lixo geral some na medição seguinte
        // (que já roda depois de um GC), mascarando o crescimento
        // realmente retido pela cache -- as duas medições precisam
        // partir do mesmo estado "assentado" pós-GC para a diferença
        // refletir só o que este método alocou.
        forcarColetaDeLixo();
        long heapAntesBytes = heapUsadoReal();

        if (variante == VarianteMemoria.COM_VAZAMENTO) {
            cacheComVazamento.adicionar(QUANTIDADE_ENTRADAS, TAMANHO_BYTES_POR_ENTRADA);
        } else {
            cacheSemVazamento.adicionar(QUANTIDADE_ENTRADAS, TAMANHO_BYTES_POR_ENTRADA);
        }

        // Primeiro GC: limpa as referências fracas das chaves já
        // inalcançáveis (variante sem-vazamento) -- mas os VALORES
        // (os byte[]) continuam presos até a entrada morta ser
        // efetivamente removida da tabela interna do WeakHashMap.
        forcarColetaDeLixo();

        // Achado real: WeakHashMap só expurga entradas com chave já
        // coletada durante uma operação real no mapa (size(), get(),
        // put()...), não sozinho. Sem chamar tamanho() ANTES da
        // medição final, os valores ainda apareciam presos no heap
        // mesmo com as chaves já coletadas -- o primeiro teste real
        // mostrou os ~20MB inteiros "retidos" mesmo na variante
        // corrigida. Ver "Achados reais" na SPEC.
        int tamanhoCacheAposExecucao = variante == VarianteMemoria.COM_VAZAMENTO
                ? cacheComVazamento.tamanho()
                : cacheSemVazamento.tamanho();

        // Segundo GC, depois do expurgo: agora os valores das entradas
        // mortas estão de fato inalcançáveis e podem ser coletados.
        forcarColetaDeLixo();
        long heapDepoisBytes = heapUsadoReal();

        long crescimentoRetidoBytes = Math.max(0, heapDepoisBytes - heapAntesBytes);
        boolean vazamentoDetectado = crescimentoRetidoBytes > LIMIAR_VAZAMENTO_BYTES;

        long duracaoMs = Duration.between(inicio, Instant.now()).toMillis();

        VarianteExecucao varianteExecucao = variante == VarianteMemoria.COM_VAZAMENTO
                ? VarianteExecucao.PROBLEMATICO
                : VarianteExecucao.CORRIGIDO;

        Map<String, Object> metricas = Map.of(
                "tecnica", variante.name(),
                "heapAntesBytes", heapAntesBytes,
                "heapDepoisBytes", heapDepoisBytes,
                "crescimentoRetidoBytes", crescimentoRetidoBytes,
                "tamanhoCacheAposExecucao", tamanhoCacheAposExecucao,
                "vazamentoDetectado", vazamentoDetectado
        );

        return new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(),
                "memory-leak",
                varianteExecucao,
                OrigemDados.REAL,
                inicio,
                duracaoMs,
                metricas
        );
    }

    private long heapUsadoReal() {
        return memoryMXBean.getHeapMemoryUsage().getUsed();
    }

    /**
     * {@code System.gc()} é um pedido, não uma garantia da JVM -- a
     * chamada dupla com uma pequena espera entre elas é uma técnica
     * pragmática para dar tempo real do coletor concluir antes da
     * medição seguinte. Ver "System.gc() real, com tolerância
     * documentada" na SPEC.
     */
    private void forcarColetaDeLixo() {
        System.gc();
        aguardarBrevemente();
        System.gc();
        aguardarBrevemente();
    }

    private void aguardarBrevemente() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
