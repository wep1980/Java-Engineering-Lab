package com.javaengineeringlab.backend.laboratorios.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProdutorEventoPagamento {

    // O JsonSerializer serializa pelo tipo em tempo de execução, então a
    // desserialização continua correta apesar do parâmetro genérico aqui.
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ProdutorEventoPagamento(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicar(String topico, EventoPagamentoConfirmado evento) {
        // chave = carteiraId garante que eventos da mesma carteira caiam
        // sempre na mesma particao, preservando ordem entre eles.
        kafkaTemplate.send(topico, evento.carteiraId().toString(), evento);
    }
}
