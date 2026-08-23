package com.javaengineeringlab.backend.plataforma;

/**
 * Exceção base para erros de requisição inválida (mapeada para 400 pelo
 * ManipuladorGlobalDeExcecoes). Laboratórios específicos podem estendê-la
 * (ex.: variante de execução inexistente) sem precisar de um handler
 * próprio.
 */
public class RequisicaoInvalidaException extends RuntimeException {

    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
