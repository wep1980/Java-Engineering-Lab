# SPEC-JEL-003 — MVP: Plataforma Base de Laboratórios

- **Status**: Aprovada — Fase 2 concluída em 2026-08-22 (RF-02/RF-04/RF-05
  completos apenas quando `SPEC-LAB-N1-001` for implementada, Fase 3)
- **Título**: Catálogo de laboratórios, modelo de execução e contrato de
  métricas
- **Relacionadas**: `SPEC-JEL-002` (arquitetura), `SPEC-LAB-N1-001`
  (primeiro laboratório a consumir esta base)

## Nota de escopo desta fase (Fase 2)

RF-02 (disparar execução real e obter métricas) só pode ser cumprido de
fato por um laboratório concreto — sem isso, o endpoint seria um stub
retornando dados fabricados, o que viola o princípio de "métricas reais
são reais" (`specs/manifest/MANIFESTO.md`). Por isso, **a Fase 2 entrega
RF-01, RF-03 (contrato definido em código) RF-04/RF-05 (catálogo + shell
da página de laboratório)**, e RF-02 é concluído junto com
`SPEC-LAB-N1-001` (Fase 3), quando há uma execução real para expor. O
laboratório de N+1 aparece no catálogo com `status: PLANEJADO` até lá.

## Contexto

Cada laboratório (seção 17 do prompt mestre) segue o mesmo fluxo: introdução
→ arquitetura → executar problema → observar → diagnosticar → código
problemático → soluções → aplicar solução → executar novamente → antes ×
depois → trade-offs → entrevista. Sem uma base comum, esse fluxo seria
reimplementado do zero a cada laboratório do backlog (seção 28).

## Objetivo

Especificar o núcleo mínimo da plataforma necessário para que **qualquer**
laboratório (começando pelo de N+1) possa ser catalogado, executado e ter
seus resultados exibidos de forma consistente.

## Escopo do MVP

1. Catálogo de laboratórios (listagem + metadados de cada laboratório).
2. Modelo de execução de laboratório (disparar o cenário problemático ou o
   corrigido, e obter um resultado).
3. Contrato de métricas de execução, com classificação `REAL` / `SIMULADO`
   / `ESTIMADO` (seção 20 do prompt mestre).
4. Página de laboratório no frontend, seguindo o fluxo visual padrão.

## Fora de escopo do MVP

- Autenticação/autorização (não há multiusuário no MVP; avaliar
  `Spring Security` apenas se/quando necessário).
- Kafka, Redis (só entram quando um laboratório específico exigir).
- Engineering AI Assistant (Fase 7).
- Qualquer laboratório além do N+1 (Race Condition e Kafka/Idempotência são
  Fases 4 e 5).

## Requisitos funcionais

| ID | Requisito |
|---|---|
| RF-01 | O backend expõe um endpoint que lista os laboratórios disponíveis com metadados (id, nome, objetivo, status). |
| RF-02 | O backend expõe um endpoint por laboratório para disparar uma execução problemática e uma execução corrigida, retornando métricas da execução. |
| RF-03 | Toda métrica retornada indica sua origem (`REAL`, `SIMULADO` ou `ESTIMADO`); no MVP, apenas `REAL` é aceitável para os laboratórios implementados — `SIMULADO`/`ESTIMADO` existem no contrato para uso futuro, mas não podem ser usados para maquiar um resultado como se fosse `REAL`. |
| RF-04 | O frontend exibe o catálogo de laboratórios e uma página por laboratório com o fluxo: introdução, arquitetura, execução, observação, diagnóstico, código, soluções, comparação antes/depois, trade-offs, seção de entrevista. |
| RF-05 | O frontend exibe claramente as seções PROBLEMA, DIAGNÓSTICO, SOLUÇÃO e RESULTADO (seção 18 do prompt mestre). |

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | Nenhuma entidade JPA é serializada diretamente pelas APIs (DTO obrigatório). |
| RNF-02 | Erros seguem o formato padrão definido em `docs/security.md`. |
| RNF-03 | O catálogo desta fase não usa persistência (lista em memória, sem estado externo) — não há necessidade de Testcontainers ainda; testes são unitários/`@WebMvcTest`. Testcontainers (PostgreSQL) entra quando a primeira entidade real for persistida, em `SPEC-LAB-N1-001`. |
| RNF-05 | Toda resposta de erro segue o formato de `docs/security.md` (código, mensagem, timestamp, caminho, correlation ID), com um filtro de correlation ID compartilhado por toda a plataforma. |
| RNF-04 | A interface deve ser responsiva e utilizável em desktop e mobile (seção 19). |

