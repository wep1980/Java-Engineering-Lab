package com.javaengineeringlab.backend.laboratorios.indice;

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
 * Valida, contra PostgreSQL real (Testcontainers) e dados reais em
 * volume, o comportamento das duas variantes do laboratório de Query
 * sem índice — RNF-03 de SPEC-LAB-INDICE-001-query-sem-indice.md.
 */
@Testcontainers
@SpringBootTest
class ExecucaoIndiceServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ExecucaoIndiceService servico;

    @Autowired
    private SeedRegistrosBusca seedRegistrosBusca;

    @BeforeEach
    void garantirRegistrosDemonstracao() {
        seedRegistrosBusca.garantirRegistrosDemonstracao();
    }

    @Test
    void semIndiceDeveProduzirSeqScanReal() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteIndice.SEM_INDICE);

        assertThat(resultado.metricas().get("tipoDoPlano")).isEqualTo("Seq Scan");
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.PROBLEMATICO);
        assertThat(resultado.origemDados()).isEqualTo(OrigemDados.REAL);
        assertThat((Long) resultado.metricas().get("quantidadeRegistros"))
                .isEqualTo((long) SeedRegistrosBusca.QUANTIDADE_REGISTROS);
    }

    @Test
    void comIndiceDeveProduzirPlanoQueUsaOIndice() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteIndice.COM_INDICE);

        // O otimizador pode escolher Index Scan, Index Only Scan ou
        // Bitmap Heap Scan (que usa um Bitmap Index Scan por baixo) --
        // qual exatamente depende das estatísticas e do plano de custo;
        // o que importa pedagogicamente é que deixa de ser Seq Scan.
        String tipoDoPlano = (String) resultado.metricas().get("tipoDoPlano");
        assertThat(tipoDoPlano).isNotEqualTo("Seq Scan");
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.CORRIGIDO);
    }
}
