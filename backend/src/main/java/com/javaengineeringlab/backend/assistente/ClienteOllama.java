package com.javaengineeringlab.backend.assistente;

import com.javaengineeringlab.backend.plataforma.LaboratorioIndisponivelException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Implementação concreta de AssistenteIA usando Ollama (modelo local) —
 * ver SPEC-JEL-006-engineering-ai-assistant.md. Chama
 * POST /api/generate sem streaming (resposta única).
 */
@Service
public class ClienteOllama implements AssistenteIA {

    private final RestClient restClient;
    private final ConhecimentoLaboratorios conhecimentoLaboratorios;
    private final String modelo;

    public ClienteOllama(
            @Value("${ollama.url}") String urlBase,
            @Value("${ollama.modelo}") String modelo,
            ConhecimentoLaboratorios conhecimentoLaboratorios
    ) {
        this.modelo = modelo;
        this.conhecimentoLaboratorios = conhecimentoLaboratorios;

        // Inferência em CPU pode levar dezenas de segundos, especialmente
        // na primeira chamada apos o modelo carregar -- timeout generoso
        // (ver RNF-02 da SPEC).
        SimpleClientHttpRequestFactory fabricaRequisicao = new SimpleClientHttpRequestFactory();
        fabricaRequisicao.setConnectTimeout(10_000);
        fabricaRequisicao.setReadTimeout(120_000);

        this.restClient = RestClient.builder()
                .baseUrl(urlBase)
                .requestFactory(fabricaRequisicao)
                .build();
    }

    @Override
    public String responder(ContextoPergunta contexto) {
        String prompt = montarPrompt(contexto);

        try {
            RespostaOllama resposta = restClient.post()
                    .uri("/api/generate")
                    .body(new RequisicaoOllama(modelo, prompt, false))
                    .retrieve()
                    .body(RespostaOllama.class);

            if (resposta == null || resposta.response() == null) {
                throw new LaboratorioIndisponivelException(
                        "O assistente de IA respondeu de forma inesperada (Ollama).");
            }

            return resposta.response().trim();
        } catch (RestClientException e) {
            throw new LaboratorioIndisponivelException(
                    "Assistente de IA indisponível — verifique se o profile 'ai' do "
                            + "docker-compose está no ar e se o modelo já foi baixado.");
        }
    }

    private String montarPrompt(ContextoPergunta contexto) {
        String conhecimento = conhecimentoLaboratorios.buscar(contexto.laboratorioId());
        String resultado = contexto.ultimoResultado() == null
                ? "Nenhum resultado de execução foi fornecido ainda pelo usuário."
                : contexto.ultimoResultado().toString();

        return """
                Você é um assistente educacional que explica problemas de Engenharia de \
                Software em aplicações Java/Spring, dentro do laboratório "%s".

                Conhecimento sobre este laboratório:
                %s

                Resultado da última execução que o usuário viu na tela:
                %s

                Responda em português do Brasil, de forma direta e técnica, usando o \
                resultado da execução acima quando ele for relevante para a pergunta. \
                Se a pergunta pedir uma explicação para entrevista de emprego, seja \
                conciso e objetivo.

                Pergunta do usuário: %s
                """.formatted(contexto.laboratorioId(), conhecimento, resultado, contexto.pergunta());
    }

    private record RequisicaoOllama(String model, String prompt, boolean stream) {
    }

    private record RespostaOllama(String response) {
    }
}
