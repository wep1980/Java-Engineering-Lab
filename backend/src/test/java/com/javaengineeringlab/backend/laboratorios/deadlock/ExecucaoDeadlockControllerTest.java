package com.javaengineeringlab.backend.laboratorios.deadlock;

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

@WebMvcTest(ExecucaoDeadlockController.class)
@Import(ManipuladorGlobalDeExcecoes.class)
class ExecucaoDeadlockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExecucaoDeadlockService servico;

    @Test
    void deveExecutarVarianteSemOrdemConsistente() throws Exception {
        var resultado = new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(), "deadlock", VarianteExecucao.PROBLEMATICO, OrigemDados.REAL,
                Instant.now(), 1300L, Map.of("quantidadeSucesso", 1, "quantidadeDeadlocksDetectados", 1)
        );
        given(servico.executar(eq(VarianteDeadlock.SEM_ORDEM_CONSISTENTE))).willReturn(resultado);

        mockMvc.perform(post("/api/laboratorios/deadlock/execucoes/sem-ordem-consistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricas.quantidadeDeadlocksDetectados").value(1));
    }

    @Test
    void deveRetornar400ParaVarianteInvalida() throws Exception {
        mockMvc.perform(post("/api/laboratorios/deadlock/execucoes/inexistente"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }
}
