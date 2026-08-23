# SPEC-JEL-006 — Engineering AI Assistant

- **Status**: Implementada e validada (2026-08-23) — ver "Evidências de
  conclusão" ao final deste documento
- **Título**: Assistente de IA contextualizado por laboratório, com
  provedor abstraído
- **Relacionadas**: `SPEC-JEL-002` (arquitetura), `docs/observability.md`

## Contexto

Seção 29 do prompt mestre: um assistente complementar que recebe contexto
real do laboratório (descrição, execução, métricas, código) e responde
perguntas nesse contexto — não um chatbot genérico solto na tela. Seção
30: evitar acoplamento a um único provedor; avaliar abstração compatível
com a stack Java; credenciais de IA nunca versionadas.

**Decisão do usuário**: provedor local via Ollama — sem custo por token,
sem chave de API externa, mas com qualidade de resposta menor que um
provedor comercial e latência maior em CPU. Essa é uma escolha
explicitamente aprovada, não uma suposição.

## Objetivo

1. Definir uma abstração de provedor de IA no backend (interface), para
   trocar de provedor no futuro sem reescrever os consumidores.
2. Implementar a variante concreta para Ollama.
3. Endpoint que recebe uma pergunta e, opcionalmente, o resultado da
   última execução do laboratório (contexto real, vindo da tela),
   monta um prompt com conhecimento do laboratório + esse contexto, e
   retorna a resposta do modelo.
4. Painel de pergunta/resposta no frontend, embutido nos três
   laboratórios já implementados.
5. A plataforma continua funcionando por completo sem este recurso — se
   o Ollama não estiver acessível, os laboratórios em si não são afetados,
   apenas o assistente fica indisponível (503 com mensagem clara).

## Escopo

- Interface `AssistenteIA` (pacote `assistente`, novo, paralelo a
  `plataforma` e `laboratorios`) com um único método de perguntar.
- Implementação `ClienteOllama`, via `RestClient`, chamando
  `POST /api/generate` do Ollama (`stream: false`).
- Conhecimento condensado por laboratório (`ConhecimentoLaboratorios`) —
  não duplica todo o conteúdo do frontend, só o suficiente para
  fundamentar respostas (problema, causa, soluções, trade-offs).
- Novo serviço `ollama` no `docker-compose.yml` (profile `ai`), com um
  segundo serviço auxiliar que baixa o modelo na primeira subida.
- Endpoint `POST /api/laboratorios/{id}/assistente/perguntas`.
- Painel de chat simples no frontend (pergunta, resposta, histórico da
  sessão em memória — sem persistência).

## Fora de escopo

- Múltiplos provedores implementados simultaneamente (só Ollama agora;
  a interface existe para permitir adicionar outro depois, sem reescrever
  os consumidores).
- Histórico de conversa persistido em banco.
- Streaming de resposta (token a token) — resposta única, de uma vez.
- Rate limiting / autenticação do endpoint (sem multiusuário no MVP,
  como já definido em `SPEC-JEL-003`).

## Requisitos funcionais

| ID | Requisito |
|---|---|
| RF-01 | `POST /api/laboratorios/{id}/assistente/perguntas` recebe `{pergunta, ultimoResultado?}` e retorna `{resposta}`. |
| RF-02 | O prompt enviado ao modelo inclui: nome/objetivo do laboratório (do catálogo real), conhecimento condensado do laboratório, e o `ultimoResultado` da execução (se fornecido pelo frontend). |
| RF-03 | Se o Ollama estiver inacessível, o endpoint responde `503` com mensagem clara (reaproveitando `LaboratorioIndisponivelException`), sem afetar os demais endpoints do laboratório. |
| RF-04 | Painel de assistente presente nas três páginas de laboratório existentes (N+1, Race Condition, Kafka/Idempotência), incluindo automaticamente o resultado da última execução feita na mesma página como contexto. |

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | Nenhuma credencial de IA é necessária (Ollama local) nem versionada. |
| RNF-02 | Timeout generoso na chamada ao Ollama (inferência em CPU pode levar dezenas de segundos, especialmente na primeira chamada após o modelo carregar). |
| RNF-03 | A abstração (`AssistenteIA`) não vaza detalhes específicos do Ollama para os consumidores (controller/frontend) — trocar de provedor no futuro não deve exigir mudança de contrato HTTP. |

## Design técnico — decisões

### Modelo escolhido: `llama3.2:3b`

Equilíbrio entre qualidade de resposta e viabilidade de rodar em CPU
dentro do Docker Desktop, sem GPU dedicada. Modelos menores (1B)
tendem a divagar mais em respostas técnicas explicativas; modelos muito
maiores (8B+) seriam lentos demais em CPU para uma demonstração
interativa. Decisão de implementação, não uma exigência da SPEC —
documentada aqui para rastreabilidade, revisável se a qualidade real
observada não for satisfatória.

