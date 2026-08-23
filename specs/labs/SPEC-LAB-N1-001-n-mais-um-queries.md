# SPEC-LAB-N1-001 — Laboratório: N+1 Queries

- **Status**: Implementada e validada (2026-08-22) — paginação (RF-05)
  deliberadamente adiada, ver seção "Escopo entregue nesta fase"
- **Título**: Problema de N+1 consultas com JPA/Hibernate
- **Depende de**: `SPEC-JEL-003` (plataforma base)
- **Fase do roadmap**: Fase 3

## Contexto

N+1 é o problema de performance mais comum em aplicações Spring Data
JPA/Hibernate: uma consulta inicial retorna N registros, e o acesso a uma
associação lazy de cada um dispara uma consulta adicional por registro —
1 + N consultas ao total, no lugar de uma ou poucas.

## Domínio de demonstração

```text
Pedido (1) ── possui muitos ──> (N) ItemPedido
```

`Pedido` tem uma coleção `itens` (`@OneToMany`, lazy por padrão). Listar
pedidos e acessar `pedido.getItens()` para cada um dispara o N+1.

## Objetivo

Demonstrar o problema de forma reproduzível e observável, e apresentar
três soluções (`JOIN FETCH`, `@EntityGraph`, DTO Projection), com
trade-offs explícitos — incluindo por que `FetchType.EAGER` global **não**
é solução.

## Requisitos funcionais

| ID | Requisito |
|---|---|
| RF-01 | Endpoint que lista pedidos com itens usando a versão problemática (lazy loading acessado em loop), retornando métricas reais de quantidade de queries e tempo. |
| RF-02 | Três endpoints (ou variantes de um mesmo endpoint) demonstrando as soluções: `JOIN FETCH`, `@EntityGraph`, DTO Projection. |
| RF-03 | Contagem real de queries SQL executadas por requisição (não estimada), exposta como métrica `REAL`. |
| RF-04 | Massa de dados de demonstração (pedidos + itens) gerada de forma determinística, para permitir comparação antes/depois com o mesmo volume. |
| RF-05 | Paginação suportada em todas as variantes (para poder explicar o risco de paginação em memória com `JOIN FETCH` para coleções `*-to-many`). |

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | Contagem de queries obtida por instrumentação real (ex.: datasource-proxy, `p6spy`, ou listener do Hibernate) — não por estimativa de código. A ferramenta exata é decisão de implementação, registrada em ADR no momento da Fase 3. |
| RNF-02 | Testes de integração com Testcontainers (PostgreSQL) validando a quantidade exata de queries de cada variante. |
| RNF-03 | Nenhuma variante deve expor entidades JPA diretamente na resposta HTTP (DTOs obrigatórios). |

## Soluções a demonstrar

### 1. `JOIN FETCH`

```java
@Query("SELECT p FROM Pedido p JOIN FETCH p.itens")
List<Pedido> buscarTodosComItens();
```

- **Vantagem**: uma única query, controle total do JPQL.
- **Limitação**: paginação (`LIMIT`/`OFFSET`) com fetch join em coleção
  `*-to-many` é feita em memória pelo Hibernate — risco real de carregar
  mais dados do que o necessário. Deve ser explicado explicitamente no
  conteúdo educacional do laboratório.

### 2. `@EntityGraph`

```java
@EntityGraph(attributePaths = "itens")
List<Pedido> findAll();
```

- **Vantagem**: reaproveita métodos padrão do Spring Data, mais declarativo.
- **Limitação**: mesma restrição de paginação em coleções `*-to-many` que
  `JOIN FETCH`.

### 3. DTO Projection

```java
public record PedidoResumoProjecao(Long id, String status, Long quantidadeItens) {}
```

- **Vantagem**: paginação segura, menor volume de dados trafegado, não
  carrega entidades gerenciadas.
- **Limitação**: não serve quando o caso de uso realmente precisa dos
  dados completos das entidades associadas.

### Por que não usar `FetchType.EAGER` global

Trocar a associação para `EAGER` resolve a listagem, mas move o custo do
N+1 para **toda** consulta que carregue um `Pedido`, mesmo quando os itens
não são necessários (ex.: buscar um pedido só para atualizar seu status).
É uma correção local que cria um problema de performance global e
oculto — deve ser documentado como antipadrão no conteúdo do laboratório,
não como alternativa válida.

## Experiência do laboratório (mapeada ao fluxo padrão da seção 17)

