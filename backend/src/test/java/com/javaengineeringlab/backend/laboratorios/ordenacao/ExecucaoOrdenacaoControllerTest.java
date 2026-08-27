package com.javaengineeringlab.backend.laboratorios.ordenacao;

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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExecucaoOrdenacaoController.class)
@Import(ManipuladorGlobalDeExcecoes.class)
class ExecucaoOrdenacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExecucaoOrdenacaoService servico;

    @Test
    void deveExecutarVarianteComChaveParticionamento() throws Exception {
        var resultado = new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(), "ordenacao-de-eventos", VarianteExecucao.CORRIGIDO, OrigemDados.REAL,
                Instant.now(), 420L,
                Map.of("quantidadeParticoesUsadas", 1, "ordemPreservada", true, "ordemRecebida", List.of(0, 1, 2))
        );
        given(servico.executar(eq(VarianteOrdenacao.COM_CHAVE_PARTICIONAMENTO))).willReturn(resultado);

        mockMvc.perform(post("/api/laboratorios/ordenacao-de-eventos/execucoes/com-chave-particionamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metricas.ordemPreservada").value(true));
    }

    @Test
    void deveRetornar400ParaVarianteInvalida() throws Exception {
        mockMvc.perform(post("/api/laboratorios/ordenacao-de-eventos/execucoes/inexistente"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }
}
