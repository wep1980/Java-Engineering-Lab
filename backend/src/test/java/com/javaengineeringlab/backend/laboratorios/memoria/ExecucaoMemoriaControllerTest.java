package com.javaengineeringlab.backend.laboratorios.memoria;

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

@WebMvcTest(ExecucaoMemoriaController.class)
@Import(ManipuladorGlobalDeExcecoes.class)
class ExecucaoMemoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExecucaoMemoriaService servico;

    @Test
    void deveExecutarVarianteSemVazamento() throws Exception {
        var resultado = new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(), "memory-leak", VarianteExecucao.CORRIGIDO, OrigemDados.REAL,
                Instant.now(), 420L,
                Map.of("vazamentoDetectado", false, "crescimentoRetidoBytes", 100_000L)
        );
        given(servico.executar(eq(VarianteMemoria.SEM_VAZAMENTO))).willReturn(resultado);

        mockMvc.perform(post("/api/laboratorios/memory-leak/execucoes/sem-vazamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricas.vazamentoDetectado").value(false));
    }

    @Test
    void deveRetornar400ParaVarianteInvalida() throws Exception {
        mockMvc.perform(post("/api/laboratorios/memory-leak/execucoes/inexistente"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }
}
