package com.javaengineeringlab.backend.laboratorios.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaengineeringlab.backend.plataforma.LaboratorioIndisponivelException;
import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Orquestra as duas variantes do laboratório de Transactional Outbox --
 * ver specs/labs/SPEC-LAB-OUTBOX-001-transactional-outbox.md.
 */
@Service
public class ExecucaoOutboxService {

    static final String DESCRICAO_PEDIDO = "Pedido de demonstração";
    static final BigDecimal VALOR_PEDIDO = BigDecimal.valueOf(199.90);
    static final long TIMEOUT_ESPERA_PUBLICACAO_MS = 5000;
    private static final long INTERVALO_POLL_MS = 50;

    private final PedidoOutboxRepository pedidoRepository;
    private final OutboxEventoRepository outboxEventoRepository;
    private final PublicadorDiretoInstavel publicadorDiretoInstavel;
    // Construído diretamente, não injetado -- o bean ObjectMapper
    // autoconfigurado não estava disponível neste projeto sob Spring
    // Boot 4.1 (achado real durante a implementação, mesma categoria
    // das relocações de autoconfiguração já documentadas para
    // JdbcConnectionDetails/WebMvcTest). EventoPedidoCriado só usa
    // tipos suportados nativamente pelo Jackson core (UUID, Long,
    // String, BigDecimal), então nenhum módulo adicional é necessário.
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExecucaoOutboxService(
            PedidoOutboxRepository pedidoRepository,
            OutboxEventoRepository outboxEventoRepository,
            PublicadorDiretoInstavel publicadorDiretoInstavel
    ) {
        this.pedidoRepository = pedidoRepository;
        this.outboxEventoRepository = outboxEventoRepository;
        this.publicadorDiretoInstavel = publicadorDiretoInstavel;
    }

    public ResultadoExecucaoLaboratorio executar(VarianteOutbox variante) {
        Instant inicio = Instant.now();

        Long pedidoId;
        boolean eventoRegistradoNaOutbox;
        boolean eventoPublicadoNoKafka;

        if (variante == VarianteOutbox.SEM_OUTBOX) {
            PedidoOutbox pedido = criarPedidoSemOutbox();
            pedidoId = pedido.getId();
            eventoRegistradoNaOutbox = false;

            EventoPedidoCriado payload = new EventoPedidoCriado(
                    UUID.randomUUID(), pedidoId, pedido.getDescricao(), pedido.getValor());
            eventoPublicadoNoKafka = publicadorDiretoInstavel.tentarPublicar(
                    RelayOutbox.TOPICO, pedidoId.toString(), payload);
        } else {
            OutboxEvento evento = criarPedidoComOutboxEvento();
            pedidoId = evento.getAgregadoId();
            eventoRegistradoNaOutbox = true;
            eventoPublicadoNoKafka = aguardarPublicacao(evento.getId());

            if (!eventoPublicadoNoKafka) {
                throw new LaboratorioIndisponivelException(
                        "Timeout aguardando o relay publicar o evento outbox no Kafka — verifique "
                                + "se o profile 'messaging' do docker-compose está no ar.");
            }
        }

        long duracaoMs = Duration.between(inicio, Instant.now()).toMillis();

        VarianteExecucao varianteExecucao = variante == VarianteOutbox.SEM_OUTBOX
                ? VarianteExecucao.PROBLEMATICO
                : VarianteExecucao.CORRIGIDO;

        // só é possível ficar "sem rastro" quando não há registro na
        // outbox e a publicação direta falhou -- com outbox, o evento
        // nunca some, só é adiado (e só chegamos aqui, sem lançar
        // LaboratorioIndisponivelException, quando ele já publicou).
        boolean inconsistente = !eventoRegistradoNaOutbox && !eventoPublicadoNoKafka;

        Map<String, Object> metricas = Map.of(
                "tecnica", variante.name(),
                "pedidoId", pedidoId,
                "pedidoPersistido", true,
                "eventoRegistradoNaOutbox", eventoRegistradoNaOutbox,
                "eventoPublicadoNoKafka", eventoPublicadoNoKafka,
                "inconsistente", inconsistente
        );

        return new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(),
                "transactional-outbox",
                varianteExecucao,
                OrigemDados.REAL,
                inicio,
                duracaoMs,
                metricas
        );
    }

    @Transactional
    PedidoOutbox criarPedidoSemOutbox() {
        return pedidoRepository.save(new PedidoOutbox(DESCRICAO_PEDIDO, VALOR_PEDIDO));
    }

    @Transactional
    OutboxEvento criarPedidoComOutboxEvento() {
        PedidoOutbox pedido = pedidoRepository.save(new PedidoOutbox(DESCRICAO_PEDIDO, VALOR_PEDIDO));

        EventoPedidoCriado payload = new EventoPedidoCriado(
                UUID.randomUUID(), pedido.getId(), pedido.getDescricao(), pedido.getValor());

        OutboxEvento evento = new OutboxEvento(pedido.getId(), "PedidoCriado", serializar(payload));
        return outboxEventoRepository.save(evento);
    }

    private String serializar(EventoPedidoCriado payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar payload do evento outbox", e);
        }
    }

    private boolean aguardarPublicacao(UUID outboxEventoId) {
        long limite = System.currentTimeMillis() + TIMEOUT_ESPERA_PUBLICACAO_MS;
        try {
            while (System.currentTimeMillis() < limite) {
                Optional<OutboxEvento> atual = outboxEventoRepository.findById(outboxEventoId);
                if (atual.isPresent() && atual.get().getStatus() == StatusOutboxEvento.PUBLICADO) {
                    return true;
                }
                Thread.sleep(INTERVALO_POLL_MS);
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Espera pela publicação do evento outbox foi interrompida", e);
        }
    }
}
