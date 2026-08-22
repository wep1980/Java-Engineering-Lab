package com.javaengineeringlab.backend.plataforma;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Catálogo de laboratórios. Lista fixa em memória — sem persistência
 * nesta fase (ver RNF-03 de SPEC-JEL-003). Cada laboratório novo entra
 * aqui quando sua SPEC (specs/labs/) for aprovada.
 */
@Service
public class CatalogoLaboratoriosService {

    private static final List<LaboratorioResumo> LABORATORIOS = List.of(
            new LaboratorioResumo(
                    "n1-queries",
                    "N+1 Queries",
                    "Demonstrar o problema de N+1 consultas com JPA/Hibernate e suas soluções (JOIN FETCH, EntityGraph, DTO Projection).",
                    StatusLaboratorio.PLANEJADO
            )
    );

    public List<LaboratorioResumo> listar() {
        return LABORATORIOS;
    }

    public LaboratorioResumo buscarPorId(String id) {
        return LABORATORIOS.stream()
                .filter(laboratorio -> laboratorio.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new LaboratorioNaoEncontradoException(id));
    }
}
