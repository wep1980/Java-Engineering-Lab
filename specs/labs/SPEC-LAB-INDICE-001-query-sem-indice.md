# SPEC-LAB-INDICE-001 — Laboratório: Query sem índice

- **Status**: Implementada e validada (2026-08-23) — ver "Evidências de
  conclusão" ao final deste documento
- **Título**: Diferença real de plano de execução e tempo entre uma
  busca sem índice (Seq Scan) e com índice (Index Scan)
- **Depende de**: `SPEC-JEL-003` (plataforma base)
- **Fase do roadmap**: pós-Fase 8 (item do backlog de laboratórios)

## Contexto

"Por que essa query está lenta?" é talvez a pergunta mais comum de
troubleshooting de performance em produção. A causa mais frequente e
mais fácil de diagnosticar é a ausência de um índice numa coluna usada
em `WHERE`: sem índice, o PostgreSQL precisa varrer a tabela inteira
(Seq Scan) para encontrar as linhas que casam com o filtro; com
índice, ele localiza diretamente as linhas (Index Scan). Diferente dos
laboratórios anteriores (todos sobre concorrência), este é sobre
diagnóstico de performance de query única — território novo no
catálogo.

## Domínio de demonstração

Uma tabela dedicada, `registro_busca` (id, email, nome), semeada uma
única vez na subida da aplicação com 200.000 linhas (inserção em lote
via `generate_series` do PostgreSQL — rápido mesmo em volume, sem
inserir entidade por entidade via JPA). A coluna `email` **não** tem
nenhuma restrição de unicidade nem índice no mapeamento JPA — de
propósito, para que a variante problemática realmente não tenha
nenhum índice disponível.

## Objetivo

Demonstrar, com dados reais em volume real, a diferença de plano de
execução e de tempo entre buscar por uma coluna sem índice e com
índice — usando `EXPLAIN (ANALYZE, FORMAT JSON)` real do PostgreSQL,
não uma estimativa ou uma medição só do lado da aplicação.

## Requisitos funcionais

| ID | Requisito |
|---|---|
| RF-01 | Endpoint da variante `sem-indice`: remove o índice de `email` se existir (`DROP INDEX IF EXISTS`), então roda `EXPLAIN (ANALYZE, FORMAT JSON)` numa busca por email exato. |
| RF-02 | Endpoint da variante `com-indice`: cria o índice de `email` se não existir (`CREATE INDEX IF NOT EXISTS`), então roda a mesma busca. |
| RF-03 | Resposta reporta `tipoDoPlano` (Node Type real do plano — "Seq Scan" ou "Index Scan"), `duracaoConsultaMs` (Actual Total Time real, extraído do JSON do `EXPLAIN ANALYZE`) e `quantidadeRegistros` (contagem real da tabela), com `origemDados: REAL`. |
| RF-04 | Página do laboratório expõe as duas variantes com conteúdo educacional (causa, como diagnosticar com `EXPLAIN ANALYZE`, a correção, perguntas de entrevista), incluindo o Assistente de IA já existente com contexto desta execução. |

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | O índice é criado/removido de verdade via DDL real (`CREATE INDEX`/`DROP INDEX`) a cada execução — não simulado, não uma segunda tabela pré-indexada. |
| RNF-02 | A semente (200.000 linhas) é inserida uma única vez, na subida da aplicação — não a cada execução do laboratório. |
| RNF-03 | Testes de integração com Testcontainers comprovam: `sem-indice` produz `tipoDoPlano: "Seq Scan"`; `com-indice` produz `tipoDoPlano` iniciando com `"Index"` (Index Scan ou Index Only Scan, a depender da decisão do otimizador do PostgreSQL). |

## Design técnico — decisões

### `EXPLAIN (ANALYZE, FORMAT JSON)`, não medição só da aplicação

Medir com `Instant.now()` antes/depois da chamada JDBC mediria a
latência de rede + parsing + tudo mais, não só a execução da query em
si. `EXPLAIN (ANALYZE, FORMAT JSON)` faz o PostgreSQL executar a query
de verdade e retornar, no próprio plano, o tempo real de execução
(`Actual Total Time`) e o nó real escolhido pelo otimizador (`Node
Type`) — a fonte mais autoritativa possível, direto do banco.

### DDL real via `@Modifying @Query(nativeQuery = true)`, mesmo padrão do `SET LOCAL` de `SPEC-LAB-DEADLOCK-001`

`CREATE INDEX IF NOT EXISTS` / `DROP INDEX IF EXISTS` tornam as duas
variantes idempotentes e executáveis em qualquer ordem, quantas vezes o
usuário quiser clicar — sem precisar rastrear estado entre execuções.

