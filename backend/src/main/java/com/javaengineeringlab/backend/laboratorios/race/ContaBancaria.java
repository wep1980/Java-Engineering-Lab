package com.javaengineeringlab.backend.laboratorios.race;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Sem controle de concorrência — usada pelas variantes "sem-controle" e
 * "pessimista" (o lock pessimista é um hint de query, não depende de
 * coluna de versão).
 */
@Entity
@Table(name = "conta_bancaria")
public class ContaBancaria extends ContaBancariaBase {

    protected ContaBancaria() {
    }

    public ContaBancaria(String titular) {
        super(titular);
    }
}
