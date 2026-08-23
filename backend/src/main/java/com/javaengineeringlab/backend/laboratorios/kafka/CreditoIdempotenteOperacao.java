package com.javaengineeringlab.backend.laboratorios.kafka;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Ver CreditoSemIdempotenciaOperacao — mesmo motivo para ser um bean
 * separado do @KafkaListener.
 */
@Service
public class CreditoIdempotenteOperacao {

    private final CarteiraRepository carteiraRepository;
    private final RegistroProcessamentoRepository registroProcessamentoRepository;

    public CreditoIdempotenteOperacao(
            CarteiraRepository carteiraRepository,
            RegistroProcessamentoRepository registroProcessamentoRepository
    ) {
        this.carteiraRepository = carteiraRepository;
        this.registroProcessamentoRepository = registroProcessamentoRepository;
    }

    /**
     * @return true se o efeito de negócio foi de fato aplicado (evento
     * novo); false se foi ignorado por já ter sido processado.
     */
    @Transactional
    public boolean creditarSeNovo(UUID eventoId, Long carteiraId, BigDecimal valor) {
        if (registroProcessamentoRepository.existsByEventoId(eventoId)) {
            return false;
        }

        registroProcessamentoRepository.save(new RegistroProcessamento(eventoId));
        Carteira carteira = carteiraRepository.findById(carteiraId).orElseThrow();
        carteira.creditar(valor);
        carteiraRepository.save(carteira);
        return true;
    }
}
