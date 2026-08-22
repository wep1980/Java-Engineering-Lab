package com.javaengineeringlab.backend.plataforma;

/**
 * Origem de uma métrica exibida ao usuário. Ver
 * specs/manifest/MANIFESTO.md e specs/architecture/SPEC-JEL-003-mvp-plataforma-base.md —
 * nunca apresentar SIMULADO/ESTIMADO como se fosse REAL.
 */
public enum OrigemDados {
    REAL,
    SIMULADO,
    ESTIMADO
}
