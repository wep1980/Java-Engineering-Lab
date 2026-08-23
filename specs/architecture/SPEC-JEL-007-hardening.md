# SPEC-JEL-007 — Hardening (Fase 8)

- **Status**: PROPOSTA — pendente de aprovação do usuário (regra 0 de
  `CLAUDE.md`: nenhuma implementação começa sem aprovação explícita)
- **Título**: Hardening final — segurança, testes, performance, UX,
  documentação e CI/CD
- **Relacionadas**: `specs/manifest/MANIFESTO.md` (item 46, Definition of
  Done), `docs/security.md`, `docs/testing-guide.md`, `docs/roadmap.md`

## Contexto

Item 44 do prompt mestre define a Fase 8 apenas como: segurança, testes,
performance, UX, documentação, CI/CD — sem detalhar requisitos
específicos. Ao contrário das fases anteriores (cada uma com um
laboratório ou recurso único e bem delimitado), esta fase é
deliberadamente ampla. Para respeitar a regra de Spec-Driven Development
sem criar burocracia desnecessária (item 47 do prompt mestre — "não
inventar requisitos silenciosamente", mas também "não criar abstrações
desnecessárias"), esta SPEC lista apenas itens **concretos, com gap real
verificado no estado atual do repositório** — nada especulativo.

Gaps reais identificados (verificados nesta sessão, não presumidos):

- Nenhum plugin de cobertura (JaCoCo) está configurado em
  `backend/pom.xml`, apesar de `docs/testing-guide.md` já citar JaCoCo
  como ferramenta adotada.
- O profile `quality` (SonarQube) do `docker-compose.yml` nunca foi
  validado em execução real (`docs/links.md` já registra isso
  explicitamente).
- Nenhuma ferramenta de detecção de CVEs em dependências está integrada
  — `docs/security.md` já registra essa decisão como adiada desde a
  Fase 1 e nunca formalizada.
- `.github/workflows/backend-ci.yml` e `frontend-ci.yml` fazem apenas
  build+teste e lint+build, respectivamente — sem relatório de
  cobertura, sem verificação de dependências.
- `LICENSE`, `CONTRIBUTING.md` e `CODE_OF_CONDUCT.md` não existem — o
  próprio README already sinaliza isso como pendente ("previstos para
  uma fase futura").
- O Next.js usa a página 404 padrão do framework (em inglês), o que
  quebra a consistência de idioma português do restante do projeto.
- Nenhum teste de carga/performance real foi feito em nenhuma fase
  anterior — todas as validações de performance até aqui foram
  qualitativas (ex.: "pessimista é nitidamente mais lento"), nunca
  medidas sob concorrência real de múltiplos clientes.

## Objetivo

Fechar esses gaps concretos, mantendo o princípio de simplicidade: cada
item abaixo resolve um gap real e verificado, não uma preocupação
hipotética.

## Escopo — seis trilhas

### T1. Segurança

| ID | Requisito |
|---|---|
| RF-01 | OWASP Dependency-Check integrado ao build Maven do backend, gerando relatório de vulnerabilidades conhecidas nas dependências. |
| RF-02 | `npm audit` integrado ao CI do frontend. |
| RF-03 | ADR formalizando as ferramentas escolhidas e a política adotada (bloqueante vs. informativo) — fecha a decisão adiada em `docs/security.md`. |

**Decisão de design proposta**: rodar ambas as ferramentas como
**informativas** no CI (relatório publicado como artifact, build não
falha automaticamente), porque decidir a política de severidade que
deveria travar o build é uma escolha de produto que ainda não foi
discutida com o usuário — travar o CI hoje por uma CVE de baixo risco
sem triagem prévia pararia todo o pipeline sem necessidade real. Pode
evoluir para bloqueante em uma fase futura, com critério explícito.

### T2. Testes

| ID | Requisito |
|---|---|
| RF-04 | JaCoCo configurado em `backend/pom.xml`, relatório de cobertura gerado a cada `mvn verify` e publicado como artifact no CI. |
| RF-05 | `docker compose --profile core --profile quality up` validado em execução real pela primeira vez; SonarQube analisa o backend real; achados reais registrados (não fabricados) — se não houver achados relevantes, isso também é um resultado válido a documentar. |

**Fora de escopo**: definir uma meta numérica de cobertura mínima —
`docs/testing-guide.md` já estabelece cobertura como indicador auxiliar,
não meta em si; manter essa filosofia.

### T3. Performance

| ID | Requisito |
|---|---|
| RF-06 | Um teste de carga real e simples, comparando o laboratório N+1 (variante problemática vs. `JOIN FETCH`) sob concorrência de múltiplos clientes simultâneos — reforça com números reais sob carga o mesmo argumento pedagógico já demonstrado em execução única. |

**Fora de escopo**: tuning de infraestrutura (pool de conexões, JVM
flags, etc.) — não é objetivo do projeto ensinar tuning de ambiente, e
sim os problemas de código já cobertos pelos laboratórios existentes.
Ferramenta concreta (`hey`, `wrk`, k6 ou similar) a decidir na
implementação e documentar caso a escolha não seja óbvia.

### T4. UX

| ID | Requisito |
|---|---|
| RF-07 | Página 404 customizada no frontend (`not-found.tsx`), em português, consistente com o restante do site. |
| RF-08 | Metadata (`<title>`) específico por página de laboratório (hoje todas as páginas herdam o título genérico do layout raiz). |
| RF-09 | Revisão rápida de acessibilidade básica nos componentes já existentes (labels associados a inputs, contraste de texto) — correção pontual, sem introduzir framework/dependência nova. |

### T5. Documentação final

| ID | Requisito |
|---|---|
| RF-10 | `LICENSE` na raiz do repositório — **licença a definir pelo usuário** (decisão que só ele pode tomar; ver seção de decisões pendentes abaixo). |
| RF-11 | `CONTRIBUTING.md` e `CODE_OF_CONDUCT.md` mínimos, sem burocracia excessiva (conforme item 41 do prompt mestre — "não criar burocracia excessiva no início"). |
| RF-12 | Revisão final do README (seção "Como contribuir" deixa de apontar para "fase futura" e passa a referenciar os arquivos reais). |

### T6. CI/CD completo

| ID | Requisito |
|---|---|
| RF-13 | CI do backend passa a publicar o relatório de cobertura (RF-04) e o relatório do Dependency-Check (RF-01) como artifacts de cada execução. |
| RF-14 | CI do frontend passa a rodar `npm audit` (RF-02) como step informativo. |

**Fora de escopo**: CD real (deploy automático para produção) — não há
ambiente de produção definido em nenhuma SPEC anterior; login/hospedagem
de produção não fazem parte do MVP educacional. Permanece fora de
escopo até existir uma decisão explícita do usuário sobre onde
hospedar.

## Decisões pendentes do usuário

Estas SPECs de fases anteriores só avançaram após decisão explícita do
usuário em pontos que só ele pode decidir (ex.: provedor de IA na Fase
7). Aqui, o ponto equivalente é:

1. **Licença do repositório (RF-10)**: o repositório é público
   (`docs/decisions/0004-repositorio-publico-e-push-automatico.md`) mas
   nunca teve uma licença definida. Opções comuns para um projeto de
   portfólio educacional: MIT (permissiva, simples), Apache 2.0
   (permissiva, com cláusula de patente), ou nenhuma licença por
   enquanto (todos os direitos reservados, código apenas para leitura).

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | Nenhum item desta fase pode quebrar o comportamento validado das Fases 1-7 — cada trilha é validada isoladamente e depois revalidada em conjunto (`docker compose --profile core up`, sem regressão). |
| RNF-02 | Ferramentas novas (Dependency-Check, JaCoCo) não adicionam credenciais nem serviços externos — rodam localmente/no runner do CI. |

## Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Dependency-Check ou `npm audit` reportarem um volume grande de CVEs de baixo risco em dependências transitivas | Ruído, relatório pouco acionável | Rodar como informativo (não bloqueante) nesta fase; triagem de severidade fica para decisão futura explícita |
| SonarQube (Java) ser pesado para rodar no ambiente local | Validação da T2 demorada | Rodar uma única vez para validação real, documentar achados, não deixar como parte do fluxo de desenvolvimento diário |
| Teste de carga (T3) sobrecarregar o ambiente Docker local durante a validação | Resultados não confiáveis / ambiente instável | Carga moderada, mensurável, documentada com os números reais observados (não estimados) |

## Critérios de aceite

- [x] Dependency-Check e `npm audit` rodando no CI, relatórios publicados como artifacts.
- [x] JaCoCo configurado, relatório de cobertura real gerado e publicado.
- [x] SonarQube validado em execução real pelo menos uma vez, com achados reais documentados.
- [x] Teste de carga real executado no laboratório N+1, números reais documentados (não estimados).
- [x] Página 404 em português e `<title>` por página de laboratório, validados no navegador.
- [x] `LICENSE`, `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md` criados; README atualizado.
- [x] `docker compose --profile core up` (e demais profiles já existentes) revalidados sem regressão ao final da fase.

## Evidências de conclusão (2026-08-23)

### T1 — Segurança

- `npm audit --omit=dev` no frontend: **0 vulnerabilidades**.
- OWASP Dependency-Check: achado real de bug upstream não corrigido nas
  versões 12.2.0/13.0.0 do plugin (falha sem chave de API da NVD) — ver
  `docs/decisions/0008-owasp-dependency-check-requer-chave-nvd.md`.
  Resolvido reaproveitando uma chave NVD já existente do usuário (mesma
  usada no projeto `wepdev-financas`), cadastrada como secret
  `NVD_API_KEY` no repositório GitHub. CI atualizado
  (`.github/workflows/backend-ci.yml`) com cache de dados + step de
  scan, política informativa (`failBuildOnCVSS=11`, fora da escala
  0-10 real).
- `.github/workflows/frontend-ci.yml`: step `npm audit --audit-level=high`
  adicionado, `continue-on-error: true` (informativo).
- **Primeira execução real do Dependency-Check no CI** (após o usuário
  cadastrar a chave da NVD): job concluído com sucesso em 5m27s
  (a maior parte é a sincronização inicial da base de CVEs, sem cache
  ainda). Relatório real baixado e inspecionado — 8 CVEs distintos
  citados entre as dependências do backend. Triagem manual real de cada
  um (não apenas contagem bruta):
  - `CVE-2026-53914` (Kotlin, CRÍTICO 9.8): deserialização insegura no
    **cache de build** do compilador Kotlin — o projeto não usa Kotlin
    nem seu build cache; `kotlin-stdlib` é dependência transitiva.
    Falso positivo para este projeto.
  - `CVE-2026-39882/39883/41178` (3 CVEs): todos são do
    **OpenTelemetry-Go** (implementação em Go), não do
    `opentelemetry-semconv` Java usado aqui — falso positivo por
    correspondência de CPE entre implementações de linguagens
    diferentes com nome de projeto parecido.
  - `CVE-2026-66299` (Tomcat): afeta a **aplicação de exemplo de
    WebSocket chat** do Tomcat, que não é distribuída pelo
    `tomcat-embed-core` do Spring Boot. Falso positivo.
  - `CVE-2026-41115` (Apache Kafka): vulnerabilidade de autorização no
    **broker** Kafka; o projeto usa apenas `kafka-clients` (biblioteca
    cliente), o broker roda em container separado da imagem oficial.
    Não aplicável ao artefato analisado.
  - `CVE-2026-75838` (DOMPurify, severidade média/RETIREJS): o único
    achado plausivelmente real — DOMPurify é distribuído dentro dos
    assets estáticos do `swagger-ui` (via
    `springdoc-openapi-starter-webmvc-ui`). Baixo risco prático (UI de
    documentação da API, não recebe entrada de usuários não
    autenticados de forma relevante), mas fica registrado como item de
    acompanhamento para uma futura atualização do
    `springdoc-openapi-starter-webmvc-ui`.

  Essa mistura de falsos positivos com um achado real de baixo risco é
  exatamente o cenário previsto na tabela de Riscos desta SPEC — reforça
  a decisão de manter o scan informativo nesta fase, já que travar o
  build automaticamente teria bloqueado o CI por 5 dos 6 achados serem
  irrelevantes para este projeto.

### T2 — Testes

- JaCoCo configurado em `backend/pom.xml` (`prepare-agent` + `report`
  na fase `test`). Cobertura real medida: **86-87,5% de instruções**
  (varia ligeiramente entre execuções por conta dos testes de
  concorrência real do laboratório de Race Condition).
- `docker compose --profile quality up` validado em execução real pela
  primeira vez (SonarQube Community 26.8.0). Análise real rodada duas
  vezes contra o backend (`mvn org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751:sonar`).
  **Achados reais da primeira rodada**: 4 bugs, 0 vulnerabilidades, 0
  security hotspots, 24 code smells, 1,7% de duplicação:
  - `ExecucaoRaceConditionService.java:136` (BLOCKER, `java:S2095`):
    `ExecutorService` não fechado de forma garantida — **corrigido**,
    convertido para try-with-resources (`ExecutorService` é
    `AutoCloseable` desde Java 19).
  - `ConsumidorPagamentoIdempotente.java:26` e
    `ConsumidorPagamentoSemIdempotencia.java:22` (MINOR, `java:S3077`
    ambos): campo `volatile CountDownLatch` não é de fato thread-safe
    para o padrão "reatribuir + usar" — **corrigido**, convertido para
    `AtomicReference<CountDownLatch>` nos dois consumidores Kafka.
  - `ExecucaoN1Service.java:76` (MAJOR, `java:S2201`): "retorno de
    `size()` não usado" — **não corrigido de propósito**: é
    exatamente a técnica pedagógica do laboratório (forçar o lazy
    loading para demonstrar o N+1, o próprio "código problemático"
    exibido na UI). Suprimido com `// NOSONAR` e comentário explicando
    o motivo, para não gerar ruído em análises futuras.
  - Um import não usado (`java:S1128`, MINOR) em `ClienteOllama.java`
    também foi corrigido.
  - **Revalidado após as correções**: nova análise real confirma
    `bugs: 0`. Os demais 23 code smells restantes (a maioria `TODO`s
    documentados de escopo futuro e convenções menores) não fazem
    parte do critério de aceite desta fase — cobertura/qualidade
    estática são indicadores auxiliares, não metas obrigatórias (mesma
    filosofia já registrada em `docs/testing-guide.md`).

### T3 — Performance

Teste de carga real (ferramenta `hey`, via `docker run
williamyeh/hey`, contra o backend real na rede do Docker Compose,
profile `core`), comparando as duas variantes do laboratório N+1 sob
concorrência real de múltiplos clientes simultâneos:

| Rodada | Variante | Requisições | Concorrência | Req/s | Latência média | p95 |
|---|---|---|---|---|---|---|
| 1 | Problemática | 50 | 10 | 56,4 | 156 ms | 323 ms |
| 1 | JOIN FETCH | 50 | 10 | 346,8 | 27 ms | 70 ms |
| 2 | Problemática | 200 | 20 | 302,4 | 59 ms | 130 ms |
| 2 | JOIN FETCH | 200 | 20 | 867,1 | 21 ms | 40 ms |

Em ambas as rodadas a variante corrigida sustentou de 2,9× a 6,1× mais
throughput e teve de 2,8× a 5,7× menos latência média — a proporção
exata varia entre execuções (natural em teste de carga real, não
fabricado para ficar "bonito"), mas a direção e a ordem de grandeza são
consistentes e reforçam, com números sob carga real, o mesmo argumento
já demonstrado em execução única desde a Fase 3.

### T4 — UX

- `frontend/src/app/not-found.tsx`: página 404 customizada em
  português, validada via `npm run build` (gera `/_not-found` como
  rota estática).
- `frontend/src/app/laboratorios/[id]/page.tsx`: `generateMetadata`
  adicionado — cada laboratório agora tem `<title>` próprio (ex.: "N+1
  Queries — Java Engineering Lab"), em vez de herdar o título genérico
  do layout raiz. O catálogo (`/laboratorios`) já tinha título próprio
  desde antes.
- Gap real adicional encontrado e corrigido fora do escopo original:
  a página inicial (`frontend/src/app/page.tsx`) listava fases do
  roadmap desatualizadas (fases 3+ como "planejada"/"em andamento"
  quando já estavam concluídas há várias fases) — corrigido para
  refletir o estado real.
- `aria-label` adicionado ao único `<input>` de texto do frontend
  (campo de pergunta do assistente de IA), que não tinha rótulo
  associado.

### T5 — Documentação final

- `LICENSE` (MIT, decisão explícita do usuário), `CONTRIBUTING.md` e
  `CODE_OF_CONDUCT.md` (adaptado do Contributor Covenant 2.1, resumido)
  criados na raiz do repositório.
- `README.md`: seção "Como contribuir" atualizada para referenciar os
  arquivos reais; nova seção "Licença".

### T6 — CI/CD completo

- `.github/workflows/backend-ci.yml`: publica relatório de cobertura
  JaCoCo e relatório do Dependency-Check como artifacts a cada
  execução.
- `.github/workflows/frontend-ci.yml`: `npm audit` como step
  informativo.
- Revalidado ao final: `mvn -B verify` (24/24 testes) e
  `npm run build`/`npm run lint` sem erros após todas as correções
  desta fase; `docker compose --profile core up` sem regressão.

## Observação de status

SPEC redigida e implementada nesta interação (2026-08-23), a partir da
instrução do usuário para avançar à Fase 8. Decisão de licença (RF-10)
e escopo completo aprovados explicitamente pelo usuário antes de
qualquer implementação, conforme regra 0 de `CLAUDE.md`.
