"use client";

import { useState } from "react";
import { PainelAssistenteIA } from "@/componentes/PainelAssistenteIA";
import { PainelExecucaoOrdenacao } from "@/componentes/PainelExecucaoOrdenacao";

export function ConteudoLaboratorioOrdenacao() {
  const [ultimoResultado, setUltimoResultado] = useState<Record<string, unknown> | null>(null);

  return (
    <div className="flex flex-col gap-10">
      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Cenário
        </h2>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          <strong>&quot;Kafka preserva a ordem dos eventos&quot; é uma
          meia-verdade.</strong> Isso só é garantido{" "}
          <strong>dentro de uma única partição</strong>. Um tópico com
          múltiplas partições — a forma como o Kafka escala — não
          garante nenhuma ordem relativa entre eventos em partições
          diferentes.
        </p>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Um tópico real de 3 partições recebe 20 eventos numerados
          (0 a 19) do mesmo agregado. A diferença entre as duas
          variantes está inteiramente em como cada evento escolhe sua
          partição.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Execução real
        </h2>
        <PainelExecucaoOrdenacao
          laboratorioId="ordenacao-de-eventos"
          onResultado={setUltimoResultado}
        />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Assistente de engenharia
        </h2>
        <PainelAssistenteIA laboratorioId="ordenacao-de-eventos" ultimoResultado={ultimoResultado} />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Código problemático
        </h2>
        <pre className="overflow-x-auto rounded-lg bg-black/[.04] p-4 text-xs dark:bg-white/[.06]">
          <code>{`// sem chave -- o particionador decide, sem nenhum vínculo com o agregado
kafkaTemplate.send(topico, particao, null, evento);`}</code>
        </pre>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Sem uma chave de particionamento consistente, os 20 eventos
          do mesmo agregado se espalham pelas 3 partições reais.
          Consumidos por 3 threads reais (uma por partição), a ordem
          de chegada deixa de ser garantida — nenhuma falha, nenhum
          erro, só a ausência de uma chave bem escolhida.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          O papel da chave de particionamento
        </h2>
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="rounded-lg border border-black/[.08] p-4 text-sm dark:border-white/[.145]">
            <p className="font-medium text-black dark:text-zinc-50">
              Sem chave
            </p>
            <p className="mt-1 text-zinc-600 dark:text-zinc-400">
              Os 20 eventos usam as 3 partições reais do tópico.
              Consumidos por threads diferentes, correndo de verdade,
              a ordem de chegada não é garantida.
            </p>
          </div>
          <div className="rounded-lg border border-emerald-600/30 bg-emerald-50 p-4 text-sm dark:bg-emerald-950/30">
            <p className="font-medium text-emerald-800 dark:text-emerald-300">
              Com chave
            </p>
            <p className="mt-1 text-emerald-700 dark:text-emerald-400">
              O particionador padrão do Kafka faz o hash da chave — a{" "}
              <strong>mesma chave sempre cai na mesma partição</strong>.
              Os 20 eventos ficam garantidamente numa única partição, e
              o Kafka entrega em ordem exata de publicação a um único
              consumidor.
            </p>
          </div>
        </div>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Perguntas comuns de entrevista
        </h2>
        <ul className="flex flex-col gap-2 text-sm text-zinc-600 dark:text-zinc-400">
          <li>— Kafka garante ordem? Em que condição exatamente?</li>
          <li>— O que decide em qual partição um evento cai?</li>
          <li>— Aumentar o número de partições sempre melhora a escalabilidade? Qual o trade-off com ordenação?</li>
          <li>— Como você garantiria que todos os eventos de um mesmo pedido/usuário/conta cheguem em ordem?</li>
        </ul>
      </section>
    </div>
  );
}
