package com.javaengineeringlab.backend.laboratorios.kafka;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Isolada em um bean próprio (em vez de um método @Transactional dentro
 * do @KafkaListener) para que a transação commite antes do listener
 * contar a mensagem como processada — chamar um método @Transactional
 * na mesma classe do listener não passaria pelo proxy do Spring, e
 * countDown() executado antes do commit criaria uma corrida real entre
 * "a mensagem foi processada" e "a escrita está durável".
 */
@Service
public class CreditoSemIdempotenciaOperacao {

    private final CarteiraRepository carteiraRepository;

    public CreditoSemIdempotenciaOperacao(CarteiraRepository carteiraRepository) {
        this.carteiraRepository = carteiraRepository;
    }

    @Transactional
    public void creditar(Long carteiraId, BigDecimal valor) {
        Carteira carteira = carteiraRepository.findById(carteiraId).orElseThrow();
        carteira.creditar(valor);
        carteiraRepository.save(carteira);
    }
}
