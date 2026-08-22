# Diagrama C4 — Nível 2 (Contêiner)

> Status: proposta inicial, sujeita a revisão junto com
> `specs/architecture/SPEC-JEL-002-arquitetura.md`. Reflete o alvo do MVP
> (Fase 2/3) — Kafka e Redis aparecem porque fazem parte da stack
> obrigatória, mas só entram em uso efetivo a partir dos laboratórios que
> os exigem (Fases 4/5).

```mermaid
C4Container
    title Java Engineering Lab — Contêineres

    Person(usuario, "Usuário")

    System_Boundary(jel, "Java Engineering Lab") {
        Container(frontend, "Frontend", "Next.js, React, TypeScript", "Catálogo de laboratórios, execução, comparação antes/depois")
        Container(backend, "Backend", "Java 21, Spring Boot", "API REST dos laboratórios, orquestra execução e métricas")
        ContainerDb(postgres, "PostgreSQL", "Banco relacional", "Dados de domínio de cada laboratório")
        ContainerDb(redis, "Redis", "Cache", "Usado pelos laboratórios de cache (backlog)")
        Container(kafka, "Kafka", "Mensageria", "Usado pelo laboratório de idempotência e futuros")
        Container(observabilidade, "Stack de Observabilidade", "Prometheus, Grafana, OpenTelemetry", "Métricas, traces e logs dos laboratórios")
    }

    Rel(usuario, frontend, "Usa", "HTTPS")
    Rel(frontend, backend, "Consome API REST", "HTTPS/JSON")
    Rel(backend, postgres, "Lê/escreve", "JDBC")
    Rel(backend, redis, "Lê/escreve (laboratórios de cache)", "Redis protocol")
    Rel(backend, kafka, "Publica/consome (laboratório de idempotência)", "Kafka protocol")
    Rel(backend, observabilidade, "Exporta métricas, traces e logs")
```
