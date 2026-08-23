package com.javaengineeringlab.backend.laboratorios.connpool;

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

@WebMvcTest(ExecucaoConnPoolController.class)
@Import(ManipuladorGlobalDeExcecoes.class)
class ExecucaoConnPoolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExecucaoConnPoolService servico;

    @Test
    void deveExecutarVariantePoolPequeno() throws Exception {
        var resultado = new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(), "connection-pool-exhaustion", VarianteExecucao.PROBLEMATICO, OrigemDados.REAL,
                Instant.now(), 1500L, Map.of("quantidadeSucesso", 4, "quantidadeFalhasPorTimeout", 6)
        );
        given(servico.executar(eq(VarianteConnPool.POOL_PEQUENO))).willReturn(resultado);

        mockMvc.perform(post("/api/laboratorios/connection-pool-exhaustion/execucoes/pool-pequeno"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricas.quantidadeFalhasPorTimeout").value(6));
    }

    @Test
    void deveRetornar400ParaVarianteInvalida() throws Exception {
        mockMvc.perform(post("/api/laboratorios/connection-pool-exhaustion/execucoes/inexistente"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }
}
