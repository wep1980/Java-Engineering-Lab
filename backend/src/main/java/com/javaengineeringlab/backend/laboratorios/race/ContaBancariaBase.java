package com.javaengineeringlab.backend.laboratorios.race;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.math.BigDecimal;

/**
 * Campos e comportamento comuns às duas variantes de persistência da
 * conta bancária do laboratório de Race Condition — ver
 * specs/labs/SPEC-LAB-RACE-001-race-condition-lost-update.md ("Por que
 * duas entidades").
 */
@MappedSuperclass
public abstract class ContaBancariaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titular;

    private BigDecimal saldo = BigDecimal.ZERO;

    protected ContaBancariaBase() {
    }

    protected ContaBancariaBase(String titular) {
        this.titular = titular;
    }

    public void depositar(BigDecimal valor) {
        this.saldo = this.saldo.add(valor);
    }

    public void reiniciarSaldo() {
        this.saldo = BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public String getTitular() {
        return titular;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}
