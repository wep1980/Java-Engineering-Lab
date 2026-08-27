package com.javaengineeringlab.backend.laboratorios.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Registro do evento pendente de publicação, escrito na mesma transação
 * local do agregado de negócio -- é essa atomicidade (só o banco precisa
 * garantir, não banco + Kafka) que dá nome ao padrão Transactional
 * Outbox. Ver specs/labs/SPEC-LAB-OUTBOX-001-transactional-outbox.md.
 */
@Entity
@Table(name = "outbox_evento")
public class OutboxEvento {

    @Id
    private UUID id;

    private Long agregadoId;

    private String tipoEvento;

    @Column(columnDefinition = "text")
    private String payload;

    @Enumerated(EnumType.STRING)
    private StatusOutboxEvento status;

    private Instant criadoEm;

    private Instant publicadoEm;

    protected OutboxEvento() {
    }

    public OutboxEvento(Long agregadoId, String tipoEvento, String payload) {
        this.id = UUID.randomUUID();
        this.agregadoId = agregadoId;
        this.tipoEvento = tipoEvento;
        this.payload = payload;
        this.status = StatusOutboxEvento.PENDENTE;
        this.criadoEm = Instant.now();
    }

    public void marcarComoPublicado() {
        this.status = StatusOutboxEvento.PUBLICADO;
        this.publicadoEm = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Long getAgregadoId() {
        return agregadoId;
    }

    public String getPayload() {
        return payload;
    }

    public StatusOutboxEvento getStatus() {
        return status;
    }
}
