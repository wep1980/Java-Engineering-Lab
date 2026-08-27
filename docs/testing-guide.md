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

## Validação do laboratório de Kafka/Idempotência (Fase 5, 2026-08-22)

Pré-requisito: backend rodando com PostgreSQL **e Kafka** reais —
`docker compose --profile core --profile messaging up`. Sem o Kafka no
ar, o endpoint responde `503` (não trava, não erra genericamente).

| Endpoint | Método | Cenário | Resultado esperado |
|---|---|---|---|
| `/api/laboratorios/kafka-idempotencia/execucoes/sem-idempotencia` | POST | Evento duplicado, sem proteção | `200`, `metricas.saldoFinal: 100`, `metricas.quantidadeProcessamentosEfetivos: 2`, `metricas.quantidadeEventosConsumidos: 2` |
| `/api/laboratorios/kafka-idempotencia/execucoes/idempotente` | POST | Evento duplicado, com deduplicação | `200`, `metricas.saldoFinal: 50`, `metricas.quantidadeProcessamentosEfetivos: 1`, `metricas.quantidadeEventosConsumidos: 2` |
| `/api/laboratorios/kafka-idempotencia/execucoes/inexistente` | POST | Variante inválida | `400`, formato de erro padrão |

Validações executadas:
- Testes de integração com Testcontainers (Kafka **e** PostgreSQL reais
  simultâneos) — `ExecucaoKafkaIdempotenciaServiceIntegrationTest`, 2
  testes, rodados 3 vezes seguidas sem falha.
- **Bug real encontrado e corrigido** durante a validação manual (não
  pelos testes automatizados — ver `docs/decisions/0006-sincronizacao-so-apos-commit-em-listeners.md`):
  sinalizar conclusão do processamento antes do commit da transação
  causava leitura de saldo inconsistente. Corrigido e revalidado com 2
  execuções consecutivas de cada variante, resultado idêntico nas duas.
- Os 3 cenários da tabela acima validados manualmente com `curl` contra
  o ambiente Docker Compose real (`core` + `messaging`).
- Tópicos Kafka confirmados via API do Kafka UI.
- Painel interativo em `/laboratorios/kafka-idempotencia` validado no
  Chrome: os dois botões disparam publicação real e dupla do evento; o
  card de saldo final fica vermelho (sem idempotência) ou verde
  (idempotente). Sem erros no console.

## Validação da observabilidade consolidada (Fase 6, 2026-08-23)

Pré-requisito: `docker compose --profile core --profile observability up`.

| O quê | Como validar | Resultado esperado |
|---|---|---|
| Logs estruturados | `docker logs <container>` | Cada linha é um JSON (ECS); linhas geradas durante uma requisição incluem `correlationId`, `traceId`, `spanId` |
| Prometheus | `curl .../api/v1/targets` | `java-engineering-lab-backend` com `health: up` |
| Grafana — datasources | `curl -u admin:senha .../api/datasources` | Prometheus e Tempo listados |
| Grafana — dashboard | Abrir `http://localhost:3300/d/jel-backend-overview` no navegador | 5 painéis com dados reais (não vazios) |
| Tracing | `curl .../api/traces/{traceId}` no Tempo, usando um `traceId` de um log real | `200` com o span completo |
| Regressão | `docker compose --profile core up` (sem observability nem messaging) | Backend sobe e responde normalmente |

Validações executadas: todos os itens acima confirmados com dados reais
(não simulados) nesta sessão. Cinco problemas reais foram encontrados e
corrigidos durante essa validação — detalhados em
`specs/architecture/SPEC-JEL-005-observabilidade-consolidada.md`, seção
"Percalços técnicos reais". Nenhum deles seria detectável só lendo o
código ou a configuração.

## Validação do Engineering AI Assistant (Fase 7, 2026-08-23)

Pré-requisito: `docker compose --profile core --profile ai up --build`.
Na primeira subida, o serviço auxiliar `ollama-modelo` baixa o modelo
`llama3.2:3b` (~2GB) e encerra sozinho — aguardar antes de testar.

