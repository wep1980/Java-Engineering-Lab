# SPEC-JEL-002 — Arquitetura da Plataforma

- **Status**: Proposta (pendente de aprovação do usuário)
- **Título**: Arquitetura geral do Java Engineering Lab
- **Relacionadas**: `SPEC-JEL-001` (bootstrap), `SPEC-JEL-003` (MVP),
  `docs/decisions/0002-monorepo.md`

## Contexto

Antes de qualquer código, o projeto precisa de uma arquitetura de
referência que sirva a todos os laboratórios futuros sem exigir redesenho
a cada novo laboratório adicionado ao backlog (seção 28 do prompt mestre).

## Problema

Sem uma arquitetura comum, cada laboratório reinventaria sua própria
estrutura de execução, contrato de API e forma de expor métricas —
dificultando manutenção, comparação entre laboratórios e reuso de
observabilidade.

## Objetivo

Definir: estrutura de repositório, arquitetura de backend, arquitetura de
frontend, modelo de dados compartilhado, estratégia de observabilidade,
estratégia de contratos de API, e infraestrutura local — como propostas
sujeitas a aprovação.

## Estrutura de repositório — PROPOSTA

```text
/
├── backend/                # Java 21 + Spring Boot (Maven)
├── frontend/                # Next.js + React + TypeScript
├── docs/                    # Documentação normativa e histórico
│   └── decisions/           # ADRs
├── specs/                   # SPECs (Spec-Driven Development)
│   ├── manifest/
│   ├── architecture/
│   ├── backend/
│   ├── frontend/
│   ├── labs/
│   ├── infra/
│   ├── security/
│   └── testing/
├── diagrams/                 # C4, sequência, ERD (Mermaid)
├── infra/                    # Docker Compose, configs de observabilidade
├── scripts/                  # Scripts de apoio (ex.: histórico de conversas)
├── .github/                  # GitHub Actions (a partir da Fase 1)
├── docker-compose.yml         # Criado na Fase 1
└── README.md
```

Justificativa em `docs/decisions/0002-monorepo.md`.

## Arquitetura de backend — PROPOSTA

### Separação de camadas (decisão base, sem overengineering)

```text
Controller → Service → Domínio ← Repository
                ↓
              Mapper ↔ DTO
```

- **Controller**: apenas tradução HTTP ↔ chamada de serviço. Nenhuma regra
  de negócio.
- **Service**: orquestra casos de uso, aplica regras de negócio.
- **Domínio**: entidades JPA e objetos de valor. Nunca retornados
  diretamente por controllers (ver `SPEC-JEL-002` seção Contratos).
- **Repository**: Spring Data JPA.
- **Mapper/DTO**: fronteira entre domínio e API pública.

### Hexagonal — HIPÓTESE, não decidida

O prompt mestre pede para "avaliar Arquitetura Hexagonal ... quando trouxer
benefício real" e explicitamente veta abstrações artificiais. Para o MVP
(catálogo de laboratórios + laboratório de N+1), a separação em camadas
acima já é suficiente: não há múltiplos adaptadores de entrada/saída reais
ainda (uma API REST, um banco). **Proposta**: adiar arquitetura hexagonal
até que um laboratório concreto exija múltiplos adaptadores (ex.: consumo
de eventos Kafka como uma segunda porta de entrada no laboratório de
idempotência). Reavaliar em `SPEC-LAB-KAFKA-IDEMP-001`.

### Módulos por domínio, não por camada técnica global

Cada laboratório é um pacote de domínio próprio dentro do backend (ex.:
`laboratorios.n1`, `laboratorios.race`), evitando um único pacote
`service`/`repository` gigante compartilhado entre laboratórios não
relacionados. Um módulo `plataforma` (ou `core`) concentra o que é
realmente compartilhado: catálogo de laboratórios, modelo de execução,
contrato de métricas.

## Arquitetura de frontend — PROPOSTA

Next.js (App Router) + TypeScript + Tailwind CSS. Estrutura por domínio de
tela (catálogo, página de laboratório, painel de execução) em vez de por
tipo técnico de arquivo, evitando pastas genéricas `components/`,
`hooks/`, `utils/` como categorias-catch-all sem coesão.

## Modelo de dados — PROPOSTA (alto nível)

- **PostgreSQL** como banco relacional principal (dados de negócio de cada
  laboratório, ex.: `pedido`, `item_pedido`).
- **Redis**: cache e suporte a laboratórios futuros (cache stampede, cache
  inconsistente — backlog).
- **Kafka**: mensageria para os laboratórios 3 (idempotência) e futuros
  (outbox, ordenação de eventos).

Nenhum desses três é necessário para o primeiro laboratório funcional
(N+1, que usa apenas PostgreSQL) — ver `SPEC-JEL-003` para o que
efetivamente entra no MVP.

## Contratos de API — PROPOSTA

- Entidades JPA nunca são serializadas diretamente em respostas HTTP.
- Toda API pública usa DTO + mapper explícito.
- Erros seguem um formato padrão (`código`, `mensagem`, `timestamp`,
  `caminho`, `correlationId`) sem stack trace ou detalhes internos — ver
  `docs/security.md`.
- Documentação OpenAPI/Swagger obrigatória e mantida atualizada a cada
  mudança de contrato.

## Observabilidade — PROPOSTA

Micrometer + Prometheus + Grafana para métricas; OpenTelemetry para
tracing distribuído; logs estruturados com correlation ID. Cada
laboratório expõe apenas a telemetria relevante ao problema que demonstra
(ver `docs/observability.md`) — não é obrigatório instrumentar tudo em
todos os laboratórios.

## Infraestrutura local — PROPOSTA

Docker Compose com profiles, para não obrigar subir todo o ambiente
sempre:

| Profile | Serviços |
|---|---|
| `core` | frontend, backend, PostgreSQL |
| `messaging` | Kafka, Kafka UI |
| `observability` | Prometheus, Grafana |
| `quality` | SonarQube |
| `full` | todos os anteriores |

Criação efetiva do `docker-compose.yml` fica para a Fase 1
(`SPEC-JEL-003`), não para esta SPEC de arquitetura.

## Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Adiar decisão sobre Hexagonal pode exigir refactor depois | Retrabalho médio | Módulo `plataforma` já isola o que muda entre laboratórios, reduzindo o custo de um refactor posterior |
| Monorepo com muitos laboratórios pode crescer descontroladamente | Manutenibilidade | Pacotes por domínio de laboratório + CI com path filters (Fase 1) |

## Decisões

Ver `docs/decisions/0002-monorepo.md` e `docs/decisions/0003-ids-de-spec.md`.
Decisão sobre Hexagonal permanece **HIPÓTESE**, não tomada nesta SPEC.

## Critérios de aceite desta SPEC

- [x] Estrutura de repositório proposta e documentada.
- [x] Arquitetura de backend e frontend propostas, sem overengineering.
- [x] Estratégia de observabilidade e infraestrutura local propostas.
- [ ] Aprovação explícita do usuário.
