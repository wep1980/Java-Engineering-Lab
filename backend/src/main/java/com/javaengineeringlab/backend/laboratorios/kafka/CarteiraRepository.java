package com.javaengineeringlab.backend.laboratorios.kafka;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarteiraRepository extends JpaRepository<Carteira, Long> {

    Optional<Carteira> findByTitular(String titular);
}
