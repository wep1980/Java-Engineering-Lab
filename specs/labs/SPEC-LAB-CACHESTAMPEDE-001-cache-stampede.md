# SPEC-LAB-CACHESTAMPEDE-001 — Laboratório: Cache Stampede

- **Status**: Implementada e validada (2026-08-27)
- **Título**: Quando uma entrada de cache fica fria, N requisições
  concorrentes acertam o recurso lento ao mesmo tempo — a menos que
  apenas uma delas tenha permissão real para recalcular
- **Depende de**: `SPEC-JEL-003` (plataforma base)
- **Fase do roadmap**: pós-Fase 8 (item do backlog de laboratórios)

## Contexto — primeira infraestrutura nova desde a Fase 8

Todos os laboratórios pós-Fase 8 anteriores reaproveitaram
infraestrutura já existente (PostgreSQL, Kafka), de propósito — ver o
critério de priorização registrado nas interações de
2026-08-23 em diante em `docs/conversation-history.md`. Este é o
primeiro a introduzir infraestrutura nova (Redis): o backlog restante
sem infraestrutura nova ou repete famílias já cobertas (JPA fetching
×3, resiliência via Circuit Breaker) ou é pouco concreto
(Observabilidade insuficiente). Cache é um território genuinamente
novo e um tópico frequente em entrevista — o usuário aprovou
explicitamente abrir mão do critério de zero infraestrutura nova para
este item.

Cache Stampede (também chamado de *thundering herd*): quando uma
entrada de cache expira ou está fria, e várias requisições chegam ao
mesmo tempo pedindo a mesma chave, **todas** encontram cache miss
simultaneamente e vão direto para o recurso mais lento por trás do
cache (banco de dados, API externa, cálculo caro) — o mesmo recurso
que o cache existia justamente para proteger. Sob carga real, isso
pode gerar picos que esgotam o próprio recurso que o cache protegia
(o mesmo tipo de esgotamento já demonstrado em
`SPEC-LAB-CONN-POOL-001`/`SPEC-LAB-THREADPOOL-001`, aqui com uma causa
raiz diferente).

Este laboratório é escopado deliberadamente como **Cache Stampede**,
não "Cache inconsistente" (item separado do backlog, ainda pendente) —
os dois problemas têm causas raiz diferentes (coordenação de
recálculo concorrente vs. invalidação/atualização do cache) e não
cabem numa única demonstração de duas variantes coerente.

## Domínio de demonstração

**Redis real** (perfil `cache`, novo em `docker-compose.yml`) como
cache, e como o próprio mecanismo de coordenação da correção — não só
como armazenamento de valores.

Cada execução usa uma chave nova (`UUID` por execução) — garante cache
frio real a cada demonstração, sem depender de esperar um TTL expirar.
10 requisições concorrentes (mesmo padrão de barreira de largada de
`SPEC-LAB-RACE-001`) pedem a mesma chave ao mesmo tempo. Por trás do
cache, um recurso lento simulado (`Thread.sleep`, 500ms — mesma
técnica já aceita e documentada em SPECs anteriores).

- **`sem-protecao`**: cada requisição, ao encontrar a chave fria,
  chama o recurso lento e grava o resultado no cache — sem nenhuma
  coordenação entre elas. As 10 requisições encontram a mesma chave
  fria ao mesmo tempo e todas acessam o recurso lento.
- **`com-protecao`**: ao encontrar a chave fria, cada requisição tenta
  adquirir um lock distribuído real no Redis (`SET lock:<chave> ...
  NX PX <ttl>`, uma operação atômica real do Redis — não simulada em
  memória da aplicação). Só quem consegue o lock acessa o recurso
  lento e povoa o cache; as demais aguardam (poll limitado) o cache
  ser populado pela vencedora, sem acessar o recurso lento.

## Objetivo

Demonstrar, com Redis real (lock distribuído via `SETNX` real, não
simulado) e concorrência real, a diferença entre deixar N requisições
concorrentes recalcularem o mesmo valor caro ao mesmo tempo e
coordenar para que só uma delas realmente recalcule.

## Requisitos funcionais

