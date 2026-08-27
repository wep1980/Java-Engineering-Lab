package com.javaengineeringlab.backend.laboratorios.outbox;

import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/laboratorios/transactional-outbox/execucoes")
@Tag(name = "Laboratório Transactional Outbox", description = "Execução das variantes do laboratório de outbox transacional")
public class ExecucaoOutboxController {

    private final ExecucaoOutboxService servico;

    public ExecucaoOutboxController(ExecucaoOutboxService servico) {
        this.servico = servico;
    }

    @PostMapping("/{variante}")
    @Operation(summary = "Executa uma variante do laboratório de Transactional Outbox",
            description = "variante: sem-outbox ou com-outbox")
    public ResultadoExecucaoLaboratorio executar(@PathVariable String variante) {
        return servico.executar(VarianteOutbox.apartirDoSegmentoUrl(variante));
    }
}
