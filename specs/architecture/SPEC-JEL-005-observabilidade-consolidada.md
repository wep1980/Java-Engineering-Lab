# SPEC-JEL-005 — Observabilidade Consolidada

- **Status**: Implementada e validada (2026-08-23)
- **Título**: Logs estruturados, métricas via Prometheus/Grafana e
  tracing distribuído via OpenTelemetry/Tempo
- **Relacionadas**: `SPEC-JEL-002` (arquitetura), `docs/observability.md`

## Contexto

Desde a Fase 2, o backend já expõe correlation ID
(`FiltroCorrelationId`) e métricas via Actuator/Micrometer/Prometheus
(`/actuator/prometheus`), mas nunca foram validados em execução real: o
profile `observability` do `docker-compose.yml` nunca subiu, e não há
tracing distribuído. Esta fase consolida os três pilares de
observabilidade citados na stack obrigatória (seção 10 do prompt mestre):
métricas, logs estruturados e tracing.

## Objetivo

1. Logs estruturados (JSON) incluindo o correlation ID em cada linha.
2. Validar Prometheus de fato coletando métricas reais do backend.
3. Grafana com datasources e um dashboard provisionados automaticamente
   (não uma instância vazia exigindo configuração manual).
4. Tracing distribuído real: cada requisição gera um trace exportado via
   OpenTelemetry, visível no Grafana (via Tempo).

## Escopo

- `logging.structured.format` do Spring Boot (Elastic Common Schema) para
  logs em JSON, sem dependência nova — recurso nativo do Spring Boot
  desde a 3.4, presente na 4.1.1.
- Micrometer Tracing + ponte OpenTelemetry + exportador OTLP no backend.
- Novo serviço `tempo` (Grafana Tempo) no profile `observability` do
  `docker-compose.yml`, recebendo traces via OTLP.
- Provisionamento automático do Grafana: datasources (Prometheus e
  Tempo) e um dashboard inicial (métricas JVM, taxa/latência de
  requisições HTTP) via arquivos de provisionamento montados por volume
  — sem exigir clique manual em "Add data source".

## Fora de escopo

- Alertas/alerting rules (Prometheus Alertmanager).
- Métricas de negócio customizadas por laboratório (ex.: gráfico
  dedicado de "quantidade de queries" no Grafana) — os laboratórios já
  expõem isso na própria UI de cada um; um dashboard Grafana dedicado por
  laboratório fica para uma fase futura, se houver valor real.
- Amostragem de tracing para produção (aqui, 100% das requisições geram
  trace — aceitável em ambiente de laboratório/demonstração, não seria em
  produção real com alto volume).

## Requisitos funcionais

| ID | Requisito |
|---|---|
| RF-01 | Logs do backend em formato JSON estruturado, incluindo `correlationId` quando presente. |
| RF-02 | `docker compose --profile observability up` sobe Prometheus, Grafana e Tempo, todos funcionais. |
| RF-03 | Prometheus coleta métricas reais do backend (`up{job="java-engineering-lab-backend"} == 1`). |
| RF-04 | Grafana já vem com os datasources Prometheus e Tempo configurados e um dashboard visível, sem configuração manual. |
| RF-05 | Uma requisição HTTP ao backend gera um trace real, visível no Tempo/Grafana, com o correlation ID correlacionável ao trace. |

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | Sem dependência nova para logs estruturados (usar suporte nativo do Spring Boot). |
| RNF-02 | Tracing e métricas não podem quebrar o funcionamento dos laboratórios já existentes (N+1, Race Condition, Kafka) nem os profiles `core`/`messaging` isolados. |
| RNF-03 | Validação real: subir o profile, gerar tráfego real, confirmar visualmente no Grafana — não apenas "a configuração existe". |

## Critérios de aceite

- [x] Log do backend em JSON, com `correlationId` visível, confirmado em execução real.
- [x] `docker compose --profile core --profile observability up` sobe Postgres + Backend + Prometheus + Grafana + Tempo sem erro.
- [x] Prometheus mostra o target do backend como `UP`.
- [x] Grafana abre com datasources e dashboard já provisionados (sem passos manuais) — validado visualmente no Chrome, 5 painéis com dados reais.
- [x] Uma requisição real gera um trace visível no Tempo via Grafana (consultado pelo proxy do datasource, HTTP 200).
- [x] Profiles `core`/`messaging` continuam funcionando isoladamente — `--profile core` sozinho validado após a correção do ADR-0007 (regressão real encontrada e corrigida nesta mesma fase).

