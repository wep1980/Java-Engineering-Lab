package com.javaengineeringlab.backend.laboratorios.kafka;

import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/laboratorios/kafka-idempotencia/execucoes")
@Tag(name = "Laboratório Kafka / Idempotência", description = "Execução das variantes do laboratório de mensagem duplicada")
public class ExecucaoKafkaIdempotenciaController {

    private final ExecucaoKafkaIdempotenciaService servico;

    public ExecucaoKafkaIdempotenciaController(ExecucaoKafkaIdempotenciaService servico) {
        this.servico = servico;
    }

    @PostMapping("/{variante}")
    @Operation(summary = "Executa uma variante do laboratório de Kafka/idempotência",
            description = "variante: sem-idempotencia ou idempotente")
    public ResultadoExecucaoLaboratorio executar(@PathVariable String variante) {
        return servico.executar(VarianteKafka.apartirDoSegmentoUrl(variante));
    }
}
