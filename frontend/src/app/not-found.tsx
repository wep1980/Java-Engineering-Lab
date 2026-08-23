import Link from "next/link";

export const metadata = {
  title: "Página não encontrada — Java Engineering Lab",
};

export default function PaginaNaoEncontrada() {
  return (
    <div className="flex flex-col flex-1 items-center justify-center bg-zinc-50 font-sans dark:bg-black">
      <main className="flex w-full max-w-md flex-col items-center gap-4 px-6 py-24 text-center">
        <p className="text-sm font-medium uppercase tracking-wide text-zinc-500 dark:text-zinc-400">
          Erro 404
        </p>
        <h1 className="text-2xl font-semibold tracking-tight text-black dark:text-zinc-50">
          Página não encontrada
        </h1>
        <p className="text-zinc-600 dark:text-zinc-400">
          O endereço acessado não existe ou o laboratório procurado ainda
          não foi implementado.
        </p>
        <Link
          href="/laboratorios"
          className="mt-2 flex h-11 items-center justify-center rounded-full bg-foreground px-6 text-sm font-medium text-background transition-colors hover:bg-[#383838] dark:hover:bg-[#ccc]"
        >
          Ver catálogo de laboratórios
        </Link>
      </main>
    </div>
  );
}
