# Histórico de Conversas — Java Engineering Lab

> Este arquivo é o registro cronológico e persistente de toda a interação entre o usuário e o Claude Code relacionada ao projeto **Java Engineering Lab**.
>
> Regras deste arquivo (ver `CLAUDE.md` e `specs/manifest/SPEC-JEL-001-bootstrap.md`):
> - Somente acréscimo (append-only). Nunca reescrever, resumir ou apagar conteúdo anterior.
> - Codificação UTF-8, ordem cronológica.
> - Nenhuma credencial, token, senha ou segredo deve ser registrado. Quando algo assim aparecer, substituir por `[VALOR SENSÍVEL REMOVIDO]`.
> - Este arquivo registra contexto e evolução da conversa. Ele **não substitui** SPECs, ADRs, README ou demais documentos normativos — decisões definitivas também devem ser refletidas no documento normativo correspondente.

---

## Interação 2026-08-22 19:40:07

### Usuário

````markdown
# JAVA ENGINEERING LAB — PROMPT MESTRE DEFINITIVO PARA CLAUDE CODE

Quero que você atue como **Engenheiro de Software Sênior, Arquiteto de Software e agente de desenvolvimento responsável pela criação completa deste projeto**.

O nome provisório do sistema é:

**Java Engineering Lab**

O projeto deverá ser construído utilizando obrigatoriamente uma abordagem de **Spec-Driven Development**, com documentação versionada, decisões rastreáveis, testes automatizados, qualidade de código, segurança, observabilidade, desenvolvimento incremental e histórico integral das conversas entre usuário e Claude Code.

Este sistema será também um **projeto profissional de portfólio**, portanto arquitetura, documentação, experiência visual, qualidade técnica e capacidade de demonstrar problemas reais de Engenharia de Software são requisitos centrais.

---

# 1. PRINCÍPIOS FUNDAMENTAIS

Estas regras devem permanecer válidas durante todo o projeto:

1. Nenhuma implementação relevante deve começar sem uma SPEC correspondente.
2. Português do Brasil é o idioma oficial do projeto.
3. Código próprio deve utilizar português sempre que tecnicamente apropriado.
4. Termos impostos por linguagens, frameworks, APIs, protocolos e padrões consolidados devem permanecer no idioma tecnicamente correto.
5. Toda conversa relacionada ao projeto deve ser registrada em histórico.
6. Toda mensagem de commit deve ser escrita em português.
7. Nenhuma credencial ou dado sensível deve ser exposto ou versionado.
8. Toda funcionalidade deve possuir testes adequados.
9. Métricas apresentadas como reais devem ser obtidas de execuções reais.
10. Documentação deve evoluir junto com o código.
11. Não criar abstrações, interfaces, camadas ou padrões sem necessidade real.
12. Toda decisão arquitetural relevante deve possuir justificativa.
13. Antes de implementar, entender e especificar.
14. Antes de considerar concluído, testar, validar e documentar.
15. O sistema deve ser evolutivo, seguro, legível e fácil de manter.
16. O projeto deve demonstrar Engenharia de Software profissional, não apenas geração rápida de código.
17. Toda funcionalidade deve ser construída de forma incremental e auditável.
18. Toda decisão ainda não aprovada deve ser marcada como proposta, hipótese ou pendente.

---

# 2. IDIOMA OFICIAL DO PROJETO

Todo o projeto deve utilizar **português do Brasil** como idioma padrão sempre que tecnicamente apropriado.

Essa regra vale desde o primeiro arquivo criado.

## 2.1 Código-fonte

Utilize português sempre que possível para:

* classes;
* records;
* enums;
* interfaces;
* métodos;
* atributos;
* variáveis;
* constantes;
* DTOs;
* mappers;
* services;
* repositories;
* controllers;
* exceptions próprias;
* eventos;
* comandos;
* objetos de domínio;
* casos de uso;
* testes;
* métodos de teste;
* fixtures;
* builders;
* factories;
* componentes;
* módulos;
* pacotes relacionados ao domínio.

Exemplos desejados:

```java
public class PedidoService {
}

public enum StatusPedido {
    CRIADO,
    PROCESSANDO,
    CONCLUIDO,
    CANCELADO
}

public record PedidoResponse(...) {
}

public class PedidoNaoEncontradoException extends RuntimeException {
}

public void deveBuscarPedidosComItensSemGerarNMaisUm() {
}
```

Evite sem necessidade:

```text
OrderService
CustomerRepository
PaymentService
ProductController
findOrderById
processPayment
```

Prefira:

```text
PedidoService
ClienteRepository
PagamentoService
ProdutoController
buscarPedidoPorId
processarPagamento
```

## 2.2 O que deve permanecer em inglês

Não traduza quando a tradução prejudicar:

* interoperabilidade;
* compatibilidade;
* entendimento técnico;
* funcionamento de frameworks;
* protocolos;
* padrões reconhecidos.

Exemplos:

* Java;
* Spring Boot;
* Spring Data JPA;
* Spring Security;
* Hibernate;
* Kafka;
* Redis;
* PostgreSQL;
* Docker;
* Kubernetes;
* OpenTelemetry;
* Prometheus;
* Grafana;
* Testcontainers;
* GitHub Actions;
* HTTP;
* REST;
* JSON;
* YAML;
* SQL;
* OpenAPI;
* OAuth2;
* OIDC;
* JWT;
* métodos obrigatórios de frameworks;
* propriedades de configuração;
* nomes exigidos por ferramentas;
* palavras reservadas de linguagens;
* APIs externas.

Princípio:

**Português sempre que tecnicamente apropriado. Inglês somente quando necessário por interoperabilidade, convenção técnica consolidada ou exigência tecnológica.**

## 2.3 Banco de dados

Quando não houver impedimento técnico ou integração externa, utilizar português para:

* tabelas;
* colunas;
* índices próprios;
* constraints;
* sequences;
* dados de demonstração.

Exemplos:

```text
pedido
item_pedido
cliente
data_criacao
status_pedido
```

## 2.4 APIs

As APIs próprias podem utilizar recursos em português:

```text
/api/pedidos
/api/laboratorios
/api/execucoes
/api/metricas
```

Preserve convenções REST e elementos de protocolo.

## 2.5 Frontend

Utilizar português para:

* componentes próprios;
* hooks próprios;
* funções;
* tipos;
* interfaces TypeScript;
* estados;
* labels;
* mensagens;
* testes;
* conceitos de domínio.

Exemplos:

```text
CatalogoLaboratorios
DetalhesLaboratorio
PainelMetricas
ComparacaoAntesDepois
ExecucaoLaboratorio
```

## 2.6 Documentação

Toda documentação própria deverá ser escrita em português do Brasil:

* README;
* SPECs;
* ADRs;
* diagramas;
* comentários relevantes;
* documentação de APIs;
* arquitetura;
* testes;
* histórico;
* mensagens da aplicação.

---

# 3. COMMITS

Todas as mensagens de commit devem ser obrigatoriamente escritas em português.

Utilizar Conventional Commits.

Exemplos:

```text
feat: adiciona estrutura inicial dos laboratórios
feat: implementa laboratório de N+1
test: adiciona testes de integração do laboratório de N+1
docs: documenta arquitetura inicial do projeto
fix: corrige coleta de métricas das consultas
refactor: separa execução e diagnóstico do laboratório
chore: configura ambiente local com Docker
```

---

# 4. HISTÓRICO INTEGRAL DA CONVERSA

O projeto deve possuir obrigatoriamente:

```text
docs/conversation-history.md
```

Esse arquivo será o registro cronológico persistente de toda a interação entre usuário e Claude Code relacionada ao projeto.

## 4.1 Regras do arquivo