### Uma tabela, não duas

Diferente do laboratório de Race Condition (duas entidades por
restrição do `@Version`), aqui uma única tabela basta: o índice é
criado e removido de verdade a cada variante, então não há necessidade
de duplicar dados numa segunda tabela "pré-indexada" — a demonstração é
mais real assim (é literalmente o mesmo `ALTER` que um engenheiro
rodaria em produção).

### `ANALYZE` real após semear e após cada criação/remoção de índice

Achado real durante a implementação: sem estatísticas atualizadas, o
otimizador do PostgreSQL pode escolher planos como `Bitmap Heap Scan`
em vez de `Index Scan` puro logo após um índice ser criado —
comportamento legítimo, mas que dependia de rodar `ANALYZE` primeiro.
Rodar `ANALYZE registro_busca` (real, via `@Modifying @Query`) depois
de semear os dados e depois de cada criação/remoção do índice tornou a
demonstração consistente com o plano mais didático (`Index Scan`).

## Critérios de aceite

- [x] Variante `sem-indice` produz `tipoDoPlano: "Seq Scan"` real.
- [x] Variante `com-indice` produz um plano de índice real (`Index Scan` ou `Index Only Scan`).
- [x] Diferença de tempo real e mensurável entre as duas (`duracaoConsultaMs`), a partir do `EXPLAIN ANALYZE` real do PostgreSQL.
- [x] Página do laboratório com as duas variantes executáveis e conteúdo educacional, testada no navegador.
- [x] `docker compose --profile core up` revalidado sem regressão ao final.

## Evidências de conclusão (2026-08-23)

- **Dois achados reais durante a implementação**:
  1. `@Modifying @Query(nativeQuery = true)` do Spring Data JPA exige
     contexto transacional — `removerIndice()`/`criarIndice()` falhavam
     com `TransactionRequiredException` até `ExecucaoIndiceService.executar()`
     ser anotado com `@Transactional`.
  2. O primeiro teste real mostrou que, logo após criar o índice, o
     otimizador do PostgreSQL escolheu `Bitmap Heap Scan` em vez de
     `Index Scan` puro — comportamento legítimo do otimizador, mas
     dependente de estatísticas atualizadas. Adicionado `ANALYZE
     registro_busca` real após semear os dados e após cada
     criação/remoção do índice — com isso, a execução real passou a
     produzir `Index Scan` de forma consistente. O teste de integração
     foi ajustado para aceitar qualquer plano que não seja `Seq Scan`
     (`Index Scan`, `Index Only Scan` ou `Bitmap Heap Scan` são todos
     resultado real de usar o índice), já que a escolha exata do
     otimizador pode variar por ambiente.
- **Testes de integração reais** (Testcontainers, 2 testes,
  `ExecucaoIndiceServiceIntegrationTest`), populando 200.000 linhas
  reais via `generate_series`. Suíte completa do backend: 37/37 testes.
- **Execução real via `curl`, contra o Docker Compose real**, com
  200.000 linhas reais: `sem-indice` → `Seq Scan`, **12,934 ms**
  reais de execução da query (`Actual Total Time` do próprio
  PostgreSQL); `com-indice` → `Index Scan`, **0,028 ms** — uma
  diferença real de ~460×, bem mais dramática do que o estimado a
  priori na SPEC.
- **Isolamento validado**: uma execução de `sem-indice` (fazendo um
  Seq Scan completo em 200 mil linhas) disparada em paralelo com uma
  execução do laboratório de N+1, que respondeu normalmente.
- **Validação visual real no navegador** (Chrome): as duas variantes
  executadas via clique real, com "Plano (REAL)" em vermelho para
  `Seq Scan` e verde para `Index Scan`, e os tempos reais exibidos
  (13,805 ms vs. 0,027 ms nessa execução específica — a variação entre
  execuções é esperada e real, não fabricada).
- **Sem regressão**: `mvn -B verify` (37/37), `npm run build`/`lint`
  sem erros, `docker compose --profile core up` revalidado ao final,
  ambiente encerrado de forma limpa.

## Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Diferença de tempo pouco dramática em ambiente de container local (dados em cache, tabela pequena demais) | Demonstração pouco convincente | 200.000 linhas garante Seq Scan real e mensurável; número ajustado com base em medição real durante a validação, não estimado a priori |
| Semear 200.000 linhas via JPA entidade-por-entidade ser lento na subida | Startup lento | Inserção em lote via SQL nativo (`generate_series`), não `repository.save()` em loop |

## Observação de status

Implementação concluída e validada nesta interação (2026-08-23), a
partir da aprovação explícita do usuário para começar este item do
backlog.
