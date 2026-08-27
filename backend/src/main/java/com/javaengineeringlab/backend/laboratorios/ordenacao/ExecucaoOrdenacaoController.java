package com.javaengineeringlab.backend.laboratorios.ordenacao;

import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/laboratorios/ordenacao-de-eventos/execucoes")
@Tag(name = "Laboratório Ordenação de Eventos", description = "Execução das variantes do laboratório de ordenação de eventos")
public class ExecucaoOrdenacaoController {

    private final ExecucaoOrdenacaoService servico;

    public ExecucaoOrdenacaoController(ExecucaoOrdenacaoService servico) {
        this.servico = servico;
    }

    @PostMapping("/{variante}")
    @Operation(summary = "Executa uma variante do laboratório de Ordenação de Eventos",
            description = "variante: sem-chave-particionamento ou com-chave-particionamento")
    public ResultadoExecucaoLaboratorio executar(@PathVariable String variante) {
        return servico.executar(VarianteOrdenacao.apartirDoSegmentoUrl(variante));
    }
}
