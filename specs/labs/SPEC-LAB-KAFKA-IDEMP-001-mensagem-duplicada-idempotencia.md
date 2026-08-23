# SPEC-LAB-KAFKA-IDEMP-001 — Laboratório: Mensagem Duplicada / Idempotência

- **Status**: Implementada e validada (2026-08-22)
- **Título**: Reprocessamento de mensagem Kafka e correção via chave de
  idempotência
- **Depende de**: `SPEC-JEL-003` (plataforma base)
- **Fase do roadmap**: Fase 5

## Contexto

Kafka garante *at-least-once delivery* por padrão: um evento pode ser
entregue mais de uma vez ao consumidor (reenvio do produtor, rebalanceamento
do grupo de consumidores, retomada após falha antes do commit de offset).
Isso é comportamento normal e esperado do protocolo — o problema real é
quando o **efeito de negócio** de processar o evento não é seguro para
repetir (ex.: creditar um valor duas vezes).

**Não afirmamos que Kafka elimina duplicidades.** Ao contrário: este
laboratório existe justamente porque ele não elimina.

## Domínio de demonstração

```text
Carteira { id, titular, saldo }
```

Evento `EventoPagamentoConfirmado { eventoId (UUID), carteiraId, valor }`
publicado no Kafka. Processá-lo credita `valor` na carteira.

## Objetivo

Publicar deliberadamente **o mesmo evento duas vezes** (mesmo `eventoId`)
— reprodução real e honesta de uma entrega duplicada, não uma simulação
sequencial — e mostrar a diferença entre um consumidor sem proteção
(credita duas vezes) e um consumidor idempotente (credita uma vez,
usando o `eventoId` como chave de deduplicação).

## Requisitos funcionais

| ID | Requisito |
|---|---|
| RF-01 | Endpoint publica o mesmo evento duas vezes em um tópico Kafka real e aguarda o consumidor processar ambas as mensagens antes de responder. |
| RF-02 | Variante `sem-idempotencia`: cada mensagem recebida credita a carteira, mesmo que já tenha sido processada antes. |
| RF-03 | Variante `idempotente`: antes de creditar, verifica se o `eventoId` já foi processado (tabela de registro); se sim, ignora sem aplicar o efeito de negócio novamente. |
| RF-04 | Resposta reporta `quantidadeEventosConsumidos` (semântica de entrega — sempre 2, o Kafka entregou as duas), `quantidadeProcessamentosEfetivos` (efeito de negócio realmente aplicado), `saldoEsperado` e `saldoFinal`, todos de execução real. |
| RF-05 | Conteúdo do laboratório explica a diferença entre semântica de entrega, processamento idempotente e efeito da operação de negócio. |

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | A duplicidade é real: duas mensagens de fato publicadas e consumidas via um broker Kafka real, não uma chamada de método duplicada em memória. |
| RNF-02 | Testes de integração com Testcontainers (Kafka real) comprovam os números exatos de cada variante. |
| RNF-03 | `eventoId` tem constraint de unicidade no banco (defesa em profundidade), além da verificação de aplicação. |

## Design técnico — decisões

### Dois tópicos, não um com bandeira de variante

`pagamentos-confirmados-sem-idempotencia` e
`pagamentos-confirmados-idempotente`, cada um com seu próprio
`@KafkaListener`. Reflete como a correção acontece na prática — troca-se
a implementação do consumidor, não se acrescenta um `if` de variante
dentro de um único consumidor. Partição única em ambos: ordenamento
entre partições é assunto de laboratório futuro, fora de escopo aqui.

### Por que "verificar-então-inserir" é seguro aqui (e não seria em HTTP)

O container de listener do Spring Kafka processa mensagens de uma
partição **sequencialmente**, em uma única thread. Por isso, um
`existsByEventoId` seguido de `save()` é seguro sem lock adicional —
diferente do laboratório de Race Condition
(`SPEC-LAB-RACE-001-race-condition-lost-update.md`), onde requisições
HTTP concorrentes de verdade exigiam `@Version`/lock explícito. O
conteúdo do laboratório explicita esse contraste.

### Sincronização da execução (barreira de chegada)

