const URL_BASE_API = process.env.BACKEND_API_URL ?? "http://localhost:8080";

/**
 * Proxy same-origin para o backend. Existe para que o navegador nunca
 * precise chamar o backend diretamente (que, em produção/Docker, só é
 * alcançável pela rede interna) — evita CORS e mantém BACKEND_API_URL
 * como uma variável só do lado do servidor.
 */
export async function POST(
  _request: Request,
  { params }: RouteContext<"/api/laboratorios/[id]/execucoes/[variante]">,
) {
  const { id, variante } = await params;

  const resposta = await fetch(
    `${URL_BASE_API}/api/laboratorios/${id}/execucoes/${variante}`,
    { method: "POST", cache: "no-store" },
  );

  const corpo = await resposta.text();
  return new Response(corpo, {
    status: resposta.status,
    headers: { "Content-Type": "application/json" },
  });
}
