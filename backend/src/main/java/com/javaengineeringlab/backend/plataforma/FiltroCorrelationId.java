package com.javaengineeringlab.backend.plataforma;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Propaga um correlation ID por requisição: reaproveita o cabeçalho
 * X-Correlation-Id recebido, ou gera um novo. Disponibiliza o valor via
 * MDC (para logs) e no cabeçalho de resposta.
 */
@Component
public class FiltroCorrelationId extends HttpFilter {

    private static final Logger LOG = LoggerFactory.getLogger(FiltroCorrelationId.class);

    public static final String CABECALHO = "X-Correlation-Id";
    public static final String CHAVE_MDC = "correlationId";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String correlationId = request.getHeader(CABECALHO);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(CHAVE_MDC, correlationId);
        response.setHeader(CABECALHO, correlationId);
        try {
            chain.doFilter(request, response);
            LOG.info("Requisição concluída: {} {} -> {}", request.getMethod(), request.getRequestURI(), response.getStatus());
        } finally {
            MDC.remove(CHAVE_MDC);
        }
    }
}
