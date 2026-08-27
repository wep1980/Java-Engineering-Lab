# SPEC-LAB-CIRCUITBREAKER-001 — Laboratório: Circuit Breaker

- **Status**: Implementada e validada (2026-08-27) — ver "Evidências de
  conclusão" ao final deste documento
- **Título**: Falha em cascata contra uma dependência externa instável,
  e como um circuit breaker real (Resilience4j) interrompe chamadas
  inúteis em vez de deixar cada requisição pagar o custo total da falha
- **Depende de**: `SPEC-JEL-003` (plataforma base)
- **Fase do roadmap**: pós-Fase 8 (item do backlog de laboratórios)

## Contexto

Circuit Breaker é um dos padrões de resiliência mais cobrados em
entrevista de Engenharia Sênior, e um dos mais mal-entendidos na
prática. Quando uma dependência externa (outro serviço, uma API de
terceiro) fica instável — lenta, retornando erro, ou as duas coisas —
uma aplicação sem proteção continua tentando chamá-la a cada
requisição nova. Cada tentativa paga o custo total da falha (a latência
até o erro aparecer), sem nenhum ganho: o resultado já era previsível a
partir das tentativas anteriores. Sob carga, isso desperdiça threads e
conexões esperando por uma dependência que já se provou fora do ar, e
pode derrubar a própria aplicação por exaustão de recursos — mesmo que
o problema real esteja em outro serviço.

Um circuit breaker resolve isso monitorando a taxa de falha recente e,
ao ultrapassar um limite, "abre o circuito": passa a rejeitar chamadas
imediatamente (sem sequer tentar a dependência), até um tempo de espera
configurado se passar. É o mesmo raciocínio de um disjuntor elétrico —
não tenta "forçar a passagem" quando já sabe que vai falhar.

Este laboratório é território diferente dos últimos três implementados
(Connection Pool Exhaustion, Deadlock, Query sem índice), todos girando
em torno de banco de dados/persistência — aqui a demonstração é sobre
resiliência entre serviços, sem tocar em PostgreSQL, Kafka ou qualquer
infraestrutura nova.

## Domínio de demonstração

Não introduz nenhuma entidade de negócio nem infraestrutura nova
(nenhum serviço novo no `docker-compose.yml`). A "dependência externa"
é simulada por um componente Spring comum (`DependenciaExternaInstavel`)
que representa um serviço fora do ar: toda chamada espera 300ms
(latência real de rede simulada, mesma técnica de `Thread.sleep` já
aceita e documentada em `SPEC-LAB-RACE-001` e `SPEC-LAB-CONN-POOL-001`)
e então lança uma exceção real — nunca tem sucesso, simulando uma
indisponibilidade completa e sustentada, o cenário em que um circuit
breaker realmente compensa.

