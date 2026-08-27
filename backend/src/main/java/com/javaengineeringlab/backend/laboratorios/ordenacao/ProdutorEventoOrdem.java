package com.javaengineeringlab.backend.laboratorios.ordenacao;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Achado real durante a implementação: bloquear em {@code .get()} logo
 * após cada publicação serializava o envio inteiro (evento N+1 só era
 * publicado depois do ACK do evento N) -- sem nenhuma sobreposição
 * real entre partições, a "corrida" entre as 3 threads do consumidor
 * nunca acontecia de verdade, e a variante `sem-chave-particionamento`
 * chegava sempre em ordem por acidente. Corrigido: os métodos abaixo
 * só disparam o envio (não bloqueiam); quem chama dispara todos os 20
 * antes de aguardar qualquer resultado -- ver
 * "Publicação em lote, não bloqueante" em
 * specs/labs/SPEC-LAB-ORDEM-001-ordenacao-de-eventos.md.
 */
@Service
public class ProdutorEventoOrdem {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ProdutorEventoOrdem(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Dispara a publicação numa partição explícita, sem chave -- ver
     * "Round-robin explícito" na SPEC. Não bloqueia.
     */
    public CompletableFuture<SendResult<String, Object>> enviarSemChave(String topico, int particao, EventoOrdem evento) {
        return kafkaTemplate.send(topico, particao, null, evento);
    }

    /**
     * Dispara a publicação com uma chave consistente -- o particionador
     * padrão do Kafka garante que a mesma chave sempre cai na mesma
     * partição. Não bloqueia.
     */
    public CompletableFuture<SendResult<String, Object>> enviarComChave(String topico, String chave, EventoOrdem evento) {
        return kafkaTemplate.send(topico, chave, evento);
    }
}
