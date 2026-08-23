import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { BadgeStatusLaboratorio } from "@/componentes/BadgeStatusLaboratorio";
import { ConteudoLaboratorioN1 } from "@/componentes/ConteudoLaboratorioN1";
import { ConteudoLaboratorioRace } from "@/componentes/ConteudoLaboratorioRace";
import { ConteudoLaboratorioKafka } from "@/componentes/ConteudoLaboratorioKafka";
import { ConteudoLaboratorioConnPool } from "@/componentes/ConteudoLaboratorioConnPool";
import { buscarLaboratorioPorId } from "@/lib/laboratorios";

export async function generateMetadata({
  params,
}: PageProps<"/laboratorios/[id]">): Promise<Metadata> {
  const { id } = await params;
  const laboratorio = await buscarLaboratorioPorId(id);

  if (!laboratorio) {
    return { title: "Laboratório não encontrado — Java Engineering Lab" };
  }

  return {
    title: `${laboratorio.nome} — Java Engineering Lab`,
    description: laboratorio.objetivo,
  };
}

export default async function PaginaLaboratorio({
  params,
}: PageProps<"/laboratorios/[id]">) {
  const { id } = await params;
  const laboratorio = await buscarLaboratorioPorId(id);

  if (!laboratorio) {
    notFound();
  }

  return (
    <div className="flex flex-col flex-1 items-center bg-zinc-50 font-sans dark:bg-black">
      <main className="flex flex-1 w-full max-w-3xl flex-col gap-8 py-16 px-6">
        <Link
          href="/laboratorios"
          className="text-sm text-zinc-500 hover:underline dark:text-zinc-400"
        >
          ← Voltar ao catálogo
        </Link>

        <div className="flex flex-col gap-3">
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-semibold tracking-tight text-black dark:text-zinc-50">
              {laboratorio.nome}
            </h1>
            <BadgeStatusLaboratorio status={laboratorio.status} />
          </div>
          <p className="max-w-xl text-zinc-600 dark:text-zinc-400">
            {laboratorio.objetivo}
          </p>
        </div>

        {laboratorio.status === "PLANEJADO" && (
          <div className="rounded-lg border border-dashed border-black/[.12] p-6 text-sm text-zinc-600 dark:border-white/[.15] dark:text-zinc-400">
            Este laboratório ainda não está disponível para execução — a
            SPEC já existe (
            <code className="rounded bg-black/[.06] px-1.5 py-0.5 font-mono text-[0.85em] dark:bg-white/[.08]">
              specs/labs/
            </code>
            ), mas a implementação faz parte de uma fase futura do
            roadmap.
          </div>
        )}

        {laboratorio.status === "DISPONIVEL" && laboratorio.id === "n1-queries" && (
          <ConteudoLaboratorioN1 />
        )}

        {laboratorio.status === "DISPONIVEL" && laboratorio.id === "race-condition" && (
          <ConteudoLaboratorioRace />
        )}

        {laboratorio.status === "DISPONIVEL" && laboratorio.id === "kafka-idempotencia" && (
          <ConteudoLaboratorioKafka />
        )}

        {laboratorio.status === "DISPONIVEL" && laboratorio.id === "connection-pool-exhaustion" && (
          <ConteudoLaboratorioConnPool />
        )}
      </main>
    </div>
  );
}
