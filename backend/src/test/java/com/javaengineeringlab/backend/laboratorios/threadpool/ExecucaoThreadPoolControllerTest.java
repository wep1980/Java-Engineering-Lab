package com.javaengineeringlab.backend.laboratorios.threadpool;

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

@WebMvcTest(ExecucaoThreadPoolController.class)
@Import(ManipuladorGlobalDeExcecoes.class)
class ExecucaoThreadPoolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExecucaoThreadPoolService servico;

    @Test
    void deveExecutarVarianteFilaLimitada() throws Exception {
        var resultado = new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(), "thread-pool-exhaustion", VarianteExecucao.CORRIGIDO, OrigemDados.REAL,
                Instant.now(), 1000L,
                Map.of("quantidadeAceitas", 4, "quantidadeRejeitadas", 6)
        );
        given(servico.executar(eq(VarianteThreadPool.FILA_LIMITADA))).willReturn(resultado);

        mockMvc.perform(post("/api/laboratorios/thread-pool-exhaustion/execucoes/fila-limitada"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricas.quantidadeRejeitadas").value(6));
    }

    @Test
    void deveRetornar400ParaVarianteInvalida() throws Exception {
        mockMvc.perform(post("/api/laboratorios/thread-pool-exhaustion/execucoes/inexistente"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }
}