## Evidências de conclusão

- `mvn test`: 21/21 testes continuam passando após todas as mudanças.
- Log estruturado real capturado: `{"...,"message":"Requisição concluída: GET /api/laboratorios -> 200","traceId":"...","spanId":"...","correlationId":"teste-12345",...}` — confirma RF-01 e a correlação entre correlationId, traceId e spanId na mesma linha.
- Prometheus: `curl .../api/v1/targets` retornou `java-engineering-lab-backend: up`.
- Grafana: `GET /api/search` e `GET /api/datasources` confirmaram o dashboard e os dois datasources provisionados automaticamente; visualização real no Chrome mostrou os 5 painéis com dados reais (incluindo o tráfego de todos os laboratórios testados na sessão).
- Tempo: trace publicado por uma requisição real (`GET /api/laboratorios`) encontrado via `GET /api/traces/{traceId}` (200, span completo com `service.name=java-engineering-lab-backend`) e confirmado acessível via proxy do datasource Tempo no Grafana (200).
- `docker compose --profile core up` (sem observability, sem messaging) validado funcionando normalmente após a correção do ADR-0007 — sem regressão.

## Riscos

| Risco | Impacto | Mitigação | Status |
|---|---|---|---|
| Tracing adicionar overhead/latência perceptível | Métricas de duração dos laboratórios distorcidas | Overhead do Micrometer Tracing é desprezível para o volume desses laboratórios (dezenas de requisições, não milhares/segundo) | Não observado nenhum impacto perceptível durante a validação |
| Provisionamento do Grafana mal configurado deixa datasource "quebrado" silenciosamente | Falsa sensação de que funciona | Validação real: abrir o dashboard e confirmar dados reais aparecendo, não só que o container subiu | **Materializou-se de fato**: referência de datasource por nome em vez de UID deixou os painéis vazios sem nenhum erro visível — só a inspeção visual real (não a API) revelou o problema. Corrigido. |

## Percalços técnicos reais (achados durante a validação, não previstos na SPEC original)

1. **Regressão de crash sem o profile `messaging`** — `docker compose --profile core --profile observability up` (sem `messaging`) derrubava o backend inteiro, porque o hostname `kafka` não resolve via DNS quando o serviço não está no ar, e o cliente Kafka falha de forma síncrona nesse caso específico (diferente de "conexão recusada", que é tolerado em background). A premissa documentada na Fase 5 nunca tinha sido validada nessa combinação de profiles. Corrigido — ver `docs/decisions/0007-fallback-de-bootstrap-servers-do-kafka.md`.
2. **Receptor OTLP do Tempo escutando só em `127.0.0.1`** — inacessível para outros containers até configurar `endpoint: 0.0.0.0:...` explicitamente em `infra/tempo/tempo.yaml`.
3. **Propriedade OTLP errada** — `management.otlp.tracing.endpoint` (documentação/conhecimento anterior a esta versão) não existe efetivamente no Spring Boot 4.1.1; a propriedade correta é `management.opentelemetry.tracing.export.otlp.endpoint`, e o módulo `spring-boot-micrometer-tracing-opentelemetry` precisou ser adicionado explicitamente (mesmo padrão de modularização já visto com `spring-boot-webmvc-test` e `spring-boot-starter-kafka`).
4. **Nenhum log da aplicação carregava o correlation ID** — `FiltroCorrelationId` colocava o valor no MDC, mas nenhum código da aplicação de fato logava durante uma requisição, então o campo nunca aparecia. Adicionado um log real de conclusão de requisição no próprio filtro.
5. **Dashboard do Grafana com painéis vazios sem nenhum erro visível** — causado por referenciar o datasource pelo nome ("Prometheus") em vez do UID no JSON do dashboard. Corrigido definindo `uid` explícito no provisionamento do datasource e referenciando esse UID tanto no `datasource` do painel quanto em cada `target`.

Estes cinco achados reforçam, de forma concreta, por que este projeto insiste em validar contra infraestrutura real em vez de confiar apenas em "a configuração parece correta" — nenhum deles seria detectado por uma leitura do código ou da configuração.
