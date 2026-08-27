package com.javaengineeringlab.backend.laboratorios.circuitbreaker;

import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Orquestra as duas variantes do laboratório de Circuit Breaker -- ver
 * specs/labs/SPEC-LAB-CIRCUITBREAKER-001-circuit-breaker.md.
 *
 * <p>O {@link CircuitBreaker} é construído manualmente aqui a partir do
 * módulo núcleo {@code resilience4j-circuitbreaker} -- de propósito,
 * sem usar o módulo {@code resilience4j-spring-boot3} e sua
 * autoconfiguração própria, pelo mesmo motivo (risco de
 * autoconfiguração de terceiros sob Spring Boot 4.1) já documentado em
 * ADR-0009 para os pools de demonstração do laboratório de Connection
 * Pool Exhaustion. Ver "Design técnico" na SPEC.
 */
@Service
public class ExecucaoCircuitBreakerService {

    static final int QUANTIDADE_CHAMADAS = 20;
    static final int TAMANHO_JANELA = 10;
    static final int MINIMO_CHAMADAS_ANTES_DE_CALCULAR = 5;
    static final float LIMITE_TAXA_FALHA_PERCENTUAL = 50.0f;

    private final DependenciaExternaInstavel dependencia;
    private final CircuitBreaker circuitBreaker;

    public ExecucaoCircuitBreakerService(DependenciaExternaInstavel dependencia) {
        this.dependencia = dependencia;

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(TAMANHO_JANELA)
                .minimumNumberOfCalls(MINIMO_CHAMADAS_ANTES_DE_CALCULAR)
                .failureRateThreshold(LIMITE_TAXA_FALHA_PERCENTUAL)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();

        this.circuitBreaker = CircuitBreaker.of("dependencia-instavel", config);
    }

    public ResultadoExecucaoLaboratorio executar(VarianteCircuitBreaker variante) {
        // reinicia o circuito para CLOSED a cada execução -- API real
        // do Resilience4j, garante que cada clique no laboratório
        // parte do mesmo estado inicial (RF-03 da SPEC).
        circuitBreaker.reset();

        Instant inicio = Instant.now();

        int sucessos = 0;
        int falhasReais = 0;
        int rejeitadasPeloCircuito = 0;

        for (int i = 0; i < QUANTIDADE_CHAMADAS; i++) {
            try {
                if (variante == VarianteCircuitBreaker.COM_CIRCUIT_BREAKER) {
                    circuitBreaker.executeSupplier(dependencia::chamar);
                } else {
                    dependencia.chamar();
                }
                sucessos++;
            } catch (CallNotPermittedException chamadaRejeitadaPeloCircuito) {
                rejeitadasPeloCircuito++;
            } catch (DependenciaExternaIndisponivelException falhaRealDaDependencia) {
                falhasReais++;
            }
        }

        long duracaoMs = Duration.between(inicio, Instant.now()).toMillis();

        VarianteExecucao varianteExecucao = variante == VarianteCircuitBreaker.SEM_CIRCUIT_BREAKER
                ? VarianteExecucao.PROBLEMATICO
                : VarianteExecucao.CORRIGIDO;

        String estadoFinalDoCircuito = variante == VarianteCircuitBreaker.COM_CIRCUIT_BREAKER
                ? circuitBreaker.getState().name()
                : "DESABILITADO";

        Map<String, Object> metricas = Map.of(
                "tecnica", variante.name(),
                "quantidadeChamadas", QUANTIDADE_CHAMADAS,
                "quantidadeSucesso", sucessos,
                "quantidadeFalhasReais", falhasReais,
                "quantidadeRejeitadasPeloCircuito", rejeitadasPeloCircuito,
                "estadoFinalDoCircuito", estadoFinalDoCircuito
        );

        return new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(),
                "circuit-breaker",
                varianteExecucao,
                OrigemDados.REAL,
                inicio,
                duracaoMs,
                metricas
        );
    }
}
