# Java Engineering Lab

Repositório: https://github.com/wep1980/Java-Engineering-Lab

> Um laboratório interativo de Engenharia de Software para aprender — na
> prática, não só na teoria — os problemas mais comuns (e mais cobrados em
> entrevistas) de aplicações Java/Spring: N+1, race conditions,
> mensageria duplicada, e outros.

**Status atual: seis laboratórios completos, pós-Fase 8 (Hardening).**
Logs estruturados, métricas (Prometheus/Grafana), tracing distribuído
(OpenTelemetry/Tempo), um assistente de IA (Ollama, modelo local) com
contexto real de cada laboratório, análise de qualidade estática
(SonarQube) e de dependências (OWASP Dependency-Check/npm audit),
cobertura de testes (JaCoCo) e um teste de carga real comparando as
variantes do laboratório de N+1 sob concorrência — tudo validado contra
infraestrutura real. Veja
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
| Connection Pool Exhaustion | **Disponível** — `/laboratorios/connection-pool-exhaustion`, 3 variantes com pools HikariCP isolados e concorrência real (`specs/labs/SPEC-LAB-CONN-POOL-001-connection-pool-exhaustion.md`) |
| Deadlock | **Disponível** — `/laboratorios/deadlock`, 2 variantes com deadlock real detectado pelo PostgreSQL (`specs/labs/SPEC-LAB-DEADLOCK-001-deadlock.md`) |
| Query sem índice | **Disponível** — `/laboratorios/query-sem-indice`, 2 variantes com EXPLAIN ANALYZE real do PostgreSQL (`specs/labs/SPEC-LAB-INDICE-001-query-sem-indice.md`) |
| Circuit Breaker | **Disponível** — `/laboratorios/circuit-breaker`, 2 variantes com circuit breaker real (Resilience4j) contra uma dependência instável (`specs/labs/SPEC-LAB-CIRCUITBREAKER-001-circuit-breaker.md`) |
| Transactional Outbox | **Disponível** — `/laboratorios/transactional-outbox`, 2 variantes com Kafka real e relay assíncrono (`specs/labs/SPEC-LAB-OUTBOX-001-transactional-outbox.md`) |
| Ordenação de Eventos | **Disponível** — `/laboratorios/ordenacao-de-eventos`, 2 variantes com tópico Kafka real de 3 partições (`specs/labs/SPEC-LAB-ORDEM-001-ordenacao-de-eventos.md`) |
| Memory Leak / OutOfMemoryError | **Disponível** — `/laboratorios/memory-leak`, 2 variantes com heap e GC reais da JVM (`specs/labs/SPEC-LAB-MEMLEAK-001-memory-leak.md`) |
| Thread Pool Exhaustion | **Disponível** — `/laboratorios/thread-pool-exhaustion`, 2 variantes com `ThreadPoolExecutor` real (`specs/labs/SPEC-LAB-THREADPOOL-001-thread-pool-exhaustion.md`) |
| Saga | **Disponível** — `/laboratorios/saga`, 2 variantes com ações de compensação reais (`specs/labs/SPEC-LAB-SAGA-001-saga.md`) |
| Cache Stampede | **Disponível** — `/laboratorios/cache-stampede`, 2 variantes com lock distribuído real no Redis (`specs/labs/SPEC-LAB-CACHESTAMPEDE-001-cache-stampede.md`) |
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

Para o assistente de IA (contexto real de cada laboratório, respondido
por um modelo local via Ollama), o profile `ai` — na primeira subida,
aguarde o download automático do modelo (~2GB):

```bash
docker compose --profile core --profile ai up --build
```

- Backend: http://localhost:8080 (health-check em `/actuator/health`,
  Swagger UI em `/swagger-ui/index.html`).
- Frontend: http://localhost:3000.
- Kafka UI (com `messaging` ativo): http://localhost:8081.
- Grafana (com `observability` ativo): http://localhost:3300 — dashboard
  e datasources (Prometheus, Tempo) já provisionados.
