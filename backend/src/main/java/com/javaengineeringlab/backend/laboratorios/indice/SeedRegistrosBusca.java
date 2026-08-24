package com.javaengineeringlab.backend.laboratorios.indice;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Semente de demonstração do laboratório de Query sem índice -- ver
 * SPEC-LAB-INDICE-001-query-sem-indice.md. Inserida uma única vez, na
 * subida da aplicação.
 */
@Component
public class SeedRegistrosBusca {

    static final int QUANTIDADE_REGISTROS = 200_000;

    private final RegistroBuscaRepository repositorio;

    public SeedRegistrosBusca(RegistroBuscaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public void garantirRegistrosDemonstracao() {
        if (repositorio.count() == 0) {
            repositorio.semearRegistros(QUANTIDADE_REGISTROS);
            repositorio.atualizarEstatisticas();
        }
    }
}
