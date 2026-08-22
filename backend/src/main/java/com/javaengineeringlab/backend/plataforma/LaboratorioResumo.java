package com.javaengineeringlab.backend.plataforma;

public record LaboratorioResumo(
        String id,
        String nome,
        String objetivo,
        StatusLaboratorio status
) {
}