| Etapa do fluxo padrão | Conteúdo específico deste laboratório |
|---|---|
| Introdução / Objetivo | O que é N+1, por que acontece com lazy loading |
| Arquitetura | Diagrama `Pedido → ItemPedido`, diagrama de sequência da requisição problemática |
| Executar problema | Disparar listagem de pedidos na variante problemática |
| Observar | Quantidade de queries, SQL gerado, tempo de resposta |
| Diagnosticar | Explicação de por que 1 consulta virou 1+N |
| Código problemático | Trecho de código com lazy loading acessado em loop |
| Soluções | `JOIN FETCH`, `@EntityGraph`, DTO Projection — com quando usar cada uma |
| Aplicar solução | Disparar a mesma listagem em cada variante corrigida |
| Antes × depois | Mesma massa de dados, mesma operação, comparação de quantidade de queries e tempo |
| Trade-offs | Paginação, volume de dados, acoplamento a entidade completa |
| Entrevista | "Por que essa API fez 101 queries?", "Quando `JOIN FETCH` não resolve?", "Por que EAGER não é solução?" |

## Escopo entregue nesta fase

Implementados: domínio (`Pedido`/`ItemPedido`), massa de dados
determinística (50 pedidos × 3 itens, semeada no startup), as quatro
variantes de execução (problemático, JOIN FETCH, EntityGraph, DTO
Projection) com contagem real de queries via Hibernate Statistics (ver
ADR-0005), página do laboratório com conteúdo educacional e painel de
execução interativo.

**Adiado deliberadamente**: RF-05 (paginação) — implementar paginação
`Pageable` de forma correta exigiria também instrumentar e explicar o
efeito de "paginação em memória" do Hibernate ao usar `JOIN FETCH`/
`@EntityGraph` em coleções `*-to-many`, o que é uma peça de conteúdo
própria. Registrado como item pendente, não como requisito descartado.

## Critérios de aceite

- [x] Variante problemática reproduz um N+1 real e mensurável: 1 query de
      listagem + 50 queries de itens (N = 50 pedidos), validado por teste
      de integração contra PostgreSQL real (Testcontainers).
- [x] As três variantes corrigidas reduzem a quantidade de queries para
      um número fixo (1 query, independente de N) — validado pelos
      mesmos testes de integração. Trade-off de paginação documentado no
      conteúdo do laboratório, não implementado nesta fase (ver acima).
- [x] Testes de integração comprovam a contagem exata de queries de cada
      variante, com a mesma massa de dados (`ExecucaoN1ServiceIntegrationTest`,
      4 testes, todos passando).
- [x] Conteúdo educacional cobre: introdução, arquitetura, execução real,
      código problemático, as três soluções com trade-offs, por que EAGER
      não é solução, perguntas de entrevista. (Diff explícito de código e
      seção de referências externas ficam para uma iteração futura.)
- [x] Nenhuma métrica de "antes"/"depois" é fabricada — todas vêm de
      execução real (`origemDados: REAL`) contra a mesma massa de dados
      determinística, validado manualmente via `curl` e via navegador.
- [x] Aprovação explícita do usuário para iniciar a implementação (Fase 3) —
      dada em 2026-08-22.

## Evidências de conclusão

- `mvn test`: 12/12 testes passando, incluindo 4 testes de integração com
  Testcontainers (PostgreSQL real) comprovando as contagens exatas: 51
  queries (problemático), 1 query cada para JOIN FETCH/EntityGraph/DTO
  Projection.
- Validação manual via `curl` contra o backend real (Docker Compose,
  profile `core`): catálogo retornando `n1-queries` com `status:
  DISPONIVEL`; as quatro execuções retornando `origemDados: REAL` e as
  contagens de query esperadas.
- Validação visual real no Chrome: página `/laboratorios/n1-queries`
  renderizando o conteúdo educacional e o painel de execução; os quatro
  botões executados de fato, com o card "Antes × depois" aparecendo
  corretamente após a execução da variante problemática e de ao menos uma
  corrigida. Nenhum erro no console do navegador.
- `npm run build`/`lint` (frontend) sem erros.

## Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Ferramenta de contagem de queries adicionar overhead de instrumentação | Métricas distorcidas | Usar instrumentação leve e documentar seu próprio custo separadamente das métricas do laboratório |
| Paginação com `JOIN FETCH` mascarar o problema de memória em massas de dados pequenas | Conclusão didática incorreta | Massa de dados de demonstração dimensionada para tornar o efeito visível (decisão de implementação, Fase 3) |

## Observação de status

Esta SPEC é a especificação completa do primeiro laboratório funcional,
mas sua implementação está na **Fase 3** do roadmap. Nenhum código desta
SPEC é implementado durante a Fase 0.
