package com.javaengineeringlab.backend.plataforma;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Contrato comum de resultado de execução de laboratório — definido em
 * specs/architecture/SPEC-JEL-003-mvp-plataforma-base.md. Ainda sem
 * nenhum endpoint que o produza nesta fase: o primeiro laboratório a
 * usá-lo é o de N+1 (specs/labs/SPEC-LAB-N1-001-n-mais-um-queries.md).
 */
public record ResultadoExecucaoLaboratorio(
        UUID execucaoId,
        String laboratorioId,
        VarianteExecucao variante,
        OrigemDados origemDados,
        Instant iniciadoEm,
        long duracaoMs,
        Map<String, Object> metricas
) {
}
