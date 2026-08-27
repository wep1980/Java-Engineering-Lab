# SPEC-LAB-ORDEM-001 — Laboratório: Ordenação de Eventos

- **Status**: Implementada e validada (2026-08-27) — ver "Evidências de
  conclusão" ao final deste documento
- **Título**: Kafka garante ordem só dentro de uma partição, não entre
  partições — o efeito real de publicar sem uma chave de particionamento
  consistente
- **Depende de**: `SPEC-JEL-003` (plataforma base), reaproveita a
  infraestrutura Kafka já existente desde `SPEC-LAB-KAFKA-IDEMP-001`
- **Fase do roadmap**: pós-Fase 8 (item do backlog de laboratórios)

## Contexto

`SPEC-LAB-KAFKA-IDEMP-001` deixou este tópico explicitamente de fora do
escopo: *"Partição única em ambos: ordenamento entre partições é
assunto de laboratório futuro, fora de escopo aqui."* Este laboratório
é esse futuro.

Um mal-entendido comum: "Kafka preserva a ordem dos eventos". Isso só é
verdade **dentro de uma única partição**. Um tópico com múltiplas
partições (a forma como Kafka escala) não garante nenhuma ordem relativa
entre eventos publicados em partições diferentes — e qual partição um
evento cai é decidido pela **chave de particionamento** usada ao
publicar. Sem uma chave consistente para os eventos do mesmo agregado
(ex.: usar `null`, ou um identificador diferente a cada evento), eles
podem cair em partições diferentes e chegar ao consumidor fora de ordem
— mesmo sem nenhuma falha, sem nenhum bug óbvio, só pela ausência de uma
chave bem escolhida.

## Domínio de demonstração

```text
EventoOrdem { execucaoId (UUID), sequencia (int, 0..19) }
```

Um tópico real de 3 partições (`eventos-ordem`, criado explicitamente
via `NewTopic`, não por auto-criação — o broker de demonstração usa 1
partição por padrão). 20 eventos, numerados em sequência (0 a 19),
publicados um a um para o mesmo "agregado" (identificado por
`execucaoId`, único por execução do laboratório):

- **`sem-chave-particionamento`**: cada evento é publicado numa
  partição escolhida por round-robin explícito (`sequencia % 3`), sem
  chave. Isso reproduz de forma **determinística** o mesmo efeito
  estrutural de publicar sem uma chave de particionamento consistente
  (em vez de depender do hash de uma chave aleatória, que
  teoricamente — embora com probabilidade desprezível — poderia
  colidir sempre na mesma partição e mascarar a demonstração). O
  resultado observável é idêntico: os 20 eventos ficam espalhados por
  partições diferentes.
- **`com-chave-particionamento`**: os mesmos 20 eventos, publicados com
  `execucaoId` como chave — o particionador padrão do Kafka usa o hash
  da chave para escolher a partição, e a **mesma chave sempre cai na
  mesma partição**. Os 20 eventos ficam, de forma garantida, na mesma
  partição.

Um consumidor real (`@KafkaListener(concurrency = 3)`, uma thread por
partição) registra a ordem exata em que os eventos chegam. Dentro de
uma partição, o Kafka garante entrega em ordem de publicação a um único
consumidor — daí a garantia (ou a falta dela) valer para o conjunto dos
20 eventos.

## Objetivo

Demonstrar, com um tópico Kafka real de múltiplas partições e
publicação real, que "Kafka preserva ordem" é uma meia-verdade — a
ordem só é garantida dentro de uma partição — e que a chave de
particionamento é o que decide se os eventos de um mesmo agregado
compartilham essa garantia ou não.

## Requisitos funcionais

| ID | Requisito |
|---|---|
| RF-01 | Tópico `eventos-ordem` criado explicitamente com 3 partições (`NewTopic`), não por auto-criação. |
| RF-02 | Variante `sem-chave-particionamento`: 20 eventos publicados com partição explícita via round-robin (`sequencia % 3`), sem chave. |
| RF-03 | Variante `com-chave-particionamento`: os mesmos 20 eventos publicados com `execucaoId` como chave, deixando o particionador padrão do Kafka escolher a partição. |
| RF-04 | Um consumidor real (`concurrency = 3`) registra a sequência exata em que os 20 eventos chegam, filtrando por `execucaoId` (para não misturar com execuções anteriores que ainda estejam no tópico). |
| RF-05 | Resposta reporta `quantidadeParticoesUsadas` (número real de partições distintas em que os eventos desta execução caíram, a partir do `RecordMetadata` real de cada publicação), `ordemPreservada` (se a sequência recebida bate exatamente com a sequência publicada) e `ordemRecebida`, todos de execução real (`origemDados: REAL`). |
| RF-06 | Página do laboratório expõe as duas variantes com conteúdo educacional (o que Kafka garante e o que não garante, o papel da chave de particionamento, trade-offs, perguntas de entrevista), incluindo o Assistente de IA já existente. |

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | Nenhuma infraestrutura nova: reaproveita o Kafka já existente (perfil `messaging`/`full`); só o tópico é novo. |
| RNF-02 | Testes de integração com Testcontainers (Kafka real) comprovam: `sem-chave-particionamento` usa mais de uma partição e (na prática, com concorrência real de 3 threads) não preserva a ordem; `com-chave-particionamento` usa exatamente uma partição e preserva a ordem exata (`[0, 1, ..., 19]`) de forma determinística. |
| RNF-03 | Novo tópico e novo pacote adicionados a `spring.json.trusted.packages`; o tipo padrão do `JsonDeserializer` é sobrescrito só para este listener via `@KafkaListener(properties = ...)`, sem alterar o padrão global (usado pelo laboratório de Idempotência). |