| ID | Requisito |
|---|---|
| RF-01 | Cada execução usa uma chave de cache nova (garantia de cache frio real, não dependente de TTL). |
| RF-02 | Variante `sem-protecao`: 10 requisições concorrentes, cada uma chamando o recurso lento diretamente ao encontrar cache frio, sem nenhuma coordenação. |
| RF-03 | Variante `com-protecao`: as mesmas 10 requisições disputam um lock distribuído real no Redis (`setIfAbsent` com expiração — `SET NX PX`); só a vencedora chama o recurso lento; as demais aguardam o cache ser populado (poll limitado, sem acessar o recurso lento). |
| RF-04 | Resposta reporta `quantidadeRequisicoesConcorrentes` e `quantidadeAcessosAoRecursoLentoReal` (contagem real de chamadas ao recurso lento), de execução real (`origemDados: REAL`). |
| RF-05 | Página do laboratório expõe as duas variantes com conteúdo educacional (o que é cache stampede, o papel do lock distribuído, trade-offs, perguntas de entrevista), incluindo o Assistente de IA já existente. |

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | Redis roda num perfil próprio (`cache`), separado de `core` — subir só `core` (a maioria dos outros laboratórios) continua funcionando normalmente, sem exigir Redis no ar. |
| RNF-02 | Testes de integração com Testcontainers (Redis real) comprovam: `sem-protecao` produz `quantidadeAcessosAoRecursoLentoReal == 10`; `com-protecao` produz `quantidadeAcessosAoRecursoLentoReal == 1`, de forma determinística (o `SETNX` do Redis é atômico — exatamente um vencedor, sempre). |

## Design técnico — decisões

### Lock distribuído via Redis (`SETNX` real), não coordenação em memória

Um `ConcurrentHashMap` de locks em memória da própria JVM também
resolveria a corrida entre as 10 threads deste processo — mas não
seria uma demonstração real do padrão usado em produção, onde a
coordenação precisa funcionar entre múltiplas instâncias da aplicação
(múltiplos pods/processos), não só entre threads do mesmo processo.
`ValueOperations.setIfAbsent(chave, valor, duracao)` do Spring Data
Redis mapeia diretamente para o comando atômico `SET chave valor NX
PX <ms>` do Redis — o mesmo padrão real de lock distribuído citado em
qualquer discussão séria sobre Redis em produção.

### Redis num perfil próprio (`cache`), não em `core`

Só este laboratório precisa de Redis. Colocá-lo em `core` obrigaria
todo `docker compose --profile core up` (usado pela maioria dos outros
laboratórios) a subir um serviço a mais sem necessidade — mesmo
raciocínio já usado para `messaging` (só os laboratórios de Kafka
precisam dele).

### Recurso lento simulado, não uma consulta real ao PostgreSQL

Mesma técnica já aceita e documentada (`Thread.sleep`) em SPECs
anteriores — o ponto pedagógico deste laboratório é a coordenação via
Redis, não uma query específica. Introduzir o PostgreSQL aqui também
adicionaria uma dependência que este laboratório não precisa.

## Critérios de aceite

- [x] Variante `sem-protecao` produz `quantidadeAcessosAoRecursoLentoReal == 10` (todas as 10 requisições acessam o recurso lento).
- [x] Variante `com-protecao` produz `quantidadeAcessosAoRecursoLentoReal == 1` (só a vencedora do lock acessa o recurso lento), de forma determinística.
- [x] `docker compose --profile core up` (sem o perfil `cache`) continua funcionando sem regressão para os demais laboratórios.
- [x] Página do laboratório com as duas variantes executáveis e conteúdo educacional, testada no navegador.
- [x] Nenhuma métrica fabricada — contagem de acessos ao recurso lento e resultado do lock vêm de execução real contra Redis real.
- [x] `docker compose --profile core --profile cache up` revalidado sem regressão ao final.

## Achados reais durante a implementação

- **`@SpringBootTest` precisa de PostgreSQL real mesmo neste
  laboratório**: a primeira versão do teste de integração só subia um
  `GenericContainer` de Redis — falhou com
  `BeanCreationException`/`ServiceException: Unable to determine
  Dialect without JDBC metadata`. A aplicação inteira sobe em qualquer
  `@SpringBootTest` (JPA incluído), independente do laboratório testado
  usar o banco ou não. Corrigido adicionando também um
  `@Container @ServiceConnection PostgreSQLContainer`, mesmo padrão já
  usado nos testes de integração dos laboratórios de Kafka.
