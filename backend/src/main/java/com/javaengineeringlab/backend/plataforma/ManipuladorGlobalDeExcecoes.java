package com.javaengineeringlab.backend.plataforma;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ManipuladorGlobalDeExcecoes {

    @ExceptionHandler(LaboratorioNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarLaboratorioNaoEncontrado(
            LaboratorioNaoEncontradoException excecao, HttpServletRequest request) {
        return construirResposta(HttpStatus.NOT_FOUND, excecao.getMessage(), request);
    }

    private ResponseEntity<ErroResposta> construirResposta(
            HttpStatus status, String mensagem, HttpServletRequest request) {
        String correlationId = MDC.get(FiltroCorrelationId.CHAVE_MDC);
        ErroResposta corpo = new ErroResposta(
                status.value(),
                mensagem,
                Instant.now(),
                request.getRequestURI(),
                correlationId
        );
        return ResponseEntity.status(status).body(corpo);
    }
}
