package com.javaengineeringlab.backend.laboratorios.race;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contas de demonstração do laboratório de Race Condition. Uma conta sem
 * versionamento (reutilizada pelas variantes "sem-controle" e
 * "pessimista") e uma conta versionada (variante "otimista") — ver
 * SPEC-LAB-RACE-001, seção "Por que duas entidades".
 */
@Component
public class SeedDadosRace {

    static final String TITULAR_SEM_VERSIONAMENTO = "Conta de demonstração (sem versionamento)";
    static final String TITULAR_VERSIONADA = "Conta de demonstração (versionada)";

    private final ContaBancariaRepository contaBancariaRepository;
    private final ContaBancariaVersionadaRepository contaBancariaVersionadaRepository;

    public SeedDadosRace(
            ContaBancariaRepository contaBancariaRepository,
            ContaBancariaVersionadaRepository contaBancariaVersionadaRepository
    ) {
        this.contaBancariaRepository = contaBancariaRepository;
        this.contaBancariaVersionadaRepository = contaBancariaVersionadaRepository;
    }

    @Transactional
    public void garantirContasDemonstracao() {
        if (contaBancariaRepository.findByTitular(TITULAR_SEM_VERSIONAMENTO).isEmpty()) {
            contaBancariaRepository.save(new ContaBancaria(TITULAR_SEM_VERSIONAMENTO));
        }
        if (contaBancariaVersionadaRepository.findByTitular(TITULAR_VERSIONADA).isEmpty()) {
            contaBancariaVersionadaRepository.save(new ContaBancariaVersionada(TITULAR_VERSIONADA));
        }
    }

    @Transactional
    public Long reiniciarContaSemVersionamento() {
        ContaBancaria conta = contaBancariaRepository.findByTitular(TITULAR_SEM_VERSIONAMENTO).orElseThrow();
        conta.reiniciarSaldo();
        return conta.getId();
    }

    @Transactional
    public Long reiniciarContaVersionada() {
        ContaBancariaVersionada conta = contaBancariaVersionadaRepository.findByTitular(TITULAR_VERSIONADA).orElseThrow();
        conta.reiniciarSaldo();
        return conta.getId();
    }
}
