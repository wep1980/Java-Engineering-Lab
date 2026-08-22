const URL_BASE_API = process.env.BACKEND_API_URL ?? "http://localhost:8080";

export type StatusLaboratorio = "PLANEJADO" | "DISPONIVEL";

export type LaboratorioResumo = {
  id: string;
  nome: string;
  objetivo: string;
  status: StatusLaboratorio;
};

export async function buscarLaboratorios(): Promise<LaboratorioResumo[]> {
  const resposta = await fetch(`${URL_BASE_API}/api/laboratorios`, {
    cache: "no-store",
  });

  if (!resposta.ok) {
    throw new Error(
      `Falha ao buscar laboratórios: ${resposta.status} ${resposta.statusText}`,
    );
  }

  return resposta.json();
}

export async function buscarLaboratorioPorId(
  id: string,
): Promise<LaboratorioResumo | null> {
  const resposta = await fetch(`${URL_BASE_API}/api/laboratorios/${id}`, {
    cache: "no-store",
  });

  if (resposta.status === 404) {
    return null;
  }

  if (!resposta.ok) {
    throw new Error(
      `Falha ao buscar laboratório ${id}: ${resposta.status} ${resposta.statusText}`,
    );
  }

  return resposta.json();
}
