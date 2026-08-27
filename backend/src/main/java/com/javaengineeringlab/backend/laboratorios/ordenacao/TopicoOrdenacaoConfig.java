package com.javaengineeringlab.backend.laboratorios.ordenacao;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * O broker de demonstração cria tópicos automaticamente com 1 partição
 * -- insuficiente para este laboratório, que depende de múltiplas
 * partições reais. Declarar via {@link NewTopic} é consumido pelo
 * {@code KafkaAdmin} já autoconfigurado -- nenhum bean novo do tipo
 * {@code KafkaAdmin}/{@code KafkaTemplate}. Ver "NewTopic explícito" em
 * specs/labs/SPEC-LAB-ORDEM-001-ordenacao-de-eventos.md.
 */
@Configuration
public class TopicoOrdenacaoConfig {

    static final int QUANTIDADE_PARTICOES = 3;

    @Bean
    public NewTopic topicoEventosOrdem() {
        return TopicBuilder.name(ConsumidorOrdem.TOPICO)
                .partitions(QUANTIDADE_PARTICOES)
                .replicas(1)
                .build();
    }
}
