# SPEC-LAB-RACE-001 — Laboratório: Race Condition / Lost Update

- **Status**: Implementada e validada (2026-08-22)
- **Título**: Lost update por concorrência real, sem controle de acesso
  concorrente, com Optimistic Locking (`@Version`) e Pessimistic Locking
- **Depende de**: `SPEC-JEL-003` (plataforma base)
- **Fase do roadmap**: Fase 4

## Contexto

Lost Update é o problema clássico de concorrência em sistemas com estado
mutável compartilhado: duas requisições leem o mesmo estado, cada uma
calcula um novo valor a partir do que leu, e a segunda escrita sobrescreve
a primeira — uma atualização é silenciosamente perdida, sem erro, sem
log, sem exceção.

## Domínio de demonstração

Uma conta bancária simples, com **depósitos concorrentes**:

```text
ContaBancaria { id, titular, saldo }
```

10 requisições concorrentes depositam R$ 100,00 cada na mesma conta. Se
nada se perder, o saldo final deve ser R$ 1.000,00.

## Objetivo

Demonstrar o Lost Update com **concorrência real** (threads reais, não
simulação sequencial), e as duas soluções canônicas — Optimistic Locking
(`@Version`) e Pessimistic Locking (`SELECT ... FOR UPDATE`) — com seus
trade-offs.

## Requisitos funcionais

| ID | Requisito |
|---|---|
| RF-01 | Endpoint dispara um cenário completo: reinicia o saldo da conta de demonstração e executa 10 depósitos concorrentes reais (`ExecutorService` + barreira de largada), para a variante `sem-controle`. |
| RF-02 | Mesma mecânica para as variantes `otimista` (`@Version`, com retentativa em conflito) e `pessimista` (`SELECT ... FOR UPDATE`, serializando o acesso). |
| RF-03 | Resposta reporta `saldoEsperado`, `saldoFinal`, `atualizacoesPerdidas` e `conflitosDetectados`, todos calculados a partir de execução real (`origemDados: REAL`). |
| RF-04 | Página do laboratório expõe as três variantes com conteúdo educacional (causa, sintomas, soluções, trade-offs, perguntas de entrevista). |

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | A concorrência é real (múltiplas threads/transações JDBC simultâneas contra PostgreSQL), não simulada com um único thread chamando métodos em sequência. |
| RNF-02 | Testes de integração com Testcontainers comprovam: `sem-controle` perde atualizações de forma determinística; `otimista` e `pessimista` chegam ao saldo correto (nenhuma perda). |
| RNF-03 | Nenhuma entidade JPA é serializada diretamente pelas APIs. |

## Design técnico — decisões

### Por que duas entidades (`ContaBancaria` e `ContaBancariaVersionada`)

`@Version` do JPA/Hibernate se aplica a **toda** escrita da entidade,
incondicionalmente — não é possível ter "a mesma entidade" ora com
controle otimista, ora sem, escolhendo em tempo de execução. Por isso:

- `ContaBancaria` (sem `@Version`) — usada pelas variantes `sem-controle`
  e `pessimista` (o lock pessimista é um hint de query, não depende de
  coluna de versão).
- `ContaBancariaVersionada` (com `@Version`) — usada pela variante
  `otimista`.

Ambas herdam de `ContaBancariaBase` (`@MappedSuperclass`) para não
duplicar os campos comuns (`titular`, `saldo`) nem o método de domínio
`depositar(valor)`.

### Como a concorrência real é forçada a colidir

Cada operação de depósito faz **leitura → espera artificial (100ms) →
escrita**. A espera é uma técnica padrão para ampliar a janela de corrida
e tornar o bug 100% reproduzível em teste automatizado — sem ela, o
resultado dependeria do agendamento de threads da JVM e o teste seria
instável (flaky). Isso é documentado explicitamente aqui e no código: é
uma técnica de teste, não um padrão de produção.

Todas as threads são liberadas ao mesmo tempo por uma
`CountDownLatch` (barreira de largada), garantindo início efetivamente
simultâneo — não é uma corrida "torcida" para o resultado que queremos,
é a forma padrão de tornar uma condição de corrida real e determinística
em teste.

### Retentativa na variante otimista

Ao detectar `ObjectOptimisticLockingFailureException`, a operação é
re-tentada (até um limite) sem a espera artificial na re-tentativa —
apenas a primeira tentativa de cada thread usa a espera, para garantir a
colisão inicial sem prolongar artificialmente o teste em rodadas
subsequentes.

## Critérios de aceite

- [x] Variante `sem-controle` perde atualizações de forma determinística:
      validado com saldo final exatamente R$ 100 (9 de 10 depósitos
      perdidos), em 5 execuções consecutivas sem flakiness.
- [x] Variante `otimista` atinge o saldo esperado (R$ 1.000) sem perdas,
      com conflitos reais detectados e retentados (entre 1 e 45 conflitos
      observados nas execuções, sempre > 0).
- [x] Variante `pessimista` atinge o saldo esperado sem perdas e sem
      conflitos (acesso serializado) — validado com `conflitosDetectadosERetentados: 0`.
- [x] Página do laboratório com as três variantes executáveis e conteúdo
      educacional (cenário, código problemático, as duas soluções com
      trade-offs, perguntas de entrevista).
- [x] Nenhuma métrica fabricada — todas vêm de execução real
      (`origemDados: REAL`) contra PostgreSQL real, com threads reais.

## Evidências de conclusão

- `mvn test`: 17/17 testes passando, incluindo 3 testes de integração
  com Testcontainers e concorrência real (`ExecucaoRaceConditionServiceIntegrationTest`).
  Suite de concorrência rodada 3 vezes seguidas sem falha (não-flaky).
- Validação manual via `curl` contra o Docker Compose real, exemplo de
  uma execução: `sem-controle` → saldo final R$ 100,00 (202ms);
  `otimista` → saldo final R$ 1.000,00, 45 conflitos retentados (241ms);
  `pessimista` → saldo final R$ 1.000,00, 0 conflitos (1123ms — evidencia
  concretamente o custo de serialização do lock pessimista).
- Validação visual real no Chrome: os três botões executados um a um,
  com o card de "Saldo final" colorido em vermelho quando há perda e em
  verde quando não há. Zero erros no console.

## Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Teste de concorrência instável (flaky) em CI | Falso negativo/positivo | Espera artificial + barreira de largada tornam o resultado determinístico, não probabilístico |
| Pool de conexões insuficiente para 10 transações simultâneas segurando conexão durante a espera | Deadlock/timeout no teste | `spring.datasource.hikari.maximum-pool-size` aumentado explicitamente |
| Confundir Lock Pessimista com solução "sempre superior" | Conclusão didática incorreta | Trade-off de serialização (mais lento sob concorrência) documentado explicitamente no conteúdo do laboratório |

## Observação de status

Implementada e validada nesta interação (2026-08-22), a partir da
aprovação explícita do usuário para a Fase 4.
