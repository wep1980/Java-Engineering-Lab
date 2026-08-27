# SPEC-LAB-OUTBOX-001 — Laboratório: Transactional Outbox

- **Status**: Implementada e validada (2026-08-27) — ver "Evidências de
  conclusão" ao final deste documento
- **Título**: Escrita dupla não-atômica (banco + Kafka) e a correção via
  padrão Transactional Outbox
- **Depende de**: `SPEC-JEL-003` (plataforma base), reaproveita a
  infraestrutura Kafka já existente desde `SPEC-LAB-KAFKA-IDEMP-001`
- **Fase do roadmap**: pós-Fase 8 (item do backlog de laboratórios)

## Contexto

Um padrão comum (e frágil) em sistemas que precisam notificar outros
serviços após uma mudança de estado é: salvar no banco, depois publicar
um evento no Kafka. São **duas operações separadas contra dois sistemas
diferentes**, sem nenhuma garantia atômica entre elas. Conforme o prompt
mestre deste projeto (seção 27):

```text
Banco confirma
+
Kafka falha
=
inconsistência
```

Se o banco confirma e a publicação no Kafka falha (rede instável, broker
fora do ar, timeout), o dado existe no sistema de origem, mas nenhum
outro serviço jamais saberá que aquilo aconteceu — e, pior, nada detecta
nem corrige isso automaticamente. É o mesmo tipo de bug silencioso do
laboratório de Race Condition, mas entre dois sistemas em vez de duas
requisições.

O **Transactional Outbox** resolve isso removendo a necessidade de duas
operações atômicas contra dois sistemas: a aplicação escreve a entidade
de negócio **e** um registro do evento pendente na **mesma transação
local** (só o banco precisa ser atômico, o que ele já garante nativamente).
Um processo separado (*relay*) lê os eventos pendentes e os publica no
Kafka de forma assíncrona, marcando-os como processados após confirmação
real de entrega — se o Kafka estiver fora do ar, o evento simplesmente
continua pendente no banco (nunca é perdido) até o relay conseguir
publicá-lo.

Este laboratório reaproveita 100% da infraestrutura Kafka já existente
(perfil `messaging` do `docker-compose.yml`, mesmo broker do laboratório
de Mensagem Duplicada/Idempotência) — nenhum serviço novo.

## Domínio de demonstração

```text
Pedido { id, descricao, valor, criadoEm }
OutboxEvento { id (UUID), agregadoId, tipoEvento, payload (JSON), status (PENDENTE | PUBLICADO), criadoEm, publicadoEm }
```

Criar um `Pedido` deveria sempre resultar, mais cedo ou mais tarde, na
publicação de um evento `PedidoCriado` no tópico `pedidos-criados`.

- **`sem-outbox`**: salva o `Pedido` (commit real, numa transação já
  concluída) e, **separadamente**, tenta publicar o evento direto no
  Kafka usando um produtor apontado deliberadamente para um endereço
  inalcançável (`127.0.0.1:1`) — falha real de conexão do cliente Kafka
  (não fabricada), rápida e determinística, simulando uma rede/broker
  instável no exato momento da tentativa. Nenhum registro do evento
  pendente existe em lugar nenhum: se a publicação falha, a intenção de
  notificar se perde para sempre.
- **`com-outbox`**: salva o `Pedido` e um `OutboxEvento` (status
  `PENDENTE`) **na mesma transação local**. Um relay real
  (`@Scheduled`, roda a cada 200ms, independente da requisição HTTP)
  publica eventos pendentes no Kafka real (o mesmo broker do laboratório
  de Idempotência, funcionando normalmente) e marca como `PUBLICADO`
  após confirmação real de entrega. O endpoint aguarda (poll limitado,
  timeout 5s) o relay publicar antes de responder, para que a demonstração
  seja observável numa única requisição.

## Objetivo

Demonstrar, com uma falha real de rede do cliente Kafka (não simulada em
memória) e um relay assíncrono real (não uma chamada síncrona disfarçada),
a diferença entre uma escrita dupla não-atômica que perde a intenção de
notificar silenciosamente, e o padrão Outbox, que garante que o evento
nunca se perde — mesmo que sua publicação seja adiada.

## Requisitos funcionais