O arquivo:

* deve ser versionado no Git;
* não deve estar no `.gitignore`;
* não pode ser apagado;
* não pode ser recriado apagando conteúdo anterior;
* não pode ser sobrescrito;
* deve receber apenas novos registros ao final;
* deve utilizar UTF-8;
* deve manter ordem cronológica;
* deve permanecer legível em Markdown.

## 4.2 Registro da mensagem do usuário

Sempre que eu enviar uma nova mensagem, a **primeira ação do turno**, sempre que tecnicamente possível, deve ser registrar minha mensagem integralmente.

Formato:

```markdown
---

## Interação YYYY-MM-DD HH:mm:ss

### Usuário

<mensagem integral enviada pelo usuário>
```

Não:

* resumir;
* reinterpretar;
* corrigir;
* remover contexto;
* mudar significado.

## 4.3 Registro da resposta do Claude

Antes de encerrar qualquer turno, registrar:

```markdown
### Claude

<resposta final apresentada ao usuário>
```

Cada interação deve resultar em:

```markdown
---

## Interação YYYY-MM-DD HH:mm:ss

### Usuário

...

### Claude

...
```

## 4.4 O que deve ser registrado

Registrar todas as interações relacionadas ao projeto:

* perguntas;
* respostas;
* decisões;
* correções;
* dúvidas;
* solicitações;
* aprovações;
* rejeições;
* erros;
* resultados;
* mudanças de arquitetura;
* mudanças de requisitos;
* discussões técnicas;
* resultados de testes;
* alterações de escopo.

## 4.5 Histórico não é documentação normativa

`docs/conversation-history.md` registra contexto e evolução.

Ele não substitui:

* SPEC;
* ADR;
* README;
* roadmap;
* arquitetura;
* documentação de testes.

Quando uma conversa produzir decisão definitiva:

1. registrar a conversa;
2. registrar a decisão no documento normativo adequado.

---

# 5. AUTOMAÇÃO DO HISTÓRICO NO CLAUDE CODE

Durante o bootstrap:

1. detecte a versão instalada do Claude Code;
2. identifique mecanismos oficialmente suportados;
3. investigue especialmente:

   * hooks;
   * eventos de submissão de prompt;
   * eventos de término de resposta;
   * scripts de projeto;
   * instruções persistentes;
   * configurações locais.

Utilize algo equivalente a:

```bash
claude --version
```

Não invente:

* hooks;
* nomes de eventos;
* propriedades;
* arquivos;
* configurações.

Utilize somente mecanismos oficialmente suportados pela versão instalada.

Se possível, implemente:

```text
USUÁRIO ENVIA MENSAGEM
        ↓
REGISTRO NO HISTÓRICO
        ↓
CLAUDE PROCESSA
        ↓
CLAUDE RESPONDE
        ↓
REGISTRO DA RESPOSTA
```

Se não for tecnicamente possível registrar exatamente no momento em que Enter é pressionado, documente a limitação e utilize:

```text
nova mensagem
      ↓
primeira ação do Claude
      ↓
registrar usuário
      ↓
executar trabalho
      ↓
preparar resposta
      ↓
registrar resposta
      ↓
encerrar turno
```

Se necessário, crie:

```text
scripts/registrar-conversa.*
```

O mecanismo deve priorizar:

* Windows;
* Linux;
* UTF-8;
* simplicidade;
* segurança;
* ausência de dependências desnecessárias;
* proteção contra corrupção do arquivo;
* append seguro.

---

# 6. SEGURANÇA DO HISTÓRICO

Não copiar automaticamente para o histórico valores sensíveis encontrados em execuções:

* tokens;
* senhas;
* secrets;
* chaves privadas;
* cookies;
* credenciais;
* connection strings com senha.

Substituir por:

```text
[VALOR SENSÍVEL REMOVIDO]
```

Caso uma mensagem do usuário pareça conter uma credencial que será versionada, alertar antes de persistir.

---

# 7. INSTRUÇÕES PERSISTENTES DO PROJETO

Durante o bootstrap, descubra o mecanismo oficial do Claude Code para instruções persistentes do projeto.

Configure regras permanentes para novas sessões:

1. português é o idioma oficial;
2. código próprio deve usar português quando apropriado;
3. commits devem estar em português;
4. desenvolvimento é spec-driven;
5. mensagens do usuário devem ser registradas;
6. respostas do Claude devem ser registradas;
7. `docs/conversation-history.md` não pode ser destruído;
8. implementação relevante exige SPEC;
9. credenciais não podem ser expostas;
10. documentação deve acompanhar o código.

---

# 8. OBJETIVO DO JAVA ENGINEERING LAB

Criar uma plataforma web educacional e interativa para demonstrar problemas reais e recorrentes em aplicações Java/Spring.

O usuário deverá conseguir:

1. conhecer o problema;
2. entender onde ele acontece;
3. visualizar a arquitetura envolvida;
4. executar um cenário problemático;
5. observar o comportamento;
6. visualizar métricas, queries, logs, traces e eventos;
7. diagnosticar a causa raiz;
8. visualizar o código problemático;
9. conhecer possíveis soluções;
10. aplicar uma solução;
11. executar novamente;
12. comparar antes e depois;
13. entender trade-offs;
14. aprender como explicar o problema em entrevista.

O produto não deve ser apenas um CRUD.

Ele deve funcionar como um **laboratório interativo de Engenharia de Software**.

---

# 9. PROPOSTA DE VALOR

O sistema deve:

* ensinar problemas reais de aplicações Java;
* transformar conceitos abstratos em experiências visuais;
* mostrar causa, sintomas e impacto;
* mostrar código problemático e corrigido;
* permitir execução prática;
* permitir comparação antes x depois;
* explicar trade-offs;
* servir como preparação para entrevistas;
* servir como projeto de portfólio;
* demonstrar Java, Engenharia de Software e IA aplicada.

---

# 10. STACK OBRIGATÓRIA

## Backend

* Java 21;
* Spring Boot;
* Spring Data JPA;
* Hibernate;
* Spring Validation;
* Spring Security quando necessário;
* Maven.

Utilizar separação adequada entre:

* controller;
* service;
* repository;
* domínio;
* DTO;
* mapper;
* exceptions;
* configurações;
* integrações.

Avaliar Arquitetura Hexagonal ou arquitetura modular equivalente quando trouxer benefício real.

Não criar abstrações artificiais.

## Frontend

* Next.js;
* React;
* TypeScript;
* Tailwind CSS.

## Dados

* PostgreSQL;
* Redis.

## Mensageria

* Apache Kafka.

## Testes

* JUnit 5;
* Mockito;
* Testcontainers;
* testes unitários;
* testes de integração;
* testes de concorrência;
* testes end-to-end quando aplicável.

## Observabilidade

* Micrometer;
* Prometheus;
* Grafana;
* OpenTelemetry;
* tracing distribuído;
* métricas;
* logs estruturados;
* correlation ID.

## Qualidade

* SonarQube;
* JaCoCo;
* análise estática;
* análise de vulnerabilidades.

## Infraestrutura

* Docker;
* Docker Compose.

## CI/CD

* GitHub Actions.

---

# 11. SPEC-DRIVEN DEVELOPMENT

Nenhuma implementação relevante deve começar antes da criação e aprovação da SPEC correspondente.

Criar estrutura semelhante a:

```text
specs/
├── manifest/
├── architecture/
├── backend/
├── frontend/
├── labs/
├── infra/
├── security/
└── testing/
```

Cada SPEC deve conter, quando aplicável:

