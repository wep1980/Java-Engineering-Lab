package com.javaengineeringlab.backend.laboratorios.indice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Orquestra a comparação real de plano de execução do laboratório de
 * Query sem índice — ver
 * specs/labs/SPEC-LAB-INDICE-001-query-sem-indice.md.
 */
@Service
public class ExecucaoIndiceService {

    static final String EMAIL_BUSCADO = "usuario150000@exemplo.com";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RegistroBuscaRepository repositorio;

    public ExecucaoIndiceService(RegistroBuscaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public ResultadoExecucaoLaboratorio executar(VarianteIndice variante) {
        Instant inicio = Instant.now();

        if (variante == VarianteIndice.SEM_INDICE) {
            repositorio.removerIndice();
        } else {
            repositorio.criarIndice();
        }
        repositorio.atualizarEstatisticas();

        String planoJson = repositorio.explicarBuscaPorEmail(EMAIL_BUSCADO);
        PlanoExecucao plano = interpretarPlano(planoJson);

        long duracaoMs = Duration.between(inicio, Instant.now()).toMillis();

        VarianteExecucao varianteExecucao = variante == VarianteIndice.SEM_INDICE
                ? VarianteExecucao.PROBLEMATICO
                : VarianteExecucao.CORRIGIDO;

        Map<String, Object> metricas = Map.of(
                "tecnica", variante.name(),
                "tipoDoPlano", plano.tipoNo(),
                "duracaoConsultaMs", plano.duracaoRealMs(),
                "quantidadeRegistros", repositorio.count()
        );

        return new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(),
                "query-sem-indice",
                varianteExecucao,
                OrigemDados.REAL,
                inicio,
                duracaoMs,
                metricas
        );
    }

    /**
     * EXPLAIN (ANALYZE, FORMAT JSON) retorna um array com um objeto
     * contendo "Plan" -- extraímos o tipo real do nó escolhido pelo
     * otimizador e o tempo real de execução, ambos reportados pelo
     * próprio PostgreSQL, não medidos do lado da aplicação.
     */
    private PlanoExecucao interpretarPlano(String planoJson) {
        try {
            JsonNode raiz = OBJECT_MAPPER.readTree(planoJson).get(0).get("Plan");
            String tipoNo = raiz.get("Node Type").asText();
            double duracaoRealMs = raiz.get("Actual Total Time").asDouble();
            return new PlanoExecucao(tipoNo, duracaoRealMs);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao interpretar o plano de execução do PostgreSQL", e);
        }
    }

    private record PlanoExecucao(String tipoNo, double duracaoRealMs) {
    }
}
