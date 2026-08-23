package com.javaengineeringlab.backend.laboratorios.deadlock;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;

/**
 * Conta bancária de demonstração do laboratório de Deadlock — ver
 * specs/labs/SPEC-LAB-DEADLOCK-001-deadlock.md.
 */
@Entity
public class ContaBancariaDeadlock {

    static final BigDecimal SALDO_INICIAL = BigDecimal.valueOf(500);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titular;

    private BigDecimal saldo = BigDecimal.ZERO;

    protected ContaBancariaDeadlock() {
    }

    public ContaBancariaDeadlock(String titular) {
        this.titular = titular;
    }

    public void depositar(BigDecimal valor) {
        this.saldo = this.saldo.add(valor);
    }

    public void debitar(BigDecimal valor) {
        this.saldo = this.saldo.subtract(valor);
    }

    public void reiniciarSaldo() {
        this.saldo = SALDO_INICIAL;
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