* ID;
* título;
* status;
* contexto;
* problema;
* objetivo;
* escopo;
* fora de escopo;
* requisitos funcionais;
* requisitos não funcionais;
* regras de negócio;
* arquitetura;
* contratos;
* fluxo;
* critérios de aceite;
* estratégia de testes;
* observabilidade;
* segurança;
* riscos;
* dependências;
* decisões;
* evidências de conclusão.

IDs sugeridos:

```text
SPEC-JEL-001
SPEC-JEL-002
SPEC-LAB-N1-001
SPEC-LAB-RACE-001
SPEC-LAB-KAFKA-IDEMP-001
```

Fluxo obrigatório:

```text
SPEC
  ↓
IMPLEMENTAÇÃO
  ↓
TESTES
  ↓
VALIDAÇÃO
  ↓
DOCUMENTAÇÃO
  ↓
EVIDÊNCIAS
```

---

# 12. DOCUMENTAÇÃO OBRIGATÓRIA

Criar e manter:

```text
README.md
docs/conversation-history.md
docs/architecture.md
docs/testing-guide.md
docs/links.md
docs/security.md
docs/observability.md
```

## README

Deve conter:

* visão;
* propósito;
* arquitetura;
* stack;
* como executar;
* laboratórios;
* roadmap;
* links;
* comandos importantes;
* como contribuir futuramente.

## docs/testing-guide.md

Documentar:

* pré-requisitos;
* ordem de inicialização;
* ordem de execução;
* endpoints;
* métodos HTTP;
* payloads;
* headers;
* tokens;
* obtenção de tokens;
* passagem de tokens;
* autenticação;
* autorização;
* cenários positivos;
* cenários negativos;
* cenários de erro;
* resultados esperados;
* validações no banco;
* validações no Kafka;
* validações no Redis;
* validações das métricas;
* validações dos traces.

Quando apropriado, fornecer exemplos para Postman.

## docs/links.md

Registrar somente URLs reais do ambiente:

* frontend;
* backend;
* Swagger;
* Grafana;
* Prometheus;
* Kafka UI;
* SonarQube;
* outras interfaces administrativas.

Não registrar links fictícios como disponíveis.

---

# 13. OPENAPI / SWAGGER

O backend deve possuir documentação OpenAPI/Swagger atualizada.

Documentar:

* endpoints;
* parâmetros;
* payloads;
* respostas;
* códigos HTTP;
* erros;
* autenticação;
* autorização;
* exemplos.

Sempre que uma API mudar, revisar sua documentação.

---

# 14. CONTRATOS DA API

Não retornar entidades JPA diretamente em APIs públicas.

Utilizar DTOs quando apropriado.

Responsabilidades devem permanecer separadas entre:

```text
Controller
Service
Domínio
Repository
DTO
Mapper
Integrações
```

Evitar:

* lógica de negócio em controller;
* lazy loading causado por serialização;
* exposição de estruturas internas;
* acoplamento desnecessário entre API e persistência.

---

# 15. TRATAMENTO DE ERROS

Definir padrão consistente de erros.

Quando apropriado, retornar:

* código;
* mensagem;
* timestamp;
* caminho;
* detalhes seguros;
* correlation ID.

Nunca retornar:

* stack trace;
* detalhes internos sensíveis;
* credenciais;
* secrets.

Criar exceptions próprias quando necessário.

---

# 16. DIAGRAMAS

Criar diagramas versionáveis em Mermaid, PlantUML ou formato textual equivalente.

Incluir:

* C4 Context;
* C4 Container;
* C4 Component quando necessário;
* diagramas de sequência;
* fluxos;
* ERD;
* Kafka;
* observabilidade;
* diagramas específicos dos laboratórios.

---

# 17. EXPERIÊNCIA PADRÃO DE CADA LABORATÓRIO

Cada laboratório deverá conter, quando aplicável:

1. Nome.
2. Objetivo.
3. Contexto.
4. Pré-requisitos.
5. Arquitetura visual.
6. Implementação problemática.
7. Como reproduzir.
8. Execução do problema.
9. Sintomas.
10. Métricas antes.
11. Logs.
12. Queries.
13. Traces.
14. Eventos.
15. Diagnóstico.
16. Causa raiz.
17. Explicação do comportamento.
18. Código problemático.
19. Solução principal.
20. Alternativas.
21. Trade-offs.
22. Quando utilizar.
23. Quando não utilizar.
24. Código corrigido.
25. Diff.
26. Execução corrigida.
27. Métricas depois.
28. Comparação antes x depois.
29. Testes relacionados.
30. Perguntas comuns de entrevista.
31. Resposta resumida para entrevista.
32. Referências técnicas quando apropriado.

Fluxo visual desejado:

```text
INTRODUÇÃO
    ↓
ARQUITETURA
    ↓
EXECUTAR PROBLEMA
    ↓
OBSERVAR
    ↓
DIAGNOSTICAR
    ↓
CÓDIGO PROBLEMÁTICO
    ↓
SOLUÇÕES
    ↓
APLICAR SOLUÇÃO
    ↓
EXECUTAR NOVAMENTE
    ↓
ANTES × DEPOIS
    ↓
TRADE-OFFS
    ↓
ENTREVISTA
```

---

# 18. INTERFACE GRÁFICA

A interface é requisito central.

Criar futuramente:

* dashboard;
* catálogo de laboratórios;
* página do laboratório;
* diagramas;
* painel de execução;
* painel de métricas;
* logs formatados;
* SQL formatado;
* traces;
* eventos;
* diff de código;
* comparação antes/depois;
* explicação técnica;
* seção para entrevistas.

A interface deve destacar claramente:

```text
PROBLEMA
DIAGNÓSTICO
SOLUÇÃO
RESULTADO
```

Evitar grandes páginas apenas textuais.

Priorizar:

* cards;
* timeline;
* fluxos;
* indicadores;
* tabelas;
* gráficos;
* diff;
* logs;
* SQL;
* traces.

---

# 19. ACESSIBILIDADE E RESPONSIVIDADE

Aplicar:

* HTML semântico;
* navegação por teclado;
* contraste;
* labels;
* foco visível;
* feedback de carregamento;
* mensagens de erro compreensíveis.

Funcionar em:

* desktop;
* notebook;
* tablet;
* mobile.

Experiências técnicas densas podem ser otimizadas para desktop, mas não devem quebrar em mobile.

---

# 20. MÉTRICAS REAIS

Quando a interface apresentar resultados de execução, os dados devem possuir origem definida.

Possíveis classificações internas:

```text
REAL
SIMULADO
ESTIMADO
```

Valores reais devem vir da execução.

Valores simulados ou estimados devem ser identificados visualmente.

Nunca apresentar números fictícios como benchmark real.

---

# 21. COMPARAÇÃO ANTES X DEPOIS

Sempre que possível, comparar utilizando:

```text
MESMA MASSA DE DADOS
MESMA OPERAÇÃO
MESMO AMBIENTE
MESMOS PARÂMETROS
```

Evitar comparações enganosas.

---

# 22. OBSERVABILIDADE EDUCACIONAL

Observabilidade faz parte do produto.

Cada laboratório deverá mostrar apenas o que fizer sentido, como:

* latência;
* queries;
* traces;
* logs;
* erros;
* retries;
* mensagens Kafka;
* conexões;
* tempo de processamento;
* estado de circuit breakers.

Não é obrigatório exibir toda telemetria em todos os laboratórios.

---

# 23. LOGS

Utilizar logs estruturados quando apropriado.

Possuir correlation ID para fluxos relevantes.

Não registrar:

* senha;
* token;
* secret;
* dados sensíveis desnecessários.

Não criar excesso de logs apenas para demonstração.

---

# 24. LABORATÓRIO 1 — N+1 QUERIES

Primeiro laboratório funcional.

Domínio sugerido:

```text
Pedido
   ↓
Itens do Pedido
```

