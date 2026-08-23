package com.javaengineeringlab.backend.laboratorios.kafka;

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
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valida, contra Kafka e PostgreSQL reais (Testcontainers), o
 * comportamento das duas variantes do laboratório de mensagem duplicada
 * — RNF-02 de SPEC-LAB-KAFKA-IDEMP-001-mensagem-duplicada-idempotencia.md.
 */
@Testcontainers
@SpringBootTest
class ExecucaoKafkaIdempotenciaServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0"));

    @Autowired
    private ExecucaoKafkaIdempotenciaService servico;

    @Autowired
    private SeedDadosKafka seedDadosKafka;

    @BeforeEach
    void garantirCarteirasDemonstracao() {
        seedDadosKafka.garantirCarteirasDemonstracao();
    }

    @Test
    void semIdempotenciaDeveCreditarDuasVezesParaOMesmoEvento() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteKafka.SEM_IDEMPOTENCIA);

        assertThat(resultado.metricas().get("quantidadeEventosConsumidos")).isEqualTo(2);
        assertThat(resultado.metricas().get("quantidadeProcessamentosEfetivos")).isEqualTo(2);
        assertThat((BigDecimal) resultado.metricas().get("saldoFinal"))
                .isEqualByComparingTo(ExecucaoKafkaIdempotenciaService.VALOR_DO_EVENTO.multiply(BigDecimal.valueOf(2)));
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.PROBLEMATICO);
        assertThat(resultado.origemDados()).isEqualTo(OrigemDados.REAL);
    }

    @Test
    void idempotenteDeveCreditarUmaUnicaVezMesmoComEventoDuplicado() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteKafka.IDEMPOTENTE);

        assertThat(resultado.metricas().get("quantidadeEventosConsumidos")).isEqualTo(2);
        assertThat(resultado.metricas().get("quantidadeProcessamentosEfetivos")).isEqualTo(1);
        assertThat((BigDecimal) resultado.metricas().get("saldoFinal"))
                .isEqualByComparingTo(ExecucaoKafkaIdempotenciaService.VALOR_DO_EVENTO);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.CORRIGIDO);
    }
}
