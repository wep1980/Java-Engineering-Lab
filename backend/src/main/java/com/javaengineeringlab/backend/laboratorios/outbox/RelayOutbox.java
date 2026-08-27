package com.javaengineeringlab.backend.laboratorios.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Relay real do padrão Transactional Outbox -- roda de forma
 * assíncrona e independente de qualquer requisição HTTP, publicando no
 * Kafka real os eventos ainda pendentes. Ver "Relay real (@Scheduled)"
 * em specs/labs/SPEC-LAB-OUTBOX-001-transactional-outbox.md.
 *
 * <p>Reutiliza o {@code KafkaTemplate<String, Object>} autoconfigurado
 * padrão (o mesmo já usado por
 * {@code laboratorios.kafka.ProdutorEventoPagamento}) -- nenhum bean
 * novo, ao contrário do {@link PublicadorDiretoInstavel}.
 */
@Component
public class RelayOutbox {

    static final String TOPICO = "pedidos-criados";
    static final long INTERVALO_MS = 200;

    private static final Logger log = LoggerFactory.getLogger(RelayOutbox.class);

    private final OutboxEventoRepository outboxEventoRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    // Construído diretamente -- ver o mesmo comentário em
    // ExecucaoOutboxService sobre o bean ObjectMapper autoconfigurado
    // não estar disponível neste projeto.
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Ligado por padrão (comportamento real de produção). Achado real
    // durante a implementação: como este componente roda em QUALQUER
    // teste que suba o contexto Spring completo (@SpringBootTest), o
    // polling a cada 200ms -- mesmo sem nada pendente -- incrementa o
    // contador global de statements do Hibernate
    // (SessionFactory.getStatistics()), usado pelos testes de
    // contagem exata de queries do laboratório de N+1. Desligado
    // explicitamente lá (`outbox.relay.habilitado=false`) -- ver
    // "Achados reais" em specs/labs/SPEC-LAB-OUTBOX-001-transactional-outbox.md.
    @Value("${outbox.relay.habilitado:true}")
    private boolean habilitado;

    public RelayOutbox(
            OutboxEventoRepository outboxEventoRepository,
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.outboxEventoRepository = outboxEventoRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = INTERVALO_MS)
    @Transactional
    public void publicarPendentes() {
        if (!habilitado) {
            return;
        }

        List<OutboxEvento> pendentes = outboxEventoRepository.findByStatus(StatusOutboxEvento.PENDENTE);

        for (OutboxEvento evento : pendentes) {
            try {
                EventoPedidoCriado payload = objectMapper.readValue(evento.getPayload(), EventoPedidoCriado.class);
                kafkaTemplate.send(TOPICO, evento.getAgregadoId().toString(), payload)
                        .get(3, TimeUnit.SECONDS);
                evento.marcarComoPublicado();
                outboxEventoRepository.save(evento);
            } catch (Exception falhaRealDePublicacao) {
                // fica PENDENTE de propósito -- o próximo ciclo tenta de
                // novo. É exatamente a garantia que o padrão Outbox dá:
                // o evento nunca é perdido, só adiado.
                log.warn("Falha ao publicar evento outbox {} -- tentando de novo no próximo ciclo", evento.getId(), falhaRealDePublicacao);
            }
        }
    }
}
