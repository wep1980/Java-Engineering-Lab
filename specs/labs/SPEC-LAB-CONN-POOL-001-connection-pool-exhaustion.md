# SPEC-LAB-CONN-POOL-001 — Laboratório: Connection Pool Exhaustion

- **Status**: Implementada e validada (2026-08-23) — ver "Evidências de
  conclusão" ao final deste documento
- **Título**: Esgotamento do pool de conexões sob concorrência real, e
  por que reduzir o tempo de retenção da conexão costuma ser mais eficaz
  que apenas aumentar o tamanho do pool
- **Depende de**: `SPEC-JEL-003` (plataforma base)
- **Fase do roadmap**: pós-Fase 8 (item do backlog de laboratórios)

## Contexto

Connection Pool Exhaustion é um problema clássico de entrevista Java
Sênior: uma aplicação segura conexões de banco por mais tempo do que o
estritamente necessário (ex.: fazendo uma chamada externa lenta, ou
processamento pesado, enquanto ainda está com a conexão JDBC em mãos).
Sob carga concorrente, o pool — um recurso finito e caro — esgota antes
do esperado, e requisições começam a falhar por timeout esperando uma
conexão livre, mesmo que o banco em si esteja ocioso e saudável.

A correção ingênua ("aumentar o tamanho do pool") funciona até um certo
ponto, mas não escala: cada conexão a mais consome memória tanto na
aplicação quanto no banco (que também tem um limite de conexões
simultâneas). A correção mais robusta, na maioria dos casos reais, é
reduzir o tempo de retenção da conexão — fazer o trabalho lento **fora**
do escopo em que a conexão está aberta.

## Domínio de demonstração

Não introduz nenhuma entidade de negócio nova. Usa dois `HikariDataSource`
dedicados e isolados do pool principal da aplicação (que continua
compartilhado pelos demais laboratórios, sem ser afetado por este):

- **Pool pequeno** (tamanho 2) — usado pelas variantes `pool-pequeno` e
  `conexao-curta`.
- **Pool redimensionado** (tamanho 12) — usado pela variante
  `pool-redimensionado`.

Ambos apontam para o **mesmo** PostgreSQL já usado pela aplicação (via
`JdbcConnectionDetails`, que funciona identicamente em produção/Docker
Compose e em testes com Testcontainers + `@ServiceConnection` — não
precisa de configuração duplicada nem de um novo serviço no
`docker-compose.yml`).

Cada uma das 10 "requisições" concorrentes executa uma consulta SQL
trivial (`SELECT 1`) e, a depender da variante, faz um trabalho lento
simulado (`Thread.sleep`, 500ms — mesma técnica de teste já documentada
e aceita em `SPEC-LAB-RACE-001`) **antes ou depois** de obter a conexão.

## Objetivo

Demonstrar, com concorrência real (não simulada), o esgotamento real do
pool de conexões e comparar duas correções: aumentar o pool (funciona,
mas não é a lição principal) vs. reduzir o tempo de retenção da conexão
(a correção que realmente escala, mesmo com o pool pequeno).

## Requisitos funcionais

