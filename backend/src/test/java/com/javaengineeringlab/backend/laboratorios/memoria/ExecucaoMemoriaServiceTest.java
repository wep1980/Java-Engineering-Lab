package com.javaengineeringlab.backend.laboratorios.memoria;

import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Nenhuma infraestrutura externa envolvida (só heap e GC reais da
 * própria JVM rodando o teste) -- teste de unidade direto, sem
 * Testcontainers. Margens de tolerância generosas, não igualdade
 * exata -- {@code System.gc()} é um pedido, não uma garantia da JVM,
 * ver "Riscos" em specs/labs/SPEC-LAB-MEMLEAK-001-memory-leak.md.
 */
class ExecucaoMemoriaServiceTest {

    private final ExecucaoMemoriaService servico =
            new ExecucaoMemoriaService(new CacheComVazamento(), new CacheSemVazamento());

    @Test
    void comVazamentoDeveReterAMaiorParteDoAlocadoAposGcReal() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteMemoria.COM_VAZAMENTO);

        long totalAlocado = (long) ExecucaoMemoriaService.QUANTIDADE_ENTRADAS * ExecucaoMemoriaService.TAMANHO_BYTES_POR_ENTRADA;
        long crescimentoRetido = (long) resultado.metricas().get("crescimentoRetidoBytes");

        assertThat(crescimentoRetido).isGreaterThan(totalAlocado / 2);
        assertThat(resultado.metricas().get("vazamentoDetectado")).isEqualTo(true);
        assertThat((int) resultado.metricas().get("tamanhoCacheAposExecucao"))
                .isGreaterThanOrEqualTo(ExecucaoMemoriaService.QUANTIDADE_ENTRADAS);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.PROBLEMATICO);
        assertThat(resultado.origemDados()).isEqualTo(OrigemDados.REAL);
    }

    @Test
    void semVazamentoDeveRecuperarAMaiorParteDoAlocadoAposGcReal() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteMemoria.SEM_VAZAMENTO);

        long totalAlocado = (long) ExecucaoMemoriaService.QUANTIDADE_ENTRADAS * ExecucaoMemoriaService.TAMANHO_BYTES_POR_ENTRADA;
        long crescimentoRetido = (long) resultado.metricas().get("crescimentoRetidoBytes");

        assertThat(crescimentoRetido).isLessThan(totalAlocado / 2);
        assertThat(resultado.metricas().get("vazamentoDetectado")).isEqualTo(false);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.CORRIGIDO);
    }
}
