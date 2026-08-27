package com.javaengineeringlab.backend.laboratorios.threadpool;

import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Nenhuma infraestrutura externa envolvida (só {@code java.util.concurrent}
 * real) -- teste de unidade direto, sem Testcontainers. Ver RNF-02 de
 * SPEC-LAB-THREADPOOL-001-thread-pool-exhaustion.md.
 */
class ExecucaoThreadPoolServiceTest {

    private final ExecucaoThreadPoolService servico = new ExecucaoThreadPoolService();

    @Test
    void filaIlimitadaDeveAceitarTodasAsTarefasEAUltimaEsperarNaFila() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteThreadPool.FILA_ILIMITADA);

        assertThat(resultado.metricas().get("quantidadeAceitas")).isEqualTo(ExecucaoThreadPoolService.QUANTIDADE_REQUISICOES);
        assertThat(resultado.metricas().get("quantidadeRejeitadas")).isEqualTo(0);
        assertThat((long) resultado.metricas().get("tempoMaximoEsperaNaFilaMs"))
                .isGreaterThan(ExecucaoThreadPoolService.TRABALHO_LENTO_MS);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.PROBLEMATICO);
        assertThat(resultado.origemDados()).isEqualTo(OrigemDados.REAL);
    }

    @Test
    void filaLimitadaDeveAceitarSoQuatroERejeitarSeis() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteThreadPool.FILA_LIMITADA);

        int aceitasEsperadas = ExecucaoThreadPoolService.TAMANHO_POOL + ExecucaoThreadPoolService.CAPACIDADE_FILA_LIMITADA;
        assertThat(resultado.metricas().get("quantidadeAceitas")).isEqualTo(aceitasEsperadas);
        assertThat(resultado.metricas().get("quantidadeRejeitadas"))
                .isEqualTo(ExecucaoThreadPoolService.QUANTIDADE_REQUISICOES - aceitasEsperadas);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.CORRIGIDO);
    }
}
