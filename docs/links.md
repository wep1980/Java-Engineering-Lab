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
| Laboratório de Connection Pool Exhaustion (frontend) | http://localhost:3000/laboratorios/connection-pool-exhaustion | Validado no navegador (2026-08-23) — 3 variantes, pools de demonstração isolados do pool principal |
| Execução do laboratório Connection Pool Exhaustion (API) | `POST http://localhost:8080/api/laboratorios/connection-pool-exhaustion/execucoes/{variante}` | `variante`: `pool-pequeno`, `pool-redimensionado`, `conexao-curta` — validado, `origemDados: REAL` |
| Laboratório de Deadlock (frontend) | http://localhost:3000/laboratorios/deadlock | Validado no navegador (2026-08-23) — 2 variantes, deadlock real detectado pelo PostgreSQL |
| Execução do laboratório Deadlock (API) | `POST http://localhost:8080/api/laboratorios/deadlock/execucoes/{variante}` | `variante`: `sem-ordem-consistente`, `ordem-consistente` — validado, `origemDados: REAL` |
| Laboratório de Query sem índice (frontend) | http://localhost:3000/laboratorios/query-sem-indice | Validado no navegador (2026-08-23) — 2 variantes, EXPLAIN ANALYZE real do PostgreSQL |
| Execução do laboratório Query sem índice (API) | `POST http://localhost:8080/api/laboratorios/query-sem-indice/execucoes/{variante}` | `variante`: `sem-indice`, `com-indice` — validado, `origemDados: REAL` |
| Laboratório de Circuit Breaker (frontend) | http://localhost:3000/laboratorios/circuit-breaker | Validado no navegador (2026-08-27) — 2 variantes, circuit breaker real (Resilience4j) |
| Execução do laboratório Circuit Breaker (API) | `POST http://localhost:8080/api/laboratorios/circuit-breaker/execucoes/{variante}` | `variante`: `sem-circuit-breaker`, `com-circuit-breaker` — validado, `origemDados: REAL` |
| Laboratório de Transactional Outbox (frontend) | http://localhost:3000/laboratorios/transactional-outbox | Validado no navegador (2026-08-27) — 2 variantes, exige perfil `messaging` (Kafka real) |
| Execução do laboratório Transactional Outbox (API) | `POST http://localhost:8080/api/laboratorios/transactional-outbox/execucoes/{variante}` | `variante`: `sem-outbox`, `com-outbox` — validado, `origemDados: REAL` |
| Laboratório de Ordenação de Eventos (frontend) | http://localhost:3000/laboratorios/ordenacao-de-eventos | Validado no navegador (2026-08-27) — 2 variantes, exige perfil `messaging` (Kafka real, tópico de 3 partições) |
| Execução do laboratório Ordenação de Eventos (API) | `POST http://localhost:8080/api/laboratorios/ordenacao-de-eventos/execucoes/{variante}` | `variante`: `sem-chave-particionamento`, `com-chave-particionamento` — validado, `origemDados: REAL` |
| Laboratório de Memory Leak / OutOfMemoryError (frontend) | http://localhost:3000/laboratorios/memory-leak | Validado no navegador (2026-08-27) — 2 variantes, nenhuma infraestrutura extra (só o profile `core`) |
| Execução do laboratório Memory Leak (API) | `POST http://localhost:8080/api/laboratorios/memory-leak/execucoes/{variante}` | `variante`: `com-vazamento`, `sem-vazamento` — validado, `origemDados: REAL` |
| Laboratório de Thread Pool Exhaustion (frontend) | http://localhost:3000/laboratorios/thread-pool-exhaustion | Validado no navegador (2026-08-27) — 2 variantes, nenhuma infraestrutura extra (só o profile `core`) |
| Execução do laboratório Thread Pool Exhaustion (API) | `POST http://localhost:8080/api/laboratorios/thread-pool-exhaustion/execucoes/{variante}` | `variante`: `fila-ilimitada`, `fila-limitada` — validado, `origemDados: REAL` |
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

## Ambiente local (profiles `core` + `ai`, validado em 2026-08-23)

`docker compose --profile core --profile ai up --build`. Na primeira
subida, o serviço auxiliar `ollama-modelo` baixa o modelo (~2GB) e
encerra sozinho — acompanhe com `docker compose logs -f ollama-modelo`.

| Serviço | URL | Observação |
|---|---|---|
| Ollama (API) | http://localhost:11434 | Porta configurável via `PORTA_OLLAMA` no `.env`. Validado — `GET /api/tags` retorna `llama3.2:3b` baixado (2.02 GB) |
| Assistente de IA (API) | `POST http://localhost:8080/api/laboratorios/{id}/assistente/perguntas` | Corpo `{pergunta, ultimoResultado?}`. Validado via `curl` direto no backend e via proxy do frontend — resposta real do modelo, não fabricada |
| Painel do assistente (frontend) | Embutido em cada página de laboratório (`/laboratorios/n1-queries`, `/laboratorios/race-condition`, `/laboratorios/kafka-idempotencia`) | Validado no navegador — pergunta real respondida usando o resultado real da última execução exibida na tela como contexto |

Sem o profile `ai`, os laboratórios continuam funcionando normalmente
— validado parando os containers `ollama`/`ollama-modelo` e confirmando
que uma execução do laboratório N+1 continuou respondendo `200`
enquanto o endpoint do assistente passou a responder `503` com mensagem
clara.

## Ambiente local (profile `quality`, validado em 2026-08-23)

`docker compose --profile quality up`.

| Serviço | URL | Observação |
|---|---|---|
| SonarQube | http://localhost:9000 | Porta configurável via `PORTA_SONARQUBE` no `.env`. Validado — análise real do backend (`mvn ...:sonar`) processada, achados reais consultados via API (ver `SPEC-JEL-007-hardening.md`, seção "Evidências de conclusão") |

Login inicial `admin`/`admin`, com troca de senha obrigatória no
primeiro acesso (via UI ou `POST /api/users/change_password`). Um token
de análise é gerado em **My Account → Security** (ou via
`POST /api/user_tokens/generate`) para uso com
`-Dsonar.token=...` no `sonar-maven-plugin`.

Este arquivo será atualizado conforme cada profile for efetivamente
validado em execução.
