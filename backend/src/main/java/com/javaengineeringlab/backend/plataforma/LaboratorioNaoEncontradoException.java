package com.javaengineeringlab.backend.plataforma;

public class LaboratorioNaoEncontradoException extends RuntimeException {

    public LaboratorioNaoEncontradoException(String id) {
        super("Laboratório não encontrado: " + id);
    }
}
