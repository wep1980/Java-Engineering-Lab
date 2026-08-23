# Links do Ambiente — Java Engineering Lab

> Este arquivo registra **apenas** URLs reais, validadas por execução
> local nesta máquina. URLs de serviços ainda não implementados/validados
> ficam marcadas como "Não implementado" — nunca como se já estivessem
> disponíveis.

## Repositório

| Recurso | URL |
|---|---|
| GitHub (público) | https://github.com/wep1980/Java-Engineering-Lab |

## Ambiente local (profile `core`, validado em 2026-08-22)

Subindo com `docker compose --profile core up --build` (após copiar
`.env.example` para `.env`):

| Serviço | URL | Observação |
|---|---|---|
| Frontend | http://localhost:3000 | Porta configurável via `PORTA_FRONTEND` no `.env` |
| Catálogo de laboratórios (frontend) | http://localhost:3000/laboratorios | Validado, consumindo a API real |
| Laboratório de N+1 (frontend) | http://localhost:3000/laboratorios/n1-queries | Validado no navegador — execução real das 4 variantes |
| Backend (API) | http://localhost:8080 | |
| Catálogo de laboratórios (API) | http://localhost:8080/api/laboratorios | Validado retornando `200` |
| Detalhe de laboratório (API) | http://localhost:8080/api/laboratorios/n1-queries | Validado retornando `200`, `status: DISPONIVEL`; id inexistente retorna `404` |
| Execução do laboratório N+1 (API) | `POST http://localhost:8080/api/laboratorios/n1-queries/execucoes/{variante}` | `variante`: `problematico`, `join-fetch`, `entity-graph`, `dto-projection` — validado, `origemDados: REAL` |
| Laboratório de Race Condition (frontend) | http://localhost:3000/laboratorios/race-condition | Validado no navegador — 3 variantes com concorrência real |
| Execução do laboratório Race Condition (API) | `POST http://localhost:8080/api/laboratorios/race-condition/execucoes/{variante}` | `variante`: `sem-controle`, `otimista`, `pessimista` — validado, `origemDados: REAL` |
| Health-check do backend | http://localhost:8080/actuator/health | Validado retornando `{"status":"UP"}` |
| Swagger UI | http://localhost:8080/swagger-ui/index.html | Validado retornando `200` |
| OpenAPI (JSON) | http://localhost:8080/v3/api-docs | Validado retornando `200` |
| PostgreSQL | localhost:5432 | Em uso real pelo backend desde a Fase 3 (dados do laboratório de N+1) |

Sem Docker, os mesmos serviços sobem com `mvn spring-boot:run` (backend,
porta 8080) e `npm run dev` (frontend, porta 3000 por padrão — o Next.js
escolhe automaticamente outra porta se a 3000 já estiver em uso por outro
processo na máquina).

## Ambiente local (profiles `core` + `messaging`, validado em 2026-08-22)

O laboratório de Kafka/idempotência exige os dois profiles juntos:
`docker compose --profile core --profile messaging up --build`.

| Serviço | URL | Observação |
|---|---|---|
| Laboratório de Kafka/Idempotência (frontend) | http://localhost:3000/laboratorios/kafka-idempotencia | Validado no navegador — 2 variantes, evento duplicado real |
| Execução do laboratório Kafka/Idempotência (API) | `POST http://localhost:8080/api/laboratorios/kafka-idempotencia/execucoes/{variante}` | `variante`: `sem-idempotencia`, `idempotente` — validado, `origemDados: REAL` |
| Kafka (broker) | localhost:9092 | KRaft, sem Zookeeper — validado (partições atribuídas, tópicos criados) |
| Kafka UI | http://localhost:8081 | Validado — tópicos `pagamentos-confirmados-sem-idempotencia` e `pagamentos-confirmados-idempotente` visíveis |

Sem o profile `messaging`, o backend sobe normalmente (profile `core`
sozinho continua funcionando para N+1 e Race Condition) — o endpoint
deste laboratório responde `503` com mensagem clara em vez de travar.
**Isso só é verdade a partir da correção em
`docs/decisions/0007-fallback-de-bootstrap-servers-do-kafka.md`** — antes
dela, o backend travava na inicialização sem o profile `messaging`
(regressão real, encontrada durante a validação da Fase 6).

## Ambiente local (profiles `core` + `observability`, validado em 2026-08-23)

`docker compose --profile core --profile observability up --build`.

| Serviço | URL | Observação |
|---|---|---|
| Prometheus | http://localhost:9090 | Validado — target `java-engineering-lab-backend` reportando `UP` |
| Grafana | http://localhost:3300 | Login `admin` / `GRAFANA_SENHA_ADMIN` do `.env`. Datasources (Prometheus, Tempo) e o dashboard "Java Engineering Lab — Backend" já vêm provisionados, sem passos manuais |
| Dashboard do backend | http://localhost:3300/d/jel-backend-overview | Validado no navegador com dados reais: disponibilidade, heap JVM, taxa de requisições HTTP, latência média, threads ativas |
| Tempo (traces) | http://localhost:3200 | Validado — trace real de uma requisição encontrado via `/api/traces/{traceId}` e acessível pelo proxy do datasource no Grafana |

Logs do backend em JSON estruturado (Elastic Common Schema), incluindo
`correlationId`, `traceId` e `spanId` em cada linha gerada durante uma
requisição — validado via `docker logs`.

Sem o profile `observability`, o backend sobe normalmente (a exportação
de traces apenas falha silenciosamente em background).

## Profiles ainda não validados em execução

| Serviço | Profile | Status |
|---|---|---|
| SonarQube | `quality` | Configuração escrita, não validada em execução — entra na Fase 8 |

Este arquivo será atualizado conforme cada profile for efetivamente
validado em execução.
