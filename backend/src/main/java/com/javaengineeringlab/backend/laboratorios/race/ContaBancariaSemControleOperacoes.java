package com.javaengineeringlab.backend.laboratorios.race;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Variante problemática: lê, espera (amplia a janela de corrida — ver
 * SPEC-LAB-RACE-001), escreve. Sem nenhum controle de concorrência.
 */
@Service
public class ContaBancariaSemControleOperacoes {

    private final ContaBancariaRepository repositorio;

    public ContaBancariaSemControleOperacoes(ContaBancariaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public void depositar(Long contaId, BigDecimal valor, long atrasoSimuladoMs) {
        ContaBancaria conta = repositorio.findById(contaId).orElseThrow();
        aguardar(atrasoSimuladoMs);
        conta.depositar(valor);
        repositorio.save(conta);
    }

    private void aguardar(long ms) {
        if (ms <= 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
