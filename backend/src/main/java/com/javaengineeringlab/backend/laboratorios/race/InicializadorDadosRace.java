package com.javaengineeringlab.backend.laboratorios.race;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InicializadorDadosRace implements ApplicationRunner {

    private final SeedDadosRace seedDadosRace;

    public InicializadorDadosRace(SeedDadosRace seedDadosRace) {
        this.seedDadosRace = seedDadosRace;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedDadosRace.garantirContasDemonstracao();
    }
}
