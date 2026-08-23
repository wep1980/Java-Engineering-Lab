"use client";

import { useState } from "react";

type ResultadoExecucao = {
  execucaoId: string;
  laboratorioId: string;
  variante: "PROBLEMATICO" | "CORRIGIDO";
  origemDados: "REAL" | "SIMULADO" | "ESTIMADO";
  iniciadoEm: string;
  duracaoMs: number;
  metricas: {
    tecnica: string;
    quantidadeQueries: number;
    quantidadePedidos: number;
  };
};

type ChaveVariante = "problematico" | "join-fetch" | "entity-graph" | "dto-projection";

const VARIANTES: { chave: ChaveVariante; rotulo: string }[] = [
  { chave: "problematico", rotulo: "Executar versão problemática" },
  { chave: "join-fetch", rotulo: "Executar com JOIN FETCH" },
  { chave: "entity-graph", rotulo: "Executar com @EntityGraph" },
  { chave: "dto-projection", rotulo: "Executar com DTO Projection" },
];

type EstadoExecucao =
  | { status: "ocioso" }
  | { status: "carregando" }
  | { status: "erro"; mensagem: string }
  | { status: "concluido"; resultado: ResultadoExecucao };

export function PainelExecucaoN1({ laboratorioId }: { laboratorioId: string }) {
  const [resultados, setResultados] = useState<Record<ChaveVariante, EstadoExecucao>>({
    problematico: { status: "ocioso" },
    "join-fetch": { status: "ocioso" },
    "entity-graph": { status: "ocioso" },
    "dto-projection": { status: "ocioso" },
  });

  async function executar(chave: ChaveVariante) {
    setResultados((atual) => ({ ...atual, [chave]: { status: "carregando" } }));

    try {
      const resposta = await fetch(
        `/api/laboratorios/${laboratorioId}/execucoes/${chave}`,
        { method: "POST" },
      );

      if (!resposta.ok) {
        const erro = await resposta.json().catch(() => null);
        throw new Error(erro?.mensagem ?? `Falha na execução (${resposta.status})`);
      }

      const resultado: ResultadoExecucao = await resposta.json();
      setResultados((atual) => ({ ...atual, [chave]: { status: "concluido", resultado } }));
    } catch (erro) {
      const mensagem = erro instanceof Error ? erro.message : "Erro desconhecido";
      setResultados((atual) => ({ ...atual, [chave]: { status: "erro", mensagem } }));
    }
  }

  const problematico = resultados.problematico;
  const algumaCorrigidaConcluida = (["join-fetch", "entity-graph", "dto-projection"] as const).some(
    (chave) => resultados[chave].status === "concluido",
  );

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-wrap gap-3">
        {VARIANTES.map(({ chave, rotulo }) => {
          const estado = resultados[chave];
          return (
            <button
              key={chave}
              onClick={() => executar(chave)}
              disabled={estado.status === "carregando"}
              className="rounded-full border border-black/[.12] px-4 py-2 text-sm font-medium transition-colors hover:bg-black/[.04] disabled:opacity-50 dark:border-white/[.15] dark:hover:bg-white/[.06]"
            >
              {estado.status === "carregando" ? "Executando…" : rotulo}
            </button>
          );
        })}
      </div>

      <ul className="flex flex-col gap-3">
        {VARIANTES.map(({ chave, rotulo }) => {
          const estado = resultados[chave];
          if (estado.status === "ocioso") return null;

          return (
            <li
              key={chave}
              className="rounded-lg border border-black/[.08] p-4 text-sm dark:border-white/[.145]"
            >
              <div className="font-medium text-black dark:text-zinc-50">{rotulo}</div>
              {estado.status === "carregando" && (
                <p className="text-zinc-500 dark:text-zinc-400">Executando contra o banco real…</p>
              )}
              {estado.status === "erro" && (
                <p className="text-red-600 dark:text-red-400">{estado.mensagem}</p>
              )}
              {estado.status === "concluido" && (
                <dl className="mt-2 grid grid-cols-3 gap-3 text-zinc-700 dark:text-zinc-300">
                  <div>
                    <dt className="text-xs text-zinc-500 dark:text-zinc-400">Queries (REAL)</dt>
                    <dd className="text-lg font-semibold">
                      {estado.resultado.metricas.quantidadeQueries}
                    </dd>
                  </div>
                  <div>
                    <dt className="text-xs text-zinc-500 dark:text-zinc-400">Pedidos retornados</dt>
                    <dd className="text-lg font-semibold">
                      {estado.resultado.metricas.quantidadePedidos}
                    </dd>
                  </div>
                  <div>
                    <dt className="text-xs text-zinc-500 dark:text-zinc-400">Duração</dt>
                    <dd className="text-lg font-semibold">{estado.resultado.duracaoMs} ms</dd>
                  </div>
                </dl>
              )}
            </li>
          );
        })}
      </ul>

      {problematico.status === "concluido" && algumaCorrigidaConcluida && (
        <div className="rounded-lg border border-emerald-600/30 bg-emerald-50 p-4 text-sm dark:bg-emerald-950/30">
          <p className="font-medium text-emerald-800 dark:text-emerald-300">Antes × depois</p>
          <p className="text-emerald-700 dark:text-emerald-400">
            A versão problemática executou{" "}
            <strong>{problematico.resultado.metricas.quantidadeQueries} queries</strong> para{" "}
            {problematico.resultado.metricas.quantidadePedidos} pedidos. As versões corrigidas
            executam sempre <strong>1 query</strong>, independente da quantidade de pedidos —
            mesma massa de dados, mesma operação.
          </p>
        </div>
      )}
    </div>
  );
}
