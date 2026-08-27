# SPEC-LAB-THREADPOOL-001 — Laboratório: Thread Pool Exhaustion

- **Status**: Implementada e validada (2026-08-27) — ver "Evidências de
  conclusão" ao final deste documento
- **Título**: `Executors.newFixedThreadPool` esconde uma fila ilimitada
  — o efeito real de um `ThreadPoolExecutor` sem limite de fila sob
  carga sustentada
- **Depende de**: `SPEC-JEL-003` (plataforma base)
- **Fase do roadmap**: pós-Fase 8 (item do backlog de laboratórios)

## Contexto

`Executors.newFixedThreadPool(n)` é provavelmente a forma mais comum de
criar um pool de threads em Java — e esconde um problema real: por
dentro, ele usa uma `LinkedBlockingQueue` **sem limite de tamanho**.
Sob carga sustentada (mais tarefas chegando do que o pool consegue
processar), a fila cresce indefinidamente — sem nenhum erro, sem
nenhuma rejeição. O sintoma não é uma exceção óbvia; é degradação
silenciosa: tarefas enfileiradas há muito tempo, latência crescente, e
em produção, sob carga alta o bastante, a fila (cada nó dela mantendo
referências à tarefa inteira) pode crescer o suficiente para contribuir
para um `OutOfMemoryError` — o mesmo sintoma final do laboratório
anterior, só que causado por um mecanismo completamente diferente
(fila sem limite, não referência forte num cache).

Diferente do laboratório de Connection Pool Exhaustion — onde a lição
era reduzir o tempo de retenção do recurso — aqui a lição é sobre
**configuração do executor em si**: fila limitada + política de
rejeição faz o sistema falhar rápido e de forma previsível sob
sobrecarga, em vez de acumular um backlog invisível que só vira
problema depois.

**Isolamento de propósito**: os pools de demonstração são
completamente isolados do pool de threads real que atende requisições
HTTP do backend (o mesmo raciocínio de `SPEC-LAB-CONN-POOL-001` para
pools de conexão) — esgotar os pools de demonstração não pode afetar
nenhum outro laboratório nem a própria requisição que dispara esta
demonstração.

## Domínio de demonstração

Não introduz nenhuma entidade de negócio. Dois `ExecutorService`
dedicados, construídos manualmente no serviço (mesmo padrão de
`ExecucaoConnPoolService`), completamente isolados do pool de threads
do servidor de aplicação:

- **`fila-ilimitada`**: `Executors.newFixedThreadPool(2)` — por dentro,
  uma `LinkedBlockingQueue` sem limite.
- **`fila-limitada`**: `ThreadPoolExecutor` construído manualmente com
  pool de tamanho 2 e uma `ArrayBlockingQueue` de capacidade 2 (máximo
  de 4 tarefas em andamento ou na fila) — política de rejeição padrão
  (`AbortPolicy`, lança `RejectedExecutionException` de verdade).

Cada variante submete 10 tarefas ao seu pool, cada uma simulando 500ms
de trabalho lento (`Thread.sleep`, mesma técnica já aceita e
documentada em `SPEC-LAB-RACE-001`/`SPEC-LAB-CONN-POOL-001`).

## Objetivo

Demonstrar, com um `ThreadPoolExecutor` real (não simulado), a
diferença entre uma fila sem limite — que aceita tudo silenciosamente
e faz o backlog crescer sem nenhum aviso — e uma fila limitada com
política de rejeição — que falha rápido e de forma explícita quando a
capacidade real do sistema é excedida.

## Requisitos funcionais

