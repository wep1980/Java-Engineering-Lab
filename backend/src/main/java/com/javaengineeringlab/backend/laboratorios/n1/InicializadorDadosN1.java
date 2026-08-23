package com.javaengineeringlab.backend.laboratorios.n1;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InicializadorDadosN1 implements ApplicationRunner {

    private final SeedDadosN1 seedDadosN1;

    public InicializadorDadosN1(SeedDadosN1 seedDadosN1) {
        this.seedDadosN1 = seedDadosN1;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedDadosN1.garantirDadosDemonstracao();
    }
}
