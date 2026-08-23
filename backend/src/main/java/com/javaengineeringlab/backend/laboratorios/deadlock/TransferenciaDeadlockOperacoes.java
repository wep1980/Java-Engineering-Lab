package com.javaengineeringlab.backend.laboratorios.deadlock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * As duas técnicas de travamento comparadas pelo laboratório de
 * Deadlock -- ver SPEC-LAB-DEADLOCK-001-deadlock.md.
 */
@Service
public class TransferenciaDeadlockOperacoes {

    private final ContaBancariaDeadlockRepository repositorio;

    public TransferenciaDeadlockOperacoes(ContaBancariaDeadlockRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Trava a conta de origem primeiro, depois a de destino -- a ordem
     * literal da transferência. Duas transferências concorrentes em
     * direções opostas (A→B e B→A) travam suas contas em ordens
     * opostas: exatamente o cenário mínimo de deadlock.
     */
    @Transactional
    public void transferirNaOrdemDada(Long origemId, Long destinoId, BigDecimal valor, long atrasoSimuladoMs) {
        repositorio.reduzirTimeoutDeDeteccaoDeDeadlock();
        ContaBancariaDeadlock origem = repositorio.buscarParaAtualizar(origemId).orElseThrow();
        aguardar(atrasoSimuladoMs);
        ContaBancariaDeadlock destino = repositorio.buscarParaAtualizar(destinoId).orElseThrow();

        origem.debitar(valor);
        destino.depositar(valor);
        repositorio.save(origem);
        repositorio.save(destino);
    }

    /**
     * Trava as contas em ordem ascendente de ID, independente da
     * direção da transferência -- elimina matematicamente a
     * possibilidade de espera circular: as duas transferências
     * concorrentes sempre disputam a mesma conta primeiro (a de menor
     * ID), nunca em ordens opostas.
     */
    @Transactional
    public void transferirEmOrdemConsistente(Long origemId, Long destinoId, BigDecimal valor, long atrasoSimuladoMs) {
        repositorio.reduzirTimeoutDeDeteccaoDeDeadlock();
        Long primeiroId = Math.min(origemId, destinoId);
        Long segundoId = Math.max(origemId, destinoId);

        ContaBancariaDeadlock primeira = repositorio.buscarParaAtualizar(primeiroId).orElseThrow();
        aguardar(atrasoSimuladoMs);
        ContaBancariaDeadlock segunda = repositorio.buscarParaAtualizar(segundoId).orElseThrow();

        ContaBancariaDeadlock origem = origemId.equals(primeiroId) ? primeira : segunda;
        ContaBancariaDeadlock destino = origemId.equals(primeiroId) ? segunda : primeira;

        origem.debitar(valor);
        destino.depositar(valor);
        repositorio.save(origem);
        repositorio.save(destino);
    }

    private void aguardar(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
