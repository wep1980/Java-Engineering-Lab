# SPEC-LAB-MEMLEAK-001 — Laboratório: Memory Leak / OutOfMemoryError

- **Status**: Implementada e validada (2026-08-27) — ver "Evidências de
  conclusão" ao final deste documento
- **Título**: Um cache singleton sem limite retém memória mesmo depois
  de um `System.gc()` real — a diferença entre uma referência forte e
  uma referência fraca
- **Depende de**: `SPEC-JEL-003` (plataforma base)
- **Fase do roadmap**: pós-Fase 8 (item do backlog de laboratórios)

## Contexto

O padrão de memory leak mais comum em aplicações Spring reais não é
"esquecer de `close()` alguma coisa" — é um **bean singleton
(escopo de vida da aplicação inteira) guardando referências fortes**
para objetos que deveriam ter vida curta, tipicamente um cache "que
parecia uma boa ideia" e nunca ganhou uma política de expiração. Cada
entrada adicionada nunca é removida; o coletor de lixo não pode
reclamar memória que ainda está referenciada por um GC root — mesmo
que, do ponto de vista do negócio, aquele dado já não sirva para nada.

Diferente dos laboratórios anteriores, este não usa nenhuma
infraestrutura externa — é inteiramente sobre o comportamento real da
JVM (heap, GC, tipos de referência).

**Decisão deliberada de segurança**: este laboratório mede vazamento
real através do heap **sem nunca provocar um `OutOfMemoryError` de
verdade**. O backend é um processo compartilhado por todos os
laboratórios — deixar uma execução esgotar a heap derrubaria a
aplicação inteira, quebrando o isolamento que toda SPEC anterior deste
projeto validou explicitamente (RNF de isolamento da plataforma). A
demonstração usa alocações pequenas e controladas (~20 MB por
execução) e mede a diferença real de heap antes/depois de um
`System.gc()` real — o suficiente para provar a causa raiz de um
vazamento sem nenhum risco para o resto da plataforma.

## Domínio de demonstração

Dois beans singleton (escopo de aplicação inteira, o mesmo escopo de
qualquer `@Service`/`@Component` do Spring):

```text
CacheComVazamento  -- Map<UUID, byte[]> comum (referência forte)
CacheSemVazamento  -- WeakHashMap<UUID, byte[]> (referência fraca)
```

Cada execução adiciona 200 entradas de 100 KB (~20 MB no total) à
respectiva cache, usando um `UUID` novo como chave a cada entrada (sem
manter nenhuma referência externa às chaves depois de adicionadas).

- **`com-vazamento`**: `Map` comum. A cache (um bean singleton, GC
  root) segura uma referência forte a cada entrada para sempre — nada
  além de removê-la explicitamente libera essa memória. Um
  `System.gc()` real, forçado logo depois, não recupera nada.
- **`sem-vazamento`**: `WeakHashMap`. As chaves (`UUID`) não têm
  nenhuma outra referência forte no sistema depois que o método
  retorna — o coletor de lixo pode reclamá-las livremente, e quando
  reclama, a entrada inteira (chave e valor) desaparece do mapa. Um
  `System.gc()` real, forçado logo depois, recupera a maior parte.

## Objetivo

Demonstrar, com alocação e coleta de lixo reais da JVM (não simuladas,
não estimadas), a causa raiz de um memory leak clássico em aplicações
Spring — um singleton retendo referências fortes indefinidamente — e a
correção usando o tipo de referência correto para o ciclo de vida real
dos dados, sem nunca colocar em risco a estabilidade do processo
compartilhado.

## Requisitos funcionais