| ID | Requisito |
|---|---|
| RF-01 | Variante `sem-outbox`: `Pedido` é persistido com commit real; em seguida, uma tentativa de publicação direta no Kafka aponta para um endereço inalcançável e falha com uma exceção real do cliente Kafka. |
| RF-02 | Variante `com-outbox`: `Pedido` e `OutboxEvento` (status `PENDENTE`) são persistidos na mesma transação local (`@Transactional`), atomicamente. |
| RF-03 | Um relay real (`@Scheduled`, componente `RelayOutbox`) publica, de forma assíncrona e independente da requisição HTTP, os `OutboxEvento` pendentes no tópico `pedidos-criados` usando o Kafka real, e marca `status = PUBLICADO` e `publicadoEm` após confirmação real de entrega. |
| RF-04 | O endpoint da variante `com-outbox` aguarda (poll limitado a 5s) o relay publicar o evento antes de responder, tornando a demonstração observável numa única requisição — timeout mapeado para `LaboratorioIndisponivelException` (503) se o Kafka (perfil `messaging`) não estiver acessível. |
| RF-05 | Resposta reporta `pedidoPersistido`, `eventoRegistradoNaOutbox`, `eventoPublicadoNoKafka` e `inconsistente` (verdadeiro só quando o pedido existe mas o evento nunca foi registrado nem publicado), todos de execução real (`origemDados: REAL`). |
| RF-06 | Página do laboratório expõe as duas variantes com conteúdo educacional (o problema da escrita dupla, os quatro elementos do padrão Outbox, trade-offs, perguntas de entrevista), incluindo o Assistente de IA já existente. |

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | Nenhuma infraestrutura nova: reaproveita o Kafka já existente (perfil `messaging`/`full`). O tópico `pedidos-criados` é criado automaticamente pelo broker (mesmo comportamento já usado pelos tópicos do laboratório de Idempotência). |
| RNF-02 | O produtor "quebrado" da variante `sem-outbox` é construído manualmente no componente (`DefaultKafkaProducerFactory` com `bootstrap.servers` inválido), **não** como um segundo `@Bean KafkaTemplate` — evita colidir com `@ConditionalOnMissingBean(KafkaTemplate.class)` da autoconfiguração do Spring Kafka, mesmo princípio já validado em ADR-0009 para os pools do laboratório de Connection Pool Exhaustion. |
| RNF-03 | Testes de integração com Testcontainers (Kafka + PostgreSQL reais) comprovam: `sem-outbox` nunca publica e sempre reporta `inconsistente = true`; `com-outbox` sempre termina com `eventoPublicadoNoKafka = true` dentro do timeout. |

## Design técnico — decisões

### Produtor quebrado construído manualmente, não como `@Bean`

Ver RNF-02. Registrar um segundo bean `KafkaTemplate<String, Object>`
faria a autoconfiguração do Spring Kafka (`@ConditionalOnMissingBean`)
desistir de criar o `KafkaTemplate` padrão já usado por
`ProdutorEventoPagamento` — o mesmo tipo de falha silenciosa documentada
em ADR-0009, só que para Kafka em vez de `DataSource`. O relay reutiliza
o `KafkaTemplate<String, Object>` autoconfigurado normal (nenhum bean
novo); só o produtor da variante problemática é construído manualmente.

### Endereço inalcançável determinístico (`127.0.0.1:1`), não um hostname inexistente

Um hostname que não resolve depende de comportamento de DNS, que pode
variar (algumas redes retornam NXDOMAIN rápido, outras demoram). Uma
porta baixa e reservada em `127.0.0.1` garante recusa de conexão
imediata do próprio SO, em qualquer ambiente (local, CI, container) —
mesmo raciocínio de preferir uma falha rápida e determinística já usado
no `connectionTimeout` curto dos pools de demonstração de
`SPEC-LAB-CONN-POOL-001`.

### Relay real (`@Scheduled`), não uma chamada síncrona disfarçada

O prompt mestre pede explicitamente "publicação assíncrona" como um dos
quatro elementos do padrão. Uma implementação que só chama o publicador
diretamente dentro do mesmo método, só que dessa vez apontando para o
Kafka real, não demonstraria a decoupling real do padrão — o ganho do
Outbox é justamente que a escrita de negócio e a publicação acontecem em
processos/momentos diferentes, e a segunda pode ser adiada
indefinidamente sem perder o dado. O endpoint aguarda o relay via
*poll* limitado (mesma categoria de "sincronização real com processamento
assíncrono" já usada com `CountDownLatch` em `SPEC-LAB-KAFKA-IDEMP-001`,
adaptada para estado em banco em vez de callback de listener).

### Sem consumidor neste laboratório

