"use client";

import { useState } from "react";
import { PainelAssistenteIA } from "@/componentes/PainelAssistenteIA";
import { PainelExecucaoConnPool } from "@/componentes/PainelExecucaoConnPool";

export function ConteudoLaboratorioConnPool() {
  const [ultimoResultado, setUltimoResultado] = useState<Record<string, unknown> | null>(null);

  return (
    <div className="flex flex-col gap-10">
      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Cenário
        </h2>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          <strong>10 requisições concorrentes reais</strong> disputam um
          pool de conexões HikariCP propositalmente pequeno (2
          conexões), completamente isolado do pool principal da
          aplicação — os demais laboratórios continuam funcionando
          normalmente durante esta execução. Cada requisição faz um
          trabalho lento simulado (500ms, representando uma chamada
          externa ou processamento pesado) e uma consulta SQL trivial.
        </p>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          <strong>O pool é um recurso finito.</strong> Segurar uma
          conexão por mais tempo do que o necessário — mesmo que o banco
          em si esteja ocioso e saudável — faz outras requisições
          esperarem, e esperarem demais gera falha real por timeout.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Execução real
        </h2>
        <PainelExecucaoConnPool
          laboratorioId="connection-pool-exhaustion"
          onResultado={setUltimoResultado}
        />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Assistente de engenharia
        </h2>
        <PainelAssistenteIA laboratorioId="connection-pool-exhaustion" ultimoResultado={ultimoResultado} />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Código problemático
        </h2>
        <pre className="overflow-x-auto rounded-lg bg-black/[.04] p-4 text-xs dark:bg-white/[.06]">
          <code>{`try (Connection conexao = poolPequeno.getConnection()) { // pega a conexão ANTES
    Thread.sleep(500); // trabalho lento SEGURANDO a conexão -- o erro
    conexao.createStatement().execute("SELECT 1");
}`}</code>
        </pre>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Nenhuma exceção aparece nos primeiros testes com baixa carga —
          o problema só se manifesta sob concorrência real, exatamente
          o tipo de bug que passa despercebido em desenvolvimento e
          aparece em produção sob carga.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Duas correções, um trade-off importante
        </h2>
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="rounded-lg border border-black/[.08] p-4 text-sm dark:border-white/[.145]">
            <p className="font-medium text-black dark:text-zinc-50">
              Aumentar o pool
            </p>
            <p className="mt-1 text-zinc-600 dark:text-zinc-400">
              Funciona — com folga suficiente, ninguém espera. Mas não
              escala: cada conexão a mais custa memória na aplicação
              <em> e </em>no banco (que também tem um limite de
              conexões simultâneas). É preciso saber, de antemão, qual é
              o pico real de concorrência.
            </p>
          </div>
          <div className="rounded-lg border border-emerald-600/30 bg-emerald-50 p-4 text-sm dark:bg-emerald-950/30">
            <p className="font-medium text-emerald-800 dark:text-emerald-300">
              Reduzir o tempo de retenção da conexão
            </p>
            <p className="mt-1 text-emerald-700 dark:text-emerald-400">
              Fazer o trabalho lento <strong>antes</strong> de obter a
              conexão, e usá-la só pelo tempo estritamente necessário.
              Com o <strong>mesmo pool pequeno</strong> da variante
              problemática, nenhuma falha acontece — prova de que o
              tamanho do pool não era a única solução possível, e é a
              correção que realmente escala.
            </p>
          </div>
        </div>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Perguntas comuns de entrevista
        </h2>
        <ul className="flex flex-col gap-2 text-sm text-zinc-600 dark:text-zinc-400">
          <li>— Por que o pool esgota mesmo com o banco saudável e ocioso?</li>
          <li>— Aumentar o pool sempre resolve? Por que não?</li>
          <li>— O que uma aplicação não deveria fazer enquanto segura uma conexão JDBC?</li>
          <li>— Como você diagnosticaria esse problema em produção, sem acesso ao código?</li>
        </ul>
      </section>
    </div>
  );
}
