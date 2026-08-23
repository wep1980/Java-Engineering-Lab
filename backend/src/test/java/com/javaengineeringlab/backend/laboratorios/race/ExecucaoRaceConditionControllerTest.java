package com.javaengineeringlab.backend.laboratorios.race;

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

@WebMvcTest(ExecucaoRaceConditionController.class)
@Import(ManipuladorGlobalDeExcecoes.class)
class ExecucaoRaceConditionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExecucaoRaceConditionService servico;

    @Test
    void deveExecutarVarianteSemControle() throws Exception {
        var resultado = new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(), "race-condition", VarianteExecucao.PROBLEMATICO, OrigemDados.REAL,
                Instant.now(), 120L, Map.of("saldoFinal", 100, "atualizacoesPerdidas", 9)
        );
        given(servico.executar(eq(VarianteRace.SEM_CONTROLE))).willReturn(resultado);

        mockMvc.perform(post("/api/laboratorios/race-condition/execucoes/sem-controle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricas.atualizacoesPerdidas").value(9));
    }

    @Test
    void deveRetornar400ParaVarianteInvalida() throws Exception {
        mockMvc.perform(post("/api/laboratorios/race-condition/execucoes/inexistente"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }
}
