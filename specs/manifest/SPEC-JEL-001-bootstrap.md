# SPEC-JEL-001 — Bootstrap e Governança (Fase 0)

- **Status**: Em execução (esta SPEC documenta a própria Fase 0; concluída
  ao final desta interação, pendente de aprovação do usuário para avançar
  à Fase 1)
- **Título**: Governança inicial, histórico de conversas, instruções
  persistentes e estrutura documental do repositório
- **Relacionadas**: `SPEC-JEL-002` (arquitetura), `SPEC-JEL-003` (MVP),
  `SPEC-LAB-N1-001` (laboratório N+1)

## Contexto

O Java Engineering Lab deve ser construído com Spec-Driven Development,
histórico de conversas versionado, convenções de idioma e commit
consistentes, e nenhuma implementação funcional antes de aprovação. Esta
SPEC cobre exclusivamente a Fase 0 (governança e descoberta) descrita no
prompt mestre do projeto (registrado integralmente em
`docs/conversation-history.md`, interação de 2026-08-22).

## Problema

Sem um mecanismo de histórico confiável e sem instruções persistentes
carregadas automaticamente em toda sessão, decisões e contexto se perderiam
entre conversas, e a regra "SPEC antes de implementação" dependeria apenas
de memória humana.

## Objetivo

1. Detectar a versão instalada do Claude Code e os mecanismos oficiais de
   hooks e instruções persistentes disponíveis nela.
2. Criar `docs/conversation-history.md` como registro append-only.
3. Implementar um mecanismo de registro de conversas compatível com Windows
   e Linux, sem dependências desnecessárias.
4. Criar `CLAUDE.md` com as regras permanentes do projeto.
5. Criar a estrutura inicial de `specs/`, `docs/` e `diagrams/`.
6. Propor arquitetura, estrutura de repositório e roadmap iniciais.
7. Criar as SPECs de bootstrap, arquitetura, MVP e do laboratório N+1.
8. Não implementar backend, frontend ou laboratórios funcionais.

## Escopo

- Estrutura de diretórios de documentação e specs.
- Arquivo de histórico de conversas e seu mecanismo de registro.
- Instruções persistentes (`CLAUDE.md`).
- Documentos normativos iniciais: README, arquitetura, roadmap, segurança,
  observabilidade, testing-guide, links, manifesto, ADRs iniciais.
- Diagramas iniciais (C4 Contexto/Contêiner, fluxo de laboratório).
- Configuração de repositório Git local (sem push remoto).

## Fora de escopo (explicitamente adiado)

- Qualquer código-fonte funcional de backend (Java/Spring Boot).
- Qualquer código-fonte funcional de frontend (Next.js/React).
- `docker-compose.yml` e demais artefatos de infraestrutura executável.
- CI/CD (GitHub Actions) — previsto para a Fase 1.
- Implementação do laboratório de N+1 — apenas a SPEC é criada agora.

## Descoberta técnica (Claude Code 2.1.240)

Verificado via `claude --version` (retornou `2.1.240 (Claude Code)`) e
consulta à documentação oficial:

- **Hooks disponíveis** incluem, entre outros, `SessionStart`, `SessionEnd`,
  `UserPromptSubmit`, `Stop`, `PreToolUse`, `PostToolUse`, `PreCompact`.
  Relevantes para este mecanismo: `UserPromptSubmit` (dispara ao enviar uma
  mensagem) e `Stop` (dispara ao final da resposta do Claude).
- **Limitação confirmada**: o payload JSON recebido via stdin por esses
  hooks contém `session_id`, `prompt_id`, `transcript_path`, `cwd`,
  `permission_mode` e `hook_event_name` — **não** contém o texto bruto do
  prompt ou da resposta. Obtê-los exigiria ler e interpretar o transcript
  JSONL apontado por `transcript_path`.
- **Configuração de hooks**: `.claude/settings.json` (versionável) ou
  `.claude/settings.local.json` (local, não versionado), com a chave
  `hooks.<Evento>[].hooks[]` contendo `type: "command"`, `command`, `args`
  e `timeout`. A variável de ambiente `${CLAUDE_PROJECT_DIR}` resolve para
  a raiz do projeto.
- **Instruções persistentes**: `CLAUDE.md` na raiz do projeto é carregado
  automaticamente no início de toda sessão nesse diretório. É o mecanismo
  oficial e correto para as regras permanentes do projeto.

### Decisão de design do mecanismo de histórico — PROPOSTA

Dada a limitação acima, escrever automaticamente o conteúdo da conversa a
partir do hook criaria um segundo escritor concorrente do arquivo de
histórico (além do próprio Claude), com risco real de duplicidade,
divergência de formatação ou corrupção — o que viola a regra de "append
seguro" e "proteção contra corrupção" exigida para este mecanismo.

Por isso, o mecanismo adotado (marcado como **PROPOSTA**, sujeita a
revisão) é híbrido:

1. **Mecanismo autoritativo (manual, executado pelo próprio Claude)**: em
   todo turno relacionado ao projeto, a primeira ação registra a mensagem
   do usuário integralmente em `docs/conversation-history.md`; a última
   ação, antes de encerrar o turno, registra a resposta final do Claude.
   Esse protocolo está descrito em `CLAUDE.md` e é exatamente o mecanismo
   de fallback previsto no prompt mestre do projeto para quando o registro
   automático exato não é tecnicamente viável.
2. **Rede de segurança automatizada (hooks)**: `scripts/registrar-conversa.js`,
   registrado em `.claude/settings.json` nos eventos `UserPromptSubmit` e
   `Stop`, apenas verifica se `docs/conversation-history.md` cresceu de
   tamanho entre o início e o fim do turno. Se não tiver crescido, emite um
   aviso não bloqueante (código de saída 1, mensagem em stderr) — nunca
   bloqueia a sessão nem escreve conteúdo de conversa.

Esta decisão deve ser revisitada se versões futuras do Claude Code
exponerem o texto bruto do prompt/resposta nos hooks de forma segura para
escrita automática direta.

## Requisitos funcionais

| ID | Requisito |
|---|---|
| RF-01 | `docs/conversation-history.md` deve existir, ser UTF-8 e nunca ser sobrescrito. |
| RF-02 | Cada entrada deve seguir o formato `## Interação YYYY-MM-DD HH:mm:ss` com subseções `### Usuário` e `### Claude`. |
| RF-03 | O hook `UserPromptSubmit`/`Stop` deve avisar (não bloquear) quando o arquivo não crescer durante um turno. |
| RF-04 | `CLAUDE.md` deve carregar automaticamente as regras de idioma, commits, Spec-Driven Development e histórico em toda sessão iniciada nesta pasta. |
| RF-05 | A estrutura `specs/{manifest,architecture,backend,frontend,labs,infra,security,testing}` deve existir. |

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | O mecanismo de histórico não deve depender de ferramentas fora da stack já prevista para o projeto (Node.js, já mandatório para o frontend). |
| RNF-02 | O mecanismo deve funcionar em Windows e Linux sem alteração de script. |
| RNF-03 | Nenhum segredo pode ser gravado em `docs/conversation-history.md`, `.claude/settings.json` ou `.gitignore`. |

## Critérios de aceite

- [x] `claude --version` executado e versão registrada nesta SPEC.
- [x] `docs/conversation-history.md` criado, contendo a interação inicial
      completa (mensagem do usuário na íntegra).
- [x] `scripts/registrar-conversa.js` criado e referenciado em
      `.claude/settings.json`.
- [x] `CLAUDE.md` criado com as regras permanentes.
- [x] Estrutura `specs/` criada com os oito subdiretórios previstos.
- [x] `SPEC-JEL-002`, `SPEC-JEL-003` e `SPEC-LAB-N1-001` criadas.
- [x] README, roadmap, arquitetura, segurança, observabilidade,
      testing-guide e links criados ou atualizados.
- [ ] Resposta final desta interação registrada em
      `docs/conversation-history.md` (ação de encerramento de turno).
- [ ] Aprovação explícita do usuário para iniciar a Fase 1 (bootstrap de
      código).

## Estratégia de testes desta fase

Não há código funcional nesta fase. Validação é documental:

1. Reabrir o arquivo `docs/conversation-history.md` e confirmar
   codificação UTF-8 e integridade do conteúdo.
2. Confirmar, em uma nova sessão do Claude Code neste diretório, que
   `CLAUDE.md` é carregado automaticamente (visível no início da sessão).
3. Validação funcional do hook (`scripts/registrar-conversa.js`) fica
   registrada como **pendente** — só pode ser observada em uma execução
   real subsequente do Claude Code, fora desta primeira sessão de
   bootstrap.

## Segurança

Nenhuma credencial foi criada, solicitada ou versionada nesta fase.
`.gitignore` cobre `.env*`, chaves privadas e arquivos de configuração
local do Claude Code.

## Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Falha humana/agente em seguir o protocolo manual de registro | Histórico incompleto | Hook de aviso (rede de segurança) + checklist em `CLAUDE.md` |
| Divergência entre `docs/conversation-history.md` e a real conversa (paráfrase acidental) | Perda de rastreabilidade | Regra explícita de transcrição integral, sem resumir |
| Hook falhar silenciosamente em ambientes sem Node.js no PATH | Rede de segurança inativa | Script falha em modo seguro (não bloqueia a sessão); dependência de Node já é mandatória pela stack do projeto |

## Dependências

- Node.js (já disponível no ambiente: v24.13.1) — usado apenas pelo script
  de verificação, sem novas dependências de pacote.
- Git (disponível: 2.53.0.windows.1) — repositório local inicializado
  (`git init`), ainda sem commit nem remoto configurado.

## Decisões (resumo)

Ver `docs/decisions/0001-mecanismo-hibrido-de-historico.md` e demais ADRs
em `docs/decisions/` para o registro formal e justificativas.

## Evidências de conclusão

- Estrutura de arquivos criada nesta interação (ver relatório final desta
  interação em `docs/conversation-history.md`).
- Saída de `claude --version`: `2.1.240 (Claude Code)`.
- Saída de `git --version`: `2.53.0.windows.1`.
- Saída de `node --version`: `v24.13.1`.
