package com.javaengineeringlab.backend.laboratorios.race;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Com Optimistic Locking (@Version) — usada pela variante "otimista".
 */
@Entity
@Table(name = "conta_bancaria_versionada")
public class ContaBancariaVersionada extends ContaBancariaBase {

    @Version
    private Long versao;

    protected ContaBancariaVersionada() {
    }

    public ContaBancariaVersionada(String titular) {
        super(titular);
    }

    public Long getVersao() {
        return versao;
    }
}