| ID | Requisito |
|---|---|
| RF-01 | Endpoint dispara 10 requisições concorrentes reais (`ExecutorService` + barreira de largada, mesmo padrão de `SPEC-LAB-RACE-001`) para a variante `pool-pequeno`: pool de tamanho 2, conexão obtida **antes** do trabalho lento (retida durante os 500ms). |
| RF-02 | Variante `pool-redimensionado`: mesmo código de retenção de conexão da variante problemática, mas usando o pool de tamanho 12. |
| RF-03 | Variante `conexao-curta`: usa o **mesmo pool pequeno** (tamanho 2) da variante problemática, mas o trabalho lento acontece **antes** de obter a conexão — a conexão só é aberta para a consulta em si, quase instantânea. |
| RF-04 | Resposta reporta `tamanhoDoPool`, `quantidadeRequisicoesConcorrentes`, `quantidadeSucesso` e `quantidadeFalhasPorTimeout`, todos calculados a partir de execução real (`origemDados: REAL`) — falhas por timeout vêm de `SQLTransientConnectionException` real lançada pelo HikariCP, não fabricadas. |
| RF-05 | Página do laboratório expõe as três variantes com conteúdo educacional (causa, sintomas, as duas correções e seus trade-offs, perguntas de entrevista), incluindo o Assistente de IA já existente com contexto desta execução. |

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | Os pools de demonstração são completamente isolados do pool principal da aplicação (`spring.datasource.hikari.maximum-pool-size: 20`) — esgotar os pools de demonstração não pode afetar nenhum outro endpoint/laboratório rodando ao mesmo tempo. |
| RNF-02 | `connectionTimeout` dos pools de demonstração é curto o bastante (800ms) para que o resultado apareça rapidamente numa demonstração interativa, sem exigir espera longa do usuário. |
| RNF-03 | Testes de integração com Testcontainers comprovam: `pool-pequeno` produz pelo menos uma falha real por timeout; `pool-redimensionado` e `conexao-curta` não produzem nenhuma falha, mesmo `conexao-curta` usando o pool pequeno. |

## Design técnico — decisões

### `JdbcConnectionDetails`, não `@Value("${spring.datasource.url}")`

Os pools de demonstração precisam apontar para o mesmo banco que o pool
principal. Injetar `@Value("${spring.datasource.url}")` funcionaria em
produção (a propriedade existe no `application.yml`), mas **falharia em
testes** com Testcontainers + `@ServiceConnection` — essa anotação não
define a propriedade `spring.datasource.url`, ela registra um bean
`JdbcConnectionDetails` que o `DataSourceAutoConfiguration` consome
diretamente. Injetando `JdbcConnectionDetails` (interface estável desde
o Spring Boot 3.1, presente em `org.springframework.boot.jdbc.autoconfigure`
no Spring Boot 4.1) o mesmo código funciona nos dois contextos sem
duplicação de configuração.

### Pools construídos no serviço, não como `@Bean HikariDataSource`

Achado real durante a implementação, documentado em
`docs/decisions/0009-pools-de-demonstracao-nao-sao-beans-de-datasource.md`:
registrar os pools de demonstração como `@Bean HikariDataSource` avulsos
quebra `@ConditionalOnSingleCandidate(DataSource.class)` da
autoconfiguração do JPA (sem nenhum marcado `@Primary`, o Spring deixa
de encontrar um "único candidato" de `DataSource` e desiste
silenciosamente de criar o `entityManagerFactory` — derrubando **todos**
os laboratórios, não só este). Os pools são construídos diretamente no
construtor de `ExecucaoConnPoolService`, a partir do
`JdbcConnectionDetails` injetado (que não é do tipo `DataSource`, logo
não interfere), e encerrados via `@PreDestroy`.

### Por que dois pools (não um pool com tamanho ajustável em runtime)

`HikariDataSource.setMaximumPoolSize()` em runtime não é a forma
recomendada de alterar o comportamento de um pool já em uso durante um
teste de carga (o HikariCP não garante rebalanceamento imediato e
correto das conexões existentes). Dois beans `HikariDataSource`
separados, cada um com seu tamanho fixo desde a criação, dão um
resultado determinístico e correto para cada variante.

### Por que a mesma técnica de `Thread.sleep` já usada em `SPEC-LAB-RACE-001`

Mesma justificativa já documentada naquela SPEC: é uma técnica padrão
de teste para tornar o cenário de concorrência determinístico e
reproduzível, não um padrão de produção. Aqui simula uma chamada
externa lenta ou processamento pesado — o tipo de código que, na vida
real, não deveria estar "dentro" do escopo de uma conexão JDBC aberta.

## Critérios de aceite

