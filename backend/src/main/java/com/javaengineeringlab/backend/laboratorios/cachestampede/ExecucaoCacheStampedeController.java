package com.javaengineeringlab.backend.laboratorios.cachestampede;

import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/laboratorios/cache-stampede/execucoes")
@Tag(name = "Laboratório Cache Stampede", description = "Execução das variantes do laboratório de cache stampede")
public class ExecucaoCacheStampedeController {

    private final ExecucaoCacheStampedeService servico;

    public ExecucaoCacheStampedeController(ExecucaoCacheStampedeService servico) {
        this.servico = servico;
    }

    @PostMapping("/{variante}")
    @Operation(summary = "Executa uma variante do laboratório de Cache Stampede",
            description = "variante: sem-protecao ou com-protecao")
    public ResultadoExecucaoLaboratorio executar(@PathVariable String variante) {
        return servico.executar(VarianteCacheStampede.apartirDoSegmentoUrl(variante));
    }
}
