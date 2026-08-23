package com.javaengineeringlab.backend.laboratorios.n1;

import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valida, contra um PostgreSQL real (Testcontainers), a quantidade exata
 * de queries de cada variante do laboratório de N+1 — RNF-01/RNF-02 de
 * specs/labs/SPEC-LAB-N1-001-n-mais-um-queries.md. Números fixos abaixo
 * dependem da massa de dados determinística de SeedDadosN1.
 */
@Testcontainers
@SpringBootTest
class ExecucaoN1ServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ExecucaoN1Service servico;

    @Autowired
    private SeedDadosN1 seedDadosN1;

    @BeforeEach
    void garantirDadosDemonstracao() {
        seedDadosN1.garantirDadosDemonstracao();
    }

    @Test
    void variantesProblematicaDeveExecutarUmaQueryPorPedidoAlemDaListagem() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteN1.PROBLEMATICO);

        long queriesEsperadas = 1L + SeedDadosN1.QUANTIDADE_PEDIDOS;
        assertThat(resultado.metricas().get("quantidadeQueries")).isEqualTo(queriesEsperadas);
        assertThat(resultado.metricas().get("quantidadePedidos")).isEqualTo(SeedDadosN1.QUANTIDADE_PEDIDOS);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.PROBLEMATICO);
        assertThat(resultado.origemDados()).isEqualTo(OrigemDados.REAL);
    }

    @Test
    void varianteJoinFetchDeveExecutarUmaUnicaQuery() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteN1.JOIN_FETCH);

        assertThat(resultado.metricas().get("quantidadeQueries")).isEqualTo(1L);
        assertThat(resultado.metricas().get("quantidadePedidos")).isEqualTo(SeedDadosN1.QUANTIDADE_PEDIDOS);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.CORRIGIDO);
    }

    @Test
    void varianteEntityGraphDeveExecutarUmaUnicaQuery() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteN1.ENTITY_GRAPH);

        assertThat(resultado.metricas().get("quantidadeQueries")).isEqualTo(1L);
        assertThat(resultado.metricas().get("quantidadePedidos")).isEqualTo(SeedDadosN1.QUANTIDADE_PEDIDOS);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.CORRIGIDO);
    }

    @Test
    void varianteDtoProjectionDeveExecutarUmaUnicaQuery() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteN1.DTO_PROJECTION);

        assertThat(resultado.metricas().get("quantidadeQueries")).isEqualTo(1L);
        assertThat(resultado.metricas().get("quantidadePedidos")).isEqualTo(SeedDadosN1.QUANTIDADE_PEDIDOS);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.CORRIGIDO);
    }
}
