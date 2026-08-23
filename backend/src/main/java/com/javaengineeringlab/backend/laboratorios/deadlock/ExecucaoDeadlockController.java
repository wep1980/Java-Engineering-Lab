package com.javaengineeringlab.backend.laboratorios.deadlock;

import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/laboratorios/deadlock/execucoes")
@Tag(name = "Laboratório Deadlock", description = "Execução das variantes do laboratório de deadlock real de banco de dados")
public class ExecucaoDeadlockController {

    private final ExecucaoDeadlockService servico;

    public ExecucaoDeadlockController(ExecucaoDeadlockService servico) {
        this.servico = servico;
    }

    @PostMapping("/{variante}")
    @Operation(summary = "Executa uma variante do laboratório de Deadlock",
            description = "variante: sem-ordem-consistente ou ordem-consistente")
    public ResultadoExecucaoLaboratorio executar(@PathVariable String variante) {
        return servico.executar(VarianteDeadlock.apartirDoSegmentoUrl(variante));
    }
}
