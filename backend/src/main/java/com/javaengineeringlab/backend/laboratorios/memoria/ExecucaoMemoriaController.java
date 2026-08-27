package com.javaengineeringlab.backend.laboratorios.memoria;

import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/laboratorios/memory-leak/execucoes")
@Tag(name = "Laboratório Memory Leak", description = "Execução das variantes do laboratório de memory leak")
public class ExecucaoMemoriaController {

    private final ExecucaoMemoriaService servico;

    public ExecucaoMemoriaController(ExecucaoMemoriaService servico) {
        this.servico = servico;
    }

    @PostMapping("/{variante}")
    @Operation(summary = "Executa uma variante do laboratório de Memory Leak",
            description = "variante: com-vazamento ou sem-vazamento")
    public ResultadoExecucaoLaboratorio executar(@PathVariable String variante) {
        return servico.executar(VarianteMemoria.apartirDoSegmentoUrl(variante));
    }
}
