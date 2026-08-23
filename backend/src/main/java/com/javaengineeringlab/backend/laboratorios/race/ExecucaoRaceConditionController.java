package com.javaengineeringlab.backend.laboratorios.race;

import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/laboratorios/race-condition/execucoes")
@Tag(name = "Laboratório Race Condition", description = "Execução das variantes do laboratório de Lost Update / Race Condition")
public class ExecucaoRaceConditionController {

    private final ExecucaoRaceConditionService servico;

    public ExecucaoRaceConditionController(ExecucaoRaceConditionService servico) {
        this.servico = servico;
    }

    @PostMapping("/{variante}")
    @Operation(summary = "Executa uma variante do laboratório de Race Condition",
            description = "variante: sem-controle, otimista ou pessimista")
    public ResultadoExecucaoLaboratorio executar(@PathVariable String variante) {
        return servico.executar(VarianteRace.apartirDoSegmentoUrl(variante));
    }
}
