package com.javaengineeringlab.backend.laboratorios.cachestampede;

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

@WebMvcTest(ExecucaoCacheStampedeController.class)
@Import(ManipuladorGlobalDeExcecoes.class)
class ExecucaoCacheStampedeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExecucaoCacheStampedeService servico;

    @Test
    void deveExecutarVarianteComProtecao() throws Exception {
        var resultado = new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(), "cache-stampede", VarianteExecucao.CORRIGIDO, OrigemDados.REAL,
                Instant.now(), 520L,
                Map.of("quantidadeAcessosAoRecursoLentoReal", 1)
        );
        given(servico.executar(eq(VarianteCacheStampede.COM_PROTECAO))).willReturn(resultado);

        mockMvc.perform(post("/api/laboratorios/cache-stampede/execucoes/com-protecao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricas.quantidadeAcessosAoRecursoLentoReal").value(1));
    }

    @Test
    void deveRetornar400ParaVarianteInvalida() throws Exception {
        mockMvc.perform(post("/api/laboratorios/cache-stampede/execucoes/inexistente"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }
}
