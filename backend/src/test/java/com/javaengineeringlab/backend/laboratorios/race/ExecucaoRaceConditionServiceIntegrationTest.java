package com.javaengineeringlab.backend.laboratorios.race;

import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valida, contra PostgreSQL real (Testcontainers) e concorrência real
 * (threads reais), o comportamento das três variantes do laboratório de
 * Race Condition — RNF-02 de SPEC-LAB-RACE-001-race-condition-lost-update.md.
 */
@Testcontainers
@SpringBootTest
class ExecucaoRaceConditionServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ExecucaoRaceConditionService servico;

    @Autowired
    private SeedDadosRace seedDadosRace;

    private static final BigDecimal SALDO_ESPERADO = ExecucaoRaceConditionService.VALOR_POR_DEPOSITO
            .multiply(BigDecimal.valueOf(ExecucaoRaceConditionService.QUANTIDADE_REQUISICOES_CONCORRENTES));

    @BeforeEach
    void garantirContasDemonstracao() {
        seedDadosRace.garantirContasDemonstracao();
    }

    @Test
    void semControleDevePerderAtualizacoesDeFormaDeterministica() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteRace.SEM_CONTROLE);

        // Todas as 10 threads leem saldo=0 antes de qualquer escrita
        // (espera artificial >> variância de agendamento), então o saldo
        // final é exatamente 1 depósito "efetivo" -- os outros 9 se perdem.
        assertThat((BigDecimal) resultado.metricas().get("saldoFinal")).isEqualByComparingTo("100");
        assertThat(resultado.metricas().get("atualizacoesPerdidas")).isEqualTo(9);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.PROBLEMATICO);
        assertThat(resultado.origemDados()).isEqualTo(OrigemDados.REAL);
    }

    @Test
    void otimistaDeveChegarAoSaldoCorretoComConflitosDetectados() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteRace.OTIMISTA);

        assertThat((BigDecimal) resultado.metricas().get("saldoFinal")).isEqualByComparingTo(SALDO_ESPERADO);
        assertThat(resultado.metricas().get("atualizacoesPerdidas")).isEqualTo(0);
        assertThat((Integer) resultado.metricas().get("conflitosDetectadosERetentados")).isGreaterThan(0);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.CORRIGIDO);
    }

    @Test
    void pessimistaDeveChegarAoSaldoCorretoSemConflitos() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteRace.PESSIMISTA);

        assertThat((BigDecimal) resultado.metricas().get("saldoFinal")).isEqualByComparingTo(SALDO_ESPERADO);
        assertThat(resultado.metricas().get("atualizacoesPerdidas")).isEqualTo(0);
        assertThat(resultado.metricas().get("conflitosDetectadosERetentados")).isEqualTo(0);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.CORRIGIDO);
    }
}
