package com.javaengineeringlab.backend.laboratorios.circuitbreaker;

import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Nenhuma infraestrutura externa envolvida neste laboratório (sem
 * PostgreSQL, sem Kafka) -- teste de unidade direto, sem
 * Testcontainers. Valida o comportamento real da máquina de estados
 * do Resilience4j -- RNF-03 de
 * SPEC-LAB-CIRCUITBREAKER-001-circuit-breaker.md.
 */
class ExecucaoCircuitBreakerServiceTest {

    private final ExecucaoCircuitBreakerService servico =
            new ExecucaoCircuitBreakerService(new DependenciaExternaInstavel());

    @Test
    void semCircuitBreakerDeveFalharEmTodasAsChamadas() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteCircuitBreaker.SEM_CIRCUIT_BREAKER);

        assertThat(resultado.metricas().get("quantidadeChamadas")).isEqualTo(ExecucaoCircuitBreakerService.QUANTIDADE_CHAMADAS);
        assertThat(resultado.metricas().get("quantidadeSucesso")).isEqualTo(0);
        assertThat(resultado.metricas().get("quantidadeFalhasReais")).isEqualTo(ExecucaoCircuitBreakerService.QUANTIDADE_CHAMADAS);
        assertThat(resultado.metricas().get("quantidadeRejeitadasPeloCircuito")).isEqualTo(0);
        assertThat(resultado.metricas().get("estadoFinalDoCircuito")).isEqualTo("DESABILITADO");
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.PROBLEMATICO);
        assertThat(resultado.origemDados()).isEqualTo(OrigemDados.REAL);
    }

    @Test
    void comCircuitBreakerDeveAbrirOCircuitoEPararDeChamarADependencia() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteCircuitBreaker.COM_CIRCUIT_BREAKER);

        assertThat(resultado.metricas().get("quantidadeSucesso")).isEqualTo(0);
        assertThat(resultado.metricas().get("quantidadeFalhasReais")).isEqualTo(ExecucaoCircuitBreakerService.MINIMO_CHAMADAS_ANTES_DE_CALCULAR);
        assertThat(resultado.metricas().get("quantidadeRejeitadasPeloCircuito"))
                .isEqualTo(ExecucaoCircuitBreakerService.QUANTIDADE_CHAMADAS - ExecucaoCircuitBreakerService.MINIMO_CHAMADAS_ANTES_DE_CALCULAR);
        assertThat(resultado.metricas().get("estadoFinalDoCircuito")).isEqualTo("OPEN");
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.CORRIGIDO);
    }
}