| Endpoint | Método | Cenário | Resultado esperado |
|---|---|---|---|
| `/api/laboratorios/{id}/assistente/perguntas` | POST | Pergunta com `ultimoResultado` real | `200`, `{resposta}` com texto real do modelo, referenciando o contexto enviado |
| `/api/laboratorios/{id}/assistente/perguntas` | POST | `pergunta` em branco | `400`, formato de erro padrão (`@NotBlank`) |
| `/api/laboratorios/{id}/assistente/perguntas` | POST | Ollama indisponível (profile `ai` fora do ar) | `503`, formato de erro padrão, mensagem citando o profile `ai` |

Validações executadas:
- Os 3 testes de `AssistenteIAControllerTest` passaram (`mvn test`, 24/24
  no total do backend).
- `curl http://localhost:11434/api/tags` confirmou o modelo `llama3.2:3b`
  efetivamente baixado (2.02 GB).
- Pergunta real via `curl` direto no backend, com o resultado real de uma
  execução do laboratório N+1 (51 queries / 50 pedidos) como
  `ultimoResultado`: resposta real do Ollama, referenciando os números
  reais e o conceito de N+1 corretamente.
- Mesma validação repetida através do proxy same-origin do frontend
  (`frontend/src/app/api/laboratorios/[id]/assistente/perguntas/route.ts`).
- Painel interativo validado no Chrome em `/laboratorios/n1-queries`:
  clique real em "Executar versão problemática" → painel do assistente
  atualiza para "usando o resultado da sua última execução como
  contexto" → pergunta digitada e enviada pela UI → resposta real do
  modelo, usando os números exatos da execução exibida na tela.
- **Regressão validada de propósito**: com `ollama`/`ollama-modelo`
  parados, o endpoint do assistente respondeu `503` enquanto uma
  execução do laboratório N+1, no mesmo momento, continuou respondendo
  `200` normalmente — confirma que o profile `ai` é opcional e isolado
  do resto da plataforma.
- `npm run build` e `npm run lint` no frontend, sem erros, após a
  refatoração que eleva `ultimoResultado` para os três componentes de
  conteúdo de laboratório.

## Validação do Hardening (Fase 8, 2026-08-23)

Pré-requisitos variam por trilha — ver `SPEC-JEL-007-hardening.md` para
o detalhamento completo de cada uma. Resumo do que foi validado com
infraestrutura/ferramentas reais (não simulado):

| Trilha | Como foi validado | Resultado real |
|---|---|---|
| Cobertura (JaCoCo) | `mvn -B verify` no backend | 86-87,5% de instruções, relatório em `target/site/jacoco/` |
| Qualidade estática (SonarQube) | `docker compose --profile quality up` + `mvn ...:sonar-maven-plugin:sonar` | 4 bugs reais encontrados e 3 corrigidos (1 suprimido, pedagógico); 0 vulnerabilidades; 0 security hotspots |
| Dependências (OWASP Dependency-Check) | `mvn org.owasp:dependency-check-maven:13.0.0:check -DnvdApiKey=...` no CI | Exigiu chave de API da NVD — bug real do upstream sem ela, ver ADR-0008 |
| Dependências (npm audit) | `npm audit --omit=dev` no frontend | 0 vulnerabilidades |
| Performance | `docker run williamyeh/hey` contra o backend real, comparando N+1 problemático vs. JOIN FETCH | Ver tabela completa em `SPEC-JEL-007`, seção "Evidências de conclusão" — 2,9× a 6,1× mais throughput na variante corrigida |
| UX | `npm run build` + acesso real via `curl` a `/laboratorios/n1-queries` e a uma rota inexistente | Título dinâmico por laboratório confirmado (`<title>N+1 Queries — Java Engineering Lab</title>`); página 404 em português confirmada com status HTTP 404 real |
| Regressão | `docker compose --profile core up` ao final de todas as correções | Backend (`/actuator/health` → `UP`, `/api/laboratorios` → `200`) e frontend (`/laboratorios` → `200`) sem regressão |

