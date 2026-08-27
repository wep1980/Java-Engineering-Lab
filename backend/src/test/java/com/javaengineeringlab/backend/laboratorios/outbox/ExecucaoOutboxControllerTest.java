package com.javaengineeringlab.backend.laboratorios.outbox;

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

@WebMvcTest(ExecucaoOutboxController.class)
@Import(ManipuladorGlobalDeExcecoes.class)
class ExecucaoOutboxControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExecucaoOutboxService servico;

    @Test
    void deveExecutarVarianteComOutbox() throws Exception {
        var resultado = new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(), "transactional-outbox", VarianteExecucao.CORRIGIDO, OrigemDados.REAL,
                Instant.now(), 350L,
                Map.of("eventoRegistradoNaOutbox", true, "eventoPublicadoNoKafka", true, "inconsistente", false)
        );
        given(servico.executar(eq(VarianteOutbox.COM_OUTBOX))).willReturn(resultado);

        mockMvc.perform(post("/api/laboratorios/transactional-outbox/execucoes/com-outbox"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricas.inconsistente").value(false));
    }

    @Test
    void deveRetornar400ParaVarianteInvalida() throws Exception {
        mockMvc.perform(post("/api/laboratorios/transactional-outbox/execucoes/inexistente"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }
}