- [x] Variante `pool-pequeno` produz pelo menos uma falha real por timeout (`SQLTransientConnectionException`), com o pool principal da aplicação (usado pelos outros laboratórios) permanecendo saudável durante a execução.
- [x] Variante `pool-redimensionado` não produz nenhuma falha (mesmo trabalho lento retendo a conexão, mas pool com folga suficiente).
- [x] Variante `conexao-curta` não produz nenhuma falha, usando o mesmo pool pequeno da variante problemática — prova de que o tamanho do pool não era a única solução possível.
- [x] Página do laboratório com as três variantes executáveis e conteúdo educacional, testada no navegador.
- [x] Nenhuma métrica fabricada — falhas/sucessos vêm de exceções e execuções reais contra PostgreSQL real.
- [x] `docker compose --profile core up` revalidado sem regressão ao final.

## Evidências de conclusão (2026-08-23)

- **Achado real durante a implementação**: a primeira versão registrava
  os pools de demonstração como `@Bean HikariDataSource` avulsos, o que
  quebrou silenciosamente a criação do `entityManagerFactory` do JPA
  para **todos** os laboratórios (não só este) — ver
  `docs/decisions/0009-pools-de-demonstracao-nao-sao-beans-de-datasource.md`.
  Corrigido construindo os pools diretamente no serviço, a partir de
  `JdbcConnectionDetails`.
- **Testes de integração reais** (Testcontainers, 3 testes,
  `ExecucaoConnPoolServiceIntegrationTest`): `pool-pequeno` produz
  falhas reais (`> 0`); `pool-redimensionado` e `conexao-curta` não
  produzem nenhuma. Suíte completa do backend: 29/29 testes passando
  (24 anteriores + 2 do controller + 3 de integração deste
  laboratório).
- **Execução real via `curl`, contra o Docker Compose real**:
  `pool-pequeno` → 4 sucessos, 6 falhas reais por timeout, 1004ms;
  `pool-redimensionado` → 10 sucessos, 0 falhas, 504ms;
  `conexao-curta` → 10 sucessos, 0 falhas, **505ms** — praticamente
  empatada com o pool redimensionado (12 conexões) usando apenas 2,
  reforçando com números reais que reduzir o tempo de retenção da
  conexão não é só mais robusto, também não é mais lento na prática.
- **Isolamento do pool principal validado de propósito** (RNF-01): uma
  execução real de `pool-pequeno` (gerando 6 falhas) foi disparada em
  paralelo com uma execução do laboratório de N+1, que respondeu
  normalmente em 57ms — confirma que esgotar o pool de demonstração não
  afeta o resto da plataforma.
- **Validação visual real no navegador** (Chrome): as três variantes
  executadas via clique real, com os cards mostrando os números reais
  acima, "Falhas por timeout" em vermelho quando `> 0` e verde quando
  `0`, e o Assistente de IA reagindo corretamente ao contexto da última
  execução.
- **Sem regressão**: `mvn -B verify` (29/29), `npm run build`/`lint`
  sem erros, `docker compose --profile core up` revalidado ao final,
  ambiente encerrado de forma limpa.

## Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Números de sucesso/falha variarem entre execuções por causa do agendamento real de threads do SO | Resultado não 100% determinístico | Margem folgada nos parâmetros (pool de 2 vs. 10 requisições, timeout de 800ms vs. 500ms de trabalho lento) garante a direção do resultado (existência de falhas) de forma robusta, mesmo que a contagem exata varie — testes automatizados usam limites (`> 0` / `== 0`), não igualdade exata |
| Pool de demonstração pequeno afetar acidentalmente o pool principal | Regressão em outros laboratórios | Pools completamente separados (RNF-01), validado explicitamente rodando outro laboratório durante/logo após a execução deste |

## Observação de status

Implementação concluída e validada nesta interação (2026-08-23), a
partir da aprovação explícita do usuário para começar este item do
backlog.
