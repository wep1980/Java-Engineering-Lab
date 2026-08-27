package com.javaengineeringlab.backend.laboratorios.outbox;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Nomeada {@code PedidoOutbox}, não {@code Pedido} -- de propósito.
 * Achado real durante a implementação: já existe uma entidade
 * {@code Pedido}/tabela {@code pedido} no laboratório de N+1
 * ({@code laboratorios.n1.Pedido}), de domínio completamente diferente.
 * Usar o mesmo nome simples aqui colidiria tanto no nome do bean
 * Spring Data (`pedidoRepository`) quanto na tabela JPA -- ver
 * "Achados reais" em specs/labs/SPEC-LAB-OUTBOX-001-transactional-outbox.md.
 */
@Entity
@Table(name = "pedido_outbox")
public class PedidoOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;

    private BigDecimal valor;

    private Instant criadoEm;

    protected PedidoOutbox() {
    }

    public PedidoOutbox(String descricao, BigDecimal valor) {
        this.descricao = descricao;
        this.valor = valor;
        this.criadoEm = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
