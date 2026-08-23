# Roadmap — Java Engineering Lab

> Todas as fases além da Fase 0 estão **propostas**, pendentes de
> aprovação do usuário. Nenhuma fase começa sem que a anterior tenha sido
> aprovada e sem que a SPEC correspondente exista.

## Fases

| Fase | Nome | Status | Entregáveis principais |
|---|---|---|---|
| 0 | Governança e descoberta | **Concluída** (2026-08-22) | Histórico de conversas, `CLAUDE.md`, SPECs iniciais, roadmap, diagramas |
| 1 | Bootstrap de código | **Concluída** (2026-08-22) | Esqueleto de `backend/`/`frontend/` (`SPEC-JEL-004`), Docker Compose, CI básico |
| 2 | Plataforma base | **Concluída** (2026-08-22) | Catálogo de laboratórios, contrato de execução/métricas (`SPEC-JEL-003`) |
| 3 | Laboratório N+1 | **Concluída** (2026-08-22) | Implementação completa de `SPEC-LAB-N1-001` (paginação adiada) |
| 4 | Laboratório Race Condition / Lost Update | **Concluída** (2026-08-22) | Implementação completa de `SPEC-LAB-RACE-001` |
| 5 | Laboratório Kafka / Idempotência | **Concluída** (2026-08-22) | Implementação completa de `SPEC-LAB-KAFKA-IDEMP-001` |
| 6 | Observabilidade consolidada | **Concluída** (2026-08-23) | Logs estruturados, Prometheus/Grafana e tracing distribuído (Tempo) — `SPEC-JEL-005` |
| 7 | Engineering AI Assistant | **Concluída** (2026-08-23) | Assistente de IA (Ollama, modelo local) com contexto real do laboratório — `SPEC-JEL-006` |
| 8 | Hardening | **Concluída** (2026-08-23) | Dependency-Check + npm audit no CI, JaCoCo, SonarQube validado (3 bugs reais corrigidos), teste de carga real, UX (404/títulos), LICENSE/CONTRIBUTING/CODE_OF_CONDUCT — `SPEC-JEL-007` |

## Backlog de laboratórios futuros (pós Fase 5)

Ordem não definida — cada um exige sua própria SPEC (`SPEC-LAB-<CODIGO>-001`)
antes de implementação, conforme `docs/decisions/0003-ids-de-spec.md`:

1. LazyInitializationException
2. Eager Fetching excessivo
3. Cartesian Product
4. Query sem índice
5. Deadlock
6. Connection Pool Exhaustion
7. Transactional Outbox (ver seção 27 do prompt mestre)
8. Ordenação de eventos
9. Circuit Breaker
10. Retry Storm
11. Timeout Cascade
12. Saga
13. Cache inconsistente
14. Cache Stampede
15. Thread Pool Exhaustion
16. Memory Leak / OutOfMemoryError
17. Paginação incorreta
18. Observabilidade insuficiente

Não há compromisso de implementar todos — este é um backlog de
possibilidades, não uma promessa de escopo.

## Critério de avanço entre fases

Cada fase só avança para a próxima quando:

1. a(s) SPEC(s) da fase estiverem aprovadas pelo usuário;
2. a implementação estiver testada e validada (ver Definition of Done em
   `specs/manifest/MANIFESTO.md` e nas SPECs individuais);
3. a documentação afetada estiver atualizada;
4. o histórico da decisão de avanço estiver registrado em
   `docs/conversation-history.md`.