## Design técnico — decisões

### Round-robin explícito, não uma chave aleatória

Publicar com uma chave diferente a cada evento (ex.: um UUID novo por
mensagem) também espalharia os eventos entre partições na prática, mas
depende do hash de chaves aleatórias — teoricamente (com probabilidade
desprezível, mas não nula) todas as 20 chaves aleatórias poderiam
colidir na mesma partição e mascarar a demonstração numa execução
específica. Round-robin explícito (`sequencia % 3`, escolhendo a
partição diretamente na chamada de publicação) garante o mesmo efeito
estrutural — eventos do mesmo agregado espalhados por partições
diferentes — de forma 100% determinística, sem depender de hash.

### `NewTopic` explícito, não auto-criação

O broker de demonstração (`apache/kafka:3.8.0` no `docker-compose.yml`)
usa o padrão de 1 partição por tópico auto-criado — insuficiente para
esta demonstração, que depende de múltiplas partições existirem de
verdade. Um bean `NewTopic` (consumido pelo `KafkaAdmin` já
autoconfigurado, sem nenhum bean novo do tipo `KafkaAdmin`/`KafkaTemplate`
— mesmo cuidado de RNF-02 de `SPEC-LAB-OUTBOX-001`) declara o tópico
com 3 partições na subida da aplicação. Falha na criação (perfil
`messaging` fora do ar) não é fatal por padrão no Spring Kafka — vira
aviso no log, a aplicação sobe normalmente, e a falha real só aparece
na tentativa de publicação em si (mapeada para `LaboratorioIndisponivelException`).

### Override de tipo por listener, não um novo padrão global

`spring.json.value.default.type` já está configurado globalmente para
`EventoPagamentoConfirmado` (usado pelo laboratório de Idempotência).
Sobrescrever esse padrão global quebraria a desserialização daquele
listener. `@KafkaListener` aceita um atributo `properties` que
sobrescreve propriedades do consumer só para aquele listener — usado
aqui para apontar `EventoOrdem` sem tocar no padrão global.

### Consumidor com `concurrency = 3`, não `concurrency = 1`

O ponto da demonstração é observar o que acontece quando partições
diferentes são consumidas por threads diferentes de verdade — com
`concurrency = 1`, uma única thread processaria todas as partições em
sequência dentro de cada `poll()`, o que tende a produzir uma ordem
mais previsível (embora ainda não garantida) e mascararia o efeito.
`concurrency = 3` atribui uma partição a cada thread do grupo
consumidor — o cenário real de um consumidor que escala horizontalmente.

### Publicação em lote, não bloqueante

Achado real durante a implementação: a primeira versão bloqueava
(`.get()`) logo após publicar cada evento, antes de publicar o
próximo. Isso serializava a publicação inteira — o evento N+1 só era
enviado depois do ACK do evento N — e eliminava qualquer sobreposição
real entre partições; a variante `sem-chave-particionamento` chegava
sempre em ordem por acidente (sem nenhuma corrida real acontecendo
entre as threads do consumidor, mesmo usando 3 partições de verdade).
Corrigido disparando as 20 publicações primeiro (sem bloquear) e só
depois aguardando os resultados — dando ao broker e às 3 threads do
consumidor uma janela real de sobreposição para competir de verdade.

## Critérios de aceite

- [x] Variante `sem-chave-particionamento` usa as 3 partições (`quantidadeParticoesUsadas == 3`, real, via `RecordMetadata`).
- [x] Variante `com-chave-particionamento` usa exatamente 1 partição (`quantidadeParticoesUsadas == 1`).
- [x] Variante `com-chave-particionamento` sempre preserva a ordem exata (`ordemPreservada == true`, `ordemRecebida == [0..19]`), de forma determinística.
- [x] Variante `sem-chave-particionamento` demonstra a perda de ordem com concorrência real de consumo (3 threads, 3 partições).
- [x] Página do laboratório com as duas variantes executáveis e conteúdo educacional, testada no navegador.
- [x] Nenhuma métrica fabricada — partições e ordem de chegada vêm de `RecordMetadata` e do consumo reais.
- [x] `docker compose --profile core --profile messaging up` revalidado sem regressão ao final.

## Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Variante `sem-chave-particionamento`, em tese, poder preservar a ordem por coincidência de timing entre as 3 threads concorrentes | Demonstração inconsistente entre execuções | Documentado explicitamente como possível (mesma categoria do não-determinismo do vencedor em `SPEC-LAB-DEADLOCK-001`) — a garantia dura e testável automaticamente é `quantidadeParticoesUsadas == 3` (estrutural, sempre verdadeira), não a ordem observada em si. Na prática, depois da correção do achado de publicação bloqueante (ver "Achados reais" abaixo), 5/5 execuções manuais reais produziram ordem embaralhada — o risco existe, mas não se manifestou |
| `NewTopic` falhar ao criar o tópico se o profile `messaging` não estiver ativo na subida | Variantes ficam indisponíveis até o profile subir | Falha de criação não é fatal por padrão (log de aviso, app sobe normalmente); tentativa de publicação real falha depois, mapeada para `LaboratorioIndisponivelException` (503), mesmo padrão de `SPEC-LAB-KAFKA-IDEMP-001` |
| Eventos de execuções anteriores ainda presentes no tópico interferirem numa nova execução | Contagem de eventos recebidos incorreta | Consumidor filtra por `execucaoId` (UUID novo a cada execução), ignorando eventos de execuções anteriores — mesmo padrão de isolamento por identificador já usado no laboratório de Idempotência |

## Evidências de conclusão (2026-08-27)

- **Achado real durante a implementação** (o mais importante): a
  primeira versão bloqueava (`.get()`) logo após cada publicação,
  serializando o envio inteiro — sem sobreposição real entre
  partições, a variante `sem-chave-particionamento` chegava sempre em
  ordem por acidente, mesmo usando as 3 partições reais (confirmado
  ao vivo: 6 execuções manuais consecutivas, todas com
  `ordemPreservada: true`). Corrigido disparando as 20 publicações
  antes de aguardar qualquer resultado (ver "Publicação em lote, não
  bloqueante" acima) — depois da correção, 5/5 execuções manuais
  produziram ordem real e visivelmente embaralhada.
- **Testes automatizados reais** (Testcontainers, Kafka real com 3
  partições reais): `ExecucaoOrdenacaoServiceIntegrationTest` (2
  testes): `com-chave-particionamento` → `quantidadeParticoesUsadas:
  1`, `ordemPreservada: true`, `ordemRecebida` exatamente
  `[0..19]`; `sem-chave-particionamento` → `quantidadeParticoesUsadas:
  3` (garantia estrutural testada; a ordem observada não tem
  asserção rígida, por ser genuinamente não-determinística — ver
  risco acima). `ExecucaoOrdenacaoControllerTest` (2 testes):
  passando. Suíte completa do backend: **49/49 testes passando** (45
  anteriores + 4 deste laboratório), revalidada 2× (antes e depois da
  correção do achado de publicação bloqueante).
- **Execução real via `curl`, contra o Docker Compose real** (perfis
  `core` + `messaging`): `com-chave-particionamento` → 3/3 execuções
  com `quantidadeParticoesUsadas: 1`, `ordemPreservada: true`,
  ~14-18ms; `sem-chave-particionamento` (após a correção) → 5/5
  execuções com `quantidadeParticoesUsadas: 3`, `ordemPreservada:
  false`, ordem visivelmente embaralhada (ex.:
  `[0,1,2,4,3,5,6,8,9,12,11,7,15,14,18,10,17,13,16,19]`) — cada
  subsequência por partição permanece crescente entre si (ex.: os
  eventos da partição que recebeu 0,3,6,9,12,15,18 aparecem nessa
  mesma ordem relativa), confirmando que a garantia por partição vale
  e só a ordem entre partições se perde, exatamente como a SPEC prevê.
  Variante inválida → `400`.
- **Isolamento do restante da plataforma confirmado**: uma execução
  real de `sem-chave-particionamento` foi disparada em paralelo com
  uma execução do laboratório de N+1, que respondeu normalmente em
  129ms.
- **Validação visual real no Chrome**: as duas variantes executadas
  via clique real, mostrando os mesmos números reais acima —
  "Partições usadas" e "Ordem preservada" em vermelho para
  `sem-chave-particionamento` (3 / Não) e verde para
  `com-chave-particionamento` (1 / Sim), com a lista completa da
  ordem recebida exibida em cada card.
- **Sem regressão**: `mvn -B verify` (49/49), `npm run build`/`lint`
  sem erros, `docker compose --profile core --profile messaging up`
  revalidado ao final, ambiente encerrado de forma limpa.

## Observação de status

Implementação concluída e validada nesta interação (2026-08-27), a
partir da aprovação explícita do usuário para começar este item do
backlog.
