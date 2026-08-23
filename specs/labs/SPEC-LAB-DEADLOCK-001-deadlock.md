# SPEC-LAB-DEADLOCK-001 — Laboratório: Deadlock

- **Status**: Implementada e validada (2026-08-23) — ver "Evidências de
  conclusão" ao final deste documento
- **Título**: Deadlock real de banco de dados por ordem de aquisição de
  locks inconsistente, e a correção por ordenação consistente
- **Depende de**: `SPEC-JEL-003` (plataforma base)
- **Fase do roadmap**: pós-Fase 8 (item do backlog de laboratórios)

## Contexto

Deadlock é um problema clássico de entrevista Java Sênior, diferente de
Race Condition/Lost Update (que já tem laboratório próprio,
`SPEC-LAB-RACE-001`): aqui, duas transações **cada uma trava com
sucesso um recurso, e então cada uma espera indefinidamente pelo
recurso que a outra já travou** — uma espera circular. Ao contrário do
Lost Update (que falha silenciosamente, sem erro), o Deadlock é
detectado ativamente pelo banco de dados, que aborta uma das duas
transações com um erro real (`deadlock detected` no PostgreSQL).

## Domínio de demonstração

Duas contas bancárias de demonstração (`ContaBancariaDeadlock`: id,
titular, saldo — R$ 500,00 cada), reiniciadas a cada execução. Duas
transferências concorrentes reais de R$ 50,00, em direções opostas:
Conta A → Conta B, e Conta B → Conta A — o cenário mínimo que produz
uma espera circular real quando a ordem de aquisição de locks não é
consistente.

## Objetivo

Demonstrar, com concorrência real (não simulada), um deadlock real
detectado pelo PostgreSQL, e a correção padrão da indústria: ordenar a
aquisição de locks de forma consistente (por ID, independente da
direção da transferência), eliminando matematicamente a possibilidade
de espera circular.

## Requisitos funcionais

| ID | Requisito |
|---|---|
| RF-01 | Endpoint dispara duas transferências concorrentes reais (`ExecutorService` + barreira de largada, mesmo padrão de `SPEC-LAB-RACE-001`) para a variante `sem-ordem-consistente`: cada transferência trava a conta de origem primeiro, depois a de destino — na ordem literal da transferência (A→B trava A depois B; B→A trava B depois A). |
| RF-02 | Variante `ordem-consistente`: mesmas duas transferências concorrentes, mas cada uma trava as contas em ordem ascendente de ID, **independente** da direção da transferência, antes de aplicar o débito/crédito na conta correta. |
| RF-03 | Resposta reporta `quantidadeTransferenciasConcorrentes`, `quantidadeSucesso`, `quantidadeDeadlocksDetectados`, `saldoContaA` e `saldoContaB`, todos calculados a partir de execução real (`origemDados: REAL`) — deadlock vem de `CannotAcquireLockException` real (tradução Spring de um `deadlock detected` real do PostgreSQL), não fabricado. |
| RF-04 | Página do laboratório expõe as duas variantes com conteúdo educacional (causa, sintomas, a correção e seu trade-off, perguntas de entrevista), incluindo o Assistente de IA já existente com contexto desta execução. |

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | A concorrência é real (duas transações JDBC simultâneas contra PostgreSQL), não simulada. |
| RNF-02 | Testes de integração com Testcontainers comprovam: `sem-ordem-consistente` produz exatamente um deadlock real e exatamente uma transferência bem-sucedida; `ordem-consistente` não produz nenhum deadlock, com as duas transferências (opostas e de mesmo valor) se cancelando — saldo final igual ao inicial. |
| RNF-03 | O `deadlock_timeout` do PostgreSQL é reduzido só para a transação desta demonstração (`SET LOCAL`, escopo de transação, sem afetar o resto do banco), para que a detecção do deadlock apareça rapidamente numa demonstração interativa. |

## Design técnico — decisões

### Mesma técnica de espera artificial de `SPEC-LAB-RACE-001`

Cada transferência trava a primeira conta, espera artificialmente
(300ms) antes de solicitar a segunda — a mesma técnica já documentada e
aceita naquela SPEC para tornar a colisão determinística e reproduzível
em teste automatizado, não um padrão de produção.

### Por que a barreira de largada não pode forçar "as duas travaram a primeira conta" nesta demo

Diferente do Connection Pool Exhaustion (10 operações independentes), a
correção por ordenação consistente faz as **duas** transferências
disputarem a **mesma** primeira conta (a de menor ID) quando ordenadas
de forma consistente — uma barreira que exigisse "as duas seguram sua
primeira conta antes de qualquer uma pedir a segunda" travaria para
sempre nessa variante (a segunda thread nunca consegue seu primeiro
lock, porque é o mesmo recurso que a primeira já tem). Por isso, aqui,
apenas uma barreira de largada simples (início simultâneo) mais a
espera artificial dentro de cada transferência é suficiente e correta
para as duas variantes — sem essa segunda camada de sincronização.

### `SET LOCAL deadlock_timeout` — só nesta transação

