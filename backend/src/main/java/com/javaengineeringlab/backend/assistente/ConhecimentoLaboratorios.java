package com.javaengineeringlab.backend.assistente;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Conhecimento condensado por laboratório, usado para fundamentar
 * (grounding) as respostas do modelo — não duplica todo o conteúdo
 * educacional do frontend, só o essencial. Ver
 * SPEC-JEL-006-engineering-ai-assistant.md.
 */
@Component
public class ConhecimentoLaboratorios {

    private static final Map<String, String> CONHECIMENTO = Map.ofEntries(
            Map.entry("n1-queries", """
                    Laboratório de N+1 Queries: uma consulta inicial retorna N registros \
                    (Pedidos), e acessar uma associação lazy (itens) para cada um dispara \
                    uma consulta adicional por registro -- 1 + N consultas. Soluções: \
                    JOIN FETCH (1 query, mas paginação em coleções *-to-many é feita em \
                    memória pelo Hibernate), @EntityGraph (mesma restrição de paginação, \
                    mais declarativo), DTO Projection (paginação segura, mas não carrega \
                    a entidade completa). Trocar tudo para FetchType.EAGER NÃO é solução \
                    -- move o custo do N+1 para toda consulta que carregue a entidade, \
                    mesmo quando os itens não são necessários."""),
            Map.entry("race-condition", """
                    Laboratório de Race Condition / Lost Update: duas requisições \
                    concorrentes leem o mesmo saldo, cada uma soma sobre o valor lido, e \
                    a segunda escrita sobrescreve a primeira -- uma atualização se perde \
                    silenciosamente, sem erro. Soluções: Optimistic Locking (@Version) -- \
                    o commit falha com ObjectOptimisticLockingFailureException se outra \
                    transação já alterou a linha, e quem chama decide re-tentar; bom para \
                    baixa/média contenção. Pessimistic Locking (SELECT ... FOR UPDATE) -- \
                    serializa o acesso concorrente, nunca perde nem gera conflito, mas é \
                    mais lento sob alta concorrência."""),
            Map.entry("kafka-idempotencia", """
                    Laboratório de Mensagem Duplicada / Idempotência: Kafka garante \
                    at-least-once delivery -- o mesmo evento pode ser entregue mais de uma \
                    vez ao consumidor, isso é comportamento normal, não um bug. O problema \
                    é quando o efeito de negócio (ex.: creditar um valor) não é seguro \
                    para repetir. Solução: usar um identificador único do evento como \
                    chave de idempotência, verificando se ele já foi processado antes de \
                    aplicar o efeito. Diferença importante: semântica de entrega (quantas \
                    vezes o Kafka entrega) é diferente de processamento idempotente \
                    (detectar e ignorar repetição) que é diferente de efeito de negócio \
                    (o que realmente aconteceu no domínio)."""),
            Map.entry("connection-pool-exhaustion", """
                    Laboratório de Connection Pool Exhaustion: a aplicação segura uma \
                    conexão de banco durante um trabalho lento que não precisava dela \
                    (ex.: chamada externa, processamento pesado) -- sob concorrência, o \
                    pool (recurso finito) esgota antes do esperado e requisições falham \
                    por timeout esperando uma conexão livre, mesmo com o banco saudável e \
                    ocioso. Correção ingênua: aumentar o pool -- funciona, mas não escala \
                    (cada conexão a mais custa memória na aplicação e no banco, que também \
                    tem limite). Correção que realmente escala: reduzir o tempo de \
                    retenção da conexão -- fazer o trabalho lento FORA do escopo em que a \
                    conexão está aberta. O laboratório prova isso mantendo o MESMO pool \
                    pequeno e apenas reordenando quando a conexão é obtida."""),
            Map.entry("deadlock", """
                    Laboratório de Deadlock: duas transferências concorrentes entre as \
                    mesmas duas contas, em direções opostas, cada uma trava com sucesso \
                    uma conta e espera indefinidamente pela outra, que a outra \
                    transferência já travou -- espera circular. Diferente de Lost Update \
                    (falha silenciosa), o PostgreSQL detecta ativamente o ciclo e aborta \
                    uma das duas transações com um erro real (deadlock detected / \
                    CannotAcquireLockException). Solução: ordenar a aquisição de locks de \
                    forma consistente (por ID da conta, independente da direção da \
                    transferência) -- elimina matematicamente a espera circular, porque as \
                    duas transferências concorrentes sempre disputam a mesma conta \
                    primeiro, nunca em ordens opostas."""),
            Map.entry("query-sem-indice", """
                    Laboratório de Query sem índice: uma tabela com 200 mil linhas, sem \
                    índice na coluna usada no WHERE, força o PostgreSQL a fazer um Seq \
                    Scan (varre a tabela inteira) para encontrar uma única linha por \
                    igualdade de e-mail. Com um índice na mesma coluna, o otimizador \
                    escolhe um Index Scan (ou Index Only Scan), que localiza a linha \
                    diretamente. O laboratório usa EXPLAIN (ANALYZE, FORMAT JSON) real do \
                    PostgreSQL -- o tipo de nó (Node Type) e o tempo real (Actual Total \
                    Time) vêm do próprio banco, não são estimados nem medidos só do lado \
                    da aplicação. O índice é criado e removido de verdade (DROP INDEX / \
                    CREATE INDEX) a cada execução -- não é uma segunda tabela pré-indexada."""),
            Map.entry("circuit-breaker", """
                    Laboratório de Circuit Breaker: uma dependência externa simulada está \
                    completamente fora do ar (toda chamada demora 300ms e falha). Sem \
                    proteção, cada uma das 20 chamadas paga o custo total dessa latência \
                    antes de falhar. Com um circuit breaker real (Resilience4j, janela \
                    deslizante de 10 chamadas, mínimo de 5 antes de calcular a taxa, \
                    limite de 50% de falha), as 5 primeiras chamadas ainda falham de \
                    verdade -- é o preço de aprender que a dependência está fora do ar --, \
                    mas depois disso o circuito abre (estado real OPEN) e as 15 chamadas \
                    restantes são rejeitadas instantaneamente, sem sequer tentar a \
                    dependência. O circuito é uma máquina de estados real (CLOSED -> OPEN \
                    -> HALF_OPEN), não uma simulação -- o mesmo padrão usado em produção \
                    para evitar que uma dependência instável derrube a própria aplicação \
                    por exaustão de threads/conexões esperando por ela."""),
            Map.entry("transactional-outbox", """
                    Laboratório de Transactional Outbox: salvar no banco e publicar um evento \
                    no Kafka são duas operações separadas contra dois sistemas diferentes, sem \
                    garantia atômica entre elas -- se o banco confirma e o Kafka falha, o dado \
                    existe mas nenhum outro serviço jamais saberá disso (inconsistência \
                    silenciosa e permanente). O laboratório reproduz isso de verdade: a \
                    variante sem-outbox tenta publicar direto num endereço Kafka inalcançável \
                    (falha real de conexão, não fabricada) logo após o banco já ter confirmado. \
                    A correção -- Transactional Outbox -- escreve o Pedido E um registro do \
                    evento pendente na MESMA transação local (só o banco precisa ser atômico); \
                    um relay real (@Scheduled, roda a cada 200ms, independente da requisição \
                    HTTP) publica os eventos pendentes no Kafka real e marca como publicado só \
                    após confirmação real de entrega. Se o Kafka estivesse fora do ar, o evento \
                    simplesmente ficaria pendente no banco -- nunca é perdido, só adiado."""),
            Map.entry("ordenacao-de-eventos", """
                    Laboratório de Ordenação de Eventos: Kafka só garante ordem de entrega \
                    DENTRO de uma única partição -- um tópico com múltiplas partições (a \
                    forma como Kafka escala) não garante nenhuma ordem relativa entre eventos \
                    em partições diferentes. Qual partição um evento cai é decidido pela \
                    chave de particionamento usada ao publicar. A variante \
                    sem-chave-particionamento espalha 20 eventos do mesmo agregado pelas 3 \
                    partições reais de um tópico (round-robin explícito, mesmo efeito \
                    estrutural de publicar sem chave consistente); consumidos por um \
                    consumidor real com 3 threads (uma por partição), a ordem de chegada não \
                    é garantida. A variante com-chave-particionamento publica os mesmos 20 \
                    eventos usando o identificador do agregado como chave -- o particionador \
                    padrão do Kafka garante que a mesma chave sempre cai na mesma partição, \
                    então os 20 eventos ficam garantidamente numa única partição e chegam na \
                    ordem exata em que foram publicados. A lição: "Kafka preserva ordem" é \
                    uma meia-verdade -- depende inteiramente da chave de particionamento \
                    escolhida."""),
            Map.entry("memory-leak", """
                    Laboratório de Memory Leak: o padrão mais comum em aplicações Spring \
                    reais não é esquecer de fechar algum recurso -- é um bean singleton \
                    (escopo de vida da aplicação inteira, um GC root) guardando referências \
                    FORTES para objetos que deveriam ter vida curta, tipicamente um cache que \
                    nunca ganhou política de expiração. O laboratório mede heap real via \
                    MemoryMXBean antes e depois de adicionar 200 entradas de 100KB (~20MB) a \
                    duas caches singleton diferentes, seguido de um System.gc() real. A \
                    variante com-vazamento usa um Map comum -- o coletor de lixo não pode \
                    reclamar nada, porque a cache ainda referencia cada entrada, então o heap \
                    continua alto mesmo após o GC real. A variante sem-vazamento usa um \
                    WeakHashMap -- como nada mais no sistema referencia as chaves depois que o \
                    método retorna, o coletor de lixo as reclama livremente, e o heap volta \
                    perto do valor anterior. Por segurança (o backend é compartilhado por \
                    todos os laboratórios), a demonstração nunca provoca um OutOfMemoryError \
                    de verdade -- mede a retenção real de heap em escala pequena e segura \
                    (~20MB por execução), suficiente para provar a causa raiz sem nenhum \
                    risco à estabilidade do processo."""),
            Map.entry("thread-pool-exhaustion", """
                    Laboratório de Thread Pool Exhaustion: Executors.newFixedThreadPool(n) é a \
                    forma mais comum de criar um pool de threads em Java -- e esconde um \
                    problema real: por dentro, usa uma LinkedBlockingQueue SEM LIMITE de \
                    tamanho. Sob carga sustentada, a fila cresce indefinidamente sem nenhum \
                    erro, nenhuma rejeição -- degradação silenciosa (tarefas esperando cada \
                    vez mais tempo na fila), que em produção pode contribuir para um \
                    OutOfMemoryError (mesmo sintoma final do laboratório de Memory Leak, \
                    causado por um mecanismo completamente diferente: fila sem limite, não \
                    referência forte). A variante fila-ilimitada usa exatamente esse padrão -- \
                    10 tarefas, pool de 2 threads, todas aceitas, mas a última espera bastante \
                    tempo real na fila antes de começar. A variante fila-limitada usa um \
                    ThreadPoolExecutor construído manualmente com fila limitada (capacidade 2) \
                    e política de rejeição padrão -- só 4 das 10 tarefas são aceitas (pool + \
                    fila), as outras 6 são rejeitadas de verdade (RejectedExecutionException \
                    real) na hora, em vez de silenciosamente enfileiradas. A lição: fila \
                    limitada + rejeição falha rápido e de forma previsível; fila ilimitada \
                    esconde a sobrecarga até virar um problema maior depois."""),
            Map.entry("saga", """
                    Laboratório de Saga: uma operação de negócio com múltiplas etapas contra \
                    recursos/serviços diferentes (reservar estoque, depois cobrar pagamento) \
                    não tem uma transação de banco única cobrindo tudo -- cada etapa commita \
                    por conta própria. Se uma etapa posterior falha, as etapas anteriores JÁ \
                    aconteceram de verdade e não desfazem sozinhas. Neste laboratório, a etapa \
                    1 (reservar estoque) sempre funciona; a etapa 2 (cobrar pagamento) sempre \
                    falha de propósito (cartão simulado recusado, falha real e determinística). \
                    A variante sem-compensacao não faz nada quando a etapa 2 falha -- a reserva \
                    de estoque fica RESERVADA para sempre, presa a um pedido que nunca vai se \
                    completar. A variante com-compensacao dispara uma ação de compensação real \
                    (cancelarReserva) quando a etapa 2 falha -- a reserva volta para CANCELADA, \
                    um estado consistente, lido de volta do banco real após a execução. A \
                    implementação é orquestrada (chamada direta de método), não coreografada \
                    via Kafka -- decisão deliberada de simplicidade, já que a lição central \
                    (ação de compensação explícita) não depende de mensageria, e Kafka já foi \
                    demonstrado a fundo em três laboratórios anteriores.""")
    );

    public String buscar(String laboratorioId) {
        return CONHECIMENTO.getOrDefault(laboratorioId,
                "Nenhum conhecimento específico disponível para este laboratório.");
    }
}
