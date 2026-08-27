package com.javaengineeringlab.backend.laboratorios.ordenacao;

import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valida, contra Kafka real (Testcontainers, 3 partições reais), o
 * comportamento das duas variantes do laboratório de Ordenação de
 * Eventos -- RNF-02 de
 * SPEC-LAB-ORDEM-001-ordenacao-de-eventos.md.
 */
@Testcontainers
@SpringBootTest
class ExecucaoOrdenacaoServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0"));

    @Autowired
    private ExecucaoOrdenacaoService servico;

    @Test
    void comChaveParticionamentoDeveUsarUmaUnicaParticaoEPreservarAOrdem() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteOrdenacao.COM_CHAVE_PARTICIONAMENTO);

        assertThat(resultado.metricas().get("quantidadeParticoesUsadas")).isEqualTo(1);
        assertThat(resultado.metricas().get("ordemPreservada")).isEqualTo(true);
        @SuppressWarnings("unchecked")
        List<Integer> ordemRecebida = (List<Integer>) resultado.metricas().get("ordemRecebida");
        assertThat(ordemRecebida).isEqualTo(IntStream.range(0, ExecucaoOrdenacaoService.QUANTIDADE_EVENTOS).boxed().toList());
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.CORRIGIDO);
        assertThat(resultado.origemDados()).isEqualTo(OrigemDados.REAL);
    }

    @Test
    void semChaveParticionamentoDeveUsarAsTresParticoes() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteOrdenacao.SEM_CHAVE_PARTICIONAMENTO);

        assertThat(resultado.metricas().get("quantidadeParticoesUsadas")).isEqualTo(TopicoOrdenacaoConfig.QUANTIDADE_PARTICOES);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.PROBLEMATICO);
        assertThat(resultado.origemDados()).isEqualTo(OrigemDados.REAL);
    }
}
