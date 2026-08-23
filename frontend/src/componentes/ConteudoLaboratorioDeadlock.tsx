"use client";

import { useState } from "react";
import { PainelAssistenteIA } from "@/componentes/PainelAssistenteIA";
import { PainelExecucaoDeadlock } from "@/componentes/PainelExecucaoDeadlock";

export function ConteudoLaboratorioDeadlock() {
  const [ultimoResultado, setUltimoResultado] = useState<Record<string, unknown> | null>(null);

  return (
    <div className="flex flex-col gap-10">
      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Cenário
        </h2>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Duas contas de demonstração (A e B, R$ 500,00 cada) recebem{" "}
          <strong>duas transferências reais e concorrentes, em direções
          opostas</strong>: A → B e B → A, R$ 50,00 cada. Cada
          transferência trava a conta de origem, espera um instante
          (simulando processamento), e então trava a conta de destino.
        </p>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          <strong>Diferente de Lost Update</strong> (que perde dados
          silenciosamente, sem erro), aqui o PostgreSQL{" "}
          <strong>detecta ativamente</strong> a espera circular e aborta
          uma das duas transações com um erro real —{" "}
          <code className="rounded bg-black/[.06] px-1 dark:bg-white/[.08]">
            deadlock detected
          </code>
          .
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Execução real
        </h2>
        <PainelExecucaoDeadlock laboratorioId="deadlock" onResultado={setUltimoResultado} />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Assistente de engenharia
        </h2>
        <PainelAssistenteIA laboratorioId="deadlock" ultimoResultado={ultimoResultado} />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Código problemático
        </h2>
        <pre className="overflow-x-auto rounded-lg bg-black/[.04] p-4 text-xs dark:bg-white/[.06]">
          <code>{`// Transferência A → B trava A, depois B
// Transferência B → A trava B, depois A (ordem oposta!)
Conta origem = repositorio.buscarParaAtualizar(origemId); // FOR UPDATE
// ...
Conta destino = repositorio.buscarParaAtualizar(destinoId); // FOR UPDATE -- aqui trava, ou dispara deadlock`}</code>
        </pre>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Cada transferência, isoladamente, está correta. O problema só
          existe porque duas transferências concorrentes, em direções
          opostas, travam as mesmas duas contas em ordens opostas —
          exatamente a condição mínima para uma espera circular.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          A correção: ordenação consistente de locks
        </h2>
        <div className="rounded-lg border border-emerald-600/30 bg-emerald-50 p-4 text-sm dark:bg-emerald-950/30">
          <p className="font-medium text-emerald-800 dark:text-emerald-300">
            Travar sempre na mesma ordem, independente da direção
          </p>
          <p className="mt-1 text-emerald-700 dark:text-emerald-400">
            Em vez de travar &quot;origem, depois destino&quot;, trava-se
            sempre a conta de <strong>menor ID primeiro</strong> — não
            importa se ela é a origem ou o destino da transferência. As
            duas transferências concorrentes passam a disputar a mesma
            conta primeiro, nunca em ordens opostas — elimina
            matematicamente a possibilidade de espera circular. O
            trade-off: quando as transferências disputam a mesma conta
            primeiro, a segunda espera a primeira terminar — a execução
            deixa de ser paralela nesse ponto, mas nunca trava
            (deadlock) nem perde dados.
          </p>
        </div>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Perguntas comuns de entrevista
        </h2>
        <ul className="flex flex-col gap-2 text-sm text-zinc-600 dark:text-zinc-400">
          <li>— Qual a diferença entre Deadlock e Lost Update (Race Condition)?</li>
          <li>— Como o banco de dados detecta um deadlock?</li>
          <li>— Por que ordenar a aquisição de locks elimina o deadlock matematicamente?</li>
          <li>— O que a aplicação deveria fazer quando recebe um erro de deadlock?</li>
        </ul>
      </section>
    </div>
  );
}
