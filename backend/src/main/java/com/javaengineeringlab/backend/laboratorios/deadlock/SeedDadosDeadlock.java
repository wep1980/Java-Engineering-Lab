package com.javaengineeringlab.backend.laboratorios.deadlock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Duas contas de demonstração do laboratório de Deadlock -- ver
 * SPEC-LAB-DEADLOCK-001-deadlock.md. A conta A é sempre criada antes da
 * B, garantindo idA < idB (usado pela variante de ordenação
 * consistente).
 */
@Component
public class SeedDadosDeadlock {

    static final String TITULAR_CONTA_A = "Conta A (demonstração de deadlock)";
    static final String TITULAR_CONTA_B = "Conta B (demonstração de deadlock)";

    private final ContaBancariaDeadlockRepository repositorio;

    public SeedDadosDeadlock(ContaBancariaDeadlockRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public void garantirContasDemonstracao() {
        if (repositorio.findByTitular(TITULAR_CONTA_A).isEmpty()) {
            repositorio.save(new ContaBancariaDeadlock(TITULAR_CONTA_A));
        }
        if (repositorio.findByTitular(TITULAR_CONTA_B).isEmpty()) {
            repositorio.save(new ContaBancariaDeadlock(TITULAR_CONTA_B));
        }
    }

    @Transactional
    public ContasDemonstracao reiniciarContas() {
        ContaBancariaDeadlock contaA = repositorio.findByTitular(TITULAR_CONTA_A).orElseThrow();
        ContaBancariaDeadlock contaB = repositorio.findByTitular(TITULAR_CONTA_B).orElseThrow();
        contaA.reiniciarSaldo();
        contaB.reiniciarSaldo();
        return new ContasDemonstracao(contaA.getId(), contaB.getId());
    }

    public record ContasDemonstracao(Long idContaA, Long idContaB) {
    }
}