Detalhamento completo de cada achado (incluindo os 4 bugs reais do
SonarQube, com arquivo/linha/regra) está em
`specs/architecture/SPEC-JEL-007-hardening.md`.

## Validação do laboratório de Connection Pool Exhaustion (2026-08-23)

Pré-requisito: backend rodando com PostgreSQL real. Os pools de
demonstração são isolados e construídos pelo próprio serviço — nenhum
profile ou serviço adicional é necessário.

| Endpoint | Método | Cenário | Resultado esperado |
|---|---|---|---|
| `/api/laboratorios/connection-pool-exhaustion/execucoes/pool-pequeno` | POST | Pool pequeno (2), segura a conexão | `200`, `metricas.quantidadeFalhasPorTimeout > 0` |
| `/api/laboratorios/connection-pool-exhaustion/execucoes/pool-redimensionado` | POST | Pool maior (12), mesmo código | `200`, `metricas.quantidadeFalhasPorTimeout: 0`, `metricas.quantidadeSucesso: 10` |
| `/api/laboratorios/connection-pool-exhaustion/execucoes/conexao-curta` | POST | Pool pequeno (2), conexão obtida só para a consulta | `200`, `metricas.quantidadeFalhasPorTimeout: 0`, `metricas.quantidadeSucesso: 10` |
| `/api/laboratorios/connection-pool-exhaustion/execucoes/inexistente` | POST | Variante inválida | `400`, formato de erro padrão |

Validações executadas:
- Testes de integração com Testcontainers e concorrência real
  (`ExecucaoConnPoolServiceIntegrationTest`, 3 testes). Suíte completa
  do backend: 29/29 testes.
- **Bug real encontrado e corrigido durante a implementação** (não
  pelos testes automatizados, pela execução real do primeiro teste de
  integração): registrar os pools de demonstração como
  `@Bean HikariDataSource` avulsos quebrava a criação do
  `entityManagerFactory` do JPA para **todos** os laboratórios — ver
  `docs/decisions/0009-pools-de-demonstracao-nao-sao-beans-de-datasource.md`.
- Os 4 cenários validados manualmente com `curl` contra o Docker
  Compose real. Números reais observados: `pool-pequeno` → 4
  sucessos/6 falhas (1004ms); `pool-redimensionado` → 10/0 (504ms);
  `conexao-curta` → 10/0 (505ms — praticamente empatada com o pool
  redimensionado, usando só 2 conexões em vez de 12).
- **Isolamento validado de propósito**: uma execução de `pool-pequeno`
  (gerando falhas reais) disparada em paralelo com uma execução do
  laboratório de N+1 — o N+1 respondeu normalmente em 57ms, confirmando
  que o pool de demonstração não afeta o resto da plataforma.
- Painel interativo em `/laboratorios/connection-pool-exhaustion`
  validado no Chrome: as três variantes disparam execuções reais, com
  "Falhas por timeout" em vermelho quando `> 0` e verde quando `0`.

## Validação do laboratório de Deadlock (2026-08-23)

Pré-requisito: backend rodando com PostgreSQL real.

| Endpoint | Método | Cenário | Resultado esperado |
|---|---|---|---|
| `/api/laboratorios/deadlock/execucoes/sem-ordem-consistente` | POST | Locks travados em ordens opostas | `200`, `metricas.quantidadeDeadlocksDetectados: 1`, `metricas.quantidadeSucesso: 1` |
| `/api/laboratorios/deadlock/execucoes/ordem-consistente` | POST | Locks sempre em ordem ascendente de ID | `200`, `metricas.quantidadeDeadlocksDetectados: 0`, `metricas.quantidadeSucesso: 2` |
| `/api/laboratorios/deadlock/execucoes/inexistente` | POST | Variante inválida | `400`, formato de erro padrão |

Validações executadas:
- **Bug real encontrado e corrigido durante a validação via Docker
  Compose** (não pelos testes automatizados): faltava o
  `ApplicationRunner` que popula as contas de demonstração na subida
  real da aplicação — o endpoint respondia `500` até ser corrigido.
