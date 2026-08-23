package com.javaengineeringlab.backend.laboratorios.kafka;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InicializadorDadosKafka implements ApplicationRunner {

    private final SeedDadosKafka seedDadosKafka;

    public InicializadorDadosKafka(SeedDadosKafka seedDadosKafka) {
        this.seedDadosKafka = seedDadosKafka;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedDadosKafka.garantirCarteirasDemonstracao();
    }
}
