package com.javaengineeringlab.backend.laboratorios.ordenacao;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@code concurrency = 3} -- uma thread por partição, o cenário real de
 * um consumidor que escala horizontalmente. Ver "Consumidor com
 * concurrency = 3" em
 * specs/labs/SPEC-LAB-ORDEM-001-ordenacao-de-eventos.md.
 */
@Component
public class ConsumidorOrdem {

    static final String TOPICO = "eventos-ordem";
    private static final String GRUPO = "java-engineering-lab-ordenacao";

    private final List<Integer> sequenciasRecebidas = Collections.synchronizedList(new ArrayList<>());
    private final AtomicReference<UUID> execucaoAtual = new AtomicReference<>();
    private final AtomicReference<CountDownLatch> latchRef = new AtomicReference<>(new CountDownLatch(0));

    public void prepararParaReceber(UUID execucaoId, int quantidadeEsperada) {
        execucaoAtual.set(execucaoId);
        sequenciasRecebidas.clear();
        latchRef.set(new CountDownLatch(quantidadeEsperada));
    }

    public boolean aguardarRecebimento(long timeoutSegundos) throws InterruptedException {
        return latchRef.get().await(timeoutSegundos, TimeUnit.SECONDS);
    }

    public List<Integer> getSequenciasRecebidas() {
        synchronized (sequenciasRecebidas) {
            return List.copyOf(sequenciasRecebidas);
        }
    }

    @KafkaListener(
            topics = TOPICO,
            groupId = GRUPO,
            concurrency = "3",
            properties = "spring.json.value.default.type=com.javaengineeringlab.backend.laboratorios.ordenacao.EventoOrdem"
    )
    public void receber(EventoOrdem evento) {
        if (!evento.execucaoId().equals(execucaoAtual.get())) {
            // evento de uma execução anterior ainda presente no tópico
            return;
        }
        sequenciasRecebidas.add(evento.sequencia());
        latchRef.get().countDown();
    }
}
