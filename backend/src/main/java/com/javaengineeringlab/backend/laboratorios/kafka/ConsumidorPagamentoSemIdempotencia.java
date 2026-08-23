package com.javaengineeringlab.backend.laboratorios.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Variante problemática: credita a carteira em toda mensagem recebida,
 * mesmo que o mesmo evento já tenha sido processado antes. Ver
 * SPEC-LAB-KAFKA-IDEMP-001, RF-02.
 */
@Component
public class ConsumidorPagamentoSemIdempotencia {

    static final String TOPICO = "pagamentos-confirmados-sem-idempotencia";

    private final CreditoSemIdempotenciaOperacao creditoOperacao;
    private final AtomicInteger contadorMensagensRecebidas = new AtomicInteger();
    private volatile CountDownLatch latch = new CountDownLatch(0);

    public ConsumidorPagamentoSemIdempotencia(CreditoSemIdempotenciaOperacao creditoOperacao) {
        this.creditoOperacao = creditoOperacao;
    }

    public void prepararParaReceber(int quantidadeEsperada) {
        contadorMensagensRecebidas.set(0);
        latch = new CountDownLatch(quantidadeEsperada);
    }

    public boolean aguardarRecebimento(long timeoutSegundos) throws InterruptedException {
        return latch.await(timeoutSegundos, TimeUnit.SECONDS);
    }

    public int getContadorMensagensRecebidas() {
        return contadorMensagensRecebidas.get();
    }

    @KafkaListener(topics = TOPICO, groupId = "java-engineering-lab-sem-idempotencia")
    public void receber(EventoPagamentoConfirmado evento) {
        // creditoOperacao.creditar(...) é @Transactional em um bean à
        // parte — a chamada só retorna depois do commit, então contar a
        // mensagem como recebida aqui é seguro (ver CreditoSemIdempotenciaOperacao).
        creditoOperacao.creditar(evento.carteiraId(), evento.valor());
        contadorMensagensRecebidas.incrementAndGet();
        latch.countDown();
    }
}
