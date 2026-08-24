package com.javaengineeringlab.backend.laboratorios.indice;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Registro de demonstração do laboratório de Query sem índice — ver
 * specs/labs/SPEC-LAB-INDICE-001-query-sem-indice.md. A coluna
 * "email" NÃO tem nenhuma restrição de unicidade nem índice no
 * mapeamento -- de propósito, para que a variante "sem-indice" não
 * tenha mesmo nenhum índice disponível.
 */
@Entity
public class RegistroBusca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String nome;

    protected RegistroBusca() {
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNome() {
        return nome;
    }
}