- **`spring-boot-starter-data-redis` derruba `/actuator/health` inteiro
  quando o profile `cache` não está no ar**: ao validar RNF-01 (subir só
  `core`, sem `cache`), `/actuator/health` passou a responder
  `503`/`DOWN` — não por causa do backend em si, mas porque o Spring
  Boot Actuator registra automaticamente um
  `DataRedisReactiveHealthIndicator` que participa do status agregado
  por padrão assim que o starter do Redis entra no classpath. Como o
  host `redis` não resolve (perfil `cache` fora do ar), esse indicador
  falha (`RedisConnectionFailureException` → `UnknownHostException:
  Failed to resolve 'redis'`) e arrasta o `/actuator/health` inteiro
  para `DOWN`, mesmo com Postgres e todos os outros laboratórios
  saudáveis por baixo. Isso violaria RNF-01 na prática (sinal de saúde
  agregado usado por monitoramento/orquestração ficaria `DOWN` por causa
  de uma infraestrutura opcional de um único laboratório). Corrigido com
  `management.health.redis.enabled: false` em `application.yml`
  (`backend/src/main/resources/application.yml`) — o Redis deixa de
  contribuir para o status agregado; a indisponibilidade do Redis
  continua sendo sinalizada corretamente, mas no lugar certo: a resposta
  503/`LaboratorioIndisponivelException` do próprio endpoint de
  cache-stampede (já existia via `catch (DataAccessException)`), não no
  health check compartilhado por toda a plataforma. Revalidado depois da
  correção: `/actuator/health` responde `200`/`UP` com `core` sozinho,
  `n1-queries` responde `200` normalmente, e
  `cache-stampede/sem-protecao` responde `503` com a mensagem esperada
  (Redis realmente indisponível nesse cenário).

## Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Redis (perfil `cache`) não estar no ar quando a variante `com-protecao` tenta aguardar o cache ser populado | Timeout confuso | Mapeado para `LaboratorioIndisponivelException` (503) com mensagem clara, mesmo padrão de `SPEC-LAB-KAFKA-IDEMP-001` |
| Adicionar Redis afetar a subida do backend quando o perfil `cache` não está ativo | Regressão nos demais laboratórios | Cliente Redis (Lettuce, padrão do Spring Boot) conecta de forma preguiçosa/assíncrona — validado explicitamente subindo só `core` e confirmando que o backend sobe e os outros laboratórios continuam funcionando |
| `spring-boot-starter-data-redis` registra health indicator que participa do `/actuator/health` agregado | `/actuator/health` reporta `DOWN`/503 para a aplicação inteira sempre que o perfil `cache` não está ativo, mesmo com o resto saudável | `management.health.redis.enabled: false` — ver "Achados reais" |

## Evidências de conclusão (2026-08-27)

- **Testes automatizados reais** (Testcontainers, PostgreSQL real +
  Redis real): `ExecucaoCacheStampedeServiceIntegrationTest` (2
  testes): `sem-protecao` → `quantidadeAcessosAoRecursoLentoReal ==
  10`; `com-protecao` → `quantidadeAcessosAoRecursoLentoReal == 1`.
  `ExecucaoCacheStampedeControllerTest` (2 testes): passando. Suíte
  completa do backend: **65/65 testes passando** (61 anteriores + 4
  deste laboratório), sem regressão.
- **Execução real via `curl`, contra o Docker Compose real** (perfis
  `core` + `cache`), repetido 3× de cada variante: `sem-protecao` →
  sempre `quantidadeAcessosAoRecursoLentoReal: 10`; `com-protecao` →
  sempre `quantidadeAcessosAoRecursoLentoReal: 1`. 100% determinístico
  nas 6 execuções (o `SETNX` do Redis é atômico — exatamente um
  vencedor, sempre).
- **`docker compose --profile core up` (sem `cache`) validado sem
  regressão**: `/actuator/health` responde `200`/`UP` após a
  inicialização; `n1-queries` responde `200` normalmente;
  `cache-stampede/sem-protecao` responde `503` com
  `LaboratorioIndisponivelException` ("Redis indisponível..."), como
  esperado — ver "Achados reais" para o ajuste que isso exigiu no
  health check agregado.
- **Isolamento do restante da plataforma confirmado**: uma execução
  real de `cache-stampede/sem-protecao` foi disparada em paralelo com
  uma execução do laboratório de N+1 — ambas responderam corretamente
  (N+1 em 65ms, 51 queries/50 pedidos), e uma execução de N+1 logo
  depois (variante `entity-graph`) seguiu normal (2 queries/50
  pedidos).
- **Validação visual real no Chrome**: as duas variantes executadas
  via clique real em `/laboratorios/cache-stampede`, mostrando os
  mesmos números reais acima — "Acessos ao recurso lento (REAL)" em
  vermelho (10) para `sem-protecao` e verde (1) para `com-protecao`.
- **Sem regressão**: `mvn -B verify` (65/65), `npm run build`/`lint`
  sem erros, `docker compose --profile core --profile cache up`
  revalidado ao final, ambiente encerrado de forma limpa.

## Observação de status

Implementação concluída e validada nesta interação (2026-08-27), a
partir da aprovação explícita do usuário para começar este item do
backlog e para abrir mão do critério de zero infraestrutura nova usado
nos laboratórios anteriores.
