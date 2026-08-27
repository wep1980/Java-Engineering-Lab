"use client";

import { useState } from "react";
import { PainelAssistenteIA } from "@/componentes/PainelAssistenteIA";
import { PainelExecucaoCircuitBreaker } from "@/componentes/PainelExecucaoCircuitBreaker";

export function ConteudoLaboratorioCircuitBreaker() {
  const [ultimoResultado, setUltimoResultado] = useState<Record<string, unknown> | null>(null);

  return (
    <div className="flex flex-col gap-10">
      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Cenário
        </h2>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Uma dependência externa simulada está <strong>completamente
          fora do ar</strong>: toda chamada demora 300ms e falha. As
          duas variantes disparam <strong>20 chamadas sequenciais
          reais</strong> contra essa mesma dependência.
        </p>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          <strong>Um circuit breaker não evita a primeira falha</strong>
          — evita repetir uma pergunta cuja resposta você já sabe. A
          proteção usada aqui é um circuit breaker real (biblioteca
          Resilience4j, mesma máquina de estados usada em produção),
          não uma simulação: janela deslizante de 10 chamadas, mínimo
          de 5 antes de calcular a taxa, limite de 50% de falha.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Execução real
        </h2>
        <PainelExecucaoCircuitBreaker
          laboratorioId="circuit-breaker"
          onResultado={setUltimoResultado}
        />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Assistente de engenharia
        </h2>
        <PainelAssistenteIA laboratorioId="circuit-breaker" ultimoResultado={ultimoResultado} />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Código problemático
        </h2>
        <pre className="overflow-x-auto rounded-lg bg-black/[.04] p-4 text-xs dark:bg-white/[.06]">
          <code>{`for (int i = 0; i < 20; i++) {
    dependenciaInstavel.chamar(); // sem nenhuma proteção
    // cada chamada paga os 300ms completos antes de falhar
}`}</code>
        </pre>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Nada impede a 20ª chamada de tentar exatamente o mesmo que já
          falhou 19 vezes seguidas — cada uma delas consumindo uma
          thread e pagando a latência completa por um resultado já
          previsível.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          O que o circuito faz depois de abrir
        </h2>
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="rounded-lg border border-black/[.08] p-4 text-sm dark:border-white/[.145]">
            <p className="font-medium text-black dark:text-zinc-50">
              Sem circuit breaker
            </p>
            <p className="mt-1 text-zinc-600 dark:text-zinc-400">
              20 chamadas, 20 falhas reais, 20 × 300ms de latência paga
              integralmente. Sob carga concorrente, cada uma dessas
              chamadas também segura uma thread do servidor esperando
              — o mesmo tipo de exaustão de recurso já visto no
              laboratório de Connection Pool Exhaustion, só que aqui o
              recurso são threads, não conexões.
            </p>
          </div>
          <div className="rounded-lg border border-emerald-600/30 bg-emerald-50 p-4 text-sm dark:bg-emerald-950/30">
            <p className="font-medium text-emerald-800 dark:text-emerald-300">
              Com circuit breaker
            </p>
            <p className="mt-1 text-emerald-700 dark:text-emerald-400">
              As primeiras 5 chamadas ainda falham de verdade — é o
              preço de descobrir que a dependência está fora do ar.
              Depois disso o circuito abre (estado real{" "}
              <code className="rounded bg-black/[.06] px-1 font-mono dark:bg-white/[.08]">OPEN</code>
              ) e as 15 chamadas restantes são rejeitadas na hora, sem
              sequer tentar a rede — a aplicação para de bater a cabeça
              numa dependência que já provou estar fora do ar.
            </p>
          </div>
        </div>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Perguntas comuns de entrevista
        </h2>
        <ul className="flex flex-col gap-2 text-sm text-zinc-600 dark:text-zinc-400">
          <li>— Um circuit breaker evita a primeira falha? Por que não?</li>
          <li>— O que significam os estados CLOSED, OPEN e HALF_OPEN?</li>
          <li>— Por que não abrir o circuito na primeira falha?</li>
          <li>— Qual é a diferença entre circuit breaker, retry e timeout — e por que eles costumam ser usados juntos?</li>
        </ul>
      </section>
    </div>
  );
}
