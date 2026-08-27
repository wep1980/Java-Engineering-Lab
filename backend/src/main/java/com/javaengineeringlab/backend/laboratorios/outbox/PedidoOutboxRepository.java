package com.javaengineeringlab.backend.laboratorios.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoOutboxRepository extends JpaRepository<PedidoOutbox, Long> {
}
