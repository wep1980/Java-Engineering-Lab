# ADR-0005 — Instrumentação de queries via Hibernate Statistics

- **Status**: Aceita
- **Data**: 2026-08-22
- **SPEC relacionada**: `specs/labs/SPEC-LAB-N1-001-n-mais-um-queries.md` (RNF-01)

## Contexto

O laboratório de N+1 precisa de uma contagem **real** de queries SQL
executadas por variante (não estimada), para comparar de forma honesta a
versão problemática com as três soluções.

## Decisão

Usar `org.hibernate.stat.Statistics#getPrepareStatementCount()` (obtida
via `EntityManagerFactory.unwrap(SessionFactory.class).getStatistics()`),
medindo o delta antes/depois de cada execução, em vez de uma biblioteca
externa de instrumentação (ex.: `datasource-proxy`, `p6spy`).

## Justificativa

- É um mecanismo nativo do Hibernate (já uma dependência transitiva de
  `spring-boot-starter-data-jpa`) — nenhuma dependência nova.
- Conta o número real de `PreparedStatement`s preparados pelo driver
  JDBC, incluindo os disparados por lazy loading — exatamente o que o
  laboratório precisa demonstrar.
- É habilitado com uma única propriedade
  (`hibernate.generate_statistics=true`).

## Consequências

- A métrica é por `EntityManagerFactory` (processo inteiro), não por
  requisição isolada — por isso o serviço mede um delta imediatamente
  antes/depois da operação, dentro do mesmo método, para isolar o
  resultado de outras atividades concorrentes. Em uso normal (educacional,
  um usuário por vez) isso é suficiente; não é uma métrica adequada para
  produção com alta concorrência.
- Se um laboratório futuro precisar do texto exato de cada SQL (não só a
  contagem), essa decisão deve ser revisitada — `Statistics` não expõe o
  SQL.
