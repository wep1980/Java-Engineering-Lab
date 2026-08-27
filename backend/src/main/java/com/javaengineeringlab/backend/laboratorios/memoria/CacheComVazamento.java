package com.javaengineeringlab.backend.laboratorios.memoria;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean singleton (mesmo escopo de qualquer {@code @Service} do
 * projeto) guardando referências FORTES para sempre -- o vetor real
 * mais comum de memory leak em aplicações Spring: um cache "que
 * parecia uma boa ideia" e nunca ganhou política de expiração. Nada
 * além de remover a entrada explicitamente libera essa memória. Ver
 * specs/labs/SPEC-LAB-MEMLEAK-001-memory-leak.md.
 */
@Component
public class CacheComVazamento {

    private final Map<UUID, byte[]> cache = new ConcurrentHashMap<>();

    public void adicionar(int quantidade, int tamanhoBytesPorEntrada) {
        for (int i = 0; i < quantidade; i++) {
            cache.put(UUID.randomUUID(), new byte[tamanhoBytesPorEntrada]);
        }
    }

    public int tamanho() {
        return cache.size();
    }
}