## Contrato de métricas — PROPOSTA (rascunho)

```json
{
  "execucaoId": "uuid",
  "laboratorioId": "n1-queries",
  "variante": "problematico | corrigido",
  "origemDados": "REAL | SIMULADO | ESTIMADO",
  "iniciadoEm": "timestamp",
  "duracaoMs": 0,
  "metricas": {
    "quantidadeQueries": 0,
    "tempoTotalConsultasMs": 0
  }
}
```

Este contrato é intencionalmente genérico em `metricas` (mapa livre) porque
cada laboratório mede coisas diferentes (quantidade de queries, número de
retries, tempo de lock, etc.) — o campo `origemDados` e os campos de
identificação/tempo são o que é comum a todos.

## Critérios de aceite

### Fase 2 (esta fase) — concluída em 2026-08-22

- [x] Endpoint de catálogo (`GET /api/laboratorios`) implementado e testado.
- [x] Endpoint de detalhe (`GET /api/laboratorios/{id}`) implementado e
      testado, incluindo o caso `404` (laboratório inexistente) com o
      formato de erro padrão.
- [x] Contrato de execução (`ResultadoExecucaoLaboratorio`, `OrigemDados`)
      definido em código, pronto para uso por `SPEC-LAB-N1-001`.
- [x] Frontend exibindo o catálogo de laboratórios e uma página de
      detalhe por laboratório (shell do fluxo padrão; sem execução real
      ainda para laboratórios `PLANEJADO`).
- [x] Nenhuma métrica `SIMULADO`/`ESTIMADO` exibida sem identificação
      visual clara dessa origem (não se aplica ainda nesta fase — não há
      métricas exibidas até `SPEC-LAB-N1-001`).

### Fase 3 (`SPEC-LAB-N1-001`) — concluída em 2026-08-22

- [x] Endpoint de execução implementado e testado para o laboratório de N+1
      (`POST /api/laboratorios/n1-queries/execucoes/{variante}`).
- [x] Página do laboratório de N+1 com o fluxo completo (execução real,
      observação, diagnóstico, comparação antes/depois). Paginação
      (RF-05) adiada — ver `SPEC-LAB-N1-001`.
- [x] Status do laboratório de N+1 no catálogo atualizado para `DISPONIVEL`.

## Observação de status

Esta SPEC descreve o alvo da **Fase 2** (Plataforma base) do roadmap.
Aprovada em 2026-08-22; escopo da Fase 2 implementado e testado no mesmo
dia (ver evidências abaixo). Conclusão total dos requisitos
RF-02/RF-04/RF-05 depende de `SPEC-LAB-N1-001` (Fase 3).

## Evidências de conclusão (Fase 2)

- Backend: pacote `plataforma` com `CatalogoLaboratoriosController`,
  `CatalogoLaboratoriosService`, contrato `ResultadoExecucaoLaboratorio`/
  `OrigemDados`, tratamento de erro padrão
  (`ManipuladorGlobalDeExcecoes`/`ErroResposta`) e correlation ID
  (`FiltroCorrelationId`). 6 testes automatizados passando
  (`CatalogoLaboratoriosServiceTest`, `CatalogoLaboratoriosControllerTest`).
- Validado manualmente: `GET /api/laboratorios` → `200` com o laboratório
  `n1-queries` (`status: PLANEJADO`); `GET /api/laboratorios/n1-queries` →
  `200`; `GET /api/laboratorios/inexistente` → `404` com corpo no formato
  padrão e cabeçalho `X-Correlation-Id`.
- Frontend: `/laboratorios` (catálogo) e `/laboratorios/[id]` (detalhe),
  consumindo a API real do backend (`src/lib/laboratorios.ts`). Validado
  via `npm run build`/`lint` (sem erros) e via navegador real (Chrome,
  com captura de tela) mostrando o catálogo e o detalhe do laboratório de
  N+1 corretamente, incluindo o aviso de "ainda não disponível".
- Validado de ponta a ponta via `docker compose --profile core up`: o
  container do frontend consultou o container do backend pela rede
  interna do Docker (`http://backend:8080`) com sucesso.