A proteção em si, porém, **não é simulada**: é a biblioteca
[Resilience4j](https://resilience4j.readme.io/) real
(`resilience4j-circuitbreaker`), com sua própria máquina de estados
(`CLOSED` → `OPEN` → `HALF_OPEN`) real, configurada com uma janela
deslizante de 10 chamadas e limite de falha de 50%.

Cada variante dispara 20 chamadas **sequenciais** (não concorrentes —
o objetivo aqui é observar a evolução do estado do circuito ao longo
de uma sequência, não uma disputa por um recurso finito, que já é o
tema de outros laboratórios) contra a dependência instável:

- **`sem-circuit-breaker`**: chama a dependência diretamente, sem
  nenhuma proteção. Todas as 20 chamadas pagam a latência completa
  (300ms) e falham.
- **`com-circuit-breaker`**: a mesma chamada, decorada por um
  `CircuitBreaker` real do Resilience4j. Após atingir o mínimo de
  chamadas (5) com 100% de falha (≥ 50% configurado), o circuito abre;
  as chamadas restantes são rejeitadas instantaneamente
  (`CallNotPermittedException`), sem sequer tentar a dependência.

## Objetivo

Demonstrar, com uma biblioteca real de circuit breaker (não uma
simulação de máquina de estados feita à mão) contra uma dependência
real e deterministicamente instável, a diferença mensurável entre
deixar cada chamada pagar o custo total de uma falha e interromper
chamadas fadadas ao fracasso — e o estado final real do circuito
(`OPEN`) como evidência de que a proteção realmente agiu.

## Requisitos funcionais

| ID | Requisito |
|---|---|
| RF-01 | Endpoint dispara 20 chamadas sequenciais reais para a variante `sem-circuit-breaker`: cada chamada vai direto para `DependenciaExternaInstavel`, sem nenhuma proteção. |
| RF-02 | Variante `com-circuit-breaker`: mesma dependência instável, mesmas 20 chamadas, mas cada chamada passa por um `CircuitBreaker` real do Resilience4j (janela deslizante de 10, mínimo de 5 chamadas antes de calcular a taxa, limite de 50% de falha). |
| RF-03 | O circuito é reiniciado (`CircuitBreaker.reset()`, API real da biblioteca) no início de cada execução, garantindo que cada clique no laboratório comece do estado `CLOSED`, de forma determinística e reprodutível. |
| RF-04 | Resposta reporta `quantidadeChamadas`, `quantidadeSucesso`, `quantidadeFalhasReais` (chamadas que de fato tentaram a dependência e falharam), `quantidadeRejeitadasPeloCircuito` (chamadas interrompidas pelo circuito aberto, sempre `0` na variante sem proteção) e `estadoFinalDoCircuito` (estado real do `CircuitBreaker.getState()` — `"DESABILITADO"` na variante sem proteção, já que não há circuito ali), todos calculados a partir de execução real (`origemDados: REAL`). |
| RF-05 | Página do laboratório expõe as duas variantes com conteúdo educacional (causa, o papel de cada parâmetro do circuito, trade-offs, perguntas de entrevista), incluindo o Assistente de IA já existente com contexto desta execução. |

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | Nenhuma infraestrutura nova: sem novo serviço no `docker-compose.yml`, sem chamada de rede real (loopback HTTP incluído) — a dependência instável é um componente Java in-process, mesma categoria de simulação já usada e documentada nos laboratórios anteriores. |
| RNF-02 | A dependência de circuit breaker usada é `resilience4j-circuitbreaker` (módulo núcleo, sem integração Spring Boot) — decisão registrada na seção "Design técnico" abaixo. |
| RNF-03 | Testes automatizados comprovam determinística e sem Testcontainers (nenhuma infraestrutura externa envolvida): `sem-circuit-breaker` produz exatamente 20 falhas reais e nenhuma rejeição pelo circuito; `com-circuit-breaker` produz exatamente 5 falhas reais, 15 rejeições pelo circuito, e estado final `OPEN`. |

## Design técnico — decisões

### `resilience4j-circuitbreaker` (núcleo), não `resilience4j-spring-boot3`

O módulo `resilience4j-spring-boot3` traz autoconfiguração Spring Boot
própria (classes `@ConditionalOnClass`/`@ConditionalOnMissingBean`
escritas contra os pacotes de autoconfiguração do Spring Boot 3.x).
Este projeto já está no Spring Boot 4.1, que reorganizou pacotes de
autoconfiguração historicamente estáveis (ver o comentário sobre
`JdbcConnectionDetails` em `ExecucaoConnPoolService`, movido de
`org.springframework.boot.autoconfigure.jdbc` para
`org.springframework.boot.jdbc.autoconfigure`) — um módulo de terceiros
ainda publicado contra os pacotes antigos do Boot 3 é um risco real de
autoconfiguração que falha silenciosamente (a mesma classe de problema
documentada em `docs/decisions/0009-pools-de-demonstracao-nao-sao-beans-de-datasource.md`,
ainda que por outro motivo).

A solução, seguindo o mesmo princípio já validado nessa ADR (construir
o objeto manualmente em vez de depender de autoconfiguração de
terceiros), é usar só o módulo núcleo `resilience4j-circuitbreaker` —
uma biblioteca Java pura, sem nenhuma dependência de Spring nem
autoconfiguração — e construir o `CircuitBreakerConfig`/`CircuitBreaker`
diretamente no serviço, exatamente como os `HikariDataSource` de
demonstração do laboratório de Connection Pool Exhaustion. A máquina de
estados do circuito continua 100% real (é a mesma classe usada por
quem usa o módulo Spring Boot); só a integração automática com o
framework é que fica de fora, e não era necessária aqui.

### Chamadas sequenciais, não concorrentes

Diferente de Race Condition, Deadlock e Connection Pool Exhaustion —
onde a concorrência real é o próprio fenômeno demonstrado — aqui o
fenômeno é a **evolução do estado do circuito ao longo de uma
sequência** de chamadas (`CLOSED` até a 5ª chamada, `OPEN` da 6ª em
diante). Chamadas concorrentes tornariam o número exato de chamadas
"antes do circuito abrir" não-determinístico sem nenhum ganho
demonstrativo adicional — o padrão de testes deste laboratório
usa `== 5` / `== 15` exatos, não limites como `> 0`.

### Dependência sempre indisponível, não intermitente

Uma dependência que falha e se recupera de forma probabilística exigiria
seed fixa ou tolerância de faixa nos testes automatizados (mesma
discussão já registrada no risco de `SPEC-LAB-CONN-POOL-001` sobre
números variarem por agendamento de threads). Uma indisponibilidade
completa e sustentada é também o cenário real em que um circuit breaker
mais compensa (um serviço fora do ar por minutos ou horas, não uma
falha isolada) e mantém o resultado 100% determinístico.

## Critérios de aceite

- [x] Variante `sem-circuit-breaker` produz 20 falhas reais, 0 rejeições pelo circuito — todas as chamadas pagam a latência completa da dependência instável.
- [x] Variante `com-circuit-breaker` produz 5 falhas reais (mínimo de chamadas configurado) e 15 rejeições pelo circuito, com estado final `OPEN` — evidência real de que o circuito abriu e parou de tentar a dependência.
- [x] Duração real da variante `com-circuit-breaker` é sensivelmente menor que a de `sem-circuit-breaker` (1509ms vs. 6014ms via `curl` real — ~4×).
- [x] Página do laboratório com as duas variantes executáveis e conteúdo educacional, testada no navegador.
- [x] Nenhuma métrica fabricada — falhas, rejeições e estado do circuito vêm de exceções e de `CircuitBreaker.getState()` reais do Resilience4j.
- [x] `docker compose --profile core up` revalidado sem regressão ao final.

## Evidências de conclusão (2026-08-27)

- **Testes automatizados reais** (sem Testcontainers — nenhuma
  infraestrutura externa envolvida neste laboratório, o mais simples em
  termos de setup de teste até agora): `ExecucaoCircuitBreakerServiceTest`
  (2 testes): `sem-circuit-breaker` → 20 falhas reais, 0 rejeições,
  `estadoFinalDoCircuito: DESABILITADO`; `com-circuit-breaker` →
  exatamente 5 falhas reais, exatamente 15 rejeições pelo circuito,
  `estadoFinalDoCircuito: OPEN` (estado real do `CircuitBreaker.getState()`).
  `ExecucaoCircuitBreakerControllerTest` (2 testes): passando. Suíte
  completa do backend: **41/41 testes passando** (37 anteriores + 4
  deste laboratório).
- **Execução real via `curl`, contra o Docker Compose real**:
  `sem-circuit-breaker` → `quantidadeFalhasReais: 20`,
  `quantidadeRejeitadasPeloCircuito: 0`, `estadoFinalDoCircuito:
  DESABILITADO`, `duracaoMs: 6014`; `com-circuit-breaker` →
  `quantidadeFalhasReais: 5`, `quantidadeRejeitadasPeloCircuito: 15`,
  `estadoFinalDoCircuito: OPEN`, `duracaoMs: 1509` — **~4× mais rápido**
  (6014ms / 1509ms ≈ 3,99×), número real medido, não estimado. Variante
  inválida → `400`.
- **Isolamento do restante da plataforma confirmado** (RNF-01): uma
  execução real de `sem-circuit-breaker` (6s de chamadas sequenciais
  lentas) foi disparada em paralelo com uma execução do laboratório de
  N+1, que respondeu normalmente em **59ms** enquanto o circuit breaker
  ainda estava rodando — confirma que este laboratório não compartilha
  nenhum recurso finito (pool de conexões, threads do servidor de
  aplicação) de forma a afetar os demais.
- **Validação visual real no Chrome**: as duas variantes executadas via
  clique real, mostrando os mesmos números reais acima — card "Executar
  sem circuit breaker" com 20/0/DESABILITADO/6039ms, card "Executar com
  circuit breaker" com 5/15 (verde)/OPEN (laranja)/1525ms, e o painel do
  Assistente de IA reagindo corretamente ao contexto da última execução.
- **Achado real durante a validação**: o Docker Desktop do ambiente
  ficou indisponível por causa do disco `C:` quase cheio (3,3 GB livres
  de ~476 GB), travando o motor (`com.docker.backend.exe` girando por
  ~19h de CPU acumulada sem responder a nenhum comando). Resolvido fora
  do escopo deste laboratório: liberação de espaço em disco (remoção de
  uma distro WSL não utilizada, com backup prévio dos únicos arquivos
  não versionados dentro dela) e reinício forçado do motor do Docker —
  não é um achado sobre o código deste laboratório, que não usa
  Docker/Testcontainers em nada.
- **Sem regressão**: `mvn -B verify` (41/41), `npm run build`/`lint`
  sem erros, `docker compose --profile core up` revalidado ao final,
  ambiente encerrado de forma limpa.

## Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| `resilience4j-spring-boot3` (autoconfiguração) falhar silenciosamente sob Spring Boot 4.1, como já ocorreu com outra dependência (ADR-0009) | Circuito não realmente ativo, métricas enganosas | Usado só o módulo núcleo `resilience4j-circuitbreaker`, sem nenhuma autoconfiguração de terceiros — mesmo princípio de "construir manualmente" já validado |
| Números exatos (5 falhas / 15 rejeições) dependerem de uma leitura equivocada dos parâmetros do Resilience4j | Teste automatizado quebradiço | Parâmetros (janela=10, mínimo=5, limite=50%) e o comportamento resultante documentados explicitamente nesta SPEC antes da implementação, e confirmados pela execução real documentada acima |

## Observação de status

Implementação concluída em 2026-08-26 e validação completa (suíte,
`curl` real, Chrome) concluída em 2026-08-27 — entre as duas datas, a
validação ficou bloqueada por uma indisponibilidade real do Docker
Desktop no ambiente (não relacionada a este laboratório), resolvida
antes de retomar. A partir da aprovação explícita do usuário para
começar este item do
backlog.
