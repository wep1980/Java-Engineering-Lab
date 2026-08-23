package com.javaengineeringlab.backend.laboratorios.deadlock;

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
 * (threads reais), o comportamento das duas variantes do laboratório de
 * Deadlock — RNF-02 de SPEC-LAB-DEADLOCK-001-deadlock.md.
 */
@Testcontainers
@SpringBootTest
class ExecucaoDeadlockServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ExecucaoDeadlockService servico;

    @Autowired
    private SeedDadosDeadlock seedDadosDeadlock;

    @BeforeEach
    void garantirContasDemonstracao() {
        seedDadosDeadlock.garantirContasDemonstracao();
    }

    @Test
    void semOrdemConsistenteDeveProduzirUmDeadlockRealEUmaTransferenciaBemSucedida() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteDeadlock.SEM_ORDEM_CONSISTENTE);

        assertThat(resultado.metricas().get("quantidadeDeadlocksDetectados")).isEqualTo(1);
        assertThat(resultado.metricas().get("quantidadeSucesso")).isEqualTo(1);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.PROBLEMATICO);
        assertThat(resultado.origemDados()).isEqualTo(OrigemDados.REAL);

        BigDecimal saldoA = (BigDecimal) resultado.metricas().get("saldoContaA");
        BigDecimal saldoB = (BigDecimal) resultado.metricas().get("saldoContaB");
        // Exatamente uma das duas transferências (R$50) foi aplicada --
        // não sabemos de antemão qual transação o PostgreSQL escolhe
        // como vítima do deadlock, então verificamos as duas
        // possibilidades válidas, não uma vencedora específica.
        boolean transferenciaAparaBAplicada = saldoA.compareTo(BigDecimal.valueOf(450)) == 0
                && saldoB.compareTo(BigDecimal.valueOf(550)) == 0;
        boolean transferenciaBparaAAplicada = saldoA.compareTo(BigDecimal.valueOf(550)) == 0
                && saldoB.compareTo(BigDecimal.valueOf(450)) == 0;
        assertThat(transferenciaAparaBAplicada || transferenciaBparaAAplicada).isTrue();
    }

    @Test
    void ordemConsistenteNaoDeveProduzirNenhumDeadlock() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteDeadlock.ORDEM_CONSISTENTE);

        assertThat(resultado.metricas().get("quantidadeDeadlocksDetectados")).isEqualTo(0);
        assertThat(resultado.metricas().get("quantidadeSucesso")).isEqualTo(2);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.CORRIGIDO);

        // As duas transferências (A→B e B→A, mesmo valor) se cancelam.
        assertThat((BigDecimal) resultado.metricas().get("saldoContaA")).isEqualByComparingTo("500");
        assertThat((BigDecimal) resultado.metricas().get("saldoContaB")).isEqualByComparingTo("500");
    }
}
