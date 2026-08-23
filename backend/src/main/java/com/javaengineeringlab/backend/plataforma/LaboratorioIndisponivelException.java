package com.javaengineeringlab.backend.plataforma;

/**
 * Sinaliza que o laboratório depende de infraestrutura que não está
 * acessível no momento (ex.: Kafka fora do ar) — mapeada para 503, para
 * distinguir "infraestrutura de apoio indisponível" de um erro genérico.
 */
public class LaboratorioIndisponivelException extends RuntimeException {

    public LaboratorioIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
