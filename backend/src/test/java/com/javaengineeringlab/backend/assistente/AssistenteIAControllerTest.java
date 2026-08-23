package com.javaengineeringlab.backend.assistente;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaengineeringlab.backend.plataforma.LaboratorioIndisponivelException;
import com.javaengineeringlab.backend.plataforma.ManipuladorGlobalDeExcecoes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssistenteIAController.class)
@Import(ManipuladorGlobalDeExcecoes.class)
class AssistenteIAControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AssistenteIA assistenteIA;

    @Test
    void deveResponderUmaPerguntaComContexto() throws Exception {
        given(assistenteIA.responder(any())).willReturn("Porque a variante problemática dispara N+1.");

        var requisicao = new PerguntaRequest(
                "Por que essa execução gerou 51 queries?",
                Map.of("quantidadeQueries", 51)
        );

        mockMvc.perform(post("/api/laboratorios/n1-queries/assistente/perguntas")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resposta").value("Porque a variante problemática dispara N+1."));
    }

    @Test
    void deveRetornar400ParaPerguntaEmBranco() throws Exception {
        var requisicao = new PerguntaRequest("", null);

        mockMvc.perform(post("/api/laboratorios/n1-queries/assistente/perguntas")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar503QuandoAssistenteIndisponivel() throws Exception {
        given(assistenteIA.responder(any())).willThrow(new LaboratorioIndisponivelException("Ollama indisponível"));

        var requisicao = new PerguntaRequest("Qualquer pergunta", null);

        mockMvc.perform(post("/api/laboratorios/n1-queries/assistente/perguntas")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isServiceUnavailable());
    }
}
