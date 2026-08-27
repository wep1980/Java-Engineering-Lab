"use client";

import { useState } from "react";
import { PainelAssistenteIA } from "@/componentes/PainelAssistenteIA";
import { PainelExecucaoSaga } from "@/componentes/PainelExecucaoSaga";

export function ConteudoLaboratorioSaga() {
  const [ultimoResultado, setUltimoResultado] = useState<Record<string, unknown> | null>(null);

  return (
    <div className="flex flex-col gap-10">
      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Cenário
        </h2>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Criar um pedido envolve duas etapas contra recursos
          diferentes: <strong>reservar estoque</strong> e depois{" "}
          <strong>cobrar o pagamento</strong>. Não existe uma
          transação de banco única cobrindo as duas — cada etapa
          commita por conta própria.
        </p>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Neste laboratório, a etapa de pagamento{" "}
          <strong>sempre é recusada</strong> (falha real e
          determinística). A pergunta é: o que acontece com o estoque
          que já foi reservado?
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Execução real
        </h2>
        <PainelExecucaoSaga
          laboratorioId="saga"
          onResultado={setUltimoResultado}
        />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Assistente de engenharia
        </h2>
        <PainelAssistenteIA laboratorioId="saga" ultimoResultado={ultimoResultado} />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Código problemático
        </h2>
        <pre className="overflow-x-auto rounded-lg bg-black/[.04] p-4 text-xs dark:bg-white/[.06]">
          <code>{`estoqueService.reservar(pedidoId, quantidade); // etapa 1, já commitou
processadorPagamento.cobrar(pedidoId, valor);   // etapa 2, lança exceção
// nada aqui desfaz a etapa 1 -- a reserva fica presa para sempre`}</code>
        </pre>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          A reserva de estoque continua com status <code>RESERVADA</code>{" "}
          indefinidamente, mesmo que o pedido nunca vá se completar —
          nenhum erro aparece, nenhum alerta dispara.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Ações de compensação
        </h2>
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="rounded-lg border border-black/[.08] p-4 text-sm dark:border-white/[.145]">
            <p className="font-medium text-black dark:text-zinc-50">
              Sem compensação
            </p>
            <p className="mt-1 text-zinc-600 dark:text-zinc-400">
              A etapa 2 falha e nada desfaz a etapa 1. O estoque fica
              reservado para sempre — um rastro inconsistente
              permanente.
            </p>
          </div>
          <div className="rounded-lg border border-emerald-600/30 bg-emerald-50 p-4 text-sm dark:bg-emerald-950/30">
            <p className="font-medium text-emerald-800 dark:text-emerald-300">
              Com compensação
            </p>
            <p className="mt-1 text-emerald-700 dark:text-emerald-400">
              A falha da etapa 2 dispara a ação de compensação real da
              etapa 1 — a reserva volta para{" "}
              <code>CANCELADA</code>, um estado consistente, sem
              precisar de nenhuma transação distribuída de verdade.
            </p>
          </div>
        </div>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Perguntas comuns de entrevista
        </h2>
        <ul className="flex flex-col gap-2 text-sm text-zinc-600 dark:text-zinc-400">
          <li>— O que é uma ação de compensação, e por que ela não é simplesmente um rollback?</li>
          <li>— Qual a diferença entre Saga orquestrada e Saga coreografada?</li>
          <li>— O que acontece se a própria ação de compensação falhar?</li>
          <li>— Como esse laboratório se relaciona com Transactional Outbox?</li>
        </ul>
      </section>
    </div>
  );
}
