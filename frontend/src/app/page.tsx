import Link from "next/link";

const fases = [
  { nome: "Governança e Descoberta", status: "Concluída" },
  { nome: "Bootstrap de Código", status: "Concluída" },
  { nome: "Plataforma Base de Laboratórios", status: "Concluída" },
  { nome: "Laboratório de N+1 Queries", status: "Concluída" },
  { nome: "Laboratório de Race Condition", status: "Concluída" },
  { nome: "Laboratório de Kafka / Idempotência", status: "Concluída" },
  { nome: "Observabilidade Consolidada", status: "Concluída" },
  { nome: "Engineering AI Assistant", status: "Concluída" },
  { nome: "Hardening", status: "Em andamento" },
];

export default function PaginaInicial() {
  return (
    <div className="flex flex-col flex-1 items-center bg-zinc-50 font-sans dark:bg-black">
      <main className="flex flex-1 w-full max-w-3xl flex-col items-center gap-10 py-24 px-6 sm:items-start">
        <div className="flex flex-col gap-3 text-center sm:text-left">
          <h1 className="text-3xl font-semibold tracking-tight text-black dark:text-zinc-50">
            Java Engineering Lab
          </h1>
          <p className="max-w-xl text-lg leading-8 text-zinc-600 dark:text-zinc-400">
            Um laboratório interativo de Engenharia de Software para
            reproduzir, diagnosticar e corrigir problemas reais de
            aplicações Java/Spring — não apenas ler sobre eles.
          </p>
        </div>

        <Link
          href="/laboratorios"
          className="flex h-11 items-center justify-center rounded-full bg-foreground px-6 text-sm font-medium text-background transition-colors hover:bg-[#383838] dark:hover:bg-[#ccc]"
        >
          Ver catálogo de laboratórios
        </Link>

        <section className="w-full">
          <h2 className="mb-3 text-sm font-medium uppercase tracking-wide text-zinc-500 dark:text-zinc-400">
            Estado atual do projeto
          </h2>
          <ul className="flex flex-col gap-2">
            {fases.map((fase) => (
              <li
                key={fase.nome}
                className="flex items-center justify-between rounded-lg border border-black/[.08] px-4 py-3 dark:border-white/[.145]"
              >
                <span className="text-black dark:text-zinc-50">
                  {fase.nome}
                </span>
                <span className="text-sm text-zinc-500 dark:text-zinc-400">
                  {fase.status}
                </span>
              </li>
            ))}
          </ul>
        </section>

        <p className="max-w-xl text-sm text-zinc-500 dark:text-zinc-400">
          Acompanhe o histórico completo de decisões em{" "}
          <code className="rounded bg-black/[.06] px-1.5 py-0.5 font-mono text-[0.85em] dark:bg-white/[.08]">
            docs/conversation-history.md
          </code>
          .
        </p>
      </main>
    </div>
  );
}
