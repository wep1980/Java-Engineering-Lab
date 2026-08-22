# Java Engineering Lab — Instruções Persistentes

Estas instruções são carregadas automaticamente em toda sessão do Claude Code
neste repositório. Elas resumem as regras vindas do prompt mestre do projeto,
registrado integralmente em `docs/conversation-history.md` (interação de
2026-08-22) e detalhado em `specs/manifest/`.

## 0. Regra crítica de fase atual

O projeto está em **Fase 0 — Governança e Descoberta**. Nenhuma implementação
funcional de backend, frontend ou laboratórios deve começar sem aprovação
explícita do usuário às propostas em `specs/`. Consulte
`specs/manifest/SPEC-JEL-001-bootstrap.md` antes de assumir que uma fase foi
liberada.

## 1. Spec-Driven Development (obrigatório)

Nenhuma implementação relevante começa sem uma SPEC correspondente em
`specs/`, aprovada pelo usuário. Fluxo obrigatório: SPEC → implementação →
testes → validação → documentação → evidências. Decisões sem aprovação
explícita devem ser marcadas como `PROPOSTA`, `HIPÓTESE` ou
`PENDENTE DE APROVAÇÃO` — nunca tratadas como definitivas.

## 2. Idioma oficial: português do Brasil

Use português para código próprio (classes, métodos, variáveis, DTOs,
testes, exceptions, componentes, etc.), banco de dados, APIs próprias,
documentação, commits e comunicação. Mantenha em inglês apenas o que é
imposto por linguagens, frameworks, protocolos e padrões consolidados
(ex.: `Spring Boot`, `@Entity`, `HTTP`, `JSON`, nomes de métodos exigidos
por frameworks). Ver `specs/manifest/MANIFESTO.md` seção de idioma para
exemplos completos.

## 3. Commits

Sempre em português, seguindo Conventional Commits
(`feat:`, `fix:`, `test:`, `docs:`, `refactor:`, `chore:`). Nunca commitar
sem que o usuário peça explicitamente.

## 4. Histórico de conversas — `docs/conversation-history.md`

Arquivo append-only, nunca reescrito ou apagado. Protocolo obrigatório em
todo turno relacionado ao projeto:

1. **Primeira ação do turno**: registrar a mensagem do usuário
   **integralmente** (sem resumir, reinterpretar ou remover contexto) em
   uma nova entrada `## Interação YYYY-MM-DD HH:mm:ss`.
2. Executar o trabalho solicitado.
3. **Antes de encerrar o turno**: registrar a resposta final do Claude na
   mesma entrada.

Um hook `UserPromptSubmit`/`Stop` (`scripts/registrar-conversa.js`,
configurado em `.claude/settings.json`) atua apenas como **rede de
segurança não autoritativa**: ele verifica se o arquivo cresceu durante o
turno e emite um aviso não bloqueante caso não tenha crescido. Ele **não**
escreve o conteúdo da conversa — essa é responsabilidade do Claude, pois os
hooks desta versão do Claude Code não expõem o texto bruto do prompt/
resposta, apenas o caminho do transcript. Ver justificativa completa no
cabeçalho do script e em `specs/manifest/SPEC-JEL-001-bootstrap.md`.

Nunca registrar credenciais, tokens, senhas ou segredos no histórico —
substituir por `[VALOR SENSÍVEL REMOVIDO]` e alertar o usuário antes de
persistir.

## 5. Segurança

Nunca versionar credenciais, tokens, chaves ou segredos. Usar variáveis de
ambiente e `.env.example`. Ver `docs/security.md`.

## 6. Documentação acompanha o código

Toda mudança relevante em código ou contrato de API deve atualizar a
documentação correspondente (`docs/`, OpenAPI/Swagger, SPEC afetada).

## 7. Simplicidade

Não criar abstrações, camadas, dependências ou infraestrutura sem
necessidade real e justificada. Ver princípios completos em
`specs/manifest/MANIFESTO.md`.

## 8. Checklist de encerramento de turno

Antes de encerrar qualquer interação relacionada ao projeto, confirmar:

```text
[ ] Mensagem do usuário registrada em docs/conversation-history.md
[ ] Resposta do Claude registrada em docs/conversation-history.md
[ ] Histórico anterior preservado (nada sobrescrito)
[ ] UTF-8 preservado
[ ] Nenhuma credencial exposta
[ ] SPEC respeitada (ou proposta de SPEC criada, se aplicável)
[ ] Documentação afetada atualizada
[ ] Testes executados quando aplicável
```

## Referências

- Manifesto e princípios: `specs/manifest/MANIFESTO.md`
- SPEC de bootstrap: `specs/manifest/SPEC-JEL-001-bootstrap.md`
- Arquitetura: `specs/architecture/SPEC-JEL-002-arquitetura.md`
- Roadmap: `docs/roadmap.md`
- Decisões arquiteturais: `docs/decisions/`
