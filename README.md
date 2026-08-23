# Java Engineering Lab

Repositório: https://github.com/wep1980/Java-Engineering-Lab

> Um laboratório interativo de Engenharia de Software para aprender — na
> prática, não só na teoria — os problemas mais comuns (e mais cobrados em
> entrevistas) de aplicações Java/Spring: N+1, race conditions,
> mensageria duplicada, e outros.

**Status atual: Fase 6 — Observabilidade consolidada.** Três
laboratórios completos, mais logs estruturados, métricas (Prometheus/
Grafana) e tracing distribuído (OpenTelemetry/Tempo), todos validados
contra infraestrutura real. Veja
[Estado atual do projeto](#estado-atual-do-projeto).

## Por que este projeto existe

A maioria dos tutoriais mostra *como* um problema como N+1 acontece em
duas linhas de código. Poucos deixam você *ver* o problema acontecer com
métricas reais, *diagnosticar* a causa raiz, *aplicar* uma correção, e
*comparar* o antes e o depois com a mesma massa de dados. É isso que o
Java Engineering Lab tenta ser: menos um tutorial, mais um laboratório.

Cada laboratório segue o mesmo fluxo:

```text
introdução → arquitetura → executar problema → observar → diagnosticar
→ código problemático → soluções → aplicar solução → executar novamente
→ antes × depois → trade-offs → explicar em entrevista
```

## Que problema resolve

- Transforma conceitos abstratos de performance/concorrência/mensageria em
  experiências visuais e executáveis.
- Mostra causa, sintoma e impacto — não só a correção.
- Serve como preparação objetiva para entrevistas técnicas Java Sênior.
- É, ao mesmo tempo, um projeto de portfólio construído com processo de
  engenharia real: SPEC antes de código, decisões rastreáveis, testes
  automatizados, observabilidade e histórico íntegro de todas as decisões.

## Laboratórios

| Laboratório | Status |
|---|---|
| N+1 Queries | **Disponível** — `/laboratorios/n1-queries`, 4 variantes executáveis com métricas reais (`specs/labs/SPEC-LAB-N1-001-n-mais-um-queries.md`) |
| Race Condition / Lost Update | **Disponível** — `/laboratorios/race-condition`, 3 variantes com concorrência real (`specs/labs/SPEC-LAB-RACE-001-race-condition-lost-update.md`) |
| Kafka / Mensagem Duplicada / Idempotência | **Disponível** — `/laboratorios/kafka-idempotencia`, 2 variantes com Kafka real (`specs/labs/SPEC-LAB-KAFKA-IDEMP-001-mensagem-duplicada-idempotencia.md`) |
| Demais laboratórios do backlog | Ver `docs/roadmap.md` |

## Stack

**Backend**: Java 21, Spring Boot, Spring Data JPA, Hibernate, Maven.
**Frontend**: Next.js, React, TypeScript, Tailwind CSS.
**Dados**: PostgreSQL, Redis. **Mensageria**: Apache Kafka.
**Testes**: JUnit 5, Mockito, Testcontainers.
**Observabilidade**: Micrometer, Prometheus, Grafana, OpenTelemetry.
**Qualidade**: SonarQube, JaCoCo. **Infra**: Docker, Docker Compose.
**CI/CD**: GitHub Actions.

Detalhes e justificativas em
[`specs/architecture/SPEC-JEL-002-arquitetura.md`](specs/architecture/SPEC-JEL-002-arquitetura.md).

## Como executar

Para os laboratórios de N+1 e Race Condition (frontend + backend +
PostgreSQL):

```bash
cp .env.example .env
docker compose --profile core up --build
```

Para o laboratório de Kafka/Idempotência, o profile `messaging` também
precisa estar no ar:

```bash
docker compose --profile core --profile messaging up --build
```

Para métricas, logs estruturados e tracing distribuído, o profile
`observability`:

```bash
docker compose --profile core --profile observability up --build
```

- Backend: http://localhost:8080 (health-check em `/actuator/health`,
  Swagger UI em `/swagger-ui/index.html`).
- Frontend: http://localhost:3000.
- Kafka UI (com `messaging` ativo): http://localhost:8081.
- Grafana (com `observability` ativo): http://localhost:3300 — dashboard
  e datasources (Prometheus, Tempo) já provisionados.

O profile `quality` tem a configuração escrita em `docker-compose.yml`,
mas só entra em uso quando a Fase 8 existir (ver
`specs/architecture/SPEC-JEL-004-bootstrap-de-codigo.md`).

Para desenvolvimento sem Docker:

```bash
# backend (Java 21 + Maven)
cd backend && mvn spring-boot:run

# frontend (Node.js 22+)
cd frontend && npm install && npm run dev
```

## Como o projeto é construído: Spec-Driven Development

