# Arquitetura — Java Engineering Lab

> Visão narrativa da arquitetura proposta. A especificação formal, com
> requisitos e critérios de aceite, está em
> `specs/architecture/SPEC-JEL-002-arquitetura.md` e
> `specs/architecture/SPEC-JEL-003-mvp-plataforma-base.md`. Tudo neste
> documento é **proposta**, pendente de aprovação.

## Visão geral

O Java Engineering Lab é um monorepo (justificativa em
`docs/decisions/0002-monorepo.md`) com um backend Java/Spring Boot, um
frontend Next.js, e uma camada de documentação/specs tão relevante quanto
o código, já que o processo de construção segue Spec-Driven Development.

Diagrama de contexto: [`diagrams/c4-contexto.md`](../diagrams/c4-contexto.md).
Diagrama de contêineres: [`diagrams/c4-conteiner.md`](../diagrams/c4-conteiner.md).

## Backend

Separação por camadas (Controller → Service → Domínio ← Repository) com
DTOs na fronteira da API pública. Pacotes organizados por domínio de
laboratório, com um módulo `plataforma` para o que é compartilhado
(catálogo, execução, métricas). Arquitetura Hexagonal fica como hipótese
em aberto, reavaliada quando um laboratório concreto (provavelmente o de
Kafka/idempotência) exigir múltiplos adaptadores de entrada.

## Frontend

Next.js + React + TypeScript + Tailwind CSS, organizado por domínio de
tela (catálogo, página de laboratório) em vez de por tipo técnico de
arquivo.

## Dados e mensageria

PostgreSQL é o único armazenamento necessário para o primeiro laboratório
(N+1). Redis e Kafka fazem parte da stack obrigatória do projeto, mas só
entram em uso quando um laboratório específico os exigir (cache e
idempotência, respectivamente) — evitando infraestrutura ociosa desde o
início.

## Observabilidade

Micrometer/Prometheus para métricas, OpenTelemetry para tracing, logs
estruturados com correlation ID. Cada laboratório expõe apenas a
telemetria relevante ao problema que demonstra — ver `docs/observability.md`.

## Infraestrutura local

Docker Compose com profiles (`core`, `messaging`, `observability`,
`quality`, `full`) para permitir subir só o necessário. O
`docker-compose.yml` efetivo é criado na Fase 1, junto ao restante do
bootstrap de código.

## Fluxo padrão de laboratório

Todo laboratório segue o mesmo fluxo educacional (introdução → arquitetura
→ execução do problema → observação → diagnóstico → código → soluções →
aplicação → comparação → trade-offs → entrevista). Ver
[`diagrams/fluxo-laboratorio.md`](../diagrams/fluxo-laboratorio.md) e o
detalhamento para o laboratório de N+1 em
`specs/labs/SPEC-LAB-N1-001-n-mais-um-queries.md`.
