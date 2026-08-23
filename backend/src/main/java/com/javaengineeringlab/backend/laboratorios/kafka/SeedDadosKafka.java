package com.javaengineeringlab.backend.laboratorios.kafka;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SeedDadosKafka {

    static final String TITULAR_SEM_IDEMPOTENCIA = "Carteira de demonstração (sem idempotência)";
    static final String TITULAR_IDEMPOTENTE = "Carteira de demonstração (idempotente)";

    private final CarteiraRepository carteiraRepository;

    public SeedDadosKafka(CarteiraRepository carteiraRepository) {
        this.carteiraRepository = carteiraRepository;
    }

    @Transactional
    public void garantirCarteirasDemonstracao() {
        if (carteiraRepository.findByTitular(TITULAR_SEM_IDEMPOTENCIA).isEmpty()) {
            carteiraRepository.save(new Carteira(TITULAR_SEM_IDEMPOTENCIA));
        }
        if (carteiraRepository.findByTitular(TITULAR_IDEMPOTENTE).isEmpty()) {
            carteiraRepository.save(new Carteira(TITULAR_IDEMPOTENTE));
        }
    }

    @Transactional
    public Long reiniciarCarteiraSemIdempotencia() {
        Carteira carteira = carteiraRepository.findByTitular(TITULAR_SEM_IDEMPOTENCIA).orElseThrow();
        carteira.reiniciarSaldo();
        return carteira.getId();
    }

    @Transactional
    public Long reiniciarCarteiraIdempotente() {
        Carteira carteira = carteiraRepository.findByTitular(TITULAR_IDEMPOTENTE).orElseThrow();
        carteira.reiniciarSaldo();
        return carteira.getId();
    }
}
