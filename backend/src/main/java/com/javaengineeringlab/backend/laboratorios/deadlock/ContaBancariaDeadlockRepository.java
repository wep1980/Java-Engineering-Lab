package com.javaengineeringlab.backend.laboratorios.deadlock;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContaBancariaDeadlockRepository extends JpaRepository<ContaBancariaDeadlock, Long> {

    Optional<ContaBancariaDeadlock> findByTitular(String titular);

    /**
     * SELECT ... FOR UPDATE — trava a linha até o commit/rollback da
     * transação. Duas travas concorrentes em ordem oposta é exatamente
     * o cenário mínimo de deadlock que este laboratório demonstra.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ContaBancariaDeadlock c WHERE c.id = :id")
    Optional<ContaBancariaDeadlock> buscarParaAtualizar(@Param("id") Long id);

    /**
     * Escopo LOCAL -- só a transação atual, revertido automaticamente
     * no commit/rollback. Reduz a espera padrão de 1s do PostgreSQL
     * para detectar o ciclo, sem alterar configuração global do banco
     * (ver SPEC-LAB-DEADLOCK-001, RNF-03).
     */
    @Modifying
    @Query(value = "SET LOCAL deadlock_timeout = '200ms'", nativeQuery = true)
    void reduzirTimeoutDeDeteccaoDeDeadlock();
}
