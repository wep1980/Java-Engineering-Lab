package com.javaengineeringlab.backend.laboratorios.circuitbreaker;

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

@WebMvcTest(ExecucaoCircuitBreakerController.class)
@Import(ManipuladorGlobalDeExcecoes.class)
class ExecucaoCircuitBreakerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExecucaoCircuitBreakerService servico;

    @Test
    void deveExecutarVarianteComCircuitBreaker() throws Exception {
        var resultado = new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(), "circuit-breaker", VarianteExecucao.CORRIGIDO, OrigemDados.REAL,
                Instant.now(), 1512L,
                Map.of("quantidadeFalhasReais", 5, "quantidadeRejeitadasPeloCircuito", 15, "estadoFinalDoCircuito", "OPEN")
        );
        given(servico.executar(eq(VarianteCircuitBreaker.COM_CIRCUIT_BREAKER))).willReturn(resultado);

        mockMvc.perform(post("/api/laboratorios/circuit-breaker/execucoes/com-circuit-breaker"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricas.estadoFinalDoCircuito").value("OPEN"));
    }

    @Test
    void deveRetornar400ParaVarianteInvalida() throws Exception {
        mockMvc.perform(post("/api/laboratorios/circuit-breaker/execucoes/inexistente"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }
}
