package com.javaengineeringlab.backend.laboratorios.saga;

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

@WebMvcTest(ExecucaoSagaController.class)
@Import(ManipuladorGlobalDeExcecoes.class)
class ExecucaoSagaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExecucaoSagaService servico;

    @Test
    void deveExecutarVarianteComCompensacao() throws Exception {
        var resultado = new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(), "saga", VarianteExecucao.CORRIGIDO, OrigemDados.REAL,
                Instant.now(), 30L,
                Map.of("estoqueConsistente", true, "compensacaoExecutada", true)
        );
        given(servico.executar(eq(VarianteSaga.COM_COMPENSACAO))).willReturn(resultado);

        mockMvc.perform(post("/api/laboratorios/saga/execucoes/com-compensacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricas.estoqueConsistente").value(true));
    }

    @Test
    void deveRetornar400ParaVarianteInvalida() throws Exception {
        mockMvc.perform(post("/api/laboratorios/saga/execucoes/inexistente"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }
}
