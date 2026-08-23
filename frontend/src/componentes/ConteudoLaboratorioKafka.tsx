import Link from "next/link";
import { PainelExecucaoKafka } from "@/componentes/PainelExecucaoKafka";

export function ConteudoLaboratorioKafka() {
  return (
    <div className="flex flex-col gap-10">
      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Cenário
        </h2>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          O mesmo evento <code className="rounded bg-black/[.06] px-1 dark:bg-white/[.08]">EventoPagamentoConfirmado</code> é
          publicado <strong>duas vezes de verdade</strong> em um tópico
          Kafka real — reproduzindo uma entrega duplicada legítima
          (comportamento normal de &quot;at-least-once delivery&quot;, não
          um bug do Kafka). Se o efeito de negócio não for idempotente, a
          carteira é creditada duas vezes.
        </p>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          <strong>Kafka não elimina duplicidades.</strong> Ele garante que
          a mensagem chegue pelo menos uma vez — pode chegar mais de uma.
          A responsabilidade de tornar o efeito seguro para repetir é da
          aplicação.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Execução real
        </h2>
        <PainelExecucaoKafka laboratorioId="kafka-idempotencia" />
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Três conceitos que não são a mesma coisa
        </h2>
        <div className="grid gap-4 sm:grid-cols-3">
          <div className="rounded-lg border border-black/[.08] p-4 text-sm dark:border-white/[.145]">
            <p className="font-medium text-black dark:text-zinc-50">Semântica de entrega</p>
            <p className="mt-1 text-zinc-600 dark:text-zinc-400">
              Quantas vezes o Kafka <em>entrega</em> a mensagem ao
              consumidor. Aqui, sempre 2 — em ambas as variantes.
            </p>
          </div>
          <div className="rounded-lg border border-black/[.08] p-4 text-sm dark:border-white/[.145]">
            <p className="font-medium text-black dark:text-zinc-50">Processamento idempotente</p>
            <p className="mt-1 text-zinc-600 dark:text-zinc-400">
              Detectar que um evento já foi tratado (pelo `eventoId`) e
              não repetir o efeito — a correção deste laboratório.
            </p>
          </div>
          <div className="rounded-lg border border-black/[.08] p-4 text-sm dark:border-white/[.145]">
            <p className="font-medium text-black dark:text-zinc-50">Efeito de negócio</p>
            <p className="mt-1 text-zinc-600 dark:text-zinc-400">
              O que realmente aconteceu no domínio (saldo creditado). É o
              que importa para o usuário — não quantas vezes a mensagem
              trafegou.
            </p>
          </div>
        </div>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Por que &quot;verificar-então-inserir&quot; é seguro aqui
        </h2>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          O container de listener do Spring Kafka processa mensagens de
          uma partição <strong>sequencialmente</strong>, em uma única
          thread — diferente do laboratório de{" "}
          <Link href="/laboratorios/race-condition" className="underline">
            Race Condition
          </Link>
          , onde requisições HTTP concorrentes de verdade exigiam{" "}
          <code className="rounded bg-black/[.06] px-1 dark:bg-white/[.08]">@Version</code> ou
          lock explícito. Aqui, checar se o <code className="rounded bg-black/[.06] px-1 dark:bg-white/[.08]">eventoId</code>{" "}
          já foi processado e então inserir o registro é seguro sem
          nenhum controle de concorrência adicional.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium text-black dark:text-zinc-50">
          Perguntas comuns de entrevista
        </h2>
        <ul className="flex flex-col gap-2 text-sm text-zinc-600 dark:text-zinc-400">
          <li>— Por que o Kafka pode entregar a mesma mensagem mais de uma vez?</li>
          <li>— O que &quot;exactly-once&quot; do Kafka realmente garante — e o que não garante?</li>
          <li>— Como projetar uma chave de idempotência?</li>
          <li>— Por que &quot;verificar-então-inserir&quot; seria arriscado em outro contexto (ex.: HTTP concorrente)?</li>
        </ul>
      </section>
    </div>
  );
}