- Testes de integração com Testcontainers e concorrência real
  (`ExecucaoDeadlockServiceIntegrationTest`, 2 testes) — o log da
  própria execução mostrou o deadlock real detectado pelo PostgreSQL
  (`ERROR: deadlock detected`). Suíte completa do backend: 33/33
  testes.
- Os 3 cenários validados manualmente com `curl` contra o Docker
  Compose real. Números reais observados: `sem-ordem-consistente` → 1
  sucesso/1 deadlock, saldos R$450/R$550 (580ms); `ordem-consistente` →
  2 sucessos/0 deadlocks, saldos de volta a R$500/R$500 (630ms).
- **Não-determinismo do vencedor confirmado de propósito**: execuções
  diferentes produziram vencedores diferentes (ora a transferência
  A→B, ora a B→A) — confirma que qual transação o PostgreSQL aborta
  não é previsível, validado explicitamente em vez de presumido.
- **Isolamento validado**: uma execução de `sem-ordem-consistente`
  (produzindo o deadlock) disparada em paralelo com uma execução do
  laboratório de N+1, que respondeu normalmente em 51ms.
- Painel interativo em `/laboratorios/deadlock` validado no Chrome: as
  duas variantes disparam execuções reais, com "Deadlocks (REAL)" em
  vermelho quando `> 0` e verde quando `0`.

## Validação do laboratório de Query sem índice (2026-08-23)

Pré-requisito: backend rodando com PostgreSQL real. A semente de
200.000 linhas é inserida uma única vez, na subida da aplicação.

| Endpoint | Método | Cenário | Resultado esperado |
|---|---|---|---|
| `/api/laboratorios/query-sem-indice/execucoes/sem-indice` | POST | Índice removido, busca por email | `200`, `metricas.tipoDoPlano: "Seq Scan"` |
| `/api/laboratorios/query-sem-indice/execucoes/com-indice` | POST | Índice criado, mesma busca | `200`, `metricas.tipoDoPlano` iniciando com "Index" |
| `/api/laboratorios/query-sem-indice/execucoes/inexistente` | POST | Variante inválida | `400`, formato de erro padrão |

Validações executadas:
- **Dois achados reais durante a implementação**: `@Modifying` do
  Spring Data JPA exige contexto transacional (`@Transactional`
  adicionado ao serviço); e o otimizador escolheu `Bitmap Heap Scan`
  em vez de `Index Scan` até um `ANALYZE` real ser adicionado após
  semear/indexar — ver `SPEC-LAB-INDICE-001`.
- Testes de integração com Testcontainers
  (`ExecucaoIndiceServiceIntegrationTest`, 2 testes), com 200.000
  linhas reais. Suíte completa do backend: 37/37 testes.
- Os 3 cenários validados manualmente com `curl` contra o Docker
  Compose real. Números reais observados: `sem-indice` → `Seq Scan`,
  12,934 ms de execução real da query; `com-indice` → `Index Scan`,
  0,028 ms — diferença real de ~460×.
- **Isolamento validado**: uma execução de `sem-indice` (Seq Scan
  completo em 200 mil linhas) disparada em paralelo com uma execução
  do laboratório de N+1, que respondeu normalmente.
- Painel interativo em `/laboratorios/query-sem-indice` validado no
  Chrome: as duas variantes disparam execuções reais, com "Plano
  (REAL)" em vermelho para `Seq Scan` e verde para `Index Scan`.

## Validação do laboratório de Circuit Breaker (2026-08-27)

Nenhum pré-requisito de infraestrutura (não usa PostgreSQL, Kafka nem
qualquer outro serviço do `docker-compose.yml` — ver RNF-01 de
`SPEC-LAB-CIRCUITBREAKER-001`).

