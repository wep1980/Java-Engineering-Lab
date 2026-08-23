package com.javaengineeringlab.backend.laboratorios.connpool;

import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/laboratorios/connection-pool-exhaustion/execucoes")
@Tag(name = "Laboratório Connection Pool Exhaustion", description = "Execução das variantes do laboratório de esgotamento de pool de conexões")
public class ExecucaoConnPoolController {

    private final ExecucaoConnPoolService servico;

    public ExecucaoConnPoolController(ExecucaoConnPoolService servico) {
        this.servico = servico;
    }

    @PostMapping("/{variante}")
    @Operation(summary = "Executa uma variante do laboratório de Connection Pool Exhaustion",
            description = "variante: pool-pequeno, pool-redimensionado ou conexao-curta")
    public ResultadoExecucaoLaboratorio executar(@PathVariable String variante) {
        return servico.executar(VarianteConnPool.apartirDoSegmentoUrl(variante));
    }
}