O endpoint precisa saber quando **ambas** as mensagens publicadas já
foram consumidas antes de ler o estado final e responder. Cada consumidor
usa uma `CountDownLatch` preparada para 2 contagens antes da publicação
— não é *polling* nem espera fixa, é sincronização real com o
processamento assíncrono. Timeout de 15s mapeado para
`LaboratorioIndisponivelException` (503) caso o broker não esteja
acessível (perfil `messaging` do `docker-compose.yml` não estava no ar).

**Bug real encontrado e corrigido durante a implementação**: a primeira
versão contava o `latch` de dentro do próprio método `@Transactional` do
listener — o que sinalizava conclusão *antes* do commit efetivamente
acontecer (o proxy do Spring commita depois que o corpo do método
retorna). Isso causou leituras de saldo inconsistentes em execução manual
contra Kafka real (embora os testes automatizados tenham passado, por a
janela de corrida ser curta demais para se manifestar de forma confiável
em execução local rápida). Corrigido movendo a lógica transacional para
um bean separado — ver `docs/decisions/0006-sincronizacao-so-apos-commit-em-listeners.md`,
que registra o padrão para todo consumidor assíncrono futuro do projeto.

## Critérios de aceite

- [x] Variante `sem-idempotencia` credita a carteira duas vezes (saldo
      final = 2× o valor do evento = R$ 100), validado por teste de
      integração com Kafka real e por execução manual repetida (2×)
      contra o ambiente Docker Compose.
- [x] Variante `idempotente` credita a carteira uma única vez (saldo
      final = 1× o valor do evento = R$ 50), com o segundo evento
      identificado e ignorado, validado da mesma forma.
- [x] `quantidadeEventosConsumidos` é sempre 2 em ambas as variantes
      (Kafka entregou as duas mensagens); `quantidadeProcessamentosEfetivos`
      distingue as variantes (2 vs 1) — confirmado em execução real.
- [x] Página do laboratório com conteúdo explicando semântica de entrega
      vs. idempotência vs. efeito de negócio, e por que "verificar-então-inserir"
      é seguro aqui mas não seria em concorrência HTTP real.
- [x] Nenhuma métrica fabricada — todas vêm de execução real contra Kafka
      e PostgreSQL reais (`origemDados: REAL`).

## Evidências de conclusão

- `mvn test`: 21/21 testes passando, incluindo 2 testes de integração
  com Testcontainers (Kafka + PostgreSQL reais simultâneos) —
  `ExecucaoKafkaIdempotenciaServiceIntegrationTest`. Suite rodada 3 vezes
  seguidas sem falha.
- Ambiente completo (`docker compose --profile core --profile messaging up`)
  validado pela primeira vez nesta fase — Kafka em KRaft, Kafka UI e os
  dois listeners conectando e recebendo partições corretamente
  (confirmado nos logs: "partitions assigned").
- Execução manual via `curl`, repetida duas vezes para checar
  consistência: `sem-idempotencia` → sempre R$ 100 (2 processamentos
  efetivos); `idempotente` → sempre R$ 50 (1 processamento efetivo, 2
  eventos consumidos).
- Tópicos reais confirmados via API do Kafka UI
  (`pagamentos-confirmados-sem-idempotencia`,
  `pagamentos-confirmados-idempotente`, 1 partição cada).
- Validação visual real no Chrome: os dois botões executados, card de
  saldo final vermelho (sem-idempotência) e verde (idempotente). Sem
  erros no console.
- Ambiente derrubado ao final (`docker compose down` com os dois
  profiles); nenhum container ficou rodando.

## Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Backend sobe sem o profile `messaging` ativo | Endpoint deste laboratório fica indisponível | `LaboratorioIndisponivelException` (503) com mensagem clara, em vez de erro genérico; documentado em `docs/links.md` que este laboratório exige `--profile core --profile messaging` (ou `full`) |
| Timing de subscrição do consumidor vs. publicação logo após o startup do Kafka | Mensagem publicada antes do consumidor se inscrever poderia parecer "perdida" | `auto-offset-reset: earliest` — consumidor novo sempre começa do início do tópico, não perde mensagens publicadas antes da primeira subscrição |
| Confundir "duas entregas" com "dois problemas" | Conclusão didática incorreta | Conteúdo do laboratório é explícito: a entrega dupla é comportamento normal do Kafka; o problema é o efeito de negócio não idempotente |

## Observação de status

Implementada e validada nesta interação (2026-08-22), a partir da
aprovação explícita do usuário para a Fase 5.
