package com.javaengineeringlab.backend.laboratorios.kafka;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Ledger de idempotência: um eventoId aqui presente significa que o
 * evento já produziu seu efeito de negócio — reprocessá-lo deve ser
 * ignorado. Constraint de unicidade em eventoId é defesa em
 * profundidade (ver SPEC-LAB-KAFKA-IDEMP-001, RNF-03).
 */
@Entity
@Table(name = "registro_processamento")
public class RegistroProcessamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private UUID eventoId;

    private Instant processadoEm;

    protected RegistroProcessamento() {
    }

    public RegistroProcessamento(UUID eventoId) {
        this.eventoId = eventoId;
        this.processadoEm = Instant.now();
    }

    public UUID getEventoId() {
        return eventoId;
    }
}
