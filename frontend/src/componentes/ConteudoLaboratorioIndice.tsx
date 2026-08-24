"use client";

import { useState } from "react";
import { PainelAssistenteIA } from "@/componentes/PainelAssistenteIA";
import { PainelExecucaoIndice } from "@/componentes/PainelExecucaoIndice";

export function ConteudoLaboratorioIndice() {
  const [ultimoResultado, setUltimoResultado] = useState<Record<string, unknown> | null>(null);

  return (
    <div className="flex flex-col gap-10">
      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Cenário
        </h2>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Uma tabela com <strong>200 mil linhas reais</strong> recebe
          uma busca por e-mail exato. Sem índice na coluna de e-mail, o
          PostgreSQL precisa varrer a tabela inteira (
          <code className="rounded bg-black/[.06] px-1 dark:bg-white/[.08]">
            Seq Scan
          </code>
          ) para garantir que encontrou todas as linhas que casam com o
          filtro — mesmo que exista só uma. Com um índice, ele localiza
          a linha diretamente.
        </p>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          O índice é <strong>criado e removido de verdade</strong>{" "}
          (
          <code className="rounded bg-black/[.06] px-1 dark:bg-white/[.08]">
            CREATE INDEX
          </code>
          /
          <code className="rounded bg-black/[.06] px-1 dark:bg-white/[.08]">
            DROP INDEX
          </code>
          ) a cada execução — o mesmo comando que um engenheiro rodaria
          em produção, não uma segunda tabela pré-indexada escondida.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Execução real
        </h2>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Cada botão roda{" "}
          <code className="rounded bg-black/[.06] px-1 dark:bg-white/[.08]">
            EXPLAIN (ANALYZE, FORMAT JSON)
          </code>{" "}
          de verdade — o tipo de plano e o tempo vêm direto do
          PostgreSQL, não são estimados nem medidos só do lado da
          aplicação.
        </p>
        <PainelExecucaoIndice laboratorioId="query-sem-indice" onResultado={setUltimoResultado} />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Assistente de engenharia
        </h2>
        <PainelAssistenteIA laboratorioId="query-sem-indice" ultimoResultado={ultimoResultado} />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Como diagnosticar isso em produção
        </h2>
        <pre className="overflow-x-auto rounded-lg bg-black/[.04] p-4 text-xs dark:bg-white/[.06]">
          <code>{`EXPLAIN (ANALYZE, FORMAT JSON)
SELECT * FROM registro_busca WHERE email = 'usuario150000@exemplo.com';

-- "Node Type": "Seq Scan"      -> sem índice, varre a tabela inteira
-- "Node Type": "Index Scan"    -> com índice, localiza direto
-- "Actual Total Time"          -> tempo real, não estimado`}</code>
        </pre>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          <code className="rounded bg-black/[.06] px-1 dark:bg-white/[.08]">
            EXPLAIN
          </code>{" "}
          sozinho mostra o plano <em>estimado</em>, sem executar a
          query.{" "}
          <code className="rounded bg-black/[.06] px-1 dark:bg-white/[.08]">
            EXPLAIN ANALYZE
          </code>{" "}
          executa a query de verdade e mostra o que realmente
          aconteceu — essencial para confirmar se a estimativa do
          otimizador bate com a realidade.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Perguntas comuns de entrevista
        </h2>
        <ul className="flex flex-col gap-2 text-sm text-zinc-600 dark:text-zinc-400">
          <li>— Qual a diferença entre EXPLAIN e EXPLAIN ANALYZE?</li>
          <li>— Por que nem toda coluna deveria ter um índice?</li>
          <li>— O que é um Bitmap Heap Scan, e quando o otimizador escolhe ele em vez de um Index Scan puro?</li>
          <li>— Por que rodar ANALYZE depois de popular ou indexar uma tabela grande?</li>
        </ul>
      </section>
    </div>
  );
}
