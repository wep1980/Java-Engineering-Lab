# Guia de Testes — Java Engineering Lab

> Este guia descreve a **estratégia** de testes do projeto. Ele não contém
> ainda endpoints, payloads ou passos de reprodução reais, porque nenhum
> código funcional foi implementado (Fase 0 — governança e descoberta).
> Será preenchido incrementalmente a partir da Fase 1, com pré-requisitos,
> ordem de inicialização, endpoints, payloads, headers, cenários positivos
> e negativos e validações reais — nunca com exemplos fictícios.

## Estratégia por tipo de teste

| Tipo | Quando usar | Ferramentas |
|---|---|---|
| Unitário | Lógica de domínio/serviço sem dependência externa real | JUnit 5, Mockito |
| Integração | Comportamento que depende de banco, Kafka ou Redis reais | JUnit 5, Testcontainers |
| Concorrência | Laboratórios de race condition / lost update / deadlock | JUnit 5 com execução concorrente controlada (ex.: `ExecutorService` + `CountDownLatch`, ou `Awaitility` para asserts assíncronos) |
| End-to-end | Fluxos completos frontend → backend, quando aplicável | A definir na Fase 8 |

Princípio: usar mock quando a dependência real não é necessária; usar
Testcontainers quando o comportamento integrado precisa ser validado. Não
usar mock para substituir uma dependência cujo comportamento real é o que
está sendo testado (ex.: contagem de queries do laboratório de N+1 precisa
de um PostgreSQL real).

## Cobertura

JaCoCo mede cobertura como indicador auxiliar, não como meta em si. Testes
não são criados apenas para aumentar percentual.

## Qualidade estática

SonarQube analisa bugs, vulnerabilidades, code smells e duplicação a
partir da Fase 1 (quando o pipeline de CI for configurado).

## Validação do esqueleto (Fase 1, 2026-08-22)

Sem regras de negócio ainda, a validação da Fase 1 foi de infraestrutura:

1. `mvn -f backend/pom.xml test` — passou (`contextoDeveCarregar`, contexto Spring sobe).
2. `mvn -f backend/pom.xml spring-boot:run` + `curl http://localhost:8080/actuator/health` — retornou `{"status":"UP"}`.
3. `curl http://localhost:8080/swagger-ui/index.html` e `/v3/api-docs` — ambos retornaram `200`.
4. `npm --prefix frontend run build` e `npm --prefix frontend run lint` — passaram sem erros.
5. `npm --prefix frontend run dev` + acesso via navegador local — página inicial renderizou o conteúdo esperado em português.
6. `docker compose --profile core config` — sintaxe validada, sem erros.
7. `docker compose --profile core up --build` — os três serviços (`postgres`, `backend`, `frontend`) subiram; `postgres` reportou `healthy`; `backend` respondeu `UP` em `/actuator/health`; `frontend` respondeu `200` com o conteúdo esperado.

Os profiles `messaging`, `observability` e `quality` tiveram apenas a
sintaxe validada (`docker compose config`) — sua subida efetiva fica para
as fases que os utilizam (ver `docs/links.md`).

## Validação do catálogo de laboratórios (Fase 2, 2026-08-22)

Pré-requisito: backend rodando (`mvn spring-boot:run` ou via Docker Compose).

| Endpoint | Método | Cenário | Resultado esperado |
|---|---|---|---|
| `/api/laboratorios` | GET | Catálogo | `200`, lista com `n1-queries` (`status: PLANEJADO`) |
| `/api/laboratorios/n1-queries` | GET | Laboratório existente | `200`, corpo com `nome`, `objetivo`, `status` |
| `/api/laboratorios/inexistente` | GET | Laboratório inexistente | `404`, corpo `{codigo, mensagem, timestamp, caminho, correlationId}`, cabeçalho `X-Correlation-Id` |

Validações executadas: os 6 testes automatizados do backend passaram
(`mvn test`); os três cenários acima foram validados manualmente com
`curl`, inclusive confirmando acentuação UTF-8 correta na mensagem de
erro ("Laboratório não encontrado"); o frontend (`/laboratorios` e
`/laboratorios/[id]`) foi validado com `npm run build`/`lint` e visualmente
no Chrome (catálogo e detalhe do laboratório de N+1, incluindo o estado
"ainda não disponível" para status `PLANEJADO`); e a comunicação
frontend→backend foi validada tanto localmente (`localhost`) quanto dentro
da rede do Docker Compose (`http://backend:8080`).

## Validação do laboratório de N+1 (Fase 3, 2026-08-22)