| ID | Requisito |
|---|---|
| RF-01 | Variante `fila-ilimitada`: 10 tarefas submetidas a `Executors.newFixedThreadPool(2)`; todas são aceitas (nenhuma rejeição), mas as últimas esperam bastante tempo na fila antes de começar a executar. |
| RF-02 | Variante `fila-limitada`: as mesmas 10 tarefas submetidas a um `ThreadPoolExecutor` de pool 2 e fila limitada a 2 — só as 4 primeiras (pool + fila) são aceitas; as 6 restantes são rejeitadas de verdade (`RejectedExecutionException` real, capturada). |
| RF-03 | Cada tarefa registra o tempo real de espera entre a submissão e o início da execução (`System.nanoTime()` antes/depois). |
| RF-04 | Resposta reporta `quantidadeRequisicoesConcorrentes`, `quantidadeAceitas`, `quantidadeRejeitadas` e `tempoMaximoEsperaNaFilaMs`, todos de execução real (`origemDados: REAL`). |
| RF-05 | Página do laboratório expõe as duas variantes com conteúdo educacional (o problema escondido de `Executors.newFixedThreadPool`, fila limitada vs. ilimitada, políticas de rejeição, trade-offs, perguntas de entrevista), incluindo o Assistente de IA já existente. |

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | Os pools de demonstração são completamente isolados do pool de threads que atende requisições HTTP do backend — esgotar os pools de demonstração não pode afetar nenhum outro laboratório rodando ao mesmo tempo. |
| RNF-02 | Testes automatizados comprovam, sem infraestrutura externa (só `java.util.concurrent`, execução real): `fila-ilimitada` aceita as 10 tarefas (nenhuma rejeição) e a última espera um tempo real considerável na fila; `fila-limitada` aceita exatamente 4 e rejeita exatamente 6, de forma determinística. |

## Design técnico — decisões

### Submissão sequencial, não com barreira de largada

Diferente de `SPEC-LAB-RACE-001`/`SPEC-LAB-CONN-POOL-001`, a decisão de
aceitar ou rejeitar uma tarefa em um `ThreadPoolExecutor` é síncrona e
imediata, avaliada no momento da própria chamada a `execute()`/`submit()`
— não depende de nenhuma corrida real entre threads chamadoras. Uma
submissão sequencial simples, vinda de uma única thread, reproduz
exatamente o mesmo resultado que uma rajada de chamadas concorrentes
produziria (a submissão em si é muito mais rápida que as tarefas de
500ms), com menos complexidade e 100% de determinismo.

### Pools construídos manualmente, não como `@Bean`

Mesmo princípio de ADR-0009 e de `ExecucaoConnPoolService`: os pools de
demonstração são campos construídos diretamente no serviço, não beans
Spring registrados via `@Bean`. Para `ExecutorService`/`ThreadPoolExecutor`
isso não corre o mesmo risco específico de colisão com autoconfiguração
que `DataSource`/`KafkaTemplate` correram (Spring Boot não autoconfigura
um `ExecutorService` concorrendo pelo mesmo tipo), mas o padrão é mantido
por consistência e porque nenhum bean precisa saber da existência
desses pools além deste serviço.

### `Executors.newFixedThreadPool`, não `Executors.newCachedThreadPool` ou similar

`newFixedThreadPool` foi escolhido de propósito por ser a forma mais
comum de criar um pool de threads em código real — o objetivo é
demonstrar o problema exatamente como ele aparece na prática, não uma
variação artificial. A fila ilimitada por baixo (`LinkedBlockingQueue`
sem capacidade) é um detalhe de implementação que a assinatura do
método não deixa óbvio — parte do motivo pelo qual esse é um erro tão
comum.

## Critérios de aceite

- [x] Variante `fila-ilimitada` aceita as 10 tarefas (`quantidadeRejeitadas == 0`) e a última tarefa espera um tempo real considerável na fila antes de começar (`tempoMaximoEsperaNaFilaMs` bem acima do tempo de uma única tarefa).
- [x] Variante `fila-limitada` aceita exatamente 4 tarefas e rejeita exatamente 6 (`RejectedExecutionException` real), de forma determinística.
- [x] Página do laboratório com as duas variantes executáveis e conteúdo educacional, testada no navegador.
- [x] Nenhuma métrica fabricada — aceitação/rejeição e tempo de espera vêm de exceções reais do `ThreadPoolExecutor` e de medição real de tempo.
- [x] `docker compose --profile core up` revalidado sem regressão ao final.
- [x] Isolamento do restante da plataforma confirmado explicitamente (RNF-01).

