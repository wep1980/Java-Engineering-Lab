package com.javaengineeringlab.backend.laboratorios.outbox;

import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Publicador direto da variante {@code sem-outbox} -- deliberadamente
 * apontado para um endereço inalcançável ({@code 127.0.0.1:1}), para
 * reproduzir uma falha real de conexão do cliente Kafka (não fabricada)
 * de forma rápida e determinística. Ver "Endereço inalcançável
 * determinístico" em
 * specs/labs/SPEC-LAB-OUTBOX-001-transactional-outbox.md.
 *
 * <p>Construído manualmente (não como {@code @Bean KafkaTemplate}) de
 * propósito -- ver RNF-02 da SPEC e ADR-0009: um segundo bean desse tipo
 * faria a autoconfiguração do Spring Kafka desistir de criar o
 * {@code KafkaTemplate} padrão já usado por
 * {@code laboratorios.kafka.ProdutorEventoPagamento}.
 */
@Component
public class PublicadorDiretoInstavel {

    private static final String ENDERECO_INALCANCAVEL = "127.0.0.1:1";

    private final DefaultKafkaProducerFactory<String, Object> fabricaProdutor;
    private final KafkaTemplate<String, Object> kafkaTemplateQuebrado;

    public PublicadorDiretoInstavel() {
        Map<String, Object> propriedades = new HashMap<>();
        propriedades.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ENDERECO_INALCANCAVEL);
        propriedades.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        propriedades.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        propriedades.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 1000);
        propriedades.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000);
        propriedades.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 2000);

        this.fabricaProdutor = new DefaultKafkaProducerFactory<>(propriedades);
        this.kafkaTemplateQuebrado = new KafkaTemplate<>(fabricaProdutor);
    }

    @PreDestroy
    void encerrar() {
        fabricaProdutor.destroy();
    }

    /**
     * @return {@code true} se a publicação foi confirmada de verdade
     * pelo broker; {@code false} se a tentativa falhou (o cenário
     * garantido pelo endereço inalcançável).
     */
    public boolean tentarPublicar(String topico, String chave, Object evento) {
        try {
            kafkaTemplateQuebrado.send(topico, chave, evento).get(3, TimeUnit.SECONDS);
            return true;
        } catch (Exception falhaRealDeConexao) {
            return false;
        }
    }
}
