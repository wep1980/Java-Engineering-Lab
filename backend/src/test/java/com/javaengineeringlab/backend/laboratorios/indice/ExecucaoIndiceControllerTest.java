package com.javaengineeringlab.backend.laboratorios.indice;

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

@WebMvcTest(ExecucaoIndiceController.class)
@Import(ManipuladorGlobalDeExcecoes.class)
class ExecucaoIndiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExecucaoIndiceService servico;

    @Test
    void deveExecutarVarianteSemIndice() throws Exception {
        var resultado = new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(), "query-sem-indice", VarianteExecucao.PROBLEMATICO, OrigemDados.REAL,
                Instant.now(), 15L, Map.of("tipoDoPlano", "Seq Scan", "duracaoConsultaMs", 12.5)
        );
        given(servico.executar(eq(VarianteIndice.SEM_INDICE))).willReturn(resultado);

        mockMvc.perform(post("/api/laboratorios/query-sem-indice/execucoes/sem-indice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricas.tipoDoPlano").value("Seq Scan"));
    }

    @Test
    void deveRetornar400ParaVarianteInvalida() throws Exception {
        mockMvc.perform(post("/api/laboratorios/query-sem-indice/execucoes/inexistente"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }
}
