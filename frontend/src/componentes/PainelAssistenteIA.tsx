"use client";

import { useState } from "react";

type MensagemHistorico = {
  pergunta: string;
  resposta: string;
};

type Props = {
  laboratorioId: string;
  ultimoResultado: Record<string, unknown> | null;
};

export function PainelAssistenteIA({ laboratorioId, ultimoResultado }: Props) {
  const [pergunta, setPergunta] = useState("");
  const [historico, setHistorico] = useState<MensagemHistorico[]>([]);
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function enviarPergunta() {
    const perguntaAtual = pergunta.trim();
    if (!perguntaAtual || carregando) {
      return;
    }

    setCarregando(true);
    setErro(null);

    try {
      const resposta = await fetch(
        `/api/laboratorios/${laboratorioId}/assistente/perguntas`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            pergunta: perguntaAtual,
            ultimoResultado,
          }),
        },
      );

      if (!resposta.ok) {
        const corpo = await resposta.json().catch(() => null);
        throw new Error(
          corpo?.mensagem ?? `Falha ao consultar o assistente (${resposta.status})`,
        );
      }

      const dados: { resposta: string } = await resposta.json();
      setHistorico((atual) => [...atual, { pergunta: perguntaAtual, resposta: dados.resposta }]);
      setPergunta("");
    } catch (e) {
      const mensagem = e instanceof Error ? e.message : "Erro desconhecido";
      setErro(mensagem);
    } finally {
      setCarregando(false);
    }
  }

  return (
    <div className="flex flex-col gap-4 rounded-lg border border-black/[.08] p-4 dark:border-white/[.145]">
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium text-black dark:text-zinc-50">
          Pergunte ao assistente
        </p>
        <span className="text-xs text-zinc-500 dark:text-zinc-400">
          {ultimoResultado
            ? "usando o resultado da sua última execução como contexto"
            : "execute o laboratório para dar mais contexto ao assistente"}
        </span>
      </div>

      {historico.length > 0 && (
        <ul className="flex flex-col gap-3">
          {historico.map((mensagem, indice) => (
            <li key={indice} className="flex flex-col gap-1 text-sm">
              <p className="font-medium text-black dark:text-zinc-50">
                Você: {mensagem.pergunta}
              </p>
              <p className="whitespace-pre-wrap text-zinc-600 dark:text-zinc-400">
                {mensagem.resposta}
              </p>
            </li>
          ))}
        </ul>
      )}

      {erro && (
        <p className="text-sm text-red-600 dark:text-red-400">
          {erro} — este recurso exige o profile <code>ai</code> do{" "}
          <code>docker-compose.yml</code> no ar (Ollama).
        </p>
      )}

      <div className="flex gap-2">
        <input
          type="text"
          aria-label="Pergunte ao assistente"
          value={pergunta}
          onChange={(e) => setPergunta(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") {
              enviarPergunta();
            }
          }}
          placeholder="Ex.: por que essa execução gerou tantas queries?"
          disabled={carregando}
          className="flex-1 rounded-full border border-black/[.12] bg-transparent px-4 py-2 text-sm outline-none disabled:opacity-50 dark:border-white/[.15]"
        />
        <button
          onClick={enviarPergunta}
          disabled={carregando || !pergunta.trim()}
          className="rounded-full bg-foreground px-4 py-2 text-sm font-medium text-background transition-colors hover:bg-[#383838] disabled:opacity-50 dark:hover:bg-[#ccc]"
        >
          {carregando ? "Pensando…" : "Perguntar"}
        </button>
      </div>
    </div>
  );
}