Demonstrar:

* JPA/Hibernate;
* consulta inicial;
* consultas adicionais;
* SQL executado;
* quantidade de queries;
* tempo;
* impacto.

Soluções obrigatórias:

* JOIN FETCH;
* EntityGraph;
* DTO Projection.

Explicar:

* vantagens;
* limitações;
* paginação;
* risco de carregar dados desnecessários;
* cenários adequados.

Explicar também por que trocar tudo para `EAGER` não é solução universal.

---

# 25. LABORATÓRIO 2 — RACE CONDITION / LOST UPDATE

O cenário deve executar concorrência real quando tecnicamente possível.

Demonstrar:

```text
requisição A lê estado
requisição B lê o mesmo estado
requisição A altera
requisição B altera
estado é sobrescrito
```

Solução principal:

```text
Optimistic Locking
@Version
```

Explicar também:

```text
Pessimistic Locking
```

e seus trade-offs.

Criar testes reproduzíveis de concorrência.

---

# 26. LABORATÓRIO 3 — KAFKA / MENSAGEM DUPLICADA / IDEMPOTÊNCIA

Demonstrar:

```text
evento recebido
evento processado
mesmo evento recebido novamente
operação executada novamente
```

Depois implementar abordagem idempotente.

Considerar:

* identificador único;
* registro de processamento;
* retries;
* falha parcial;
* consistência.

Não afirmar que Kafka elimina duplicidades.

Não afirmar que exactly-once resolve automaticamente todos os efeitos duplicados de negócio.

Explicar diferença entre:

* semântica de entrega;
* processamento idempotente;
* efeito da operação de negócio.

---

# 27. TRANSACTIONAL OUTBOX

Não faz parte do MVP obrigatório inicial.

Porém a arquitetura deve permitir laboratório futuro demonstrando:

```text
Banco confirma
+
Kafka falha
=
inconsistência
```

Depois demonstrar:

```text
Transactional Outbox
```

Com:

* entidade;
* evento Outbox;
* mesma transação local;
* publicação assíncrona;
* controle de processamento.

---

# 28. LABORATÓRIOS FUTUROS

Manter backlog para:

1. N+1 Queries.
2. LazyInitializationException.
3. Eager Fetching excessivo.
4. Cartesian Product.
5. Query sem índice.
6. Race Condition.
7. Lost Update.
8. Deadlock.
9. Connection Pool Exhaustion.
10. Mensagem duplicada.
11. Idempotência.
12. Transactional Outbox.
13. Ordenação de eventos.
14. Circuit Breaker.
15. Retry Storm.
16. Timeout Cascade.
17. Saga.
18. Cache inconsistente.
19. Cache Stampede.
20. Thread Pool Exhaustion.
21. Memory Leak.
22. OutOfMemoryError.
23. Paginação incorreta.
24. Observabilidade insuficiente.

Não implementar todos inicialmente.

---

# 29. ASSISTENTE DE IA

Planejar módulo chamado provisoriamente:

**Engineering AI Assistant**

A IA poderá receber contexto real do laboratório:

* descrição;
* execução;
* métricas;
* logs;
* traces;
* queries;
* código;
* eventos.

Exemplos de perguntas:

```text
Por que essa API executou 101 queries?
Por que esse evento foi processado duas vezes?
Qual a diferença entre JOIN FETCH e EntityGraph?
Por que o @Version resolveu esse Lost Update?
Quais são os trade-offs?
Como explicar isso em uma entrevista Java Sênior?
```

A IA deve ser complementar.

A plataforma deve funcionar sem IA.

O conteúdo educacional principal deve existir independentemente do assistente.

---

# 30. ABSTRAÇÃO DO PROVEDOR DE IA

Evitar acoplamento desnecessário a um único provedor.

Avaliar abstração compatível com a stack Java.

Possíveis integrações futuras:

* provedores externos;
* modelos locais.

Não adicionar complexidade prematura.

Credenciais de IA nunca devem ser versionadas.

---

# 31. SEGURANÇA

Nunca versionar:

* senhas;
* tokens;
* chaves;
* secrets;
* credenciais.

Utilizar:

* variáveis de ambiente;
* `.env.example`;
* mecanismos seguros;
* validação de entrada;
* práticas OWASP;
* menor privilégio;
* logs seguros.

Criar `.gitignore` adequado.

---

# 32. SEGURANÇA DE DEPENDÊNCIAS

Avaliar mecanismos apropriados para detectar:

* CVEs;
* dependências vulneráveis;
* vulnerabilidades conhecidas.

Não adicionar ferramentas redundantes sem justificativa.

---

# 33. QUALIDADE DE CÓDIGO

Aplicar:

* SOLID;
* Clean Code;
* DDD quando fizer sentido;
* Arquitetura Hexagonal quando trouxer valor;
* padrões de projeto quando necessários;
* alta coesão;
* baixo acoplamento;
* testabilidade.

Não usar padrões apenas para "mostrar conhecimento".

Arquitetura deve resolver problemas reais.

---

# 34. SONARQUBE E JACOCO

Utilizar SonarQube para:

* bugs;
* vulnerabilidades;
* code smells;
* duplicação.

Utilizar JaCoCo para cobertura.

Cobertura é indicador auxiliar.

Não criar testes inúteis apenas para aumentar percentual.

---

# 35. TESTCONTAINERS

Utilizar Testcontainers quando houver benefício real para:

* PostgreSQL;
* Kafka;
* Redis;
* dependências de integração.

Estratégia:

```text
teste unitário
→ quando dependência real não é necessária

teste de integração
→ quando comportamento integrado precisa ser validado
```

---

# 36. CI/CD

Configurar futuramente GitHub Actions para validar:

```text
Backend
Frontend
Testes
Cobertura
Qualidade
Vulnerabilidades
Build
Imagens Docker
```

Falhas críticas devem impedir continuidade adequada do pipeline.

---

# 37. DOCKER E DOCKER COMPOSE

A experiência local deve ser simples.

Objetivo:

```bash
docker compose up
```

Avaliar serviços:

* backend;
* frontend;
* PostgreSQL;
* Redis;
* Kafka;
* Kafka UI;
* Prometheus;
* Grafana;
* SonarQube.

Não obrigar todos a subir sempre.

Avaliar profiles semelhantes a:

```text
core
messaging
observability
quality
full
```

Exemplo conceitual:

```text
core
→ frontend
→ backend
→ PostgreSQL

messaging
→ Kafka
→ Kafka UI

observability
→ Prometheus
→ Grafana

quality
→ SonarQube
```

Os nomes não são obrigatórios se existir organização melhor.

---

# 38. EXECUÇÃO LOCAL

Documentar claramente:

```bash
docker compose up
```

ou equivalente.

Também explicar como subir somente partes do ambiente.

Não exigir infraestrutura externa obrigatória para desenvolvimento local.

---

# 39. ESTRUTURA INICIAL DO REPOSITÓRIO

Avaliar estrutura semelhante a:

```text
/
├── backend/
├── frontend/
├── docs/
├── specs/
├── diagrams/
├── infra/
├── scripts/
├── .github/
├── docker-compose.yml
└── README.md
```

Não aceitar cegamente.

Analise e proponha estrutura melhor se necessário.

Documente justificativas.

---

# 40. README COMO PORTA DE ENTRADA

O README deve responder rapidamente:

```text
O que é?
Por que existe?
Que problema resolve?
Como executar?
Quais laboratórios existem?
Como funciona?
Qual stack?
Onde estão as SPECs?
Onde estão os diagramas?
Qual o roadmap?
Como contribuir?
```

Como o projeto será parte de portfólio, o README deve ter excelente qualidade.

---

# 41. OPEN SOURCE

