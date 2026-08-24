package com.javaengineeringlab.backend.laboratorios.indice;

import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/laboratorios/query-sem-indice/execucoes")
@Tag(name = "Laboratório Query sem índice", description = "Execução das variantes do laboratório de plano de execução sem/com índice")
public class ExecucaoIndiceController {

    private final ExecucaoIndiceService servico;

    public ExecucaoIndiceController(ExecucaoIndiceService servico) {
        this.servico = servico;
    }

    @PostMapping("/{variante}")
    @Operation(summary = "Executa uma variante do laboratório de Query sem índice",
            description = "variante: sem-indice ou com-indice")
    public ResultadoExecucaoLaboratorio executar(@PathVariable String variante) {
        return servico.executar(VarianteIndice.apartirDoSegmentoUrl(variante));
    }
}
