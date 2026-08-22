# Segurança — Java Engineering Lab

> Diretrizes vigentes desde a Fase 0. Requisitos de segurança que exigirem
> tratamento formal próprio (ex.: autenticação/autorização multiusuário)
> ganham uma SPEC dedicada em `specs/security/` quando existirem.

## Credenciais e segredos

- Nenhuma credencial, token, senha, chave privada ou connection string com
  senha é versionada no repositório, registrada em
  `docs/conversation-history.md`, ou logada pela aplicação.
- Configuração sensível usa variáveis de ambiente. `.env.example` (na raiz
  do repositório, sem valores reais de produção) documenta todas as
  variáveis usadas pelo `docker-compose.yml` desde a Fase 1.
- Se uma mensagem do usuário parecer conter uma credencial real que seria
  persistida no histórico, o Claude deve alertar antes de gravar e
  substituir o valor por `[VALOR SENSÍVEL REMOVIDO]`.

## `.gitignore`

Cobre desde a Fase 0: arquivos `.env*`, chaves (`*.pem`, `*.key`),
configuração local do Claude Code (`.claude/settings.local.json`,
`.claude/.jel-history-check.json`) e artefatos de build de backend/
frontend que ainda não existem mas serão gerados a partir da Fase 1.

## Padrão de erros da API (proposta, formalizada em `SPEC-JEL-002`)

Respostas de erro incluem código, mensagem, timestamp, caminho e
correlation ID. Nunca incluem stack trace, detalhes internos de
implementação, credenciais ou segredos.

## Dependências

A partir da Fase 1, avaliar uma ferramenta de detecção de CVEs em
dependências (ex.: OWASP Dependency-Check para o backend Maven, `npm audit`
para o frontend) — decisão formal registrada em ADR quando a Fase 1 for
iniciada, para evitar redundância de ferramentas sem justificativa.

## Autenticação/autorização

Fora do escopo do MVP (`SPEC-JEL-003`). Spring Security entra apenas
quando um requisito concreto exigir (ex.: multiusuário, ou proteção de
endpoints administrativos como o de reset de dados de demonstração).

## OWASP

Práticas de validação de entrada, princípio do menor privilégio e logs
seguros (sem dados sensíveis) se aplicam desde o primeiro endpoint
implementado, ainda que não haja autenticação no MVP.
