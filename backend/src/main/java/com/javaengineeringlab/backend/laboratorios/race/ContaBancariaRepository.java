package com.javaengineeringlab.backend.laboratorios.race;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContaBancariaRepository extends JpaRepository<ContaBancaria, Long> {

    Optional<ContaBancaria> findByTitular(String titular);

    /**
     * SELECT ... FOR UPDATE — serializa o acesso concorrente à mesma
     * linha. Usada pela variante "pessimista".
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ContaBancaria c WHERE c.id = :id")
    Optional<ContaBancaria> buscarParaAtualizar(@Param("id") Long id);
}