### Contexto vem do frontend, não de persistência nova no backend

O backend não persiste resultados de execução (decisão já tomada nas
SPECs de laboratório — cada execução é efêmera, computada sob demanda).
Em vez de criar armazenamento novo só para o assistente, o frontend
envia o `ultimoResultado` que já tem em memória (o mesmo objeto exibido
no painel de execução) junto com a pergunta. Isso mantém o backend sem
estado adicional e o contexto sempre reflete exatamente o que o usuário
está vendo na tela.

## Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Modelo pequeno (3B) responder de forma imprecisa ou genérica demais | Reduz o valor educacional do recurso | Conhecimento condensado por laboratório no prompt (grounding), reduzindo dependência do conhecimento geral do modelo; recurso é complementar, não a fonte primária de ensino |
| Download do modelo (~2GB) demorar ou falhar na primeira subida | Assistente indisponível até o download terminar | Serviço auxiliar dedicado para o pull, documentado em `docs/links.md`; endpoint responde 503 claro enquanto isso |
| Latência alta em CPU | Experiência de usuário ruim | Timeout generoso + indicador de carregamento no frontend |

## Critérios de aceite

- [x] Interface `AssistenteIA` implementada e usada pelo controller (não o cliente Ollama diretamente).
- [x] `docker compose --profile core --profile ai up` sobe backend + Ollama, com o modelo baixado.
- [x] Uma pergunta real, com o resultado de uma execução real de laboratório como contexto, recebe uma resposta real do modelo (não fabricada).
- [x] Sem o profile `ai`, os laboratórios continuam funcionando normalmente; o endpoint do assistente responde 503.
- [x] Painel de assistente funcional nas três páginas de laboratório, testado no navegador.

## Evidências de conclusão (2026-08-23)

- **Suíte de testes do backend**: 24/24 testes passando (`mvn test`),
  incluindo os 3 novos testes de `AssistenteIAControllerTest` (200 com
  contexto, 400 para pergunta em branco, 503 quando o provedor está
  indisponível).
- **Build e lint do frontend**: `npm run build` e `npm run lint` sem
  erros, com os três painéis de laboratório (N+1, Race Condition,
  Kafka/Idempotência) recompilados após a refatoração que eleva o
  estado de `ultimoResultado` para o componente pai e o repassa ao
  `PainelAssistenteIA`.
- **Download real do modelo**: `curl http://localhost:11434/api/tags`
  confirmou `llama3.2:3b` baixado (2.02 GB) pelo serviço auxiliar
  `ollama-modelo` do `docker-compose.yml`.
- **Pergunta real via `curl`, direto no backend**: execução real do
  laboratório N+1 (versão problemática) retornou 51 queries para 50
  pedidos; a pergunta "Por que essa execução gerou 51 queries para 50
  pedidos?", enviada com esse resultado real como `ultimoResultado`,
  recebeu do Ollama uma resposta que referencia corretamente os números
  reais e o conceito de N+1 queries — não uma resposta fabricada ou
  fixa no backend.
- **Pergunta real via proxy do frontend** (`/api/laboratorios/{id}/assistente/perguntas`
  em Next.js): confirmado que o proxy same-origin encaminha corretamente
  para o backend e retorna a resposta real do modelo.
- **Validação real no navegador** (Chrome, via `claude-in-chrome`): na
  página `/laboratorios/n1-queries`, clique real em "Executar versão
  problemática" produziu 51 queries / 50 pedidos na tela; o painel do
  assistente atualizou automaticamente para "usando o resultado da sua
  última execução como contexto"; a pergunta "Por que 51 queries e nao
  50?", digitada e enviada pela UI, recebeu uma resposta real do modelo
  explicando "1 (consulta inicial) + 50 (consultas adicionais por
  pedido) = 51 consultas" — usando os números reais da execução exibida
  na tela, não valores fabricados.
- **Degradação graciosa (RF-03) validada de verdade**: com os
  containers `ollama`/`ollama-modelo` parados (`docker compose stop`),
  uma nova pergunta ao endpoint do assistente retornou `503` com o
  formato padrão de erro da plataforma
  (`ErroResposta`/`LaboratorioIndisponivelException`), enquanto uma
  execução do laboratório N+1 no mesmo momento continuou respondendo
  `200` normalmente — confirma que a ausência do profile `ai` não afeta
  o restante da plataforma.
- **Ambiente encerrado de forma limpa** ao final da validação
  (`docker compose down`), preservando os volumes nomeados
  (`postgres-dados`, `ollama-dados`) por serem dados persistentes por
  design, não artefatos de teste.

## Observação de status

Implementação concluída e validada nesta interação (2026-08-23), a
partir da aprovação explícita do usuário para a Fase 7 e da escolha de
Ollama como provedor.