| Endpoint | Método | Cenário | Resultado real observado |
|---|---|---|---|
| `/api/laboratorios/circuit-breaker/execucoes/sem-circuit-breaker` | POST | Dependência sempre indisponível, sem proteção | `200`, `quantidadeFalhasReais: 20`, `quantidadeRejeitadasPeloCircuito: 0`, `estadoFinalDoCircuito: "DESABILITADO"`, `duracaoMs: 6014` |
| `/api/laboratorios/circuit-breaker/execucoes/com-circuit-breaker` | POST | Mesma dependência, circuit breaker real (Resilience4j) | `200`, `quantidadeFalhasReais: 5`, `quantidadeRejeitadasPeloCircuito: 15`, `estadoFinalDoCircuito: "OPEN"`, `duracaoMs: 1509` (~4× mais rápido) |
| `/api/laboratorios/circuit-breaker/execucoes/inexistente` | POST | Variante inválida | `400`, formato de erro padrão |

Validações executadas:
- Testes automatizados reais, sem Testcontainers (nenhuma infraestrutura externa envolvida): `ExecucaoCircuitBreakerServiceTest` (2 testes) e `ExecucaoCircuitBreakerControllerTest` (2 testes), todos passando. Suíte completa do backend: **41/41 testes**.
- `npm run lint` e `npm run build` do frontend sem erros.
- Os 3 cenários validados manualmente com `curl` contra o Docker Compose real — números reais na tabela acima.
- **Isolamento validado**: uma execução de `sem-circuit-breaker` (6s de chamadas sequenciais lentas) disparada em paralelo com uma execução do laboratório de N+1, que respondeu normalmente em 59ms enquanto o circuit breaker ainda rodava.
- Painel interativo em `/laboratorios/circuit-breaker` validado no Chrome: as duas variantes disparam execuções reais, com "Rejeitadas pelo circuito" em verde quando `> 0` e "Estado final do circuito" em laranja para `OPEN`.
- **Achado real durante a validação, fora do escopo deste laboratório**: o Docker Desktop do ambiente ficou indisponível por causa do disco `C:` quase cheio (3,3 GB livres de ~476 GB), travando o motor por ~19h de CPU acumulada. Resolvido liberando espaço (remoção de uma distro WSL não utilizada, com backup prévio) e reiniciando o motor à força — sem relação com o código deste laboratório, que não usa Docker/Testcontainers em nada.

## Validação do laboratório de Transactional Outbox (2026-08-27)

Pré-requisito: backend rodando com o perfil `messaging` (Kafka real) —
`docker compose --profile core --profile messaging up`.

| Endpoint | Método | Cenário | Resultado real observado |
|---|---|---|---|
| `/api/laboratorios/transactional-outbox/execucoes/sem-outbox` | POST | Pedido salvo, publicação direta aponta para endereço inalcançável | `200`, `eventoRegistradoNaOutbox: false`, `eventoPublicadoNoKafka: false`, `inconsistente: true`, `duracaoMs: 1072` |
| `/api/laboratorios/transactional-outbox/execucoes/com-outbox` | POST | Pedido + evento outbox na mesma transação, relay real publica | `200`, `eventoRegistradoNaOutbox: true`, `eventoPublicadoNoKafka: true`, `inconsistente: false`, `duracaoMs: 617` |
| `/api/laboratorios/transactional-outbox/execucoes/inexistente` | POST | Variante inválida | `400`, formato de erro padrão |

