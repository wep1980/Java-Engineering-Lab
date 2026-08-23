"use client";

import { useState } from "react";
import { PainelAssistenteIA } from "@/componentes/PainelAssistenteIA";
import { PainelExecucaoRace } from "@/componentes/PainelExecucaoRace";

export function ConteudoLaboratorioRace() {
  const [ultimoResultado, setUltimoResultado] = useState<Record<string, unknown> | null>(null);

  return (
    <div className="flex flex-col gap-10">
      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Cenário
        </h2>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Uma conta bancária de demonstração recebe{" "}
          <strong>10 depósitos de R$ 100,00 concorrentes de verdade</strong>{" "}
          (threads reais, liberadas ao mesmo tempo por uma barreira de
          largada). Se nada se perder, o saldo final deve ser R$
          1.000,00.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Execução real
        </h2>
        <PainelExecucaoRace laboratorioId="race-condition" onResultado={setUltimoResultado} />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Assistente de engenharia
        </h2>
        <PainelAssistenteIA laboratorioId="race-condition" ultimoResultado={ultimoResultado} />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Código problemático
        </h2>
        <pre className="overflow-x-auto rounded-lg bg-black/[.04] p-4 text-xs dark:bg-white/[.06]">
          <code>{`ContaBancaria conta = repositorio.findById(contaId).orElseThrow(); // le saldo
// ... outra thread le o MESMO saldo aqui, antes desta escrever ...
conta.depositar(valor); // soma sobre o valor lido, nao sobre o valor atual real
repositorio.save(conta); // sobrescreve o que a outra thread escreveu`}</code>
        </pre>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Nenhuma exceção é lançada. Nenhum log de erro aparece. A
          atualização simplesmente desaparece.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Soluções
        </h2>
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="rounded-lg border border-black/[.08] p-4 text-sm dark:border-white/[.145]">
            <p className="font-medium text-black dark:text-zinc-50">
              Optimistic Locking (<code className="rounded bg-black/[.06] px-1 dark:bg-white/[.08]">@Version</code>)
            </p>
            <p className="mt-1 text-zinc-600 dark:text-zinc-400">
              Uma coluna de versão é conferida no UPDATE. Se outra
              transação já alterou a linha, o commit falha com{" "}
              <code className="rounded bg-black/[.06] px-1 dark:bg-white/[.08]">
                ObjectOptimisticLockingFailureException
              </code>{" "}
              — nada se perde silenciosamente. Quem chama decide
              re-tentar. Bom para baixa/média contenção: rápido quando
              não há conflito, mas desperdiça trabalho quando há.
            </p>
          </div>
          <div className="rounded-lg border border-black/[.08] p-4 text-sm dark:border-white/[.145]">
            <p className="font-medium text-black dark:text-zinc-50">
              Pessimistic Locking (<code className="rounded bg-black/[.06] px-1 dark:bg-white/[.08]">SELECT ... FOR UPDATE</code>)
            </p>
            <p className="mt-1 text-zinc-600 dark:text-zinc-400">
              A primeira transação a ler a linha trava-a; as demais
              esperam até ela terminar. Nunca há conflito nem perda, mas
              o acesso concorrente vira sequencial — mais lento sob
              alta concorrência, e risco de deadlock se locks forem
              adquiridos em ordens diferentes.
            </p>
          </div>
        </div>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Perguntas comuns de entrevista
        </h2>
        <ul className="flex flex-col gap-2 text-sm text-zinc-600 dark:text-zinc-400">
          <li>— O que é um Lost Update e por que ele não gera nenhum erro?</li>
          <li>— Qual a diferença entre Optimistic e Pessimistic Locking?</li>
          <li>— Quando Pessimistic Locking é preferível, mesmo sendo mais lento?</li>
          <li>— Por que @Version sozinho não basta — é preciso tratar a exceção?</li>
        </ul>
      </section>
    </div>
  );
}
