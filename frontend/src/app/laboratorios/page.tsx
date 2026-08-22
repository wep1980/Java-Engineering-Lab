import Link from "next/link";
import { BadgeStatusLaboratorio } from "@/componentes/BadgeStatusLaboratorio";
import { buscarLaboratorios } from "@/lib/laboratorios";

export const metadata = {
  title: "Laboratórios — Java Engineering Lab",
};

export default async function PaginaCatalogoLaboratorios() {
  const laboratorios = await buscarLaboratorios();

  return (
    <div className="flex flex-col flex-1 items-center bg-zinc-50 font-sans dark:bg-black">
      <main className="flex flex-1 w-full max-w-3xl flex-col gap-8 py-16 px-6">
        <div className="flex flex-col gap-2">
          <h1 className="text-2xl font-semibold tracking-tight text-black dark:text-zinc-50">
            Catálogo de laboratórios
          </h1>
          <p className="text-zinc-600 dark:text-zinc-400">
            Cada laboratório reproduz um problema real de Engenharia de
            Software em Java/Spring — do diagnóstico à correção.
          </p>
        </div>

        <ul className="flex flex-col gap-4">
          {laboratorios.map((laboratorio) => (
            <li key={laboratorio.id}>
              <Link
                href={`/laboratorios/${laboratorio.id}`}
                className="flex flex-col gap-2 rounded-lg border border-black/[.08] p-5 transition-colors hover:bg-black/[.02] dark:border-white/[.145] dark:hover:bg-white/[.03]"
              >
                <div className="flex items-center justify-between gap-3">
                  <h2 className="font-medium text-black dark:text-zinc-50">
                    {laboratorio.nome}
                  </h2>
                  <BadgeStatusLaboratorio status={laboratorio.status} />
                </div>
                <p className="text-sm text-zinc-600 dark:text-zinc-400">
                  {laboratorio.objetivo}
                </p>
              </Link>
            </li>
          ))}
        </ul>
      </main>
    </div>
  );
}
