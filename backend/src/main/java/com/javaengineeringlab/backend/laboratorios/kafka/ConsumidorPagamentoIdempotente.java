package com.javaengineeringlab.backend.laboratorios.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Variante idempotente: usa RegistroProcessamento como chave de
 * deduplicação — se o eventoId já foi processado, ignora sem repetir o
 * efeito de negócio. "Verificar-então-inserir" é seguro aqui porque o
 * container de listener do Spring Kafka processa uma partição
 * sequencialmente em uma única thread (ver SPEC-LAB-KAFKA-IDEMP-001,
 * seção "Por que verificar-então-inserir é seguro aqui").
 */
@Component
public class ConsumidorPagamentoIdempotente {

    static final String TOPICO = "pagamentos-confirmados-idempotente";

    private final CreditoIdempotenteOperacao creditoOperacao;
    private final AtomicInteger contadorMensagensRecebidas = new AtomicInteger();
    private final AtomicInteger contadorProcessamentosEfetivos = new AtomicInteger();
    private final AtomicReference<CountDownLatch> latchRef = new AtomicReference<>(new CountDownLatch(0));

    public ConsumidorPagamentoIdempotente(CreditoIdempotenteOperacao creditoOperacao) {
        this.creditoOperacao = creditoOperacao;
    }

    public void prepararParaReceber(int quantidadeEsperada) {
        contadorMensagensRecebidas.set(0);
        contadorProcessamentosEfetivos.set(0);
        latchRef.set(new CountDownLatch(quantidadeEsperada));
    }

    public boolean aguardarRecebimento(long timeoutSegundos) throws InterruptedException {
        return latchRef.get().await(timeoutSegundos, TimeUnit.SECONDS);
    }

    public int getContadorMensagensRecebidas() {
        return contadorMensagensRecebidas.get();
    }

    public int getContadorProcessamentosEfetivos() {
        return contadorProcessamentosEfetivos.get();
    }

    @KafkaListener(topics = TOPICO, groupId = "java-engineering-lab-idempotente")
    public void receber(EventoPagamentoConfirmado evento) {
        // creditarSeNovo é @Transactional em um bean à parte — só retorna
        // depois do commit (ou de constatar que já foi processado).
        boolean aplicouEfeito = creditoOperacao.creditarSeNovo(evento.eventoId(), evento.carteiraId(), evento.valor());
        contadorMensagensRecebidas.incrementAndGet();
        if (aplicouEfeito) {
            contadorProcessamentosEfetivos.incrementAndGet();
        }
        latchRef.get().countDown();
    }
}
