package com.javaengineeringlab.backend.assistente;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/laboratorios/{id}/assistente")
@Tag(name = "Engineering AI Assistant", description = "Assistente de IA contextualizado por laboratório")
public class AssistenteIAController {

    private final AssistenteIA assistenteIA;

    public AssistenteIAController(AssistenteIA assistenteIA) {
        this.assistenteIA = assistenteIA;
    }

    @PostMapping("/perguntas")
    @Operation(summary = "Faz uma pergunta ao assistente, com o contexto do laboratório")
    public RespostaAssistente perguntar(@PathVariable String id, @Valid @RequestBody PerguntaRequest requisicao) {
        ContextoPergunta contexto = new ContextoPergunta(id, requisicao.pergunta(), requisicao.ultimoResultado());
        return new RespostaAssistente(assistenteIA.responder(contexto));
    }
}
