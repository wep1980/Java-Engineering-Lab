# SPEC-LAB-SAGA-001 — Laboratório: Saga

- **Status**: Implementada e validada (2026-08-27) — ver "Evidências de
  conclusão" ao final deste documento
- **Título**: Uma falha no meio de uma transação de múltiplas etapas
  deixa o sistema inconsistente para sempre — a menos que cada etapa
  bem-sucedida tenha uma ação de compensação real
- **Depende de**: `SPEC-JEL-003` (plataforma base)
- **Fase do roadmap**: pós-Fase 8 (item do backlog de laboratórios)

## Contexto

Uma operação de negócio real frequentemente precisa de mais de uma
etapa, cada uma potencialmente contra um recurso/serviço diferente
(reservar estoque, depois cobrar o pagamento, depois confirmar o
pedido). Não existe uma transação de banco de dados única que cubra
todas as etapas — cada uma commita por conta própria. Se uma etapa
posterior falha, as etapas anteriores **já aconteceram de verdade** e
não desfazem sozinhas: o estoque continua reservado para um pedido que
nunca vai se completar, para sempre, a menos que alguma coisa
explicitamente desfaça essa reserva.

O padrão **Saga** resolve isso com **ações de compensação**: para cada
etapa que muda estado, existe uma etapa inversa correspondente,
executada explicitamente quando uma etapa posterior falha —
devolvendo o sistema a um estado consistente, mesmo sem uma transação
distribuída de verdade.

## Domínio de demonstração

```text
ReservaEstoque { id (UUID), pedidoId (UUID), quantidade, status (RESERVADA | CANCELADA) }
```

Duas etapas, representando dois "serviços" diferentes (o mesmo tipo de
fronteira que existiria entre microsserviços reais, aqui dentro do
mesmo backend por simplicidade — ver "Design técnico"):

1. **Reservar estoque** (`EstoqueService.reservar`): sempre funciona
   nesta demonstração — grava uma `ReservaEstoque` real, com commit
   real.
2. **Cobrar pagamento** (`ProcessadorPagamento.cobrar`): sempre falha
   nesta demonstração — uma exceção real e determinística
   (`PagamentoRecusadoException`), simulando um cartão recusado.

- **`sem-compensacao`**: a etapa 2 falha, e nada desfaz a etapa 1 — a
  reserva de estoque permanece com status `RESERVADA` para sempre,
  presa a um pedido que nunca vai se completar.
- **`com-compensacao`**: a mesma falha na etapa 2 dispara a ação de
  compensação real da etapa 1 (`EstoqueService.cancelarReserva`) — a
  reserva volta para um estado consistente (`CANCELADA`).

## Objetivo

Demonstrar, com escrita e leitura reais contra PostgreSQL (não
simuladas), a diferença entre uma operação de múltiplas etapas sem
nenhuma compensação — que deixa rastros inconsistentes permanentes
quando falha no meio do caminho — e o padrão Saga, que devolve o
sistema a um estado consistente através de ações de compensação
explícitas.

## Requisitos funcionais

| ID | Requisito |
|---|---|
| RF-01 | Etapa 1 (`reservar`) grava uma `ReservaEstoque` real com status `RESERVADA`, commit real. |
| RF-02 | Etapa 2 (`cobrar`) sempre falha com uma exceção real (`PagamentoRecusadoException`), determinística. |
| RF-03 | Variante `sem-compensacao`: a falha da etapa 2 é apenas capturada e reportada — nenhuma ação desfaz a etapa 1. |
| RF-04 | Variante `com-compensacao`: a falha da etapa 2 dispara `EstoqueService.cancelarReserva`, que atualiza a `ReservaEstoque` para status `CANCELADA`, commit real. |
| RF-05 | Resposta reporta `estoqueReservado`, `pagamentoAprovado` (sempre `false` neste laboratório), `compensacaoExecutada` e `estoqueConsistente` (lido de volta do banco real, após a execução), todos de execução real (`origemDados: REAL`). |
| RF-06 | Página do laboratório expõe as duas variantes com conteúdo educacional (o problema de transações de múltiplas etapas, ações de compensação, orquestração vs. coreografia, trade-offs, perguntas de entrevista), incluindo o Assistente de IA já existente. |

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | Nenhuma infraestrutura nova — só PostgreSQL, já em uso desde a Fase 3. |
| RNF-02 | Testes de integração com Testcontainers (PostgreSQL real) comprovam: `sem-compensacao` termina com a reserva `RESERVADA` (inconsistente); `com-compensacao` termina com a reserva `CANCELADA` (consistente), lido de volta do banco real após a execução. |

## Design técnico — decisões

### Saga orquestrada (chamada direta), não coreografada via Kafka

