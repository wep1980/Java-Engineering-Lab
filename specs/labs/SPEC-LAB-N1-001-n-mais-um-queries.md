# SPEC-LAB-N1-001 — Laboratório: N+1 Queries

- **Status**: Proposta (pendente de aprovação do usuário)
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

## Critérios de aceite

- [ ] Variante problemática reproduz um N+1 real e mensurável (ex.: 1 query
      de listagem + N queries de itens, N = quantidade de pedidos
      retornados).
- [ ] As três variantes corrigidas reduzem a quantidade de queries para um
      número fixo (independente de N), exceto onde o trade-off de
      paginação for explicitamente demonstrado como exceção.
- [ ] Testes de integração comprovam a contagem exata de queries de cada
      variante, com a mesma massa de dados.
- [ ] Conteúdo educacional cobre as 32 seções aplicáveis da seção 17 do
      manifesto (nome, objetivo, contexto, ..., referências).
- [ ] Nenhuma métrica de "antes"/"depois" é fabricada — ambas vêm de
      execução real contra a mesma massa de dados.
- [ ] Aprovação explícita do usuário para iniciar a implementação (Fase 3).

## Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Ferramenta de contagem de queries adicionar overhead de instrumentação | Métricas distorcidas | Usar instrumentação leve e documentar seu próprio custo separadamente das métricas do laboratório |
| Paginação com `JOIN FETCH` mascarar o problema de memória em massas de dados pequenas | Conclusão didática incorreta | Massa de dados de demonstração dimensionada para tornar o efeito visível (decisão de implementação, Fase 3) |

## Observação de status

Esta SPEC é a especificação completa do primeiro laboratório funcional,
mas sua implementação está na **Fase 3** do roadmap. Nenhum código desta
SPEC é implementado durante a Fase 0.
