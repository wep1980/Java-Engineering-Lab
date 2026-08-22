import type { StatusLaboratorio } from "@/lib/laboratorios";

const ROTULOS: Record<StatusLaboratorio, string> = {
  PLANEJADO: "Planejado",
  DISPONIVEL: "Disponível",
};

const CORES: Record<StatusLaboratorio, string> = {
  PLANEJADO:
    "bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300",
  DISPONIVEL:
    "bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300",
};

export function BadgeStatusLaboratorio({
  status,
}: {
  status: StatusLaboratorio;
}) {
  return (
    <span
      className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${CORES[status]}`}
    >
      {ROTULOS[status]}
    </span>
  );
}