O backlog original considerou Saga como uma continuação natural da
série de laboratórios de mensageria (Idempotência → Outbox →
Ordenação), o que sugeriria uma saga coreografada (cada etapa
publicando um evento, a próxima reagindo a ele). Optamos por
**orquestração direta** (uma chamada de método explícita para cada
etapa, dentro do mesmo serviço) de propósito: a lição central deste
laboratório — uma etapa bem-sucedida precisa de uma ação de
compensação explícita quando uma etapa posterior falha — não depende
de mensageria para ser demonstrada de forma real e completa. Kafka já
foi demonstrado a fundo em três laboratórios anteriores
(Idempotência, Outbox, Ordenação); introduzir coreografia aqui
adicionaria complexidade real (tópicos, listeners, coordenação
assíncrona) sem reforçar a lição central, contrariando o princípio de
simplicidade do projeto (`specs/manifest/MANIFESTO.md`). O conteúdo
educacional do laboratório explica a diferença entre orquestração e
coreografia como uma decisão de design real que um engenheiro
precisaria tomar, mesmo a implementação aqui sendo orquestrada.

### Falha determinística na etapa de pagamento

Mesmo raciocínio já documentado em `SPEC-LAB-CIRCUITBREAKER-001` e
`SPEC-LAB-OUTBOX-001`: uma falha sempre reproduzível (não
probabilística) mantém a demonstração 100% determinística — o ponto
pedagógico é a reação à falha (compensar ou não), não a
probabilidade dela acontecer.

### `pedidoId` como correlação, sem uma entidade `Pedido`

Achado evitado de propósito: já existe uma entidade `Pedido` no
laboratório de N+1 e uma `PedidoOutbox` no laboratório de Outbox —
ambas de domínios diferentes. Este laboratório não precisa persistir
nenhum "pedido" de verdade (o objeto de interesse é a reserva de
estoque); um `UUID` gerado por execução serve como identificador de
correlação entre as etapas, sem introduzir mais uma entidade
`Pedido`/tabela `pedido` que colidiria por nome.

## Critérios de aceite

- [x] Variante `sem-compensacao` termina com `estoqueConsistente == false` (reserva permanece `RESERVADA`).
- [x] Variante `com-compensacao` termina com `estoqueConsistente == true` (reserva `CANCELADA`, lida de volta do banco real).
- [x] `pagamentoAprovado` é sempre `false` em ambas as variantes (a falha é real e determinística).
- [x] Página do laboratório com as duas variantes executáveis e conteúdo educacional, testada no navegador.
- [x] Nenhuma métrica fabricada — reserva, falha de pagamento e compensação vêm de escrita/leitura reais contra PostgreSQL e de uma exceção real.
- [x] `docker compose --profile core up` revalidado sem regressão ao final.

## Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Confundir "orquestração" com "não é uma Saga de verdade" | Percepção de demonstração incompleta | Conteúdo do laboratório explícito sobre orquestração vs. coreografia como duas implementações válidas do mesmo padrão — ver "Design técnico" |
| Colisão de nome com entidades `Pedido` já existentes (N+1, Outbox) | Regressão / erro de bean/tabela duplicada | Nenhuma entidade `Pedido` introduzida — `pedidoId` é só um `UUID` de correlação, sem tabela própria |

## Evidências de conclusão (2026-08-27)

- **Testes automatizados reais** (Testcontainers, PostgreSQL real):
  `ExecucaoSagaServiceIntegrationTest` (2 testes): `sem-compensacao` →
  `estoqueReservado: true`, `pagamentoAprovado: false`,
  `compensacaoExecutada: false`, `estoqueConsistente: false`;
  `com-compensacao` → `compensacaoExecutada: true`,
  `estoqueConsistente: true`. `ExecucaoSagaControllerTest` (2 testes):
  passando. Suíte completa do backend: **61/61 testes passando** (57
  anteriores + 4 deste laboratório).
- **Execução real via `curl`, contra o Docker Compose real**, repetido
  4× de cada variante: `sem-compensacao` → sempre
  `estoqueConsistente: false` (reserva presa); `com-compensacao` →
  sempre `estoqueConsistente: true` (reserva desfeita pela
  compensação real). `pagamentoAprovado` sempre `false` em ambas — a
  falha é determinística por design. 100% consistente nas 8
  execuções. Variante inválida → `400`.
- **Isolamento do restante da plataforma confirmado**: uma execução
  real de `sem-compensacao` foi disparada em paralelo com uma execução
  do laboratório de N+1, que respondeu normalmente em 60ms.
- **Validação visual real no Chrome**: as duas variantes executadas
  via clique real, mostrando os mesmos números reais acima —
  "Estoque consistente" em vermelho para `sem-compensacao` (Não) e
  verde para `com-compensacao` (Sim).
- **Sem regressão**: `mvn -B verify` (61/61), `npm run build`/`lint`
  sem erros, `docker compose --profile core up` revalidado ao final,
  ambiente encerrado de forma limpa.

## Observação de status

Implementação concluída e validada nesta interação (2026-08-27), a
partir da aprovação explícita do usuário para começar este item do
backlog.
