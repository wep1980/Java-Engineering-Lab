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
    pedidoId: number;
    pedidoPersistido: boolean;
    eventoRegistradoNaOutbox: boolean;
    eventoPublicadoNoKafka: boolean;
    inconsistente: boolean;
  };
};

type ChaveVariante = "sem-outbox" | "com-outbox";

const VARIANTES: { chave: ChaveVariante; rotulo: string }[] = [
  { chave: "sem-outbox", rotulo: "Criar pedido sem outbox" },
  { chave: "com-outbox", rotulo: "Criar pedido com outbox" },
];

type EstadoExecucao =
  | { status: "ocioso" }
  | { status: "carregando" }
  | { status: "erro"; mensagem: string }
  | { status: "concluido"; resultado: ResultadoExecucao };

type Props = {
  laboratorioId: string;
  onResultado?: (resultado: ResultadoExecucao) => void;
};

export function PainelExecucaoOutbox({ laboratorioId, onResultado }: Props) {
  const [resultados, setResultados] = useState<Record<ChaveVariante, EstadoExecucao>>({
    "sem-outbox": { status: "ocioso" },
    "com-outbox": { status: "ocioso" },
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
      onResultado?.(resultado);
    } catch (erro) {
      const mensagem = erro instanceof Error ? erro.message : "Erro desconhecido";
      setResultados((atual) => ({ ...atual, [chave]: { status: "erro", mensagem } }));
    }
  }

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
              {estado.status === "carregando" ? "Aguardando…" : rotulo}
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
                <p className="text-zinc-500 dark:text-zinc-400">
                  {chave === "sem-outbox"
                    ? "Salvando pedido e tentando publicar direto no Kafka…"
                    : "Salvando pedido e evento outbox, aguardando o relay publicar…"}
                </p>
              )}
              {estado.status === "erro" && (
                <p className="text-red-600 dark:text-red-400">{estado.mensagem}</p>
              )}
              {estado.status === "concluido" && (
                <dl className="mt-2 grid grid-cols-2 gap-3 text-zinc-700 sm:grid-cols-4 dark:text-zinc-300">
                  <div>
                    <dt className="text-xs text-zinc-500 dark:text-zinc-400">Evento registrado na outbox</dt>
                    <dd className="text-lg font-semibold">
                      {estado.resultado.metricas.eventoRegistradoNaOutbox ? "Sim" : "Não"}
                    </dd>
                  </div>
                  <div>
                    <dt className="text-xs text-zinc-500 dark:text-zinc-400">Evento publicado no Kafka (REAL)</dt>
                    <dd
                      className={`text-lg font-semibold ${
                        estado.resultado.metricas.eventoPublicadoNoKafka
                          ? "text-emerald-600 dark:text-emerald-400"
                          : "text-red-600 dark:text-red-400"
                      }`}
                    >
                      {estado.resultado.metricas.eventoPublicadoNoKafka ? "Sim" : "Não"}
                    </dd>
                  </div>
                  <div>
                    <dt className="text-xs text-zinc-500 dark:text-zinc-400">Inconsistente</dt>
                    <dd
                      className={`text-lg font-semibold ${
                        estado.resultado.metricas.inconsistente
                          ? "text-red-600 dark:text-red-400"
                          : "text-emerald-600 dark:text-emerald-400"
                      }`}
                    >
                      {estado.resultado.metricas.inconsistente ? "Sim" : "Não"}
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
    </div>
  );
}