Preparar futuramente:

```text
CONTRIBUTING.md
CODE_OF_CONDUCT.md
LICENSE
```

Não criar burocracia excessiva no início.

---

# 42. CRITÉRIO EDUCACIONAL

Um laboratório só é considerado bem-sucedido quando o usuário consegue:

```text
ver o problema
↓
reproduzir
↓
observar
↓
entender a causa
↓
conhecer soluções
↓
aplicar uma solução
↓
comparar
↓
entender trade-offs
↓
explicar o conceito
```

Se houver apenas código e documentação, sem essa experiência, o objetivo central não foi atingido.

---

# 43. NÃO SUPERENGENHEIRAR

Não implementar antecipadamente:

* todos os microsserviços imagináveis;
* Kubernetes;
* service mesh;
* dezenas de módulos;
* abstrações genéricas;
* plataformas próprias de infraestrutura;
* features fora do MVP.

Arquitetura evolutiva não significa arquitetura antecipatória excessiva.

---

# 44. FASES DO DESENVOLVIMENTO

## Fase 0 — Governança e descoberta

* detectar versão do Claude Code;
* configurar histórico;
* configurar instruções persistentes;
* definir convenções;
* analisar requisitos;
* definir arquitetura;
* criar SPECs.

## Fase 1 — Bootstrap

* estrutura do repositório;
* backend;
* frontend;
* Docker;
* CI básico;
* documentação.

## Fase 2 — Plataforma base

* catálogo;
* estrutura dos laboratórios;
* execução;
* contratos;
* métricas.

## Fase 3 — N+1

Implementar laboratório completo.

## Fase 4 — Race Condition

Implementar laboratório completo.

## Fase 5 — Kafka / Idempotência

Implementar laboratório completo.

## Fase 6 — Observabilidade

Consolidar métricas, logs e traces.

## Fase 7 — IA

Implementar Engineering AI Assistant.

## Fase 8 — Hardening

* segurança;
* testes;
* performance;
* UX;
* documentação;
* CI/CD.

---

# 45. REGRA DE EXECUÇÃO DE CADA FEATURE

Antes de qualquer implementação:

```text
LER SPEC
   ↓
ANALISAR IMPACTO
   ↓
IMPLEMENTAR
   ↓
TESTAR
   ↓
VALIDAR
   ↓
ATUALIZAR DOCUMENTAÇÃO
   ↓
REGISTRAR EVIDÊNCIAS
```

---

# 46. DEFINITION OF DONE

Uma funcionalidade só pode ser concluída quando:

* SPEC atendida;
* implementação funcional;
* testes passando;
* cobertura adequada;
* critérios de aceite validados;
* segurança analisada;
* documentação atualizada;
* observabilidade adequada;
* evidências registradas;
* nenhuma regressão conhecida.

---

# 47. NÃO FAZER

Não:

* implementar feature sem SPEC;
* inventar requisitos silenciosamente;
* criar abstrações desnecessárias;
* duplicar lógica;
* esconder erros;
* retornar entidade JPA diretamente;
* misturar controller, regra de negócio e persistência;
* adicionar dependência sem justificativa;
* criar infraestrutura excessiva antes do MVP;
* utilizar métricas fictícias;
* deixar código morto;
* deixar TODO crítico sem documentação;
* expor credenciais;
* usar IA apenas como gimmick;
* usar mocks quando integração real for necessária;
* transformar hipóteses automaticamente em decisões;
* criar complexidade apenas para parecer sofisticado.

---

# 48. PRIMEIRA EXECUÇÃO — REGRA CRÍTICA

**NÃO IMPLEMENTE O SISTEMA FUNCIONAL AINDA.**

Ao receber este prompt, execute somente a fase inicial.

Primeiro:

1. preserve este prompt integralmente;
2. detecte a versão instalada do Claude Code;
3. descubra mecanismos oficiais de hooks;
4. descubra mecanismos oficiais de instruções persistentes;
5. crie `docs/conversation-history.md`;
6. implemente o mecanismo de histórico;
7. registre este prompt no histórico;
8. valide registro de entrada;
9. valide registro de saída;
10. valide timestamps;
11. valide UTF-8;
12. valide append sem sobrescrita;
13. configure instruções permanentes;
14. registre português como idioma oficial;
15. registre convenções de código;
16. registre convenções de commits;
17. analise completamente este prompt;
18. identifique ambiguidades;
19. registre premissas;
20. proponha arquitetura;
21. proponha estrutura do repositório;
22. crie roadmap;
23. crie manifesto inicial;
24. crie SPEC de bootstrap;
25. crie SPEC de arquitetura;
26. crie SPEC do MVP;
27. crie SPEC do laboratório N+1;
28. crie critérios de aceite;
29. crie estratégia inicial de testes;
30. crie diagramas iniciais;
31. crie/atualize README;
32. documente riscos;
33. documente trade-offs;
34. documente decisões propostas;
35. documente pontos pendentes;
36. apresente relatório completo do que foi criado.

Depois:

**PARE.**

Não implemente backend funcional, frontend funcional ou laboratórios ainda.

Aguarde minha aprovação explícita.

---

# 49. DECISÕES NA FASE DE DESCOBERTA

Durante a primeira execução, qualquer decisão sem evidência suficiente deve ser marcada como:

```text
PROPOSTA
HIPÓTESE
PENDENTE DE APROVAÇÃO
```

Não transforme automaticamente recomendações do Claude em decisões definitivas.

Ao concluir a fase de descoberta, apresente:

```text
DECISÕES PROPOSTAS
PONTOS PENDENTES
RISCOS
TRADE-OFFS
DÚVIDAS PARA O USUÁRIO
```

Depois aguarde aprovação.

---

# 50. REGRA DE ENCERRAMENTO DE TODO TURNO

Antes de encerrar qualquer interação, verificar:

```text
[ ] Mensagem do usuário registrada
[ ] Resposta do Claude registrada
[ ] Histórico anterior preservado
[ ] UTF-8 preservado
[ ] Nenhuma credencial exposta
[ ] SPEC respeitada
[ ] Documentação afetada atualizada
[ ] Testes executados quando aplicável
```

Se alguma condição aplicável estiver incorreta, corrigir antes de encerrar.

---

# 51. PRINCÍPIO FINAL

O objetivo deste projeto não é produzir a maior quantidade possível de código.

A prioridade é:

```text
ENTENDER
   ↓
ESPECIFICAR
   ↓
PROJETAR
   ↓
IMPLEMENTAR
   ↓
TESTAR
   ↓
OBSERVAR
   ↓
DOCUMENTAR
   ↓
VALIDAR
   ↓
EVOLUIR
```

Quero um sistema:

* profissional;
* seguro;
* didático;
* visual;
* tecnicamente demonstrável;
* bem documentado;
* testável;
* observável;
* preparado para evolução.

O próprio processo de construção do Java Engineering Lab deve demonstrar:

**Engenharia de Software + Java Full Stack + Inteligência Artificial aplicada + Spec-Driven Development.**

Agora execute **somente a PRIMEIRA EXECUÇÃO descrita neste documento**.

Ao terminar, apresente o relatório completo, registre sua resposta no histórico e **aguarde minha aprovação antes de iniciar qualquer implementação funcional**.
````

### Claude

Executei somente a Fase 0 (Governança e Descoberta), conforme a regra crítica da seção 48 do prompt. Não implementei backend, frontend, laboratórios funcionais nem `docker-compose.yml`.

