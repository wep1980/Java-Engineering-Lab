package com.javaengineeringlab.backend.laboratorios.race;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Variante Optimistic Locking: mesmo padrão leitura-espera-escrita, mas
 * a coluna @Version faz o commit falhar com
 * ObjectOptimisticLockingFailureException se outra transação já
 * alterou a linha entre a leitura e a escrita desta. Não faz
 * retentativa — isso é responsabilidade de quem chama
 * (ExecucaoRaceConditionService), para deixar explícito que o dado
 * nunca é perdido silenciosamente: ou aplica, ou falha de forma visível.
 */
@Service
public class ContaBancariaVersionadaOperacoes {

    private final ContaBancariaVersionadaRepository repositorio;

    public ContaBancariaVersionadaOperacoes(ContaBancariaVersionadaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public void depositar(Long contaId, BigDecimal valor, long atrasoSimuladoMs) {
        ContaBancariaVersionada conta = repositorio.findById(contaId).orElseThrow();
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
