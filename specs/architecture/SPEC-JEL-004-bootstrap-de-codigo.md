# SPEC-JEL-004 — Bootstrap de Código (Fase 1)

- **Status**: Aprovada (usuário aprovou avanço para a Fase 1 em 2026-08-22)
- **Título**: Esqueleto executável de backend e frontend, Docker Compose e CI básico
- **Relacionadas**: `SPEC-JEL-002` (arquitetura), `SPEC-JEL-003` (MVP, ainda não implementado)

## Contexto

A Fase 0 produziu apenas documentação e SPECs. A Fase 1, aprovada pelo
usuário, cria o esqueleto executável do projeto — sem nenhuma
funcionalidade de laboratório ainda (isso é `SPEC-JEL-003` em diante).

## Objetivo

Ter um backend e um frontend que sobem, respondem a um health-check e
podem ser buildados/testados por CI, além de um `docker-compose.yml`
funcional com profiles, sem introduzir nenhuma regra de negócio de
laboratório.

## Escopo

1. Backend: projeto Maven Java 21 / Spring Boot, com Actuator (health,
   info, prometheus), Bean Validation e OpenAPI/Swagger habilitados. Sem
   JPA/DataSource ainda — essa dependência entra junto com a primeira
   entidade real (`SPEC-JEL-003`/`SPEC-LAB-N1-001`), para não configurar
   persistência sem nenhum dado para persistir.
2. Frontend: projeto Next.js (App Router) + TypeScript + Tailwind CSS, com
   uma página inicial simples informando o estado atual do projeto (sem
   catálogo de laboratórios ainda — isso é `SPEC-JEL-003`).
3. `docker-compose.yml` com os profiles propostos em `SPEC-JEL-002`
   (`core`, `messaging`, `observability`, `quality`, `full`). Apenas
   `core` (backend, frontend, PostgreSQL) é validado nesta fase; os
   demais profiles têm a configuração escrita, mas sua subida completa
   (Kafka, Grafana, SonarQube) fica para quando os laboratórios/fases que
   os utilizam existirem, evitando gastar tempo/recursos com serviços
   ainda sem uso real.
4. CI básico via GitHub Actions: build + testes de backend e frontend,
   com path filters para não rodar um pipeline por mudanças só no outro.
5. Atualização de `README.md`, `docs/links.md` e `docs/testing-guide.md`
   refletindo o que passou a existir de fato.

## Fora de escopo

- Qualquer entidade de domínio, endpoint de negócio ou página de
  laboratório.
- Autenticação/autorização.
- Observabilidade além do que o Actuator/Micrometer expõem por padrão
  (dashboards do Grafana ficam para a Fase 6).
- SonarQube efetivamente configurado (apenas o profile `quality` no
  compose, sem pipeline de qualidade ainda).

## Requisitos funcionais

| ID | Requisito |
|---|---|
| RF-01 | `backend` sobe com `mvn spring-boot:run` (ou `./mvnw`) e responde `200` em `/actuator/health`. |
| RF-02 | `backend` expõe documentação OpenAPI em `/v3/api-docs` e Swagger UI em `/swagger-ui.html`. |
| RF-03 | `frontend` sobe com `npm run dev` e serve uma página inicial em `/`. |
| RF-04 | `docker compose --profile core up` sobe frontend, backend e PostgreSQL. |
| RF-05 | CI builda e testa backend e frontend a cada push/PR, com path filters. |

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | Nenhuma credencial hardcoded — PostgreSQL usa variáveis de ambiente mesmo sem uso real ainda pelo backend. |
| RNF-02 | Build reprodutível: versões de dependências fixadas (sem `latest` implícito além do necessário). |
| RNF-03 | Estrutura de pacotes do backend já reflete a separação por domínio prevista em `SPEC-JEL-002` (módulo `plataforma` vazio, pronto para receber o catálogo em `SPEC-JEL-003`). |

## Critérios de aceite

- [x] `backend`: `mvn -f backend/pom.xml test` passa.
- [x] `backend`: aplicação sobe e `/actuator/health` responde `UP`.
- [x] `frontend`: `npm --prefix frontend run build` completa sem erro.
- [x] `docker-compose.yml` validado com `docker compose config` sem erro.
- [x] CI configurado com dois workflows (`backend-ci.yml`, `frontend-ci.yml`) com path filters.
- [x] Documentação (`README.md`, `docs/links.md`, `docs/testing-guide.md`) atualizada com comandos e URLs reais.

## Segurança

Nenhuma credencial versionada. `backend/src/main/resources/application.yml`
usa variáveis de ambiente (`${DB_USUARIO}`, `${DB_SENHA}`, etc.) com
valores de exemplo documentados em `.env.example` (a criar nesta fase).

## Evidências de conclusão

Detalhadas em `docs/testing-guide.md` (seção "Validação do esqueleto —
Fase 1") e na resposta desta interação em `docs/conversation-history.md`:
`mvn test` passou, `/actuator/health` respondeu `UP`, Swagger UI e OpenAPI
JSON responderam `200`, `npm run build`/`lint` passaram sem erros,
`docker compose --profile core up --build` subiu os três serviços com
sucesso (postgres healthy, backend UP, frontend 200 com conteúdo
correto). Ambiente de validação derrubado ao final
(`docker compose down`) para não deixar containers órfãos.

## Decisões de versão desta fase

- **Spring Boot 4.1.1** (última versão estável disponível no Maven
  Central no momento da implementação) sobre Java 21.
- **springdoc-openapi 3.1.0**, compatível com Spring Boot 4 / Spring
  Framework 7.
- **Next.js 16.3.2 + React 19.2.8 + Tailwind CSS 4**, geradas via
  `create-next-app@latest` (última versão estável no npm no momento da
  implementação).

Essas são decisões de implementação (não requerem uma SPEC própria), mas
ficam registradas aqui para rastreabilidade — caso alguma dessas versões
se mostre instável, a atualização/downgrade é decisão de manutenção
normal, não uma mudança de escopo desta SPEC.
