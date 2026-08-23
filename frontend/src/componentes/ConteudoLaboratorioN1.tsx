"use client";

import { useState } from "react";
import { PainelAssistenteIA } from "@/componentes/PainelAssistenteIA";
import { PainelExecucaoN1 } from "@/componentes/PainelExecucaoN1";

export function ConteudoLaboratorioN1() {
  const [ultimoResultado, setUltimoResultado] = useState<Record<string, unknown> | null>(null);

  return (
    <div className="flex flex-col gap-10">
      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Arquitetura
        </h2>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Um <code className="rounded bg-black/[.06] px-1 dark:bg-white/[.08]">Pedido</code>{" "}
          possui muitos <code className="rounded bg-black/[.06] px-1 dark:bg-white/[.08]">ItemPedido</code>{" "}
          (<code className="rounded bg-black/[.06] px-1 dark:bg-white/[.08]">@OneToMany</code>,
          lazy por padrão). A massa de demonstração tem 50 pedidos com 3
          itens cada, semeada de forma determinística ao subir o backend.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Execução real
        </h2>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Cada botão dispara uma execução real contra o PostgreSQL do
          ambiente. A contagem de queries vem do{" "}
          <code className="rounded bg-black/[.06] px-1 dark:bg-white/[.08]">
            Statistics.getPrepareStatementCount()
          </code>{" "}
          do próprio Hibernate — instrumentação real, não estimada.
        </p>
        <PainelExecucaoN1 laboratorioId="n1-queries" onResultado={setUltimoResultado} />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Assistente de engenharia
        </h2>
        <PainelAssistenteIA laboratorioId="n1-queries" ultimoResultado={ultimoResultado} />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Código problemático
        </h2>
        <pre className="overflow-x-auto rounded-lg bg-black/[.04] p-4 text-xs dark:bg-white/[.06]">
          <code>{`List<Pedido> pedidos = pedidoRepository.buscarTodosSemItens(); // 1 query
for (Pedido pedido : pedidos) {
    pedido.getItens().size(); // dispara 1 query POR pedido (lazy loading)
}`}</code>
        </pre>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Soluções
        </h2>
        <div className="grid gap-4 sm:grid-cols-3">
          <div className="rounded-lg border border-black/[.08] p-4 text-sm dark:border-white/[.145]">
            <p className="font-medium text-black dark:text-zinc-50">JOIN FETCH</p>
            <p className="mt-1 text-zinc-600 dark:text-zinc-400">
              1 query, controle total do JPQL. Paginação em coleções
              *-to-many é feita em memória pelo Hibernate — risco de
              carregar mais dados que o necessário.
            </p>
          </div>
          <div className="rounded-lg border border-black/[.08] p-4 text-sm dark:border-white/[.145]">
            <p className="font-medium text-black dark:text-zinc-50">@EntityGraph</p>
            <p className="mt-1 text-zinc-600 dark:text-zinc-400">
              1 query, mais declarativo, reaproveita métodos do Spring
              Data. Mesma restrição de paginação do JOIN FETCH.
            </p>
          </div>
          <div className="rounded-lg border border-black/[.08] p-4 text-sm dark:border-white/[.145]">
            <p className="font-medium text-black dark:text-zinc-50">DTO Projection</p>
            <p className="mt-1 text-zinc-600 dark:text-zinc-400">
              1 query, paginação segura. Não carrega entidades gerenciadas.
              Não serve quando o caso de uso precisa dos dados completos
              das entidades associadas.
            </p>
          </div>
        </div>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          <strong>Por que não usar <code className="rounded bg-black/[.06] px-1 dark:bg-white/[.08]">FetchType.EAGER</code> global:</strong>{" "}
          resolve a listagem, mas move o custo do N+1 para toda consulta
          que carregue um Pedido — mesmo quando os itens não são
          necessários. Uma correção local que cria um problema de
          performance global e oculto.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Perguntas comuns de entrevista
        </h2>
        <ul className="flex flex-col gap-2 text-sm text-zinc-600 dark:text-zinc-400">
          <li>— Por que essa API executou 51 queries para 50 pedidos?</li>
          <li>— Quando JOIN FETCH não resolve o problema?</li>
          <li>— Por que EAGER não é solução universal?</li>
          <li>— Quando usar DTO Projection em vez de JOIN FETCH/EntityGraph?</li>
        </ul>
      </section>
    </div>
  );
}