- Ollama (com `ai` ativo): http://localhost:11434. Sem este profile, os
  laboratórios continuam funcionando normalmente — só o assistente fica
  indisponível (`503`).

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
própria `SPEC-JEL-005`. Na Fase 7 (`SPEC-JEL-006`, concluída) foi
implementado o Engineering AI Assistant: uma interface `AssistenteIA`
(evitando acoplamento a um único provedor) implementada com Ollama
(modelo local `llama3.2:3b`, sem custo por token nem chave de API),
exposta em `POST /api/laboratorios/{id}/assistente/perguntas` e embutida
como painel de pergunta/resposta nas três páginas de laboratório. O
contexto enviado ao modelo combina conhecimento condensado do
laboratório com o resultado real da última execução exibida na tela
(sem persistência nova no backend). Validado de ponta a ponta: execução
real de um laboratório, pergunta real digitada no navegador, resposta
real do modelo referenciando os números exatos da execução — e,
deliberadamente, a ausência do profile `ai` confirmada como não afetando
o restante da plataforma (endpoint do assistente responde `503`; os
demais continuam `200`). Na Fase 8 (`SPEC-JEL-007`, concluída) o
projeto passou por hardening em seis trilhas: segurança (OWASP
Dependency-Check e `npm audit` no CI, ambos informativos — ver
`docs/decisions/0008-owasp-dependency-check-requer-chave-nvd.md` para
um bug real do upstream encontrado e contornado), testes (JaCoCo
configurado, 86-87% de cobertura real; SonarQube validado em execução
real pela primeira vez, com 4 bugs reais encontrados — 3 corrigidos, 1
suprimido por ser a técnica pedagógica intencional do laboratório de
N+1), performance (teste de carga real comparando as variantes do
laboratório de N+1 sob concorrência — a versão corrigida sustentou de
2,9× a 6,1× mais throughput), UX (página 404 em português, título por
página de laboratório) e documentação final (`LICENSE` MIT,
`CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`). Como primeiro item do backlog
pós-Fase 8, foi implementado o laboratório de Connection Pool
Exhaustion (`SPEC-LAB-CONN-POOL-001`): dois pools HikariCP dedicados e
isolados do pool principal da aplicação demonstram esgotamento real sob
concorrência (`SQLTransientConnectionException` real, não fabricada) e
comparam duas correções — aumentar o pool vs. reduzir o tempo de
retenção da conexão, sendo a segunda tão rápida quanto a primeira
mesmo usando 6× menos conexões. Um achado real durante a implementação
(registrar os pools como beans de `DataSource` quebrava silenciosamente
a criação do `entityManagerFactory` do JPA para todos os laboratórios)
foi corrigido e documentado em
`docs/decisions/0009-pools-de-demonstracao-nao-sao-beans-de-datasource.md`.
Como segundo item do backlog, foi implementado o laboratório de
Deadlock (`SPEC-LAB-DEADLOCK-001`): duas transferências reais e
concorrentes entre duas contas, em direções opostas, travando locks em
ordens opostas — o PostgreSQL detecta ativamente a espera circular e
aborta uma das duas transações com um erro real (`deadlock detected`,
confirmado no log da própria execução de teste). A correção —
ordenar a aquisição de locks de forma consistente (por ID, não pela
direção da transferência) — elimina matematicamente a possibilidade de
deadlock, validado com as duas transferências opostas se cancelando
(saldo final igual ao inicial). Durante a validação contra o Docker
Compose real (não os testes automatizados), foi encontrado e corrigido
mais um achado real: faltava o inicializador que popula as contas de
demonstração na subida da aplicação — mesma lição já registrada nas
ADRs anteriores sobre a importância de validar contra infraestrutura
real. Como terceiro item do backlog, foi implementado o laboratório de
Query sem índice (`SPEC-LAB-INDICE-001`): uma tabela com 200.000 linhas
reais, sem índice na coluna de busca, e `EXPLAIN (ANALYZE, FORMAT
JSON)` real do PostgreSQL comparando o plano de execução (`Seq Scan`
vs. `Index Scan`) e o tempo real — o índice é criado e removido de
verdade (`CREATE INDEX`/`DROP INDEX`) a cada execução, o mesmo comando
que um engenheiro rodaria em produção. A diferença real medida foi de
~460× (12,9ms de Seq Scan contra 0,03ms de Index Scan). Dois achados
reais no caminho: `@Modifying` do Spring Data JPA exige contexto
transacional; e o otimizador só escolheu consistentemente `Index Scan`
depois de um `ANALYZE` real ser adicionado após semear/indexar os
dados — sem isso, escolhia `Bitmap Heap Scan` por estatísticas
desatualizadas. Como quarto item do backlog, foi implementado o
laboratório de Circuit Breaker (`SPEC-LAB-CIRCUITBREAKER-001`): uma
dependência externa simulada sempre indisponível (300ms de latência
real e falha garantida) chamada 20 vezes em sequência, comparando
deixar cada chamada pagar o custo total da falha contra interrompê-las
com um circuit breaker real (biblioteca Resilience4j, só o módulo
núcleo, sem a autoconfiguração Spring Boot própria — risco de
incompatibilidade com Spring Boot 4.1 pelo mesmo motivo já documentado
na ADR-0009). Números reais medidos via `curl`: variante sem proteção
→ 20 falhas reais, 6014ms; variante protegida → 5 falhas reais (o preço
de aprender que a dependência caiu) e 15 chamadas rejeitadas
instantaneamente pelo circuito já aberto, 1509ms — **~4× mais rápido**,
com o estado real do circuito (`OPEN`) como evidência de que a proteção
realmente agiu. Durante a validação, o Docker Desktop do ambiente ficou
indisponível por um problema de disco cheio, não relacionado ao código
deste laboratório — resolvido antes de concluir a validação completa
(suíte, `curl` real contra o Docker Compose, Chrome). Como quinto item
do backlog, foi implementado o laboratório de Transactional Outbox
(`SPEC-LAB-OUTBOX-001`): salvar no banco e publicar no Kafka são duas
operações separadas contra dois sistemas diferentes, sem garantia
atômica — a variante `sem-outbox` reproduz isso com uma falha real de
conexão (endereço Kafka inalcançável) logo após o banco já ter
confirmado; a correção escreve o pedido e um evento pendente na mesma
transação local, e um relay real (`@Scheduled`, roda de forma
assíncrona a cada 200ms, independente da requisição HTTP) publica os
eventos pendentes no Kafka real. Números reais medidos via `curl`:
`sem-outbox` → pedido persistido, evento nunca publicado,
`inconsistente: true`, 1072ms; `com-outbox` → evento registrado e
publicado pelo relay, `inconsistente: false`, 617ms. Três achados reais
no caminho: colisão de nome com a entidade `Pedido` já existente no
laboratório de N+1 (corrigido renomeando para `PedidoOutbox`); bean
`ObjectMapper` autoconfigurado indisponível sob Spring Boot 4.1
(corrigido construindo-o diretamente); e o relay rodando em qualquer
teste com contexto Spring completo poluiu a contagem global de queries
do Hibernate usada pelo laboratório de N+1 em `mvn verify` — corrigido
com uma flag de habilitação, desligada explicitamente nesse teste. Como
sexto item do backlog, foi implementado o laboratório de Ordenação de
Eventos (`SPEC-LAB-ORDEM-001`): "Kafka preserva ordem" é uma
meia-verdade — só é garantido dentro de uma única partição, não entre
partições de um mesmo tópico. Um tópico real de 3 partições (criado
explicitamente, já que o broker de demonstração usa 1 partição por
padrão) recebe 20 eventos do mesmo agregado; a variante
`sem-chave-particionamento` espalha os eventos pelas 3 partições
(round-robin explícito, determinístico) e um consumidor real com 3
threads (uma por partição) os recebe fora de ordem; a variante
`com-chave-particionamento` usa o identificador do agregado como
chave — o particionador padrão do Kafka garante que a mesma chave
sempre cai na mesma partição, preservando a ordem exata. Números reais
medidos via `curl`: `sem-chave-particionamento` → 3 partições usadas,
ordem embaralhada (5/5 execuções); `com-chave-particionamento` → 1
partição, ordem exata `[0..19]` (3/3 execuções). Achado real
importante no caminho: a primeira versão bloqueava a cada publicação
antes de enviar a próxima, serializando o envio inteiro e mascarando
a demonstração (a variante "problemática" chegava sempre em ordem, por
acidente) — corrigido disparando as 20 publicações antes de aguardar
qualquer resultado. Como sétimo item do backlog, foi implementado o
laboratório de Memory Leak/OutOfMemoryError (`SPEC-LAB-MEMLEAK-001`):
diferente dos anteriores, não usa nenhuma infraestrutura externa — é
inteiramente sobre o comportamento real da JVM. Duas caches singleton
(o mesmo escopo de vida da aplicação inteira de qualquer `@Service`)
recebem ~20MB de entradas; a variante `com-vazamento` usa um `Map`
comum (referência forte, nunca liberada); a variante `sem-vazamento`
usa um `WeakHashMap` (referência fraca — assim que nada mais
referencia a chave, o coletor de lixo a reclama). Por segurança (o
backend é compartilhado por todos os laboratórios), a demonstração
nunca provoca um `OutOfMemoryError` de verdade — mede heap real via
`MemoryMXBean` antes/depois de um `System.gc()` real, em escala
pequena o bastante para nunca ameaçar a estabilidade do processo.
Números reais medidos via `curl` (4 execuções de cada variante):
`com-vazamento` → sempre ~20,9MB retidos, cache crescendo
cumulativamente (200, 400, 600, 800 entradas — nunca esvazia);
`sem-vazamento` → sempre poucos KB de ruído retidos, cache sempre
zerada após o GC. Dois achados reais no caminho, ambos descobertos
porque a primeira versão da medição contava uma história errada com
números reais: linha de base de heap medida antes de qualquer GC
(mascarava o crescimento retido); e `WeakHashMap` não libera os
valores sozinho — precisa de uma chamada real ao mapa para expurgar as
entradas mortas antes de um segundo GC conseguir reclamar os valores
de verdade. Como oitavo item do backlog, foi implementado o
laboratório de Thread Pool Exhaustion (`SPEC-LAB-THREADPOOL-001`):
`Executors.newFixedThreadPool(n)`, a forma mais comum de criar um pool
de threads em Java, esconde uma `LinkedBlockingQueue` sem limite de
tamanho por dentro — sob carga sustentada, a fila cresce
indefinidamente sem nenhum erro, nenhuma rejeição. A variante
`fila-ilimitada` submete 10 tarefas a um pool de demonstração de 2
threads (completamente isolado do pool de threads real do servidor);
todas são aceitas, mas as últimas esperam bastante tempo real na fila.
A variante `fila-limitada` usa um `ThreadPoolExecutor` com fila
limitada e política de rejeição real — só 4 das 10 são aceitas, as
outras 6 são rejeitadas de verdade (`RejectedExecutionException`) na
hora. Números reais medidos via `curl` (4 execuções): `fila-ilimitada`
→ 10 aceitas, 0 rejeitadas, 2008ms de espera máxima na fila, 2511ms no
total; `fila-limitada` → 4 aceitas, 6 rejeitadas, 503ms de espera
máxima, 1004ms no total — **~2,5× mais rápido**, 100% determinístico
em todas as execuções. Achado real no caminho: `Map.of()` não aceita
mais de 10 pares — adicionar este laboratório ao conhecimento do
Assistente de IA foi a 11ª entrada, corrigido trocando para
`Map.ofEntries(Map.entry(...), ...)`. Como nono item do backlog, foi
implementado o laboratório de Saga (`SPEC-LAB-SAGA-001`): criar um
pedido envolve duas etapas contra recursos diferentes (reservar
estoque, depois cobrar pagamento) — não existe uma transação de banco
única cobrindo as duas, cada etapa commita por conta própria. Neste
laboratório, a etapa de pagamento sempre é recusada (falha real e
determinística); a variante `sem-compensacao` não desfaz a reserva de
estoque já feita — ela fica presa para sempre, um rastro inconsistente
permanente; a variante `com-compensacao` dispara uma ação de
compensação real (`cancelarReserva`) que devolve o estoque a um estado
consistente. Implementação deliberadamente orquestrada (chamada
direta de método), não coreografada via Kafka — Kafka já foi
demonstrado a fundo em três laboratórios anteriores, e a lição central
(compensação explícita) não depende de mensageria. Números reais
medidos via `curl` (4 execuções de cada variante, 100% consistente):
`sem-compensacao` → `estoqueConsistente: false` sempre;
`com-compensacao` → `estoqueConsistente: true` sempre. 61/61 testes do
backend passando. Como décimo item do backlog, foi implementado o
laboratório de Cache Stampede (`SPEC-LAB-CACHESTAMPEDE-001`) — a
primeira infraestrutura nova (Redis, profile `cache`) desde a Fase 8,
com aprovação explícita do usuário para abrir mão do critério de zero
infraestrutura nova mantido nos nove laboratórios anteriores. Quando
uma chave de cache fica fria, 10 requisições concorrentes que pedem a
mesma chave encontram cache miss ao mesmo tempo; a variante
`sem-protecao` deixa todas chamarem o recurso lento diretamente; a
variante `com-protecao` faz as 10 disputarem um lock distribuído real
no Redis (`SET chave valor NX PX`, atômico) — só a vencedora chama o
recurso lento, as demais aguardam o cache ser populado. Números reais
medidos via `curl` (3 execuções de cada variante, 100% determinístico):
`sem-protecao` → `quantidadeAcessosAoRecursoLentoReal: 10` sempre;
`com-protecao` → `quantidadeAcessosAoRecursoLentoReal: 1` sempre.
Achado real no caminho: adicionar o starter do Redis registra
automaticamente um health indicator que arrasta o `/actuator/health`
agregado inteiro para `DOWN` sempre que o profile `cache` não está no
ar, mesmo com o resto da aplicação saudável — corrigido com
`management.health.redis.enabled: false`, para que a indisponibilidade
do Redis seja sinalizada só no endpoint do próprio laboratório
(503/`LaboratorioIndisponivelException`), não no health check
compartilhado por toda a plataforma. 65/65 testes do backend passando.
Os demais laboratórios futuros do backlog seguem pendentes de
aprovação antes de começar (ver `docs/roadmap.md`).

## Licença

[MIT](LICENSE).

## Como contribuir

Ver [`CONTRIBUTING.md`](CONTRIBUTING.md) para o processo (Spec-Driven
Development para mudanças relevantes, correções pontuais direto em PR) e
[`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md) para as diretrizes de
convivência.
