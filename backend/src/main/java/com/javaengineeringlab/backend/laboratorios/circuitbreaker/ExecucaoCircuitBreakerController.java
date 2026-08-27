package com.javaengineeringlab.backend.laboratorios.circuitbreaker;

import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/laboratorios/circuit-breaker/execucoes")
@Tag(name = "Laboratório Circuit Breaker", description = "Execução das variantes do laboratório de circuit breaker")
public class ExecucaoCircuitBreakerController {

    private final ExecucaoCircuitBreakerService servico;

    public ExecucaoCircuitBreakerController(ExecucaoCircuitBreakerService servico) {
        this.servico = servico;
    }

    @PostMapping("/{variante}")
    @Operation(summary = "Executa uma variante do laboratório de Circuit Breaker",
            description = "variante: sem-circuit-breaker ou com-circuit-breaker")
    public ResultadoExecucaoLaboratorio executar(@PathVariable String variante) {
        return servico.executar(VarianteCircuitBreaker.apartirDoSegmentoUrl(variante));
    }
}