Pré-requisito: backend rodando com PostgreSQL real (`docker compose --profile core up` ou `mvn spring-boot:run` com `docker compose --profile core up postgres`). Dados semeados automaticamente no startup (50 pedidos × 3 itens).

| Endpoint | Método | Cenário | Resultado esperado |
|---|---|---|---|
| `/api/laboratorios/n1-queries/execucoes/problematico` | POST | Variante problemática | `200`, `metricas.quantidadeQueries: 51`, `metricas.quantidadePedidos: 50`, `origemDados: REAL` |
| `/api/laboratorios/n1-queries/execucoes/join-fetch` | POST | JOIN FETCH | `200`, `metricas.quantidadeQueries: 1` |
| `/api/laboratorios/n1-queries/execucoes/entity-graph` | POST | @EntityGraph | `200`, `metricas.quantidadeQueries: 1` |
| `/api/laboratorios/n1-queries/execucoes/dto-projection` | POST | DTO Projection | `200`, `metricas.quantidadeQueries: 1` |
| `/api/laboratorios/n1-queries/execucoes/inexistente` | POST | Variante inválida | `400`, corpo no formato de erro padrão |

Validações executadas:
- Testes de integração com Testcontainers (`ExecucaoN1ServiceIntegrationTest`,
  4 testes) comprovam as contagens exatas contra PostgreSQL real — não é
  estimativa, é `Statistics.getPrepareStatementCount()` do Hibernate (ADR-0005).
- Os 5 cenários da tabela acima foram validados manualmente com `curl`
  contra o ambiente Docker Compose real.
- O painel interativo em `/laboratorios/n1-queries` foi validado no
  Chrome: os quatro botões disparam execuções reais (via proxy same-origin
  em `frontend/src/app/api/laboratorios/[id]/execucoes/[variante]/route.ts`),
  os números exibidos batem com os da API, e o card de comparação
  "antes × depois" aparece corretamente. Sem erros no console.

## Validação do laboratório de Race Condition (Fase 4, 2026-08-22)

Pré-requisito: backend rodando com PostgreSQL real. Cada execução dispara
10 depósitos concorrentes reais de R$ 100,00 na conta de demonstração
correspondente (reiniciada a cada chamada).

| Endpoint | Método | Cenário | Resultado esperado |
|---|---|---|---|
| `/api/laboratorios/race-condition/execucoes/sem-controle` | POST | Sem controle de concorrência | `200`, `metricas.saldoFinal < 1000`, `metricas.atualizacoesPerdidas > 0` (tipicamente 9) |
| `/api/laboratorios/race-condition/execucoes/otimista` | POST | Optimistic Locking | `200`, `metricas.saldoFinal: 1000`, `metricas.atualizacoesPerdidas: 0`, `metricas.conflitosDetectadosERetentados > 0` |
| `/api/laboratorios/race-condition/execucoes/pessimista` | POST | Pessimistic Locking | `200`, `metricas.saldoFinal: 1000`, `metricas.atualizacoesPerdidas: 0`, `metricas.conflitosDetectadosERetentados: 0`, `duracaoMs` nitidamente maior (acesso serializado) |
| `/api/laboratorios/race-condition/execucoes/inexistente` | POST | Variante inválida | `400`, formato de erro padrão |

Validações executadas:
- Testes de integração com Testcontainers e **concorrência real**
  (`ExecutorService` + `CountDownLatch`) — `ExecucaoRaceConditionServiceIntegrationTest`,
  3 testes, rodados 3 vezes seguidas sem falha (não-flaky).
- Os 4 cenários da tabela acima validados manualmente com `curl` contra
  o ambiente Docker Compose real. Exemplo real observado: `sem-controle`
  → R$ 100 em 202ms; `otimista` → R$ 1.000 com 45 conflitos em 241ms;
  `pessimista` → R$ 1.000 com 0 conflitos em 1123ms.
- Painel interativo em `/laboratorios/race-condition` validado no
  Chrome: os três botões disparam execuções reais, o card de saldo final
  fica vermelho quando há perda e verde quando não há. Sem erros no
  console.

## Preenchimento futuro (por fase)

- **Fase 2/3**: pré-requisitos de ambiente, ordem de subida dos serviços
  (`docker compose up` com o profile `core`), endpoints do catálogo e do
  laboratório de N+1, payloads reais, exemplos de coleção Postman.
- **Fase 4/5**: cenários de concorrência e de mensageria Kafka, incluindo
  validações de idempotência e de estado no banco/Redis.
- **Fase 6**: validações de métricas (Prometheus) e traces (OpenTelemetry).

Cada seção só é escrita quando o comportamento correspondente existir de
fato e puder ser validado por execução real — nunca antecipadamente.
