package com.javaengineeringlab.backend.laboratorios.outbox;

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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valida, contra Kafka e PostgreSQL reais (Testcontainers), o
 * comportamento das duas variantes do laboratório de Transactional
 * Outbox -- RNF-03 de
 * SPEC-LAB-OUTBOX-001-transactional-outbox.md.
 */
@Testcontainers
@SpringBootTest
class ExecucaoOutboxServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0"));

    @Autowired
    private ExecucaoOutboxService servico;

    @Autowired
    private PedidoOutboxRepository pedidoRepository;

    @Autowired
    private OutboxEventoRepository outboxEventoRepository;

    @Test
    void semOutboxDevePersistirPedidoMasFalharAoPublicarEDeixarInconsistente() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteOutbox.SEM_OUTBOX);

        assertThat(resultado.metricas().get("pedidoPersistido")).isEqualTo(true);
        assertThat(resultado.metricas().get("eventoRegistradoNaOutbox")).isEqualTo(false);
        assertThat(resultado.metricas().get("eventoPublicadoNoKafka")).isEqualTo(false);
        assertThat(resultado.metricas().get("inconsistente")).isEqualTo(true);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.PROBLEMATICO);
        assertThat(resultado.origemDados()).isEqualTo(OrigemDados.REAL);

        Long pedidoId = (Long) resultado.metricas().get("pedidoId");
        assertThat(pedidoRepository.findById(pedidoId)).isPresent();
        assertThat(outboxEventoRepository.findAll())
                .noneMatch(evento -> evento.getAgregadoId().equals(pedidoId));
    }

    @Test
    void comOutboxDevePublicarViaRelayEPermanecerConsistente() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteOutbox.COM_OUTBOX);

        assertThat(resultado.metricas().get("pedidoPersistido")).isEqualTo(true);
        assertThat(resultado.metricas().get("eventoRegistradoNaOutbox")).isEqualTo(true);
        assertThat(resultado.metricas().get("eventoPublicadoNoKafka")).isEqualTo(true);
        assertThat(resultado.metricas().get("inconsistente")).isEqualTo(false);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.CORRIGIDO);

        Long pedidoId = (Long) resultado.metricas().get("pedidoId");
        assertThat(pedidoRepository.findById(pedidoId)).isPresent();
        assertThat(outboxEventoRepository.findAll())
                .filteredOn(evento -> evento.getAgregadoId().equals(pedidoId))
                .hasSize(1)
                .first()
                .satisfies(evento -> assertThat(evento.getStatus()).isEqualTo(StatusOutboxEvento.PUBLICADO));
    }
}
