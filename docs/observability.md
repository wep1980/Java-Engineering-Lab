# Observabilidade — Java Engineering Lab

> Diretrizes propostas para a Fase 6 (consolidação), mas que orientam a
> instrumentação desde o primeiro laboratório (Fase 3), já que
> observabilidade é parte do produto educacional, não um extra.

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

Este documento descreve o alvo. A instrumentação efetiva de ferramentas
(ex.: `p6spy`/datasource-proxy para contagem de queries) é decisão de
implementação tomada na SPEC de cada laboratório (ver, por exemplo,
RNF-01 em `specs/labs/SPEC-LAB-N1-001-n-mais-um-queries.md`).