Validações executadas:
- **Três achados reais durante a implementação**: colisão de nome com a entidade `Pedido`/tabela `pedido` já existente no laboratório de N+1 (corrigido renomeando para `PedidoOutbox`/`pedido_outbox`); bean `ObjectMapper` autoconfigurado indisponível (corrigido construindo diretamente); relay `@Scheduled` poluindo a contagem global de statements do Hibernate usada pelo laboratório de N+1 em `mvn verify` (corrigido com a flag `outbox.relay.habilitado`, desligada explicitamente no teste de N+1) — ver `SPEC-LAB-OUTBOX-001` para os detalhes completos.
- Testes automatizados reais com Testcontainers (Kafka + PostgreSQL reais simultâneos): `ExecucaoOutboxServiceIntegrationTest` (2 testes) e `ExecucaoOutboxControllerTest` (2 testes), todos passando. Suíte completa do backend: **45/45 testes**, revalidada 2× após a correção do achado do relay.
- `npm run lint` e `npm run build` do frontend sem erros.
- Os 3 cenários validados manualmente com `curl` contra o Docker Compose real (perfis `core` + `messaging`) — números reais na tabela acima. Tópico `pedidos-criados` confirmado via API do Kafka UI, criado automaticamente, com a mensagem real publicada.
- **Isolamento validado**: uma execução de `sem-outbox` (~1s) disparada em paralelo com uma execução do laboratório de N+1, que respondeu normalmente em 57ms.
- Painel interativo em `/laboratorios/transactional-outbox` validado no Chrome: as duas variantes disparam execuções reais, com "Inconsistente" em vermelho para `sem-outbox` e verde para `com-outbox`.
- **Achado de infraestrutura durante a validação** (fora do escopo do código): a porta do frontend não aceitava conexões logo após subir o Compose, resquício do restart forçado do Docker feito mais cedo na sessão — resolvido com `docker restart` do container do frontend.

## Validação do laboratório de Ordenação de Eventos (2026-08-27)

Pré-requisito: backend rodando com o perfil `messaging` (Kafka real) —
`docker compose --profile core --profile messaging up`. Tópico
`eventos-ordem` criado automaticamente com 3 partições reais na subida.

| Endpoint | Método | Cenário | Resultado real observado |
|---|---|---|---|
| `/api/laboratorios/ordenacao-de-eventos/execucoes/sem-chave-particionamento` | POST | 20 eventos, partição escolhida por round-robin, sem chave | `200`, `quantidadeParticoesUsadas: 3`, `ordemPreservada: false` (5/5 execuções manuais reais, ordem visivelmente embaralhada) |
| `/api/laboratorios/ordenacao-de-eventos/execucoes/com-chave-particionamento` | POST | Os mesmos 20 eventos, `execucaoId` como chave | `200`, `quantidadeParticoesUsadas: 1`, `ordemPreservada: true`, `ordemRecebida` exatamente `[0..19]` (3/3 execuções) |
| `/api/laboratorios/ordenacao-de-eventos/execucoes/inexistente` | POST | Variante inválida | `400`, formato de erro padrão |

Validações executadas:
- **Achado real durante a implementação, o mais importante desta sessão**: a primeira versão bloqueava (`.get()`) em cada publicação antes de enviar a próxima, serializando o envio inteiro — sem sobreposição real entre partições, a variante `sem-chave-particionamento` chegava sempre em ordem por acidente (confirmado ao vivo: 6/6 execuções manuais com `ordemPreservada: true`, mesmo usando as 3 partições reais). Corrigido disparando as 20 publicações antes de aguardar qualquer resultado — depois da correção, 5/5 execuções produziram ordem real e visivelmente embaralhada. Ver `SPEC-LAB-ORDEM-001` para os detalhes completos.
- Testes automatizados reais com Testcontainers (Kafka real, 3 partições reais): `ExecucaoOrdenacaoServiceIntegrationTest` (2 testes) e `ExecucaoOrdenacaoControllerTest` (2 testes), todos passando. Suíte completa do backend: **49/49 testes**, revalidada 2× (antes e depois da correção do achado de publicação bloqueante).
- `npm run lint` e `npm run build` do frontend sem erros.
- Os 3 cenários validados manualmente com `curl` contra o Docker Compose real (perfis `core` + `messaging`) — números reais na tabela acima.
- **Isolamento validado**: uma execução de `sem-chave-particionamento` disparada em paralelo com uma execução do laboratório de N+1, que respondeu normalmente em 129ms.
- Painel interativo em `/laboratorios/ordenacao-de-eventos` validado no Chrome: as duas variantes disparam execuções reais, com "Partições usadas" e "Ordem preservada" em vermelho para `sem-chave-particionamento` e verde para `com-chave-particionamento`, com a lista completa da ordem recebida exibida.

