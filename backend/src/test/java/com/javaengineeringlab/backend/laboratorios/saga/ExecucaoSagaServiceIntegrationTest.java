package com.javaengineeringlab.backend.laboratorios.saga;

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
 * Valida, contra PostgreSQL real (Testcontainers), o comportamento das
 * duas variantes do laboratório de Saga -- RNF-02 de
 * SPEC-LAB-SAGA-001-saga.md.
 */
@Testcontainers
@SpringBootTest
class ExecucaoSagaServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ExecucaoSagaService servico;

    @Test
    void semCompensacaoDeveDeixarReservaPresaEInconsistente() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteSaga.SEM_COMPENSACAO);

        assertThat(resultado.metricas().get("estoqueReservado")).isEqualTo(true);
        assertThat(resultado.metricas().get("pagamentoAprovado")).isEqualTo(false);
        assertThat(resultado.metricas().get("compensacaoExecutada")).isEqualTo(false);
        assertThat(resultado.metricas().get("estoqueConsistente")).isEqualTo(false);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.PROBLEMATICO);
        assertThat(resultado.origemDados()).isEqualTo(OrigemDados.REAL);
    }

    @Test
    void comCompensacaoDeveDesfazerAReservaEFicarConsistente() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteSaga.COM_COMPENSACAO);

        assertThat(resultado.metricas().get("estoqueReservado")).isEqualTo(true);
        assertThat(resultado.metricas().get("pagamentoAprovado")).isEqualTo(false);
        assertThat(resultado.metricas().get("compensacaoExecutada")).isEqualTo(true);
        assertThat(resultado.metricas().get("estoqueConsistente")).isEqualTo(true);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.CORRIGIDO);
    }
}