**Descoberta técnica**
- `claude --version` → `2.1.240 (Claude Code)`.
- Pesquisei (via subagente `claude-code-guide`, consultando a documentação oficial) os hooks disponíveis nesta versão e confirmei: `UserPromptSubmit` e `Stop` existem e são os eventos certos para este mecanismo, mas o payload que recebem via stdin não traz o texto bruto do prompt/resposta — só `transcript_path`, `session_id`, `prompt_id`, `cwd`, `permission_mode`. Reconstruir o conteúdo exato a partir do transcript JSONL automaticamente criaria um segundo escritor concorrente do arquivo de histórico, com risco real de duplicidade/corrupção.
- Confirmei `CLAUDE.md` na raiz do projeto como o mecanismo oficial de instruções persistentes, carregado automaticamente em toda sessão nesta pasta.
- Node.js (v24.13.1) e Git (2.53.0.windows.1) estão disponíveis; `git init` foi executado, mas nenhum commit foi feito (sem identidade de autor configurada globalmente — não alterei configuração global do Git).

**O que foi criado**
- `docs/conversation-history.md`: histórico append-only, com esta interação completa (mensagem do usuário na íntegra).
- Mecanismo de histórico híbrido: protocolo manual (Claude registra a mensagem do usuário como primeira ação do turno e sua própria resposta como última ação) + rede de segurança automatizada não bloqueante (`scripts/registrar-conversa.js`, hooks `UserPromptSubmit`/`Stop` em `.claude/settings.json`) que apenas avisa, sem nunca escrever conteúdo de conversa. Testei manualmente as três situações (registro do estado, aviso quando o arquivo não cresce, silêncio quando cresce) e o script se comportou como esperado.
- `CLAUDE.md`: regras permanentes (idioma, Spec-Driven Development, commits, protocolo de histórico, segurança, checklist de encerramento de turno).
- `.gitignore` cobrindo segredos, configuração local do Claude Code e artefatos de build futuros.
- `specs/manifest/MANIFESTO.md` e `SPEC-JEL-001-bootstrap.md` (esta própria fase).
- `specs/architecture/SPEC-JEL-002-arquitetura.md` (arquitetura geral) e `SPEC-JEL-003-mvp-plataforma-base.md` (MVP da plataforma).
- `specs/labs/SPEC-LAB-N1-001-n-mais-um-queries.md` (laboratório de N+1 completo: soluções JOIN FETCH/EntityGraph/DTO Projection, por que EAGER não resolve, critérios de aceite).
- READMEs de escopo em `specs/backend/`, `specs/frontend/`, `specs/infra/`, `specs/security/`, `specs/testing/` (ainda vazios, aguardando as fases correspondentes).
- Diagramas em Mermaid: `diagrams/c4-contexto.md`, `diagrams/c4-conteiner.md`, `diagrams/fluxo-laboratorio.md` (incluindo sequência da variante problemática do N+1).
- Documentação: `README.md`, `docs/architecture.md`, `docs/roadmap.md`, `docs/security.md`, `docs/observability.md`, `docs/testing-guide.md` (estratégia, sem endpoints fictícios), `docs/links.md` (sem URLs fictícias — nenhum ambiente está no ar).
- ADRs em `docs/decisions/`: 0001 (mecanismo híbrido de histórico), 0002 (monorepo), 0003 (esquema de IDs de SPEC).
- Repositório Git local inicializado (`git init`), sem commits ainda.

**Decisões propostas (pendentes de aprovação)**
1. Estrutura de repositório monorepo com `backend/`, `frontend/`, `docs/`, `specs/`, `diagrams/`, `infra/`, `scripts/` (ADR-0002).
2. Mecanismo híbrido de histórico: protocolo manual como fonte de verdade + hook de aviso não bloqueante (ADR-0001).
3. Esquema de IDs `SPEC-JEL-NNN` (plataforma) e `SPEC-LAB-<CODIGO>-NNN` (laboratórios) (ADR-0003).
4. Arquitetura de backend em camadas (Controller/Service/Domínio/Repository) por ora, com Arquitetura Hexagonal tratada como hipótese em aberto, reavaliada no laboratório de Kafka/idempotência.
5. Docker Compose com profiles (`core`, `messaging`, `observability`, `quality`, `full`) — a criar efetivamente na Fase 1.
6. Contrato de métricas com campo `origemDados` (`REAL`/`SIMULADO`/`ESTIMADO`).

**Pontos pendentes / dúvidas para o usuário**
1. Não configurei identidade de autor do Git (nome/e-mail) nem criei nenhum commit — não altero configuração do Git sem instrução explícita. Como deseja prosseguir: configurar a identidade e eu crio o primeiro commit, ou prefere fazer isso manualmente?
2. Confirma o mecanismo híbrido de histórico (ADR-0001) ou prefere outra abordagem (por exemplo, aceitar o risco de um hook que escreve automaticamente a partir do transcript)?
3. Confirma a estrutura de repositório e a decisão de adiar Arquitetura Hexagonal?
4. Aprova avançar para a Fase 1 (bootstrap de código: estrutura `backend/`/`frontend/`, `docker-compose.yml`, CI básico) ou deseja ajustar alguma SPEC antes?

**Riscos e trade-offs** documentados em cada SPEC (seções "Riscos") e nos ADRs — principal deles é a dependência do protocolo manual de registro de histórico ser seguido pelo próprio Claude a cada turno, mitigada pelo hook de aviso.

Aguardando aprovação explícita antes de iniciar qualquer implementação funcional (Fase 1 em diante).

---

## Interação 2026-08-22 19:51:32

### Usuário

Aprovado, pode avançar para a Fase 1

### Claude

Executei a Fase 1 (Bootstrap de Código), criando `specs/architecture/SPEC-JEL-004-bootstrap-de-codigo.md` antes de qualquer código, conforme Spec-Driven Development.

**Backend** (`backend/`): Java 21 + Spring Boot 4.1.1 (Maven), com Actuator (health/info/prometheus), Bean Validation e springdoc-openapi 3.1.0 (Swagger UI). Sem JPA/DataSource ainda — entra junto com a primeira entidade real, na Fase 2/3. Pacote `plataforma` reservado e vazio, para `SPEC-JEL-003`. `Dockerfile` multi-stage (build Maven + runtime JRE).

**Frontend** (`frontend/`): gerado com `create-next-app@latest` (Next.js 16.3.2, React 19.2.8, TypeScript, Tailwind CSS 4, App Router, `src/`). Página inicial substituída por conteúdo em português mostrando o estado atual das fases do projeto. `next.config.ts` com `output: "standalone"`. `Dockerfile` multi-stage. Mantive o `AGENTS.md`/`CLAUDE.md` que o próprio Next.js 16 gera automaticamente nessa pasta (aviso de que a versão tem mudanças de API relevantes) — não são arquivos meus, são regenerados pelo `next dev`/`build`.

**`docker-compose.yml`**: profiles `core` (postgres, backend, frontend), `messaging` (Kafka via KRaft + Kafka UI), `observability` (Prometheus + Grafana, com `infra/prometheus/prometheus.yml` já apontando para `/actuator/prometheus` do backend) e `quality` (SonarQube), mais `full`. `.env.example` na raiz documenta todas as variáveis.

**CI**: `.github/workflows/backend-ci.yml` e `frontend-ci.yml`, cada um com path filters (só roda quando o respectivo diretório muda).