## Validação do laboratório de Memory Leak / OutOfMemoryError (2026-08-27)

Nenhum pré-requisito de infraestrutura (não usa PostgreSQL, Kafka nem
qualquer outro serviço do `docker-compose.yml` — só o profile `core`,
que sobe backend + PostgreSQL + frontend, é necessário).

| Endpoint | Método | Cenário | Resultado real observado |
|---|---|---|---|
| `/api/laboratorios/memory-leak/execucoes/com-vazamento` | POST | Cache singleton com `Map` comum (referência forte) | `200`, `vazamentoDetectado: true`, ~20,9 MB retidos após `System.gc()` real (4/4 execuções) |
| `/api/laboratorios/memory-leak/execucoes/sem-vazamento` | POST | Mesma cache, `WeakHashMap` (referência fraca) | `200`, `vazamentoDetectado: false`, poucos KB de ruído retidos (496 a 5.056 bytes, 4/4 execuções), `tamanhoCacheAposExecucao: 0` sempre |
| `/api/laboratorios/memory-leak/execucoes/inexistente` | POST | Variante inválida | `400`, formato de erro padrão |

Validações executadas:
- **Dois achados reais durante a implementação**, ambos descobertos porque a primeira versão contava uma história errada com números reais: linha de base de heap medida antes de qualquer GC (mascarava o crescimento retido, `com-vazamento` chegou a mostrar `0` de retenção no primeiro teste); `WeakHashMap` não libera os valores sozinho — precisa de uma chamada real ao mapa (`size()`) para expurgar as entradas mortas antes de um segundo GC conseguir reclamar os valores de verdade. Ver `SPEC-LAB-MEMLEAK-001` para os detalhes completos.
- Testes automatizados reais, sem Testcontainers (só heap e GC reais da própria JVM): `ExecucaoMemoriaServiceTest` (2 testes) e `ExecucaoMemoriaControllerTest` (2 testes), todos passando. Suíte completa do backend: **53/53 testes**.
- `npm run lint` e `npm run build` do frontend sem erros.
- Os 3 cenários validados manualmente com `curl` contra o Docker Compose real, 4 execuções consecutivas de cada variante — números reais na tabela acima. `tamanhoCacheAposExecucao` da variante `com-vazamento` cresceu cumulativamente (200, 400, 600, 800) ao longo das execuções, confirmando que a cache nunca esvazia — comportamento esperado de um vazamento real dentro da mesma JVM.
- **Isolamento validado**: uma execução de `com-vazamento` disparada em paralelo com uma execução do laboratório de N+1, que respondeu normalmente em 51ms.
- Painel interativo em `/laboratorios/memory-leak` validado no Chrome: as duas variantes disparam execuções reais, com "Retido após GC real" e "Vazamento detectado" em vermelho para `com-vazamento` (19,9 MB / Sim) e verde para `sem-vazamento` (0,0 MB / Não).

## Validação do laboratório de Thread Pool Exhaustion (2026-08-27)

Nenhum pré-requisito de infraestrutura (não usa PostgreSQL, Kafka nem
qualquer outro serviço do `docker-compose.yml` — só o profile `core`).

| Endpoint | Método | Cenário | Resultado real observado |
|---|---|---|---|
| `/api/laboratorios/thread-pool-exhaustion/execucoes/fila-ilimitada` | POST | `Executors.newFixedThreadPool(2)`, 10 tarefas | `200`, `quantidadeAceitas: 10`, `quantidadeRejeitadas: 0`, `tempoMaximoEsperaNaFilaMs: 2008`, `duracaoMs: 2511` |
| `/api/laboratorios/thread-pool-exhaustion/execucoes/fila-limitada` | POST | `ThreadPoolExecutor` com fila limitada (capacidade 2), as mesmas 10 tarefas | `200`, `quantidadeAceitas: 4`, `quantidadeRejeitadas: 6`, `tempoMaximoEsperaNaFilaMs: 503`, `duracaoMs: 1004` — ~2,5× mais rápido no total (4/4 execuções, 100% determinístico) |
| `/api/laboratorios/thread-pool-exhaustion/execucoes/inexistente` | POST | Variante inválida | `400`, formato de erro padrão |

