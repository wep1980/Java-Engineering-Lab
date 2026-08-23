package com.javaengineeringlab.backend.laboratorios.kafka;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegistroProcessamentoRepository extends JpaRepository<RegistroProcessamento, Long> {

    boolean existsByEventoId(UUID eventoId);
}
