# ADR-0003 — Esquema de identificação das SPECs

- **Status**: Proposta (pendente de aprovação do usuário)
- **Data**: 2026-08-22

## Contexto

O prompt mestre sugere IDs como `SPEC-JEL-001`, `SPEC-JEL-002`,
`SPEC-LAB-N1-001`, `SPEC-LAB-RACE-001`, `SPEC-LAB-KAFKA-IDEMP-001`, sem
definir formalmente o esquema completo.

## Decisão

Adotar dois prefixos:

- **`SPEC-JEL-NNN`**: specs de plataforma (governança, arquitetura, MVP,
  módulos transversais como o Engineering AI Assistant). Numeração
  sequencial simples, `001`, `002`, `003`, ...
- **`SPEC-LAB-<CODIGO>-NNN`**: specs de laboratórios individuais, onde
  `<CODIGO>` é um identificador curto e estável do problema (ex.: `N1` para
  N+1, `RACE` para race condition/lost update, `KAFKA-IDEMP` para
  idempotência de mensagens, `OUTBOX` para transactional outbox). Cada
  laboratório recomeça sua numeração em `001` (permite `SPEC-LAB-N1-002`
  para uma revisão futura do mesmo laboratório, se necessário).

Arquivos ficam nomeados como `specs/<categoria>/SPEC-<ID>-<slug-em-portugues>.md`,
por exemplo `specs/labs/SPEC-LAB-N1-001-n-mais-um-queries.md`.

## Consequências

- Novos laboratórios do backlog (seção 28 do prompt mestre) já têm um
  padrão claro de nomeação a seguir sem nova decisão.
- `SPEC-JEL-` fica reservado para o núcleo da plataforma, evitando colisão
  de numeração com specs de laboratório.