Validações executadas:
- Testes automatizados reais, sem Testcontainers (só `java.util.concurrent` real): `ExecucaoThreadPoolServiceTest` (2 testes) e `ExecucaoThreadPoolControllerTest` (2 testes), todos passando. Suíte completa do backend: **57/57 testes**.
- `npm run lint` e `npm run build` do frontend sem erros.
- Os 3 cenários validados manualmente com `curl` contra o Docker Compose real, repetido 4× para a variante `fila-limitada` — mesmos números em todas as execuções, 100% determinístico.
- **Isolamento validado**: uma execução de `fila-ilimitada` (~2,5s) disparada em paralelo com uma execução do laboratório de N+1, que respondeu normalmente em 57ms.
- Painel interativo em `/laboratorios/thread-pool-exhaustion` validado no Chrome: as duas variantes disparam execuções reais, com "Rejeitadas" em laranja quando `> 0` e "Maior espera na fila" em vermelho acima de 1s.
- **Achado real durante a implementação**: `Map.of()` não aceita mais de 10 pares de chave/valor — adicionar este laboratório ao conhecimento do Assistente de IA foi a 11ª entrada, erro real de compilação, corrigido trocando para `Map.ofEntries(Map.entry(...), ...)` em todas as entradas existentes.

## Validação do laboratório de Saga (2026-08-27)

Nenhum pré-requisito de infraestrutura (não usa Kafka nem qualquer
outro serviço além do PostgreSQL, já em uso desde a Fase 3 — só o
profile `core`).

| Endpoint | Método | Cenário | Resultado real observado |
|---|---|---|---|
| `/api/laboratorios/saga/execucoes/sem-compensacao` | POST | Reserva de estoque + pagamento recusado, sem ação de compensação | `200`, `estoqueReservado: true`, `pagamentoAprovado: false`, `compensacaoExecutada: false`, `estoqueConsistente: false` (4/4 execuções) |
| `/api/laboratorios/saga/execucoes/com-compensacao` | POST | O mesmo cenário, com ação de compensação real ao falhar | `200`, `compensacaoExecutada: true`, `estoqueConsistente: true` (4/4 execuções) |
| `/api/laboratorios/saga/execucoes/inexistente` | POST | Variante inválida | `400`, formato de erro padrão |

Validações executadas:
- Testes automatizados reais com Testcontainers (PostgreSQL real): `ExecucaoSagaServiceIntegrationTest` (2 testes) e `ExecucaoSagaControllerTest` (2 testes), todos passando. Suíte completa do backend: **61/61 testes**.
- `npm run lint` e `npm run build` do frontend sem erros.
- Os 3 cenários validados manualmente com `curl` contra o Docker Compose real, repetido 4× de cada variante — 100% consistente nas 8 execuções.
- **Isolamento validado**: uma execução de `sem-compensacao` disparada em paralelo com uma execução do laboratório de N+1, que respondeu normalmente em 60ms.
- Painel interativo em `/laboratorios/saga` validado no Chrome: as duas variantes disparam execuções reais, com "Estoque consistente" em vermelho para `sem-compensacao` (Não) e verde para `com-compensacao` (Sim).

## Preenchimento futuro (por fase)

- **Fase 2/3**: pré-requisitos de ambiente, ordem de subida dos serviços
  (`docker compose up` com o profile `core`), endpoints do catálogo e do
  laboratório de N+1, payloads reais, exemplos de coleção Postman.
- **Fase 4/5**: cenários de concorrência e de mensageria Kafka, incluindo
  validações de idempotência e de estado no banco/Redis.
- **Fase 6**: validações de métricas (Prometheus) e traces (OpenTelemetry).

Cada seção só é escrita quando o comportamento correspondente existir de
fato e puder ser validado por execução real — nunca antecipadamente.