| ID | Requisito |
|---|---|
| RF-01 | Endpoint mede o heap real usado (`MemoryMXBean.getHeapMemoryUsage().getUsed()`) antes de qualquer alocação. |
| RF-02 | Variante `com-vazamento`: adiciona 200 entradas de 100 KB a um `Map` comum dentro de um bean singleton. |
| RF-03 | Variante `sem-vazamento`: adiciona as mesmas 200 entradas de 100 KB a um `WeakHashMap` dentro de outro bean singleton. |
| RF-04 | Depois de alocar, o serviço força uma coleta de lixo real (`System.gc()`, com uma pequena espera para dar tempo do coletor concluir) e mede o heap novamente. |
| RF-05 | Resposta reporta `heapAntesBytes`, `heapDepoisBytes`, `crescimentoRetidoBytes` (a diferença real, depois do GC), `tamanhoCacheAposExecucao` (tamanho real da cache depois do GC) e `vazamentoDetectado` (`crescimentoRetidoBytes` acima de um limiar), todos de medição real (`origemDados: REAL`). |
| RF-06 | Página do laboratório expõe as duas variantes com conteúdo educacional (tipos de referência em Java, por que um singleton é um GC root perigoso para caches sem limite, trade-offs, perguntas de entrevista), incluindo o Assistente de IA já existente. |

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | Nenhuma infraestrutura nova, nenhuma dependência nova — só APIs padrão da JVM (`java.lang.management`, `java.lang.ref` via `WeakHashMap`). |
| RNF-02 | O tamanho total alocado por execução (~20 MB) é pequeno o bastante para nunca ameaçar a estabilidade do processo compartilhado, mesmo somado ao longo de várias execuções manuais da variante `com-vazamento` durante uma sessão de demonstração — ver "Decisão deliberada de segurança". |
| RNF-03 | Testes automatizados comprovam, com margem de tolerância (não igualdade exata, por depender de um `System.gc()` real — ver Riscos): `com-vazamento` retém a maior parte da memória alocada após o GC; `sem-vazamento` recupera a maior parte. |

## Design técnico — decisões

### Bean singleton, não campo `static`

Um campo `static` também vazaria, mas um **bean singleton do Spring**
é o vetor real e muito mais comum de memory leak em aplicações desse
framework — o mesmo escopo de qualquer `@Service` já usado em todo o
projeto. Usar um bean singleton em vez de `static` deixa a lição mais
próxima do que realmente aparece em código de produção.

### `WeakHashMap`, não um cache com limite de tamanho manual

Uma correção alternativa válida seria um cache com política de
expiração explícita (ex.: LRU com tamanho máximo). `WeakHashMap` foi
escolhido de propósito porque ilustra o mecanismo mais fundamental —
tipos de referência em Java (`java.lang.ref.WeakReference`) — sem
precisar de nenhuma lógica de eviction escrita à mão: assim que nada
mais no sistema referencia uma chave, o próprio coletor de lixo a
remove, e a entrada correspondente desaparece do mapa. O conteúdo
educacional menciona a alternativa de cache com limite como uma opção
igualmente válida na prática.

### `System.gc()` real, com tolerância documentada

`System.gc()` é um **pedido**, não uma garantia da especificação da
JVM. Na prática, com G1GC (padrão desde Java 9) e o runtime usado
neste projeto (Eclipse Temurin 21), uma chamada explícita
normalmente resulta numa coleta completa real — mas os testes
automatizados usam margens de tolerância generosas, não igualdade
exata, reconhecendo essa garantia fraca (ver Riscos).

## Critérios de aceite

- [x] Variante `com-vazamento` retém a maior parte dos ~20 MB alocados mesmo após um `System.gc()` real (`vazamentoDetectado == true`).
- [x] Variante `sem-vazamento` recupera a maior parte dos ~20 MB alocados após o mesmo `System.gc()` real (`vazamentoDetectado == false`).
- [x] `tamanhoCacheAposExecucao` reflete o estado real de cada cache (cresce indefinidamente em `com-vazamento`; volta perto de zero em `sem-vazamento`, via `WeakHashMap.size()` real, que expurga entradas mortas).
- [x] Nenhuma execução, isolada ou repetida algumas vezes, coloca em risco a estabilidade do backend compartilhado.
- [x] Página do laboratório com as duas variantes executáveis e conteúdo educacional, testada no navegador.
- [x] Nenhuma métrica fabricada — heap antes/depois vem de `MemoryMXBean` real, coleta de lixo é uma chamada real ao coletor.
- [x] `docker compose --profile core up` revalidado sem regressão ao final.

## Achados reais durante a implementação

- **Linha de base medida antes de qualquer GC**: a primeira versão
  media `heapAntesBytes` sem forçar uma coleta de lixo antes. Isso
  fazia a medição incluir lixo geral da JVM ainda não coletado
  (execuções de teste anteriores, overhead do framework, etc.) — esse
  lixo geral era coletado junto na `System.gc()` chamada depois da
  alocação, então a diferença `heapDepoisBytes - heapAntesBytes`
  ficava mascarada (no primeiro teste real, deu `0` até para a
  variante `com-vazamento`, que deveria reter quase toda a memória
  alocada). Corrigido forçando uma coleta de lixo **também** antes de
  medir a linha de base — as duas medições passam a partir do mesmo
  estado "assentado" pós-GC, e a diferença reflete exclusivamente o
  que a própria execução alocou e reteve (ou não).
