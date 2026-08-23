package com.javaengineeringlab.backend.laboratorios.race;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Variante Pessimistic Locking: buscarParaAtualizar emite
 * SELECT ... FOR UPDATE, bloqueando outras transações que tentem
 * adquirir o mesmo lock até esta commitar. O acesso concorrente vira
 * sequencial nesta única linha — sem perda, mas mais lento sob
 * concorrência (ver comparação de duracaoMs entre variantes).
 */
@Service
public class ContaBancariaPessimistaOperacoes {

    private final ContaBancariaRepository repositorio;

    public ContaBancariaPessimistaOperacoes(ContaBancariaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public void depositar(Long contaId, BigDecimal valor, long atrasoSimuladoMs) {
        ContaBancaria conta = repositorio.buscarParaAtualizar(contaId).orElseThrow();
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
