package com.javaengineeringlab.backend.laboratorios.kafka;

import com.javaengineeringlab.backend.plataforma.ManipuladorGlobalDeExcecoes;
import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExecucaoKafkaIdempotenciaController.class)
@Import(ManipuladorGlobalDeExcecoes.class)
class ExecucaoKafkaIdempotenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExecucaoKafkaIdempotenciaService servico;

    @Test
    void deveExecutarVarianteIdempotente() throws Exception {
        var resultado = new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(), "kafka-idempotencia", VarianteExecucao.CORRIGIDO, OrigemDados.REAL,
                Instant.now(), 340L, Map.of("quantidadeEventosConsumidos", 2, "quantidadeProcessamentosEfetivos", 1)
        );
        given(servico.executar(eq(VarianteKafka.IDEMPOTENTE))).willReturn(resultado);

        mockMvc.perform(post("/api/laboratorios/kafka-idempotencia/execucoes/idempotente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricas.quantidadeProcessamentosEfetivos").value(1));
    }

    @Test
    void deveRetornar400ParaVarianteInvalida() throws Exception {
        mockMvc.perform(post("/api/laboratorios/kafka-idempotencia/execucoes/inexistente"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }
}