- **`WeakHashMap` não libera os valores sozinho**: mesmo depois de uma
  coleta de lixo real limpar a referência fraca de uma chave, o
  **valor** associado (o `byte[]`) continua preso — a entrada
  correspondente na tabela interna do `WeakHashMap` só é removida
  (expurgada) durante uma operação real no mapa (`size()`, `get()`,
  `put()`...), não sozinha em segundo plano. Sem chamar `tamanho()`
  antes da medição final de heap, o primeiro teste real mostrou os
  ~20 MB inteiros "retidos" mesmo na variante `sem-vazamento` — a
  demonstração dizia o oposto do que deveria. Corrigido: chamar
  `tamanho()` (que expurga as entradas mortas) **depois** do primeiro
  GC e **antes** de um segundo GC final, que só então consegue
  reclamar os valores de verdade.

## Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| `System.gc()` não ser honrado de forma síncrona/completa pela JVM em algum ambiente | Números de heap com ruído, teste automatizado quebradiço | Chamada dupla com pequena espera entre elas; testes com margem de tolerância generosa (fração do total alocado), não igualdade exata |
| Execuções repetidas da variante `com-vazamento` acumularem memória real ao longo de uma sessão de demonstração longa | Uso de heap crescente do backend compartilhado | ~20 MB por execução é pequeno o bastante para dezenas de cliques não ameaçarem a estabilidade (heap padrão da JVM neste ambiente é da ordem de gigabytes); documentado explicitamente que reiniciar o backend limpa o estado |
| Confundir "não atingimos OutOfMemoryError de propósito" com "o laboratório não é real" | Percepção de demonstração fraca | Conteúdo do laboratório explícito sobre a decisão de segurança (RNF-02) — o mecanismo (retenção de referência forte vs. fraca, medido via heap real) é 100% real, só o tamanho é deliberadamente pequeno |

## Evidências de conclusão (2026-08-27)

- **Testes automatizados reais** (sem Testcontainers — nenhuma
  infraestrutura externa envolvida, só heap e GC reais da própria
  JVM): `ExecucaoMemoriaServiceTest` (2 testes): `com-vazamento` →
  retém mais da metade dos ~20 MB alocados, `vazamentoDetectado:
  true`; `sem-vazamento` → retém menos da metade,
  `vazamentoDetectado: false`. `ExecucaoMemoriaControllerTest` (2
  testes): passando. Suíte completa do backend: **53/53 testes
  passando** (49 anteriores + 4 deste laboratório).
- **Execução real via `curl`, contra o Docker Compose real**, 4
  execuções consecutivas de cada variante: `com-vazamento` → sempre
  ~20,9 MB retidos após GC real, `vazamentoDetectado: true`,
  `tamanhoCacheAposExecucao` crescendo cumulativamente (200, 400, 600,
  800 — a cache nunca esvazia, exatamente o comportamento esperado de
  um vazamento real dentro da mesma JVM); `sem-vazamento` → sempre
  poucos KB de ruído retidos (496 a 5.056 bytes, nunca perto do limiar
  de 10 MB), `vazamentoDetectado: false`,
  `tamanhoCacheAposExecucao: 0` em todas as execuções (o `WeakHashMap`
  expurga de verdade). Variante inválida → `400`.
- **Dois achados reais durante a implementação**, ambos descobertos
  porque a primeira versão da medição contava uma história errada com
  números reais (ver "Achados reais" acima): linha de base medida
  antes de qualquer GC (mascarava o crescimento retido); e
  `WeakHashMap` não libera os valores sozinho (exigia chamar
  `tamanho()` entre os dois GCs para expurgar as entradas mortas antes
  da medição final).
- **Isolamento do restante da plataforma confirmado**: uma execução
  real de `com-vazamento` foi disparada em paralelo com uma execução
  do laboratório de N+1, que respondeu normalmente em 51ms enquanto a
  alocação/coleta ainda rodava (1418ms).
- **Validação visual real no Chrome**: as duas variantes executadas
  via clique real, mostrando os mesmos números reais acima — "Retido
  após GC real" e "Vazamento detectado" em vermelho para
  `com-vazamento` (19,9 MB / Sim) e verde para `sem-vazamento` (0,0 MB
  / Não).
- **Sem regressão**: `mvn -B verify` (53/53), `npm run build`/`lint`
  sem erros, `docker compose --profile core up` revalidado ao final,
  ambiente encerrado de forma limpa.

## Observação de status

Implementação concluída e validada nesta interação (2026-08-27), a
partir da aprovação explícita do usuário para começar este item do
backlog.
