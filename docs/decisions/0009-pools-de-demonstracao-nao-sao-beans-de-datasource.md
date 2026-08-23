# ADR-0009 — Pools de demonstração do laboratório de Connection Pool Exhaustion não são `@Bean HikariDataSource`

- **Status**: Aceita
- **Data**: 2026-08-23
- **Origem**: achado real durante a implementação de
  `SPEC-LAB-CONN-POOL-001-connection-pool-exhaustion.md`

## Contexto

O laboratório de Connection Pool Exhaustion precisa de dois pools
HikariCP dedicados e pequenos, isolados do pool principal da aplicação
(usado por todos os outros laboratórios). A primeira implementação os
registrou como `@Bean HikariDataSource` normais, num `@Configuration`
próprio.

Ao rodar o teste de integração real (Testcontainers), a suíte inteira
quebrou — inclusive laboratórios que nada têm a ver com este:

```text
Parameter 0 of constructor in
com.javaengineeringlab.backend.laboratorios.kafka.CreditoIdempotenteOperacao
required a bean named 'entityManagerFactory' that could not be found.
```

**Causa raiz**: a autoconfiguração do JPA no Spring Boot
(`JpaBaseConfiguration`/`HibernateJpaConfiguration`) só cria o bean
`entityManagerFactory` quando existe um **único candidato** do tipo
`DataSource` no contexto (`@ConditionalOnSingleCandidate(DataSource.class)`)
— ou exatamente um marcado `@Primary`, se houver mais de um. Com os
dois pools de demonstração registrados como beans `HikariDataSource`
(que implementa `DataSource`) e nenhum bean marcado `@Primary`, essa
condição deixou de ser satisfeita — a autoconfiguração do JPA
simplesmente **desistiu silenciosamente** de criar o
`entityManagerFactory`, derrubando todos os repositórios Spring Data de
todos os laboratórios, não só o novo.

## Decisão

Os pools de demonstração não são expostos como beans genéricos de
`DataSource`. Em vez disso, são construídos diretamente dentro de
`ExecucaoConnPoolService` (seu construtor), a partir de um
`JdbcConnectionDetails` injetado — essa interface **não** é do tipo
`DataSource`, então não interfere na contagem de candidatos da
autoconfiguração do JPA. `JdbcConnectionDetails` funciona
identicamente em produção/Docker Compose (derivado das propriedades
`spring.datasource.*`) e em testes com Testcontainers +
`@ServiceConnection` (que não define essas propriedades, só registra
esse bean) — sem duplicar configuração. Os pools são encerrados
explicitamente via `@PreDestroy`.

## Consequências

- Nenhuma mudança foi necessária em nenhum outro laboratório — a
  correção ficou inteiramente contida no pacote `laboratorios.connpool`.
- Padrão reaproveitável: qualquer futuro laboratório que precise de um
  `DataSource`/pool adicional deve seguir o mesmo caminho (construção
  manual a partir de `JdbcConnectionDetails`, não `@Bean DataSource`
  avulso) para não repetir esse problema.
- Reforça, mais uma vez, a importância de rodar o teste de integração
  real (não confiar só em `mvn compile`) antes de considerar uma
  mudança concluída — este bug não aparecia na compilação, só na
  inicialização real do contexto Spring.
