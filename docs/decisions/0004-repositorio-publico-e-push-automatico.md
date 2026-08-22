# ADR-0004 — Repositório público no GitHub e commits/pushes automáticos

- **Status**: Aceita (instrução explícita do usuário em 2026-08-22)
- **Data**: 2026-08-22

## Contexto

O usuário pediu explicitamente: "acesse meu github crie o projeto, deixe
ele publico, e faça sempre os commits e pushs". Isso muda o comportamento
padrão do projeto em dois pontos:

1. O repositório é público (`https://github.com/wep1980/Java-Engineering-Lab`),
   visível a qualquer pessoa.
2. Commits e pushes passam a ser feitos como parte normal do fluxo de
   trabalho, sem precisar de confirmação a cada vez — isso é uma
   autorização durável para *este projeto*, dada de forma explícita pelo
   usuário nesta conversa (registrada integralmente em
   `docs/conversation-history.md`).

## Decisão

1. Repositório criado como **público** via `gh repo create --public`.
2. A partir de agora, mudanças aprovadas pelo usuário são commitadas
   (Conventional Commits, em português) e enviadas com `git push` sem
   pedir confirmação adicional a cada vez — mantendo, porém, todas as
   demais salvaguardas: nunca usar `--force` para `main`/`master`, nunca
   pular hooks, nunca commitar segredos, e sempre revisar o que está
   sendo staged antes de commitar.
3. **Identidade do committer**: como este ambiente nunca teve
   `user.name`/`user.email` configurados globalmente no Git, e alterar a
   configuração do Git está fora de cotação (regra de segurança do
   agente), os commits usam as variáveis de ambiente
   `GIT_AUTHOR_NAME`/`GIT_AUTHOR_EMAIL`/`GIT_COMMITTER_NAME`/`GIT_COMMITTER_EMAIL`
   (não persistem em nenhum arquivo de configuração) com o nome e o
   e-mail público do perfil GitHub autenticado (`gh api user`), para que
   os commits fiquem corretamente atribuídos à conta `wep1980` no GitHub.

## Consequências

- Como o repositório é público, nenhum segredo pode ser commitado —
  reforça (não substitui) as práticas já descritas em `docs/security.md`.
- Cada sessão futura do Claude Code precisa repetir a definição das
  variáveis de ambiente de identidade ao commitar (elas não persistem
  entre sessões, propositalmente — não gravamos identidade em
  configuração global).
- Esta autorização vale para o fluxo normal (commit + push de mudanças
  aprovadas). Ações destrutivas ou irreversíveis (force-push, reset
  --hard, exclusão de branch) continuam exigindo confirmação explícita,
  como já vale por padrão.
