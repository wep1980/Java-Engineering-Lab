"use client";

import { useState } from "react";
import { PainelAssistenteIA } from "@/componentes/PainelAssistenteIA";
import { PainelExecucaoCacheStampede } from "@/componentes/PainelExecucaoCacheStampede";

export function ConteudoLaboratorioCacheStampede() {
  const [ultimoResultado, setUltimoResultado] = useState<Record<string, unknown> | null>(null);

  return (
    <div className="flex flex-col gap-10">
      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Cenário
        </h2>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Quando uma chave de cache expira ou está fria, e várias
          requisições chegam ao mesmo tempo pedindo a mesma chave,{" "}
          <strong>todas</strong> encontram cache miss simultaneamente e
          vão direto para o recurso lento por trás do cache — o mesmo
          recurso que o cache existia para proteger.
        </p>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Primeiro laboratório deste projeto a usar Redis real (perfil{" "}
          <code>cache</code>, separado de <code>core</code> — os
          demais laboratórios continuam funcionando sem ele).
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Execução real
        </h2>
        <PainelExecucaoCacheStampede
          laboratorioId="cache-stampede"
          onResultado={setUltimoResultado}
        />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Assistente de engenharia
        </h2>
        <PainelAssistenteIA laboratorioId="cache-stampede" ultimoResultado={ultimoResultado} />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Código problemático
        </h2>
        <pre className="overflow-x-auto rounded-lg bg-black/[.04] p-4 text-xs dark:bg-white/[.06]">
          <code>{`String valor = redis.get(chave);
if (valor == null) {
    valor = recursoLento(); // 500ms -- as 10 requisições chegam aqui ao mesmo tempo
    redis.set(chave, valor, ttl);
}`}</code>
        </pre>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Nada aqui impede 10 requisições de encontrarem a mesma chave
          fria e chamarem o recurso lento ao mesmo tempo — cada uma
          paga o custo total, mesmo que o resultado vá ser idêntico.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Lock distribuído real no Redis
        </h2>
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="rounded-lg border border-black/[.08] p-4 text-sm dark:border-white/[.145]">
            <p className="font-medium text-black dark:text-zinc-50">
              Sem proteção
            </p>
            <p className="mt-1 text-zinc-600 dark:text-zinc-400">
              As 10 requisições concorrentes acessam o recurso lento ao
              mesmo tempo — 10 acessos reais para calcular o mesmo
              valor.
            </p>
          </div>
          <div className="rounded-lg border border-emerald-600/30 bg-emerald-50 p-4 text-sm dark:bg-emerald-950/30">
            <p className="font-medium text-emerald-800 dark:text-emerald-300">
              Com proteção
            </p>
            <p className="mt-1 text-emerald-700 dark:text-emerald-400">
              As 10 requisições disputam um lock distribuído real no
              Redis (<code>SET chave valor NX PX</code>, atômico). Só a
              vencedora acessa o recurso lento; as outras 9 aguardam o
              cache ser populado — 1 acesso real, sempre.
            </p>
          </div>
        </div>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Perguntas comuns de entrevista
        </h2>
        <ul className="flex flex-col gap-2 text-sm text-zinc-600 dark:text-zinc-400">
          <li>— O que é cache stampede, e por que ele pode derrubar o próprio recurso que o cache protegia?</li>
          <li>— Por que um lock em memória da JVM não resolveria isso em produção?</li>
          <li>— O que garante que `SET ... NX` seja atômico no Redis?</li>
          <li>— O que acontece se a instância que detém o lock cair antes de liberá-lo?</li>
        </ul>
      </section>
    </div>
  );
}