`deadlock_timeout` do PostgreSQL é 1s por padrão — aceitável, mas
reduzi-lo para esta demonstração (`SET LOCAL deadlock_timeout = '200ms'`,
primeira instrução de cada método transacional) torna a resposta da
API mais rápida para quem está testando interativamente, sem alterar
nenhuma configuração global do banco (escopo `LOCAL` é só da transação
atual, revertido automaticamente no commit/rollback).

### Uma única correção (ordenação consistente), não múltiplas variantes

Diferente de N+1 (três correções) e Connection Pool Exhaustion (duas),
Deadlock tem uma correção padrão da indústria amplamente aceita e
suficiente para o objetivo pedagógico — ordenação consistente de locks.
Duas variantes bastam (mesmo padrão de `SPEC-LAB-KAFKA-IDEMP-001`, que
também tem só duas).

## Critérios de aceite

- [x] Variante `sem-ordem-consistente` produz um deadlock real (`CannotAcquireLockException`) e exatamente uma transferência bem-sucedida entre as duas concorrentes.
- [x] Variante `ordem-consistente` não produz nenhum deadlock; as duas transferências (opostas, mesmo valor) se cancelam — saldo final de cada conta igual ao inicial.
- [x] Página do laboratório com as duas variantes executáveis e conteúdo educacional, testada no navegador.
- [x] Nenhuma métrica fabricada — deadlock/sucesso vêm de exceções e execuções reais contra PostgreSQL real.
- [x] `docker compose --profile core up` revalidado sem regressão ao final.

## Evidências de conclusão (2026-08-23)

- **Bug real encontrado e corrigido durante a validação via Docker
  Compose** (não pelos testes automatizados, que passavam porque
  chamavam o seed manualmente no `@BeforeEach`): faltava um
  `ApplicationRunner` (`InicializadorDadosDeadlock`) para popular as
  contas de demonstração na subida real da aplicação — sem ele, o
  endpoint respondia `500` (`NoSuchElementException`). Corrigido
  seguindo exatamente o mesmo padrão já usado em
  `InicializadorDadosRace`. Reforça, mais uma vez, a importância de
  validar contra `docker compose up` real, não só testes automatizados
  (mesma lição já registrada em ADRs anteriores desta sessão).
- **Testes de integração reais** (Testcontainers, 2 testes,
  `ExecucaoDeadlockServiceIntegrationTest`): o log da própria execução
  mostrou o deadlock real detectado pelo PostgreSQL:
  `ERROR: deadlock detected — Process 62 waits for ShareLock on
  transaction 746; blocked by process 63. Process 63 waits for
  ShareLock on transaction 745; blocked by process 62.` Suíte completa
  do backend: 33/33 testes passando.
- **Execução real via `curl`, contra o Docker Compose real**:
  `sem-ordem-consistente` → 1 sucesso, 1 deadlock real, saldos R$
  450,00/R$ 550,00, 580ms; `ordem-consistente` → 2 sucessos, 0
  deadlocks, saldos de volta a R$ 500,00/R$ 500,00 (as duas
  transferências se cancelaram), 630ms.
- **Não-determinismo do "vencedor" confirmado de propósito**: a
  execução via `curl` teve a transferência A→B bem-sucedida (saldo A
  menor); a execução seguinte, via navegador, teve a transferência B→A
  bem-sucedida (saldo A maior) — confirma que qual transação o
  PostgreSQL escolhe como vítima não é determinístico, exatamente como
  documentado nos Riscos desta SPEC. Os testes automatizados verificam
  essa invariante (uma das duas possibilidades válidas), não um
  vencedor específico.
- **Isolamento validado de propósito**: uma execução real de
  `sem-ordem-consistente` (produzindo o deadlock) foi disparada em
  paralelo com uma execução do laboratório de N+1, que respondeu
  normalmente em 51ms.
- **Validação visual real no navegador** (Chrome): as duas variantes
  executadas via clique real, com "Deadlocks (REAL)" em vermelho
  quando `> 0` e verde quando `0`, e os saldos reais das duas contas
  exibidos corretamente.
- **Sem regressão**: `mvn -B verify` (33/33), `npm run build`/`lint`
  sem erros, `docker compose --profile core up` revalidado ao final,
  ambiente encerrado de forma limpa.

## Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Deadlock não se reproduzir de forma confiável em toda execução | Demonstração inconsistente | Espera artificial (300ms) entre o primeiro e o segundo lock garante que ambas as threads já tenham o primeiro lock antes de qualquer uma pedir o segundo — mesma técnica validada em `SPEC-LAB-RACE-001` |
| `SET LOCAL deadlock_timeout` afetar outras transações | Comportamento inesperado em outros laboratórios | Escopo `LOCAL`, revertido automaticamente ao fim da transação — validado explicitamente rodando outro laboratório durante/logo após a execução deste |
| Vitória do deadlock (qual transação é abortada) não ser determinística | Testes automatizados frágeis se assumirem um vencedor específico | Testes verificam invariantes (exatamente 1 sucesso, exatamente 1 deadlock, uma das duas transferências aplicada) em vez de qual thread especificamente venceu |

## Observação de status

Implementação concluída e validada nesta interação (2026-08-23), a
partir da aprovação explícita do usuário para começar este item do
backlog.
