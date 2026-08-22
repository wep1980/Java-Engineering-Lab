# ADR-0001 — Mecanismo híbrido de registro do histórico de conversas

- **Status**: Proposta (pendente de aprovação do usuário)
- **Data**: 2026-08-22
- **SPEC relacionada**: `specs/manifest/SPEC-JEL-001-bootstrap.md`

## Contexto

O projeto exige um registro append-only e íntegro de toda conversa em
`docs/conversation-history.md`. Investigação nos hooks do Claude Code
2.1.240 (`UserPromptSubmit`, `Stop`) mostrou que o payload recebido via
stdin não contém o texto bruto do prompt nem da resposta — apenas
`transcript_path`, que exigiria parsing de um JSONL para reconstrução do
conteúdo.

## Decisão

Adotar um mecanismo híbrido:

1. O próprio Claude registra manualmente a mensagem do usuário (primeira
   ação do turno) e sua resposta final (última ação do turno), conforme
   protocolo descrito em `CLAUDE.md`.
2. Um hook em `UserPromptSubmit`/`Stop` (`scripts/registrar-conversa.js`)
   atua apenas como verificação: compara o tamanho do arquivo de histórico
   antes e depois do turno e emite um aviso não bloqueante se ele não
   cresceu.

## Alternativas consideradas

- **Escrita automática via parsing do transcript no hook `Stop`.**
  Rejeitada nesta fase: dois escritores concorrentes do mesmo arquivo
  (hook + Claude) criam risco real de duplicidade e corrupção, e o formato
  exigido pelo projeto (transcrição integral, sem resumir) é difícil de
  garantir a partir de um transcript JSONL genérico sem risco de
  divergência.
- **Hook bloqueante (`exit code 2`) impedindo o Claude de encerrar o turno
  sem registrar.** Rejeitada por risco de travar sessões em falsos
  positivos (ex.: falha de I/O momentânea, delay de filesystem), o que
  teria maior custo (sessão travada) do que o benefício (garantia extra
  de registro).

## Consequências

- O registro depende de disciplina do agente Claude em seguir o protocolo
  documentado em `CLAUDE.md` — mitigado pelo aviso automático do hook.
- Se versões futuras do Claude Code exponerem o texto bruto do prompt/
  resposta de forma segura para escrita automática, esta decisão deve ser
  revisitada (ver seção de decisões da `SPEC-JEL-001`).
