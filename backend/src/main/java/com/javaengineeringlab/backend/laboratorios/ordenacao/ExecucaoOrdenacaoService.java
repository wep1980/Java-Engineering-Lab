package com.javaengineeringlab.backend.laboratorios.ordenacao;

import com.javaengineeringlab.backend.plataforma.LaboratorioIndisponivelException;
import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Orquestra as duas variantes do laboratório de Ordenação de Eventos --
 * ver specs/labs/SPEC-LAB-ORDEM-001-ordenacao-de-eventos.md.
 */
@Service
public class ExecucaoOrdenacaoService {

    static final int QUANTIDADE_EVENTOS = 20;
    private static final long TIMEOUT_SEGUNDOS = 15;

    private final ConsumidorOrdem consumidor;
    private final ProdutorEventoOrdem produtor;

    public ExecucaoOrdenacaoService(ConsumidorOrdem consumidor, ProdutorEventoOrdem produtor) {
        this.consumidor = consumidor;
        this.produtor = produtor;
    }

    public ResultadoExecucaoLaboratorio executar(VarianteOrdenacao variante) {
        Instant inicio = Instant.now();
        UUID execucaoId = UUID.randomUUID();

        consumidor.prepararParaReceber(execucaoId, QUANTIDADE_EVENTOS);

        // Dispara as 20 publicações antes de aguardar qualquer uma
        // delas -- bloquear em cada envio individualmente serializaria
        // a publicação inteira, eliminando a sobreposição real entre
        // partições que este laboratório depende para demonstrar a
        // corrida de consumo. Ver "Achados reais" na SPEC.
        List<CompletableFuture<SendResult<String, Object>>> envios = new ArrayList<>(QUANTIDADE_EVENTOS);
        for (int sequencia = 0; sequencia < QUANTIDADE_EVENTOS; sequencia++) {
            EventoOrdem evento = new EventoOrdem(execucaoId, sequencia);
            envios.add(variante == VarianteOrdenacao.SEM_CHAVE_PARTICIONAMENTO
                    ? produtor.enviarSemChave(ConsumidorOrdem.TOPICO, sequencia % TopicoOrdenacaoConfig.QUANTIDADE_PARTICOES, evento)
                    : produtor.enviarComChave(ConsumidorOrdem.TOPICO, execucaoId.toString(), evento));
        }

        Set<Integer> particoesUsadas = new HashSet<>();
        try {
            for (CompletableFuture<SendResult<String, Object>> envio : envios) {
                particoesUsadas.add(envio.get(5, TimeUnit.SECONDS).getRecordMetadata().partition());
            }
        } catch (Exception falhaRealDePublicacao) {
            throw new LaboratorioIndisponivelException(
                    "Falha ao publicar no tópico " + ConsumidorOrdem.TOPICO + " -- verifique se o "
                            + "profile 'messaging' do docker-compose está no ar.");
        }

        aguardar();

        List<Integer> ordemRecebida = consumidor.getSequenciasRecebidas();
        List<Integer> ordemEsperada = IntStream.range(0, QUANTIDADE_EVENTOS).boxed().toList();
        boolean ordemPreservada = ordemRecebida.equals(ordemEsperada);

        long duracaoMs = Duration.between(inicio, Instant.now()).toMillis();

        VarianteExecucao varianteExecucao = variante == VarianteOrdenacao.SEM_CHAVE_PARTICIONAMENTO
                ? VarianteExecucao.PROBLEMATICO
                : VarianteExecucao.CORRIGIDO;

        Map<String, Object> metricas = Map.of(
                "tecnica", variante.name(),
                "quantidadeEventos", QUANTIDADE_EVENTOS,
                "quantidadeParticoesUsadas", particoesUsadas.size(),
                "ordemPreservada", ordemPreservada,
                "ordemRecebida", ordemRecebida
        );

        return new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(),
                "ordenacao-de-eventos",
                varianteExecucao,
                OrigemDados.REAL,
                inicio,
                duracaoMs,
                metricas
        );
    }

    private void aguardar() {
        try {
            boolean concluiuATempo = consumidor.aguardarRecebimento(TIMEOUT_SEGUNDOS);
            if (!concluiuATempo) {
                throw new LaboratorioIndisponivelException(
                        "Timeout aguardando o consumo dos eventos Kafka — verifique se o "
                                + "profile 'messaging' do docker-compose está no ar.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LaboratorioIndisponivelException("Espera pelo consumo dos eventos foi interrompida.");
        }
    }
}