Diferente do laboratório de Idempotência, aqui o ponto pedagógico é a
garantia do **lado de escrita** (produtor) — atomicidade entre banco e
intenção de publicar, e entrega eventual mesmo sob falha temporária do
broker. Não há `@KafkaListener` consumindo `pedidos-criados`; o critério
de sucesso é o estado real do `OutboxEvento` (`PUBLICADO`) e a
confirmação real de entrega do próprio `KafkaTemplate.send(...).get(...)`.

## Critérios de aceite

- [x] Variante `sem-outbox` sempre falha ao publicar (endereço inalcançável real) e reporta `inconsistente = true`.
- [x] Variante `com-outbox` sempre publica dentro do timeout (Kafka real, perfil `messaging` no ar) e reporta `inconsistente = false`.
- [x] `PedidoOutbox` é persistido com sucesso em ambas as variantes — a diferença está inteiramente no lado do evento, não do dado de negócio.
- [x] `OutboxEvento` só existe na variante `com-outbox`, criado atomicamente com o `PedidoOutbox` na mesma transação.
- [x] Relay (`RelayOutbox`) publica de forma genuinamente assíncrona (processo `@Scheduled` independente), confirmado via teste de integração com Kafka real.
- [x] Página do laboratório com as duas variantes executáveis e conteúdo educacional, testada no navegador.
- [x] Nenhuma métrica fabricada — falha de publicação e confirmação de entrega vêm de exceções e acks reais do cliente Kafka.
- [x] `docker compose --profile core --profile messaging up` revalidado sem regressão ao final.

## Achados reais durante a implementação

- **Colisão de nome com o laboratório de N+1**: já existe uma entidade
  `Pedido`/tabela `pedido` em `laboratorios.n1.Pedido`, de domínio
  completamente diferente. Nomear a entidade deste laboratório também
  `Pedido` colidiu tanto no nome do bean Spring Data
  (`pedidoRepository`, derivado do nome simples da classe,
  independente do pacote) quanto na tabela JPA — erro real de
  `BeanDefinitionOverrideException` na subida do contexto. Corrigido
  renomeando para `PedidoOutbox`/`PedidoOutboxRepository`/tabela
  `pedido_outbox`.
- **Bean `ObjectMapper` autoconfigurado indisponível**: a injeção de
  `com.fasterxml.jackson.databind.ObjectMapper` via construtor falhou
  com `UnsatisfiedDependencyException` — mesma categoria das
  relocações de autoconfiguração do Spring Boot 4.1 já documentadas
  para `JdbcConnectionDetails`/`WebMvcTest`. Corrigido construindo o
  `ObjectMapper` diretamente (`new ObjectMapper()`) em
  `ExecucaoOutboxService` e `RelayOutbox`, em vez de injetá-lo — seguro
  aqui porque `EventoPedidoCriado` só usa tipos suportados nativamente
  pelo Jackson core (UUID, Long, String, BigDecimal), sem necessidade
  de nenhum módulo adicional.
- **Isolamento entre os dois testes de integração**: os dois métodos de
  teste compartilham o mesmo contexto Spring/banco (sem rollback
  automático entre eles), então uma asserção de "tabela outbox vazia"
  na variante `sem-outbox` falhava dependendo da ordem de execução (o
  evento criado pela variante `com-outbox` também aparecia na consulta
  global). Corrigido escopando as asserções pelo `pedidoId` da própria
  execução, não pelo estado global da tabela.
- **Relay poluindo a contagem global de statements do Hibernate**: o
  `@Scheduled` do `RelayOutbox` roda em qualquer teste que suba o
  contexto Spring completo (`@SpringBootTest`), inclusive os de outros
  laboratórios — o polling a cada 200ms (mesmo sem nada pendente, ainda
  executa 1 `SELECT`) intercalou com a asserção de contagem exata de
  queries do laboratório de N+1
  (`SessionFactory.getStatistics().getPrepareStatementCount()`),
  causando uma falha real e intermitente (`expected: 1L but was: 2L`)
  em `ExecucaoN1ServiceIntegrationTest.varianteJoinFetchDeveExecutarUmaUnicaQuery`
  na suíte completa (`mvn verify`), mesmo com todos os testes deste
  laboratório passando isoladamente. Corrigido com uma flag
  `outbox.relay.habilitado` (`@Value`, padrão `true` — comportamento
  real de produção), desligada explicitamente em
  `ExecucaoN1ServiceIntegrationTest` via
  `@SpringBootTest(properties = "outbox.relay.habilitado=false")`.
  Suíte completa revalidada 2× após a correção, sem falhas.

## Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Segundo `@Bean KafkaTemplate` colidir com a autoconfiguração do Spring Kafka, quebrando o produtor usado pelos outros laboratórios | Regressão no laboratório de Idempotência | Produtor quebrado construído manualmente (RNF-02), nenhum bean novo declarado |
| Relay rodando em produção (sempre ativo, mesmo sem nenhum outbox pendente) consumir recursos desnecessariamente | Overhead mínimo, mas real | `findByStatus(PENDENTE)` retorna lista vazia quando não há nada pendente — nenhuma chamada ao Kafka acontece nesse caso; intervalo de 200ms é curto o bastante para a demonstração ser responsiva sem sobrecarregar |
| Backend sobe sem o profile `messaging` ativo | Variante `com-outbox` fica indisponível (relay nunca publica) | `LaboratorioIndisponivelException` (503) com mensagem clara após o timeout de espera, mesmo padrão de `SPEC-LAB-KAFKA-IDEMP-001` |
| Relay ligado por padrão poluir contagens de query em testes futuros de outros laboratórios | Falhas intermitentes e difíceis de diagnosticar | Achado documentado acima; qualquer teste futuro sensível a contagem exata de statements deve desligar `outbox.relay.habilitado` explicitamente |

## Evidências de conclusão (2026-08-27)

- **Testes automatizados reais**: `ExecucaoOutboxServiceIntegrationTest`
  (2 testes, Testcontainers com Kafka + PostgreSQL reais simultâneos):
  `sem-outbox` → `pedidoPersistido: true`, `eventoRegistradoNaOutbox:
  false`, `eventoPublicadoNoKafka: false`, `inconsistente: true`, nenhum
  `OutboxEvento` criado; `com-outbox` → `eventoRegistradoNaOutbox: true`,
  `eventoPublicadoNoKafka: true` (publicado pelo relay real dentro do
  timeout), `inconsistente: false`, `OutboxEvento` com status
  `PUBLICADO`. `ExecucaoOutboxControllerTest` (2 testes): passando.
  Suíte completa do backend: **45/45 testes passando** (41 anteriores +
  4 deste laboratório), revalidada 2× após a correção do achado do
  relay.
- **Execução real via `curl`, contra o Docker Compose real** (perfis
  `core` + `messaging`): `sem-outbox` → `eventoRegistradoNaOutbox:
  false`, `eventoPublicadoNoKafka: false`, `inconsistente: true`,
  `duracaoMs: 1072` (falha real de conexão com `127.0.0.1:1`);
  `com-outbox` → `eventoRegistradoNaOutbox: true`,
  `eventoPublicadoNoKafka: true`, `inconsistente: false`, `duracaoMs:
  617` (relay publicou bem dentro do timeout de 5s). Variante inválida
  → `400`. Tópico `pedidos-criados` confirmado via API do Kafka UI,
  criado automaticamente, com a mensagem real publicada
  (`offsetMax: 1`).
- **Isolamento do restante da plataforma confirmado**: uma execução
  real de `sem-outbox` (~1s) foi disparada em paralelo com uma execução
  do laboratório de N+1, que respondeu normalmente em 57ms — confirma
  que este laboratório não compartilha nenhum recurso finito de forma a
  afetar os demais (à parte do achado do relay em testes, já corrigido
  acima).
- **Validação visual real no Chrome**: as duas variantes executadas via
  clique real, mostrando os mesmos números reais acima — "Inconsistente"
  em vermelho para `sem-outbox` (Sim) e verde para `com-outbox` (Não),
  "Evento publicado no Kafka" em vermelho/verde conforme o resultado
  real, e durações reais de 1006ms e 117ms.
- **Achado real de infraestrutura durante a validação** (fora do escopo
  do código deste laboratório): logo após subir o Docker Compose, a
  porta mapeada do frontend não aceitava conexões do host mesmo com o
  container relatando "Up" e "Ready" — resquício do restart forçado do
  Docker Desktop feito mais cedo na sessão (ver histórico de
  conversas). Resolvido reiniciando só o container do frontend
  (`docker restart`), sem precisar reconstruir nada.
- **Sem regressão**: `mvn -B verify` (45/45), `npm run build`/`lint`
  sem erros, `docker compose --profile core --profile messaging up`
  revalidado ao final, ambiente encerrado de forma limpa.

## Observação de status

Implementação concluída e validada nesta interação (2026-08-27), a
partir da aprovação explícita do usuário para começar este item do
backlog.
