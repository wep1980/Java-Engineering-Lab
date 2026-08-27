package com.javaengineeringlab.backend.laboratorios.saga;

import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/laboratorios/saga/execucoes")
@Tag(name = "Laboratório Saga", description = "Execução das variantes do laboratório de saga")
public class ExecucaoSagaController {

    private final ExecucaoSagaService servico;

    public ExecucaoSagaController(ExecucaoSagaService servico) {
        this.servico = servico;
    }

    @PostMapping("/{variante}")
    @Operation(summary = "Executa uma variante do laboratório de Saga",
            description = "variante: sem-compensacao ou com-compensacao")
    public ResultadoExecucaoLaboratorio executar(@PathVariable String variante) {
        return servico.executar(VarianteSaga.apartirDoSegmentoUrl(variante));
    }
}
