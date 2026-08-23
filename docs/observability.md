# Observabilidade — Java Engineering Lab

> Consolidada na Fase 6 (`SPEC-JEL-005`) e validada contra infraestrutura
> real — ver `docs/links.md` e `docs/testing-guide.md`.

## Princípio

Observabilidade não é só infraestrutura — é conteúdo didático. O que cada
laboratório expõe (queries, traces, logs, eventos) é o que o usuário usa
para diagnosticar o problema. Por isso, cada laboratório mostra **apenas**
a telemetria relevante ao problema que demonstra, não a totalidade
possível de métricas.

## Stack

- **Métricas**: Micrometer → Prometheus → Grafana.
- **Tracing distribuído**: OpenTelemetry.
- **Logs**: estruturados, com correlation ID propagado ao longo do fluxo
  de uma requisição.

## O que cada laboratório deve expor (conforme aplicável)

| Sinal | Quando expor |
|---|---|
| Quantidade e texto de queries SQL | Laboratórios de persistência (N+1, Cartesian Product, query sem índice) |
| Latência / duração | Praticamente todos os laboratórios |
| Traces distribuídos | Laboratórios que atravessam múltiplos componentes (ex.: Kafka) |
| Eventos Kafka (publicados/consumidos) | Laboratórios de mensageria |
| Estado de conexões / pool | Connection Pool Exhaustion |
| Retries / circuit breaker | Circuit Breaker, Retry Storm, Timeout Cascade |

## Origem dos dados exibidos

Toda métrica exibida na interface carrega uma classificação de origem —
`REAL`, `SIMULADO` ou `ESTIMADO` (ver `specs/architecture/SPEC-JEL-003-mvp-plataforma-base.md`,
contrato de métricas). Valores `REAL` vêm de execução efetiva do
laboratório contra a mesma massa de dados usada na comparação antes/depois
(seção 21 do prompt mestre). Valores `SIMULADO`/`ESTIMADO`, se usados,
devem ser identificados visualmente como tal — nunca apresentados como
benchmark real.

## Logs

Logs estruturados, com correlation ID. Nunca registram senha, token,
segredo ou dado sensível desnecessário. Volume de logs deve servir ao
propósito didático do laboratório, não ser gerado em excesso só para
parecer mais "observável".

## Status

- **Métricas**: Micrometer/Prometheus/Grafana implementados e validados
  (Fase 6). Dashboard provisionado automaticamente em
  `infra/grafana/provisioning/`.
- **Tracing distribuído**: Micrometer Tracing + ponte OpenTelemetry +
  exportador OTLP para Grafana Tempo, implementado e validado (Fase 6).
  Amostragem em 100% (adequado para o volume de um laboratório
  educacional, não para produção).
- **Logs estruturados**: JSON (Elastic Common Schema, suporte nativo do
  Spring Boot), com `correlationId`, `traceId` e `spanId` incluídos
  automaticamente quando presentes no MDC/contexto de tracing.
- **Contagem de queries por laboratório**: cada laboratório usa a
  instrumentação mais apropriada ao que precisa demonstrar — o de N+1
  usa `Hibernate Statistics` (ver ADR-0005), não um proxy de datasource
  genérico.

Este documento agora descreve o que está implementado, não mais só um
alvo. Decisões técnicas específicas (propriedades corretas, correções de
regressão) estão registradas em `docs/decisions/` (ADRs 0005-0007) e em
`specs/architecture/SPEC-JEL-005-observabilidade-consolidada.md`.
