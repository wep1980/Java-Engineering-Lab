# ADR-0002 — Monorepo para backend, frontend, docs e specs

- **Status**: Proposta (pendente de aprovação do usuário)
- **Data**: 2026-08-22
- **SPEC relacionada**: `specs/architecture/SPEC-JEL-002-arquitetura.md`

## Contexto

O projeto tem backend (Java/Spring Boot), frontend (Next.js) e uma camada
extensa de documentação/specs (12+ laboratórios previstos no backlog). É
preciso decidir entre um único repositório (monorepo) ou repositórios
separados por componente.

## Decisão

Adotar **monorepo** com `backend/`, `frontend/`, `docs/`, `specs/`,
`diagrams/`, `infra/` e `scripts/` na raiz de um único repositório Git.

## Justificativa

- O projeto é um portfólio de Engenharia de Software de escala pequena/
  média (não múltiplos times ou serviços independentes), então o custo de
  coordenação de múltiplos repositórios não se justifica.
- SPECs frequentemente atravessam backend e frontend (ex.: um laboratório
  tem contrato de API + visualização), o que é mais simples de manter
  rastreável em um único histórico de commits.
- `docs/conversation-history.md`, que precisa refletir toda a evolução do
  projeto, é naturalmente único.

## Alternativas consideradas

- **Multirepo** (um repositório por componente): rejeitado por adicionar
  overhead de sincronização de versões e PRs cruzados sem benefício real
  neste estágio do projeto.

## Consequências

- CI/CD (Fase 1) precisará de path filters para não rodar pipelines de
  backend em mudanças só de frontend/docs, e vice-versa.
- Se o projeto crescer para múltiplos serviços independentes de fato
  (ex.: um serviço de IA separado do backend principal), esta decisão deve
  ser revisitada.
