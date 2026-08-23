# Como contribuir

Obrigado pelo interesse no Java Engineering Lab. Este é, antes de tudo,
um projeto pessoal de aprendizado e portfólio — mas contribuições
pontuais são bem-vindas, respeitando o processo já em uso no projeto.

## Antes de abrir um Pull Request

- **Novos laboratórios ou mudanças de arquitetura relevantes** seguem
  Spec-Driven Development: abra uma *issue* descrevendo a proposta antes
  de escrever código. Nenhuma SPEC nova é aceita sem alinhamento prévio
  com o mantenedor. Ver `specs/manifest/MANIFESTO.md` para o processo
  completo.
- **Correções pontuais** (bug fix, typo, ajuste de documentação) podem
  ir direto para um Pull Request, sem SPEC prévia.
- Todo código próprio (classes, métodos, variáveis, testes, commits) é
  em português do Brasil — ver a seção de idioma do
  `specs/manifest/MANIFESTO.md`. Termos impostos por frameworks/
  protocolos (`@Entity`, `HTTP`, `JSON`, etc.) permanecem no idioma
  original.
- Commits seguem Conventional Commits (`feat:`, `fix:`, `test:`,
  `docs:`, `refactor:`, `chore:`), em português.

## Rodando o projeto localmente

Ver a seção "Como executar" do [`README.md`](README.md).

## Testes

Nenhuma mudança funcional é aceita sem os testes correspondentes
passando (`mvn test` no backend, `npm run lint`/`npm run build` no
frontend). Métricas e resultados de laboratórios são sempre reais,
nunca simulados ou fabricados — ver `docs/testing-guide.md` para o
padrão de validação esperado.

## Dúvidas

Abra uma *issue* no repositório:
https://github.com/wep1980/Java-Engineering-Lab/issues
