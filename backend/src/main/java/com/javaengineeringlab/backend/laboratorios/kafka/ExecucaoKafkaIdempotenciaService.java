package com.javaengineeringlab.backend.laboratorios.kafka;

import com.javaengineeringlab.backend.plataforma.LaboratorioIndisponivelException;
import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Orquestra a publicação deliberadamente duplicada de um evento Kafka e
 * aguarda o consumidor correspondente processar ambas as entregas — ver
 * specs/labs/SPEC-LAB-KAFKA-IDEMP-001-mensagem-duplicada-idempotencia.md.
 */
@Service
public class ExecucaoKafkaIdempotenciaService {

    static final BigDecimal VALOR_DO_EVENTO = BigDecimal.valueOf(50);
    private static final int QUANTIDADE_ENTREGAS = 2;
    private static final long TIMEOUT_SEGUNDOS = 15;

    private final SeedDadosKafka seedDadosKafka;
    private final CarteiraRepository carteiraRepository;
    private final ProdutorEventoPagamento produtor;
    private final ConsumidorPagamentoSemIdempotencia consumidorSemIdempotencia;
    private final ConsumidorPagamentoIdempotente consumidorIdempotente;

    public ExecucaoKafkaIdempotenciaService(
            SeedDadosKafka seedDadosKafka,
            CarteiraRepository carteiraRepository,
            ProdutorEventoPagamento produtor,
            ConsumidorPagamentoSemIdempotencia consumidorSemIdempotencia,
            ConsumidorPagamentoIdempotente consumidorIdempotente
    ) {
        this.seedDadosKafka = seedDadosKafka;
        this.carteiraRepository = carteiraRepository;
        this.produtor = produtor;
        this.consumidorSemIdempotencia = consumidorSemIdempotencia;
        this.consumidorIdempotente = consumidorIdempotente;
    }

    public ResultadoExecucaoLaboratorio executar(VarianteKafka variante) {
        Instant inicio = Instant.now();

        Long carteiraId;
        int quantidadeEventosConsumidos;
        int quantidadeProcessamentosEfetivos;

        UUID eventoId = UUID.randomUUID();

        if (variante == VarianteKafka.SEM_IDEMPOTENCIA) {
            carteiraId = seedDadosKafka.reiniciarCarteiraSemIdempotencia();
            consumidorSemIdempotencia.prepararParaReceber(QUANTIDADE_ENTREGAS);
            publicarDuasVezes(variante.getTopico(), eventoId, carteiraId);
            aguardar(() -> consumidorSemIdempotencia.aguardarRecebimento(TIMEOUT_SEGUNDOS));
            quantidadeEventosConsumidos = consumidorSemIdempotencia.getContadorMensagensRecebidas();
            quantidadeProcessamentosEfetivos = quantidadeEventosConsumidos; // sem controle: todo recebimento credita
        } else {
            carteiraId = seedDadosKafka.reiniciarCarteiraIdempotente();
            consumidorIdempotente.prepararParaReceber(QUANTIDADE_ENTREGAS);
            publicarDuasVezes(variante.getTopico(), eventoId, carteiraId);
            aguardar(() -> consumidorIdempotente.aguardarRecebimento(TIMEOUT_SEGUNDOS));
            quantidadeEventosConsumidos = consumidorIdempotente.getContadorMensagensRecebidas();
            quantidadeProcessamentosEfetivos = consumidorIdempotente.getContadorProcessamentosEfetivos();
        }

        BigDecimal saldoFinal = carteiraRepository.findById(carteiraId).orElseThrow().getSaldo();
        long duracaoMs = Duration.between(inicio, Instant.now()).toMillis();

        VarianteExecucao varianteExecucao = variante == VarianteKafka.SEM_IDEMPOTENCIA
                ? VarianteExecucao.PROBLEMATICO
                : VarianteExecucao.CORRIGIDO;

        Map<String, Object> metricas = Map.of(
                "tecnica", variante.name(),
                "saldoEsperado", VALOR_DO_EVENTO,
                "saldoFinal", saldoFinal,
                "quantidadeEventosConsumidos", quantidadeEventosConsumidos,
                "quantidadeProcessamentosEfetivos", quantidadeProcessamentosEfetivos
        );

        return new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(),
                "kafka-idempotencia",
                varianteExecucao,
                OrigemDados.REAL,
                inicio,
                duracaoMs,
                metricas
        );
    }

    private void publicarDuasVezes(String topico, UUID eventoId, Long carteiraId) {
        EventoPagamentoConfirmado evento = new EventoPagamentoConfirmado(eventoId, carteiraId, VALOR_DO_EVENTO);
        produtor.publicar(topico, evento);
        produtor.publicar(topico, evento);
    }

    private void aguardar(EsperaComTimeout espera) {
        try {
            boolean concluiuATempo = espera.aguardar();
            if (!concluiuATempo) {
                throw new LaboratorioIndisponivelException(
                        "Timeout aguardando o processamento dos eventos Kafka — verifique se o "
                                + "profile 'messaging' do docker-compose está no ar.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LaboratorioIndisponivelException("Espera pelo processamento Kafka foi interrompida.");
        }
    }

    @FunctionalInterface
    private interface EsperaComTimeout {
        boolean aguardar() throws InterruptedException;
    }
}
