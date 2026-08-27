package com.javaengineeringlab.backend.laboratorios.memoria;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Mesmo escopo singleton de {@link CacheComVazamento}, mas com
 * referências FRACAS (`WeakHashMap`) -- assim que nada mais no sistema
 * referencia uma chave, o próprio coletor de lixo a remove, e a
 * entrada inteira (chave e valor) desaparece do mapa, sem nenhuma
 * lógica de eviction escrita à mão. Ver "WeakHashMap, não um cache com
 * limite manual" em specs/labs/SPEC-LAB-MEMLEAK-001-memory-leak.md.
 */
@Component
public class CacheSemVazamento {

    private final Map<UUID, byte[]> cache = Collections.synchronizedMap(new WeakHashMap<>());

    public void adicionar(int quantidade, int tamanhoBytesPorEntrada) {
        for (int i = 0; i < quantidade; i++) {
            // a chave não é mantida em nenhuma variável fora deste
            // laço -- depois que este método retorna, nada no sistema
            // referencia essas chaves além do próprio WeakHashMap.
            cache.put(UUID.randomUUID(), new byte[tamanhoBytesPorEntrada]);
        }
    }

    public int tamanho() {
        // WeakHashMap.size() expurga entradas cuja chave já foi
        // coletada como parte da própria chamada -- reflete o estado
        // real pós-GC, não uma contagem desatualizada.
        return cache.size();
    }
}
