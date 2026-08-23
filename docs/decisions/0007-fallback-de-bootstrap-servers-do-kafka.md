# ADR-0007 — Endereço de fallback para `spring.kafka.bootstrap-servers`

- **Status**: Aceita
- **Data**: 2026-08-23
- **Origem**: regressão real encontrada durante a validação de
  `SPEC-JEL-005-observabilidade-consolidada.md`

## Contexto

Ao validar `docker compose --profile core --profile observability up`
(sem o profile `messaging`), o backend **travava na inicialização**:

```text
org.apache.kafka.common.config.ConfigException: No resolvable bootstrap
urls given in bootstrap.servers
```

A suposição registrada em `SPEC-LAB-KAFKA-IDEMP-001` — "sem o profile
messaging, o backend sobe normalmente e só fica tentando reconectar em
background" — **nunca tinha sido validada de fato** nessa combinação
específica de profiles. Ela era verdadeira apenas quando o hostname
`kafka` conseguia ser resolvido (profile `messaging` ativo, ou
desenvolvimento local com `localhost`, que sempre resolve via loopback).

**Causa raiz**: o cliente Kafka falha de forma **síncrona**, na
construção do `KafkaConsumer`, apenas quando **nenhum** endereço em
`bootstrap.servers` consegue ser resolvido via DNS. Isso é diferente de
"endereço resolve, mas a conexão é recusada" — esse segundo caso é
tratado de forma assíncrona, com retentativas em background, sem
derrubar a aplicação. Quando o profile `messaging` não está ativo, o
DNS interno do Docker Compose não tem nenhuma entrada para `kafka` —
resolução falha completamente, não "eventualmente".

## Decisão

`KAFKA_BOOTSTRAP_SERVERS` no `docker-compose.yml` passa a incluir dois
endereços: `kafka:19092,localhost:9092`. `localhost` sempre resolve
(loopback), garantindo que ao menos um endereço da lista seja resolvível
— o que evita a falha síncrona na construção do consumer. A tentativa de
conexão em si (para ambos os endereços, se `messaging` não estiver ativo)
falha de forma assíncrona e tolerada, como esperado.

## Consequências

- Nenhuma mudança de código Java foi necessária — a correção é inteiramente
  de configuração.
- Quando o profile `messaging` está ativo, a conexão real acontece via
  `kafka:19092`; a tentativa adicional a `localhost:9092` simplesmente
  falha silenciosamente (nada escuta nessa porta dentro do container do
  backend) e é ignorada pelo cliente Kafka, que já tem um endereço
  funcional.
- Reforça, mais uma vez, a importância de validar toda combinação de
  profiles relevante contra infraestrutura real, não assumir
  comportamento a partir de um cenário parcialmente testado — ver também
  ADR-0006.
