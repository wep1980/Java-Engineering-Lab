package com.javaengineeringlab.backend.laboratorios.race;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContaBancariaVersionadaRepository extends JpaRepository<ContaBancariaVersionada, Long> {

    Optional<ContaBancariaVersionada> findByTitular(String titular);
}
