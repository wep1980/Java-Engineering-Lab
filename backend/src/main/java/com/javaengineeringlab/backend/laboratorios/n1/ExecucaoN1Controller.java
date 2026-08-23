package com.javaengineeringlab.backend.laboratorios.n1;

import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/laboratorios/n1-queries/execucoes")
@Tag(name = "Laboratório N+1", description = "Execução das variantes do laboratório de N+1 queries")
public class ExecucaoN1Controller {

    private final ExecucaoN1Service servico;

    public ExecucaoN1Controller(ExecucaoN1Service servico) {
        this.servico = servico;
    }

    @PostMapping("/{variante}")
    @Operation(summary = "Executa uma variante do laboratório de N+1",
            description = "variante: problematico, join-fetch, entity-graph ou dto-projection")
    public ResultadoExecucaoLaboratorio executar(@PathVariable String variante) {
        return servico.executar(VarianteN1.apartirDoSegmentoUrl(variante));
    }
}
