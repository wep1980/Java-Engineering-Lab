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
    saldoEsperado: string;
    saldoFinal: string;
    quantidadeEventosConsumidos: number;
    quantidadeProcessamentosEfetivos: number;
  };
};

type ChaveVariante = "sem-idempotencia" | "idempotente";

const VARIANTES: { chave: ChaveVariante; rotulo: string }[] = [
  { chave: "sem-idempotencia", rotulo: "Publicar evento duplicado (sem idempotência)" },
  { chave: "idempotente", rotulo: "Publicar evento duplicado (idempotente)" },
];

type EstadoExecucao =
  | { status: "ocioso" }
  | { status: "carregando" }
  | { status: "erro"; mensagem: string }
  | { status: "concluido"; resultado: ResultadoExecucao };

export function PainelExecucaoKafka({ laboratorioId }: { laboratorioId: string }) {
  const [resultados, setResultados] = useState<Record<ChaveVariante, EstadoExecucao>>({
    "sem-idempotencia": { status: "ocioso" },
    idempotente: { status: "ocioso" },
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
              {estado.status === "carregando" ? "Publicando e aguardando consumo…" : rotulo}
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
                  Publicando o mesmo evento duas vezes e aguardando o consumidor…
                </p>
              )}
              {estado.status === "erro" && (
                <p className="text-red-600 dark:text-red-400">
                  {estado.mensagem}
                  {estado.mensagem.includes("Kafka") && (
                    <>
                      {" "}
                      — este laboratório exige o profile <code>messaging</code> do{" "}
                      <code>docker-compose.yml</code> também no ar.
                    </>
                  )}
                </p>
              )}
              {estado.status === "concluido" && (
                <dl className="mt-2 grid grid-cols-2 gap-3 text-zinc-700 sm:grid-cols-4 dark:text-zinc-300">
                  <div>
                    <dt className="text-xs text-zinc-500 dark:text-zinc-400">Eventos consumidos</dt>
                    <dd className="text-lg font-semibold">
                      {estado.resultado.metricas.quantidadeEventosConsumidos}
                    </dd>
                  </div>
                  <div>
                    <dt className="text-xs text-zinc-500 dark:text-zinc-400">Processamentos efetivos</dt>
                    <dd className="text-lg font-semibold">
                      {estado.resultado.metricas.quantidadeProcessamentosEfetivos}
                    </dd>
                  </div>
                  <div>
                    <dt className="text-xs text-zinc-500 dark:text-zinc-400">Saldo esperado</dt>
                    <dd className="text-lg font-semibold">
                      R$ {estado.resultado.metricas.saldoEsperado}
                    </dd>
                  </div>
                  <div>
                    <dt className="text-xs text-zinc-500 dark:text-zinc-400">Saldo final (REAL)</dt>
                    <dd
                      className={`text-lg font-semibold ${
                        estado.resultado.metricas.saldoFinal !== estado.resultado.metricas.saldoEsperado
                          ? "text-red-600 dark:text-red-400"
                          : "text-emerald-600 dark:text-emerald-400"
                      }`}
                    >
                      R$ {estado.resultado.metricas.saldoFinal}
                    </dd>
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
