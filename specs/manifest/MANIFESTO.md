# Manifesto — Java Engineering Lab

- **Status**: Proposto (aguardando aprovação)
- **Tipo**: Documento de governança (não é uma SPEC de implementação)

## 1. O que é o Java Engineering Lab

Uma plataforma web educacional e interativa para demonstrar problemas reais
e recorrentes em aplicações Java/Spring — não um CRUD, mas um **laboratório
interativo de Engenharia de Software**. Cada laboratório leva o usuário de
"conhecer o problema" a "explicar o problema em uma entrevista técnica",
passando por reprodução, observação, diagnóstico, solução e comparação
antes/depois.

O projeto é simultaneamente:

1. uma ferramenta de estudo/portfólio técnico;
2. um exercício real de Spec-Driven Development, com histórico de decisões
   auditável desde o primeiro commit.

## 2. Princípios não negociáveis

1. **Sem SPEC, sem implementação.** Toda funcionalidade relevante nasce de
   uma SPEC aprovada antes de qualquer código ser escrito.
2. **Português como idioma padrão do que é nosso.** Código, testes, banco
   de dados, APIs, documentação e commits usam português do Brasil sempre
   que isso não prejudicar interoperabilidade técnica. Termos impostos por
   linguagens, frameworks, protocolos e padrões consolidados (Java, Spring
   Boot, HTTP, JSON, `@Entity`, etc.) permanecem no idioma original.
3. **Rastreabilidade total.** Toda decisão arquitetural relevante tem
   justificativa registrada (ADR em `docs/decisions/`). Toda conversa fica
   registrada em `docs/conversation-history.md`.
4. **Métricas reais são reais.** Números apresentados como resultado de
   execução vêm de execução real. Valores simulados/estimados são
   identificados como tal — nunca apresentados como benchmark real.
5. **Sem engenharia antecipatória.** Não criamos abstrações, camadas,
   serviços ou infraestrutura para necessidades hipotéticas. Arquitetura
   evolutiva ≠ arquitetura antecipatória.
6. **Segurança por padrão.** Nenhuma credencial, token ou segredo é
   versionado, logado ou exposto — nem em código, nem em documentação, nem
   no histórico de conversas.
7. **Documentação evolui com o código.** Uma mudança de contrato de API ou
   de comportamento sem atualização da documentação correspondente é uma
   mudança incompleta.
8. **Decisão não aprovada é proposta.** Durante a fase de descoberta, tudo
   o que não tiver aprovação explícita do usuário é marcado como
   `PROPOSTA`, `HIPÓTESE` ou `PENDENTE DE APROVAÇÃO`.

## 3. Convenção de idioma — referência rápida

| Categoria | Idioma | Exemplos |
|---|---|---|
| Classes, métodos, variáveis, DTOs, exceptions próprias | Português | `PedidoService`, `buscarPedidoPorId`, `PedidoNaoEncontradoException` |
| Testes e nomes de métodos de teste | Português | `deveBuscarPedidosComItensSemGerarNMaisUm` |
| Tabelas, colunas, dados de demonstração | Português | `pedido`, `item_pedido`, `data_criacao` |
| Rotas de API próprias | Português | `/api/pedidos`, `/api/laboratorios` |
| Frameworks, protocolos, palavras reservadas, nomes exigidos por bibliotecas | Original (inglês/técnico) | `Spring Boot`, `@Entity`, `HTTP`, `JOIN FETCH`, `EntityGraph` |

Princípio-guia: **português sempre que tecnicamente apropriado; inglês
apenas quando exigido por interoperabilidade ou convenção técnica
consolidada.**

## 4. Critério de sucesso de um laboratório

Um laboratório só é considerado bem-sucedido quando o usuário consegue
percorrer o fluxo completo:

```text
ver o problema → reproduzir → observar → entender a causa →
conhecer soluções → aplicar uma solução → comparar → entender
trade-offs → explicar o conceito
```

Código e documentação sem essa experiência não cumprem o objetivo central
do projeto.

## 5. Escopo deliberadamente fora do MVP

Não implementaremos antecipadamente: Kubernetes, service mesh, múltiplos
microsserviços, Transactional Outbox (fica como laboratório futuro — ver
`docs/roadmap.md`), ou qualquer feature fora do que as SPECs aprovadas
definirem como MVP.

## 6. Documento vivo

Este manifesto é referenciado por `CLAUDE.md` e pelas SPECs. Mudanças nos
princípios aqui descritos devem ser propostas, discutidas com o usuário e
registradas com justificativa antes de valerem como regra vigente.
