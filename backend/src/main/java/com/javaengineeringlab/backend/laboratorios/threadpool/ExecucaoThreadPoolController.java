package com.javaengineeringlab.backend.laboratorios.threadpool;

import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/laboratorios/thread-pool-exhaustion/execucoes")
@Tag(name = "Laboratório Thread Pool Exhaustion", description = "Execução das variantes do laboratório de esgotamento de pool de threads")
public class ExecucaoThreadPoolController {

    private final ExecucaoThreadPoolService servico;

    public ExecucaoThreadPoolController(ExecucaoThreadPoolService servico) {
        this.servico = servico;
    }

    @PostMapping("/{variante}")
    @Operation(summary = "Executa uma variante do laboratório de Thread Pool Exhaustion",
            description = "variante: fila-ilimitada ou fila-limitada")
    public ResultadoExecucaoLaboratorio executar(@PathVariable String variante) {
        return servico.executar(VarianteThreadPool.apartirDoSegmentoUrl(variante));
    }
}
