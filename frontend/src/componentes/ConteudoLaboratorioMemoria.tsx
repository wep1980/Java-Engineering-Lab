"use client";

import { useState } from "react";
import { PainelAssistenteIA } from "@/componentes/PainelAssistenteIA";
import { PainelExecucaoMemoria } from "@/componentes/PainelExecucaoMemoria";

export function ConteudoLaboratorioMemoria() {
  const [ultimoResultado, setUltimoResultado] = useState<Record<string, unknown> | null>(null);

  return (
    <div className="flex flex-col gap-10">
      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Cenário
        </h2>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          O padrão mais comum de memory leak em aplicações Spring reais
          não é esquecer de fechar algum recurso — é um{" "}
          <strong>bean singleton</strong> (escopo de vida da aplicação
          inteira) guardando <strong>referências fortes</strong> para
          objetos que deveriam ter vida curta. Tipicamente, um cache
          que nunca ganhou política de expiração.
        </p>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          <strong>Nota de segurança:</strong> este backend é
          compartilhado por todos os laboratórios — a demonstração
          nunca provoca um <code>OutOfMemoryError</code> de verdade.
          Cada execução aloca ~20 MB de forma controlada e mede o heap
          real antes/depois de um <code>System.gc()</code> real —
          suficiente para provar a causa raiz, sem nenhum risco à
          estabilidade do processo.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Execução real
        </h2>
        <PainelExecucaoMemoria
          laboratorioId="memory-leak"
          onResultado={setUltimoResultado}
        />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Assistente de engenharia
        </h2>
        <PainelAssistenteIA laboratorioId="memory-leak" ultimoResultado={ultimoResultado} />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Código problemático
        </h2>
        <pre className="overflow-x-auto rounded-lg bg-black/[.04] p-4 text-xs dark:bg-white/[.06]">
          <code>{`@Component // singleton -- vive enquanto a aplicação viver
class CacheComVazamento {
    private final Map<UUID, byte[]> cache = new ConcurrentHashMap<>();
    // nada aqui remove uma entrada -- nunca`}</code>
        </pre>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          O bean é um GC root. Cada entrada adicionada é referenciada
          para sempre — mesmo que, do ponto de vista do negócio, o dado
          já não sirva para nada, o coletor de lixo não pode reclamar
          memória que ainda está referenciada.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Referência forte vs. referência fraca
        </h2>
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="rounded-lg border border-black/[.08] p-4 text-sm dark:border-white/[.145]">
            <p className="font-medium text-black dark:text-zinc-50">
              Com vazamento (<code>Map</code>)
            </p>
            <p className="mt-1 text-zinc-600 dark:text-zinc-400">
              A cache segura uma referência forte a cada entrada. Um{" "}
              <code>System.gc()</code> real, forçado logo depois, não
              recupera nada — a memória continua retida.
            </p>
          </div>
          <div className="rounded-lg border border-emerald-600/30 bg-emerald-50 p-4 text-sm dark:bg-emerald-950/30">
            <p className="font-medium text-emerald-800 dark:text-emerald-300">
              Corrigido (<code>WeakHashMap</code>)
            </p>
            <p className="mt-1 text-emerald-700 dark:text-emerald-400">
              As chaves não têm nenhuma outra referência forte no
              sistema depois que o método retorna — o coletor de lixo
              pode reclamá-las livremente, e quando reclama, a entrada
              inteira desaparece do mapa. Sem nenhuma lógica de
              eviction escrita à mão.
            </p>
          </div>
        </div>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Perguntas comuns de entrevista
        </h2>
        <ul className="flex flex-col gap-2 text-sm text-zinc-600 dark:text-zinc-400">
          <li>— Por que um cache dentro de um `@Service` é um candidato natural a memory leak?</li>
          <li>— Qual a diferença entre referência forte, fraca (`WeakReference`) e soft (`SoftReference`)?</li>
          <li>— `System.gc()` garante que a coleta aconteça? Por que (não)?</li>
          <li>— Como você diagnosticaria um memory leak em produção, sem poder alterar o código?</li>
        </ul>
      </section>
    </div>
  );
}