**Validação real executada nesta interação** (detalhada em `docs/testing-guide.md`, seção "Validação do esqueleto — Fase 1"):
- `mvn -f backend/pom.xml test` → passou.
- Backend subido localmente: `/actuator/health` → `{"status":"UP"}`; Swagger UI e `/v3/api-docs` → `200`.
- `npm --prefix frontend run build` e `run lint` → sem erros.
- Frontend subido localmente (`npm run dev`) → conteúdo em português confirmado via HTTP. (Nota: a porta 3000 já estava em uso por outro processo pré-existente nesta máquina, não relacionado a este projeto — o Next.js migrou automaticamente para a 3001, comportamento padrão, nada foi alterado nesse outro processo.)
- `docker compose --profile core config` e `--profile full config` → sintaxe válida.
- `docker compose --profile core up --build` → os três serviços subiram de verdade: `postgres` (`healthy`), `backend` (`/actuator/health` → `UP`), `frontend` (`200`, conteúdo correto). Ambiente derrubado com `docker compose down` ao final, sem deixar containers rodando.
- Processos de desenvolvimento nativos (backend na porta 8080, frontend na porta 3001) também finalizados ao final da validação.

Documentação atualizada: `README.md` (status, como executar com comandos reais), `docs/links.md` (URLs reais validadas, sem nada fictício), `docs/testing-guide.md`, `docs/roadmap.md` (Fases 0 e 1 marcadas concluídas), `docs/security.md`, `frontend/README.md` e novo `backend/README.md`.

**Decisões de versão registradas** (não exigem aprovação prévia, são decisão de implementação, mas ficam rastreáveis em `SPEC-JEL-004`): Spring Boot 4.1.1, springdoc-openapi 3.1.0, Next.js 16.3.2/React 19.2.8/Tailwind 4 — todas as versões estáveis mais recentes disponíveis no momento.

**Pendências que seguem em aberto**: identidade Git ainda não configurada (nenhum commit foi criado — não altero configuração do Git; você pode configurá-la quando quiser). Nenhuma implementação de laboratório foi iniciada — isso é `SPEC-JEL-003` (Fase 2) em diante, e continua exigindo aprovação explícita antes de começar, como sempre.

Repositório segue sem nenhum commit (apenas `git init` foi executado). Quando desejar, posso preparar o primeiro commit em português (Conventional Commits) assim que a identidade Git estiver configurada.

---

## Interação 2026-08-22 20:08:08

### Usuário

faça isso

### Claude

Como "faça isso" respondia a uma pergunta com duas opções bem diferentes (avançar para a Fase 2, ou resolver Git/commit primeiro), pedi confirmação antes de agir. Você escolheu "Avançar para a Fase 2".

Implementei a Fase 2 (`SPEC-JEL-003` — Plataforma Base de Laboratórios), atualizando a SPEC antes do código para deixar explícito o escopo real: RF-02 (execução real) só pode ser cumprido por um laboratório concreto (viola "métricas reais são reais" se eu simular), então esta fase entregou catálogo + contrato + shell de página, deixando a execução do N+1 para `SPEC-LAB-N1-001` (Fase 3).

**Backend** (`plataforma/`): `CatalogoLaboratoriosController`/`Service` (`GET /api/laboratorios`, `GET /api/laboratorios/{id}`, catálogo em memória com o laboratório `n1-queries` em status `PLANEJADO`); contrato `ResultadoExecucaoLaboratorio`/`OrigemDados`/`VarianteExecucao` (definido, ainda sem endpoint que o produza); tratamento padrão de erro (`ErroResposta`, `ManipuladorGlobalDeExcecoes`, `LaboratorioNaoEncontradoException` → 404); `FiltroCorrelationId` (gera/propaga `X-Correlation-Id`, disponível via MDC e no cabeçalho de erro). 6 testes automatizados novos, todos passando.

Nota técnica: Spring Boot 4 reorganizou `@WebMvcTest` para o módulo `spring-boot-webmvc-test` (pacote `org.springframework.boot.webmvc.test.autoconfigure`) — precisei adicionar essa dependência explicitamente, já que não vem mais dentro de `spring-boot-starter-test`.

**Frontend**: `/laboratorios` (catálogo, Server Component buscando a API real) e `/laboratorios/[id]` (detalhe, com `notFound()` para id inexistente e aviso "ainda não disponível" para status `PLANEJADO`); `src/lib/laboratorios.ts` (cliente HTTP, usa `BACKEND_API_URL`, com fallback `localhost:8080` para dev nativo e `http://backend:8080` via `docker-compose.yml` para uso em container); componente `BadgeStatusLaboratorio`; link da home para o catálogo.

**Validação real executada**:
- `mvn test` → 6/6 passando.
- `curl` manual: catálogo `200` (lista `n1-queries`), detalhe `200`, id inexistente `404` com corpo `{codigo, mensagem, timestamp, caminho, correlationId}` e cabeçalho `X-Correlation-Id` — inclusive confirmando acentuação UTF-8 correta ("Laboratório não encontrado").
- `npm run build`/`lint` → sem erros; rotas `/laboratorios` e `/laboratorios/[id]` corretamente marcadas como dinâmicas (não pré-renderizadas).
- Verificação visual real no Chrome (via `claude-in-chrome`): catálogo e página de detalhe renderizando corretamente, badge de status, botão "voltar ao catálogo". Um aviso de hydration mismatch apareceu (`cz-shortcut-listen` injetado no `<body>`) — investigado e confirmado como causado por uma extensão do navegador desta máquina, não pelo meu código; não fiz nenhuma alteração por causa disso.
- `docker compose --profile core up --build`: os três serviços subiram e o **frontend containerizado consultou o backend containerizado pela rede interna do Docker** (`http://backend:8080`) com sucesso — catálogo e detalhe responderam corretamente pela porta publicada do frontend.
- Ambiente derrubado (`docker compose down`) e processos nativos finalizados ao final; nenhum container ou processo do projeto ficou rodando. Porta 3000, ocupada por outro processo seu não relacionado a este projeto, não foi tocada.

Documentação atualizada: `SPEC-JEL-003` (critérios de aceite + evidências), `README.md`, `docs/roadmap.md` (Fase 2 concluída), `docs/links.md` (endpoints reais), `docs/testing-guide.md`.

**Pendências**: identidade Git/commit inicial segue em aberto (você pode me pedir isso quando quiser). Nenhuma execução real de laboratório existe ainda — isso é `SPEC-LAB-N1-001` (Fase 3), e continua exigindo aprovação explícita antes de começar.

---

## Interação 2026-08-22 20:28:36

### Usuário

acesse meu github crie o projeto, deixe ele publico, e faça sempre os commits e pushs

### Claude

Confirmei autenticação do GitHub CLI (conta `wep1980`, escopos `repo`/`workflow`) e a ausência de `user.name`/`user.email` configurados globalmente no Git — como nunca altero configuração do Git, usei variáveis de ambiente (`GIT_AUTHOR_NAME`/`GIT_AUTHOR_EMAIL`/`GIT_COMMITTER_*`) com o nome e e-mail públicos do seu perfil GitHub (via `gh api user`: "Waldir Escouto Pereira", `wepbike@gmail.com`) só para os comandos de commit, sem persistir nada em disco.

Revisei o `git status` (80 arquivos, nenhum segredo, `.env` real não estava presente) e criei o commit inicial cobrindo as Fases 0-2. Criei o repositório público `wep1980/Java-Engineering-Lab` via `gh repo create --public --push` e confirmei via API que ficou com `visibility: PUBLIC`: https://github.com/wep1980/Java-Engineering-Lab

Documentei a política que você pediu ("sempre commits e pushs") em `CLAUDE.md` e em `docs/decisions/0004-repositorio-publico-e-push-automatico.md`, para que isso valha em sessões futuras também: a partir de agora, mudanças aprovadas são commitadas e enviadas sem eu pedir confirmação a cada vez — mas ações destrutivas (force-push, reset --hard, exclusão de branch) continuam exigindo sua confirmação explícita, como já era o padrão. Atualizei também `docs/links.md` e o `README.md` com a URL real do repositório.

Commitei e subi essas atualizações de documentação (commit `0ec2b18`).

---

