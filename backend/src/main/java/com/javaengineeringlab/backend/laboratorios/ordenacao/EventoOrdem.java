package com.javaengineeringlab.backend.laboratorios.ordenacao;

import java.util.UUID;

/**
 * Payload do evento. {@code execucaoId} identifica o "agregado" desta
 * execução do laboratório -- só existe pra permitir que o consumidor
 * filtre eventos de execuções anteriores que ainda estejam no tópico,
 * não tem papel na demonstração de particionamento em si.
 */
public record EventoOrdem(
        UUID execucaoId,
        int sequencia
) {
}
