package com.javaengineeringlab.backend.laboratorios.deadlock;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InicializadorDadosDeadlock implements ApplicationRunner {

    private final SeedDadosDeadlock seedDadosDeadlock;

    public InicializadorDadosDeadlock(SeedDadosDeadlock seedDadosDeadlock) {
        this.seedDadosDeadlock = seedDadosDeadlock;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedDadosDeadlock.garantirContasDemonstracao();
    }
}
