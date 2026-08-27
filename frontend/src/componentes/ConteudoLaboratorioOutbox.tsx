"use client";

import { useState } from "react";
import { PainelAssistenteIA } from "@/componentes/PainelAssistenteIA";
import { PainelExecucaoOutbox } from "@/componentes/PainelExecucaoOutbox";

export function ConteudoLaboratorioOutbox() {
  const [ultimoResultado, setUltimoResultado] = useState<Record<string, unknown> | null>(null);

  return (
    <div className="flex flex-col gap-10">
      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Cenário
        </h2>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Criar um pedido deveria sempre resultar, mais cedo ou mais
          tarde, na publicação de um evento no Kafka avisando outros
          serviços. Mas salvar no banco e publicar no Kafka são{" "}
          <strong>duas operações separadas contra dois sistemas
          diferentes</strong>, sem nenhuma garantia atômica entre elas.
        </p>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          <strong>Banco confirma + Kafka falha = inconsistência.</strong>{" "}
          O pedido existe, mas nenhum outro serviço jamais saberá disso
          — e nada detecta ou corrige isso automaticamente.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Execução real
        </h2>
        <PainelExecucaoOutbox
          laboratorioId="transactional-outbox"
          onResultado={setUltimoResultado}
        />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Assistente de engenharia
        </h2>
        <PainelAssistenteIA laboratorioId="transactional-outbox" ultimoResultado={ultimoResultado} />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Código problemático
        </h2>
        <pre className="overflow-x-auto rounded-lg bg-black/[.04] p-4 text-xs dark:bg-white/[.06]">
          <code>{`pedidoRepository.save(pedido);      // commit real, já aconteceu
kafkaTemplate.send(topico, evento); // se isso falhar, ninguém nunca vai saber`}</code>
        </pre>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Neste laboratório, a segunda linha aponta deliberadamente para
          um endereço Kafka inalcançável — uma falha real de conexão do
          cliente, não fabricada. O pedido continua salvo; a intenção de
          notificar se perde para sempre.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          O padrão Transactional Outbox
        </h2>
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="rounded-lg border border-black/[.08] p-4 text-sm dark:border-white/[.145]">
            <p className="font-medium text-black dark:text-zinc-50">
              Sem outbox
            </p>
            <p className="mt-1 text-zinc-600 dark:text-zinc-400">
              O pedido é salvo, a publicação falha e não fica nenhum
              rastro em lugar nenhum de que um evento deveria ter sido
              enviado. Nada tenta de novo.
            </p>
          </div>
          <div className="rounded-lg border border-emerald-600/30 bg-emerald-50 p-4 text-sm dark:bg-emerald-950/30">
            <p className="font-medium text-emerald-800 dark:text-emerald-300">
              Com outbox
            </p>
            <p className="mt-1 text-emerald-700 dark:text-emerald-400">
              O pedido e um registro do evento pendente são salvos na{" "}
              <strong>mesma transação local</strong> (só o banco precisa
              ser atômico). Um relay real, rodando de forma assíncrona a
              cada 200ms, publica os eventos pendentes no Kafka e só os
              marca como concluídos após confirmação real de entrega. Se
              o Kafka estivesse fora do ar, o evento simplesmente
              ficaria pendente — nunca é perdido, só adiado.
            </p>
          </div>
        </div>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Perguntas comuns de entrevista
        </h2>
        <ul className="flex flex-col gap-2 text-sm text-zinc-600 dark:text-zinc-400">
          <li>— Por que não dá para simplesmente colocar o `save` e o `send` na mesma transação?</li>
          <li>— O que garante a atomicidade no padrão Outbox, já que banco e Kafka continuam sendo dois sistemas?</li>
          <li>— O que acontece se o relay publicar duas vezes o mesmo evento? Isso é um problema?</li>
          <li>— Como esse padrão se relaciona com o laboratório de Mensagem Duplicada/Idempotência?</li>
        </ul>
      </section>
    </div>
  );
}
