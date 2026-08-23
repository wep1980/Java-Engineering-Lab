package com.javaengineeringlab.backend.laboratorios.n1;

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

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExecucaoN1Controller.class)
@Import(ManipuladorGlobalDeExcecoes.class)
class ExecucaoN1ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExecucaoN1Service servico;

    @Test
    void deveExecutarVarianteProblematica() throws Exception {
        var resultado = new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(), "n1-queries", VarianteExecucao.PROBLEMATICO, OrigemDados.REAL,
                Instant.now(), 42L, Map.of("quantidadeQueries", 51L, "quantidadePedidos", 50)
        );
        given(servico.executar(eq(VarianteN1.PROBLEMATICO))).willReturn(resultado);

        mockMvc.perform(post("/api/laboratorios/n1-queries/execucoes/problematico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.origemDados").value("REAL"))
                .andExpect(jsonPath("$.metricas.quantidadeQueries").value(51));
    }

    @Test
    void deveRetornar400ParaVarianteInvalida() throws Exception {
        mockMvc.perform(post("/api/laboratorios/n1-queries/execucoes/inexistente"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }
}
