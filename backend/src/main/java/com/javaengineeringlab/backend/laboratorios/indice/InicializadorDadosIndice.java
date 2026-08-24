package com.javaengineeringlab.backend.laboratorios.indice;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InicializadorDadosIndice implements ApplicationRunner {

    private final SeedRegistrosBusca seedRegistrosBusca;

    public InicializadorDadosIndice(SeedRegistrosBusca seedRegistrosBusca) {
        this.seedRegistrosBusca = seedRegistrosBusca;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedRegistrosBusca.garantirRegistrosDemonstracao();
    }
}
