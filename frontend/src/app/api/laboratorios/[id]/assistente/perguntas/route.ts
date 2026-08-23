const URL_BASE_API = process.env.BACKEND_API_URL ?? "http://localhost:8080";

/**
 * Proxy same-origin para o backend — mesmo padrão do proxy de execuções
 * de laboratório (ver app/api/laboratorios/[id]/execucoes/[variante]/route.ts).
 */
export async function POST(
  request: Request,
  { params }: RouteContext<"/api/laboratorios/[id]/assistente/perguntas">,
) {
  const { id } = await params;
  const corpo = await request.text();

  const resposta = await fetch(
    `${URL_BASE_API}/api/laboratorios/${id}/assistente/perguntas`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: corpo,
      cache: "no-store",
    },
  );

  const respostaTexto = await resposta.text();
  return new Response(respostaTexto, {
    status: resposta.status,
    headers: { "Content-Type": "application/json" },
  });
}
