"use client";

import { useState } from "react";
import { PainelAssistenteIA } from "@/componentes/PainelAssistenteIA";
import { PainelExecucaoThreadPool } from "@/componentes/PainelExecucaoThreadPool";

export function ConteudoLaboratorioThreadPool() {
  const [ultimoResultado, setUltimoResultado] = useState<Record<string, unknown> | null>(null);

  return (
    <div className="flex flex-col gap-10">
      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Cenário
        </h2>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          <code>Executors.newFixedThreadPool(n)</code> é provavelmente
          a forma mais comum de criar um pool de threads em Java — e
          esconde um problema real: por dentro, usa uma{" "}
          <strong>fila sem limite de tamanho</strong>. Sob carga
          sustentada, a fila cresce indefinidamente — sem nenhum erro,
          sem nenhuma rejeição.
        </p>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Duas execuções, 10 tarefas cada, contra um pool de
          demonstração de apenas 2 threads — completamente isolado do
          pool que atende requisições HTTP deste backend.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Execução real
        </h2>
        <PainelExecucaoThreadPool
          laboratorioId="thread-pool-exhaustion"
          onResultado={setUltimoResultado}
        />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Assistente de engenharia
        </h2>
        <PainelAssistenteIA laboratorioId="thread-pool-exhaustion" ultimoResultado={ultimoResultado} />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Código problemático
        </h2>
        <pre className="overflow-x-auto rounded-lg bg-black/[.04] p-4 text-xs dark:bg-white/[.06]">
          <code>{`ExecutorService pool = Executors.newFixedThreadPool(2);
// por dentro: new ThreadPoolExecutor(2, 2, 0L, MILLISECONDS, new LinkedBlockingQueue<>());
//                                                              ^ SEM LIMITE`}</code>
        </pre>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Nada na assinatura do método deixa isso óbvio. Todas as 10
          tarefas são aceitas — nenhuma rejeição — mas as últimas
          esperam um tempo real considerável na fila antes de sequer
          começar a executar.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Fila ilimitada vs. fila limitada
        </h2>
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="rounded-lg border border-black/[.08] p-4 text-sm dark:border-white/[.145]">
            <p className="font-medium text-black dark:text-zinc-50">
              Fila ilimitada
            </p>
            <p className="mt-1 text-zinc-600 dark:text-zinc-400">
              Aceita tudo, sempre. O backlog cresce silenciosamente sob
              carga sustentada — sem nenhum aviso, até virar um
              problema maior (latência inaceitável, ou memória
              consumida pela própria fila).
            </p>
          </div>
          <div className="rounded-lg border border-emerald-600/30 bg-emerald-50 p-4 text-sm dark:bg-emerald-950/30">
            <p className="font-medium text-emerald-800 dark:text-emerald-300">
              Fila limitada
            </p>
            <p className="mt-1 text-emerald-700 dark:text-emerald-400">
              Um <code>ThreadPoolExecutor</code> com fila de capacidade
              limitada e uma política de rejeição real
              (<code>RejectedExecutionException</code>) falha rápido e
              de forma explícita quando a capacidade real do sistema é
              excedida — em vez de acumular um backlog invisível.
            </p>
          </div>
        </div>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Perguntas comuns de entrevista
        </h2>
        <ul className="flex flex-col gap-2 text-sm text-zinc-600 dark:text-zinc-400">
          <li>— O que há de errado com `Executors.newFixedThreadPool()` em produção?</li>
          <li>— Por que uma fila ilimitada é perigosa mesmo sem nenhum erro aparecer?</li>
          <li>— Que políticas de rejeição existem além de `AbortPolicy`, e quando cada uma faz sentido?</li>
          <li>— Como esse laboratório se relaciona com Connection Pool Exhaustion?</li>
        </ul>
      </section>
    </div>
  );
}