## Achados reais durante a implementação

- **`Map.of()` não aceita mais de 10 pares de chave/valor**: adicionar
  o conhecimento deste laboratório ao `ConhecimentoLaboratorios` (do
  Assistente de IA) foi a 11ª entrada — erro real de compilação
  (`no suitable method found for of(...)`). Corrigido trocando para
  `Map.ofEntries(Map.entry(...), ...)`, sem limite de tamanho, em
  todas as entradas existentes.

## Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Tempo exato de espera na fila variar entre execuções por causa do agendamento real de threads do SO | Teste automatizado quebradiço | Margem folgada no limiar de asserção (bem abaixo do valor teoricamente esperado), mesmo raciocínio já usado em `SPEC-LAB-CONN-POOL-001` |
| Pool de demonstração pequeno afetar acidentalmente o pool de threads real do servidor | Regressão em outros laboratórios | Pools completamente separados (RNF-01), validado explicitamente rodando outro laboratório durante/logo após a execução deste |

## Evidências de conclusão (2026-08-27)

- **Testes automatizados reais** (sem Testcontainers — só
  `java.util.concurrent`, execução real): `ExecucaoThreadPoolServiceTest`
  (2 testes): `fila-ilimitada` → 10 aceitas, 0 rejeitadas, última
  tarefa espera mais que o tempo de uma única tarefa na fila;
  `fila-limitada` → exatamente 4 aceitas, exatamente 6 rejeitadas
  (`RejectedExecutionException` real). `ExecucaoThreadPoolControllerTest`
  (2 testes): passando. Suíte completa do backend: **57/57 testes
  passando** (53 anteriores + 4 deste laboratório).
- **Execução real via `curl`, contra o Docker Compose real**:
  `fila-ilimitada` → `quantidadeAceitas: 10`,
  `quantidadeRejeitadas: 0`, `tempoMaximoEsperaNaFilaMs: 2008`,
  `duracaoMs: 2511` — número real batendo com a previsão teórica da
  SPEC (~4× 500ms); `fila-limitada` → `quantidadeAceitas: 4`,
  `quantidadeRejeitadas: 6`, `tempoMaximoEsperaNaFilaMs: 503`,
  `duracaoMs: 1004` — **~2,5× mais rápido** no total. Repetido 4×,
  100% determinístico (mesmos números em todas as execuções). Variante
  inválida → `400`.
- **Isolamento do restante da plataforma confirmado**: uma execução
  real de `fila-ilimitada` (~2,5s) foi disparada em paralelo com uma
  execução do laboratório de N+1, que respondeu normalmente em 57ms
  enquanto o pool de demonstração ainda esgotava.
- **Validação visual real no Chrome**: as duas variantes executadas
  via clique real, mostrando os mesmos números reais acima —
  "Rejeitadas" em laranja quando `> 0`, "Maior espera na fila" em
  vermelho acima de 1s e verde abaixo disso.
- **Achado real durante a implementação**: `Map.of()` não aceita mais
  de 10 pares — adicionar este laboratório ao conhecimento do
  Assistente de IA foi a 11ª entrada, erro real de compilação,
  corrigido trocando para `Map.ofEntries(Map.entry(...), ...)` em
  todas as entradas existentes.
- **Sem regressão**: `mvn -B verify` (57/57), `npm run build`/`lint`
  sem erros, `docker compose --profile core up` revalidado ao final,
  ambiente encerrado de forma limpa.

## Observação de status

Implementação concluída e validada nesta interação (2026-08-27), a
partir da aprovação explícita do usuário para começar este item do
backlog.