## Interação 2026-08-22 20:47:38

### Usuário

sim, avança para a Fase 3

### Claude

Implementei o laboratório de N+1 por completo (`SPEC-LAB-N1-001`), atualizando a SPEC com o escopo entregue antes do código.

**Domínio**: `Pedido`/`ItemPedido` (JPA, `@OneToMany` lazy por padrão), pacote `laboratorios/n1`. Massa de dados determinística (50 pedidos × 3 itens) semeada automaticamente no startup (`SeedDadosN1` + `InicializadorDadosN1`).

**Backend**: `PedidoRepository` com as três soluções (`JOIN FETCH`, `@EntityGraph`, DTO Projection via constructor expression) além da variante problemática; `ExecucaoN1Service` mede o custo real em queries via `Hibernate Statistics.getPrepareStatementCount()` (decisão registrada em ADR-0005, sem dependência nova); `ExecucaoN1Controller` expõe `POST /api/laboratorios/n1-queries/execucoes/{variante}`. Também precisei ativar JPA de verdade pela primeira vez (estava deliberadamente adiado desde a Fase 1): adicionei `spring-boot-starter-data-jpa`, driver PostgreSQL, e configurei o datasource.

Percalços técnicos resolvidos: Testcontainers 2.x renomeou os artefatos Maven (`org.testcontainers:postgresql` → `testcontainers-postgresql`, idem `junit-jupiter`) — corrigido via `testcontainers-bom` no `dependencyManagement`; o teste de contexto genérico (`JavaEngineeringLabApplicationTests`) parou de subir sem um Postgres real disponível, então passou a usar Testcontainers também, em vez de mockar a única coisa que o teste realmente valida.

**Testes**: `ExecucaoN1ServiceIntegrationTest` — 4 testes com Testcontainers (PostgreSQL real) comprovando a contagem *exata* de queries: 51 para a variante problemática (1 + 50), 1 para cada uma das três corrigidas. `ExecucaoN1ControllerTest` cobre o contrato HTTP e o caso de variante inválida (400). 12/12 testes passando no total.

**Frontend**: proxy same-origin (`app/api/laboratorios/[id]/execucoes/[variante]/route.ts`) para o navegador nunca precisar chamar o backend diretamente (evita CORS, funciona igual em dev e em Docker); `PainelExecucaoN1` (client component) com os 4 botões, métricas em tempo real e comparação "antes × depois"; `ConteudoLaboratorioN1` com o conteúdo educacional (arquitetura, código problemático, as três soluções com trade-offs, por que EAGER não é solução, perguntas de entrevista). Catálogo atualizado: N+1 agora `DISPONIVEL`.

**Validação real** (não só "deveria funcionar"):
- `mvn test`: 12/12 passando, incluindo os 4 testes de integração com Postgres real via Testcontainers.
- `docker compose --profile core up --build`: ambiente completo, dados semeados automaticamente; `curl` manual nas 4 variantes + catálogo confirmou os números exatos (51/1/1/1 queries, `origemDados: REAL`) e o `404` para variante inválida.
- **Testei no Chrome de verdade**: cliquei nos 4 botões da página `/laboratorios/n1-queries` uma por uma — os números batem exatamente com os da API, o card "antes × depois" apareceu corretamente, zero erros no console.
- Ambiente derrubado ao final (`docker compose down` + remoção do volume de teste do Postgres); nada ficou rodando.

**Adiado deliberadamente**: RF-05 (paginação) — documentado em `SPEC-LAB-N1-001` como pendência explícita, não como requisito descartado, porque implementá-la direito exige também demonstrar o efeito de "paginação em memória" do Hibernate com `JOIN FETCH`/`@EntityGraph`, que é conteúdo à parte.

Documentação atualizada: `SPEC-LAB-N1-001`, `SPEC-JEL-003`, `README.md`, `docs/roadmap.md`, `docs/links.md`, `docs/testing-guide.md`, novo `docs/decisions/0005-instrumentacao-de-queries-via-hibernate-statistics.md`.

Commitei e subi tudo (commit `fa543d8`) e confirmei que os dois workflows de CI (Backend CI, Frontend CI) rodaram e passaram no GitHub Actions — inclusive os testes com Testcontainers funcionaram no runner do GitHub, não só localmente.

**Próximo passo natural**: Fase 4 (Race Condition / Lost Update), que também depende da sua aprovação antes de começar.

---

## Interação 2026-08-22 21:05:11

### Usuário

sim, avança para a Fase 4

### Claude

Criei `SPEC-LAB-RACE-001-race-condition-lost-update.md` antes do código, seguindo o esquema de IDs (ADR-0003).

**Domínio**: conta bancária com 10 depósitos de R$ 100 concorrentes reais (`ExecutorService` + `CountDownLatch` como barreira de largada — todas as threads liberadas ao mesmo tempo). Duas entidades foram necessárias por uma restrição real do JPA: `@Version` se aplica a toda escrita de uma entidade incondicionalmente, então não dá para ter "a mesma entidade" ora com controle otimista ora sem. `ContaBancaria` (sem `@Version`, usada por `sem-controle` e `pessimista`) e `ContaBancariaVersionada` (com `@Version`, usada por `otimista`), ambas herdando de `ContaBancariaBase` (`@MappedSuperclass`) para não duplicar campos/lógica de domínio. Documentei essa decisão na própria SPEC.

**As três variantes**: cada operação faz leitura → espera artificial de 100ms → escrita — técnica padrão para ampliar a janela de corrida e tornar o bug 100% reproduzível em teste automatizado (documentado explicitamente como técnica de teste, não prática de produção). `sem-controle`: sem nenhum controle. `otimista`: `@Version` + retentativa em `ObjectOptimisticLockingFailureException` (sem repetir a espera artificial nas retentativas, só na primeira tentativa). `pessimista`: `SELECT ... FOR UPDATE` via `@Lock(PESSIMISTIC_WRITE)`, serializando o acesso. Também aumentei o pool do HikariCP (10→20) já que 10 transações concorrentes seguram conexão durante a espera artificial.

**Testes**: `ExecucaoRaceConditionServiceIntegrationTest` com Testcontainers e concorrência real — 3 testes comprovando exatamente o esperado: `sem-controle` perde 9 de 10 atualizações de forma **determinística** (não só "às vezes"), `otimista` converge para o saldo correto com conflitos reais detectados, `pessimista` converge sem nenhum conflito. Rodei a suite 3 vezes seguidas para garantir que não é flaky — passou nas três. 17/17 testes no total.

**Validação real contra o ambiente**: `curl` no Docker Compose real mostrou exatamente o trade-off que a SPEC previa — `sem-controle` R$ 100 em 202ms (rápido e errado), `otimista` R$ 1.000 com 45 conflitos em 241ms, `pessimista` R$ 1.000 com 0 conflitos mas em 1123ms (~5x mais lento — o custo real da serialização). **Testei no Chrome de verdade**: os três botões, com o card de saldo final ficando vermelho quando há perda e verde quando não há. Zero erros no console.

Frontend reaproveitou o proxy genérico já criado na Fase 3 (`app/api/laboratorios/[id]/execucoes/[variante]/route.ts`) sem nenhuma mudança — validou que aquele design generalizava bem para um segundo laboratório. Catálogo atualizado: `race-condition` agora `DISPONIVEL`.

Documentação atualizada: `SPEC-LAB-RACE-001` (evidências completas), `README.md`, `docs/roadmap.md`, `docs/links.md`, `docs/testing-guide.md`.

Ambiente derrubado ao final, nada ficou rodando. Vou commitar e subir agora.

**Próximo passo natural**: Fase 5 (Kafka / Mensagem Duplicada / Idempotência), que também depende da sua aprovação.
