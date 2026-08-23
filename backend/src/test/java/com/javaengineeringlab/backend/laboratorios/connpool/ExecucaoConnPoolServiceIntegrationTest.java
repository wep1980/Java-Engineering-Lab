package com.javaengineeringlab.backend.laboratorios.connpool;

import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valida, contra PostgreSQL real (Testcontainers) e concorrência real
 * (threads reais), o comportamento das três variantes do laboratório de
 * Connection Pool Exhaustion — RNF-03 de
 * SPEC-LAB-CONN-POOL-001-connection-pool-exhaustion.md.
 */
@Testcontainers
@SpringBootTest
class ExecucaoConnPoolServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ExecucaoConnPoolService servico;

    @Test
    void poolPequenoDeveProduzirFalhasReaisPorTimeout() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteConnPool.POOL_PEQUENO);

        int sucessos = (int) resultado.metricas().get("quantidadeSucesso");
        int falhas = (int) resultado.metricas().get("quantidadeFalhasPorTimeout");

        assertThat(falhas).isGreaterThan(0);
        assertThat(sucessos + falhas).isEqualTo(ExecucaoConnPoolService.QUANTIDADE_REQUISICOES_CONCORRENTES);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.PROBLEMATICO);
        assertThat(resultado.origemDados()).isEqualTo(OrigemDados.REAL);
    }

    @Test
    void poolRedimensionadoNaoDeveProduzirFalhas() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteConnPool.POOL_REDIMENSIONADO);

        assertThat(resultado.metricas().get("quantidadeFalhasPorTimeout")).isEqualTo(0);
        assertThat(resultado.metricas().get("quantidadeSucesso"))
                .isEqualTo(ExecucaoConnPoolService.QUANTIDADE_REQUISICOES_CONCORRENTES);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.CORRIGIDO);
    }

    @Test
    void conexaoCurtaNaoDeveProduzirFalhasMesmoComPoolPequeno() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteConnPool.CONEXAO_CURTA);

        assertThat(resultado.metricas().get("quantidadeFalhasPorTimeout")).isEqualTo(0);
        assertThat(resultado.metricas().get("quantidadeSucesso"))
                .isEqualTo(ExecucaoConnPoolService.QUANTIDADE_REQUISICOES_CONCORRENTES);
        assertThat(resultado.metricas().get("tamanhoDoPool")).isEqualTo(ExecucaoConnPoolService.TAMANHO_POOL_PEQUENO);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.CORRIGIDO);
    }
}