Nenhuma implementação relevante começa sem uma SPEC aprovada. O fluxo é:

```text
SPEC → implementação → testes → validação → documentação → evidências
```

- **SPECs**: [`specs/`](specs/) — organizadas por manifesto, arquitetura,
  backend, frontend, laboratórios, infra, segurança e testes.
- **Decisões arquiteturais (ADRs)**: [`docs/decisions/`](docs/decisions/).
- **Diagramas**: [`diagrams/`](diagrams/) — C4 e fluxos, em Mermaid.
- **Histórico integral de conversas**: [`docs/conversation-history.md`](docs/conversation-history.md) —
  registro cronológico, append-only, de toda decisão tomada com o Claude
  Code neste projeto.
- **Roadmap**: [`docs/roadmap.md`](docs/roadmap.md).

## Idioma

Português do Brasil é o idioma oficial do projeto — código próprio, banco
de dados, APIs próprias, documentação e commits. Termos técnicos impostos
por frameworks, protocolos e padrões consolidados permanecem no idioma
original. Ver `specs/manifest/MANIFESTO.md` para a convenção completa.

## Estado atual do projeto

Este projeto está sendo construído com o Claude Code seguindo um processo
declarado de governança (ver o histórico completo em
`docs/conversation-history.md`). Na Fase 0 foram criados: a estrutura de
SPECs, o mecanismo de histórico de conversas, as instruções persistentes
do projeto (`CLAUDE.md`), a arquitetura proposta, os diagramas iniciais e
o roadmap. Na Fase 1 foram criados o esqueleto executável do backend
(Java 21, Spring Boot 4, Maven) e do frontend (Next.js 16, React 19,
TypeScript, Tailwind CSS 4), o `docker-compose.yml` com profiles, e CI
básico (GitHub Actions). Na Fase 2 (`SPEC-JEL-003`, também concluída)
foram criados o catálogo de laboratórios (backend:
`GET /api/laboratorios` e `GET /api/laboratorios/{id}`; frontend:
`/laboratorios` e `/laboratorios/[id]`), o contrato de execução/métricas
e o tratamento padrão de erros com correlation ID. Na Fase 3
(`SPEC-LAB-N1-001`, concluída) o laboratório de N+1 foi implementado por
completo: entidades JPA (`Pedido`/`ItemPedido`), massa de dados
determinística, as quatro variantes de execução (problemático, JOIN
FETCH, `@EntityGraph`, DTO Projection) com contagem real de queries via
Hibernate Statistics, testes de integração com Testcontainers validando
os números exatos, e o painel interativo no frontend
(`/laboratorios/n1-queries`). Na Fase 4 (`SPEC-LAB-RACE-001`, concluída)
o laboratório de Race Condition / Lost Update foi implementado com
**concorrência real**: 10 depósitos concorrentes disparados por threads
reais (`ExecutorService` + barreira de largada), variante sem controle
perdendo atualizações de forma determinística, e as duas soluções
(Optimistic Locking com `@Version` e Pessimistic Locking com
`SELECT ... FOR UPDATE`) validadas por testes de integração com
Testcontainers. Na Fase 5 (`SPEC-LAB-KAFKA-IDEMP-001`, concluída) o
laboratório de Kafka/Idempotência publica o mesmo evento **duas vezes de
verdade** em um broker Kafka real (profile `messaging`), com um
consumidor sem proteção (credita duas vezes) e um consumidor idempotente
(credita uma única vez via chave de idempotência), validados por testes
de integração com Testcontainers (Kafka + PostgreSQL simultâneos). Um bug
real de sincronização foi encontrado e corrigido durante a validação
manual — ver `docs/decisions/0006-sincronizacao-so-apos-commit-em-listeners.md`.
Na Fase 6 (`SPEC-JEL-005`, concluída) a observabilidade foi consolidada:
logs estruturados em JSON (com `correlationId`/`traceId`/`spanId`),
Prometheus + Grafana com dashboard provisionado automaticamente, e
tracing distribuído real via OpenTelemetry + Grafana Tempo. A validação
contra infraestrutura real (não só a configuração) encontrou e corrigiu
cinco problemas reais nessa fase — incluindo uma regressão que travava o
backend sem o profile `messaging` ativo — documentados em
`docs/decisions/0007-fallback-de-bootstrap-servers-do-kafka.md` e na
própria `SPEC-JEL-005`. Os laboratórios futuros do backlog e a Fase 7
(Engineering AI Assistant) seguem pendentes de aprovação antes de
começar (ver `docs/roadmap.md`).

## Como contribuir

O projeto ainda não está aberto a contribuições externas formalmente —
`CONTRIBUTING.md`, `CODE_OF_CONDUCT.md` e `LICENSE` estão previstos para
uma fase futura (ver seção "Open Source" do manifesto original registrado
em `docs/conversation-history.md`).
