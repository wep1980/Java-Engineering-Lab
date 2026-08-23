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

    private static final Map<String, String> CONHECIMENTO = Map.of(
            "n1-queries", """
                    Laboratório de N+1 Queries: uma consulta inicial retorna N registros \
                    (Pedidos), e acessar uma associação lazy (itens) para cada um dispara \
                    uma consulta adicional por registro -- 1 + N consultas. Soluções: \
                    JOIN FETCH (1 query, mas paginação em coleções *-to-many é feita em \
                    memória pelo Hibernate), @EntityGraph (mesma restrição de paginação, \
                    mais declarativo), DTO Projection (paginação segura, mas não carrega \
                    a entidade completa). Trocar tudo para FetchType.EAGER NÃO é solução \
                    -- move o custo do N+1 para toda consulta que carregue a entidade, \
                    mesmo quando os itens não são necessários.""",
            "race-condition", """
                    Laboratório de Race Condition / Lost Update: duas requisições \
                    concorrentes leem o mesmo saldo, cada uma soma sobre o valor lido, e \
                    a segunda escrita sobrescreve a primeira -- uma atualização se perde \
                    silenciosamente, sem erro. Soluções: Optimistic Locking (@Version) -- \
                    o commit falha com ObjectOptimisticLockingFailureException se outra \
                    transação já alterou a linha, e quem chama decide re-tentar; bom para \
                    baixa/média contenção. Pessimistic Locking (SELECT ... FOR UPDATE) -- \
                    serializa o acesso concorrente, nunca perde nem gera conflito, mas é \
                    mais lento sob alta concorrência.""",
            "kafka-idempotencia", """
                    Laboratório de Mensagem Duplicada / Idempotência: Kafka garante \
                    at-least-once delivery -- o mesmo evento pode ser entregue mais de uma \
                    vez ao consumidor, isso é comportamento normal, não um bug. O problema \
                    é quando o efeito de negócio (ex.: creditar um valor) não é seguro \
                    para repetir. Solução: usar um identificador único do evento como \
                    chave de idempotência, verificando se ele já foi processado antes de \
                    aplicar o efeito. Diferença importante: semântica de entrega (quantas \
                    vezes o Kafka entrega) é diferente de processamento idempotente \
                    (detectar e ignorar repetição) que é diferente de efeito de negócio \
                    (o que realmente aconteceu no domínio).""",
            "connection-pool-exhaustion", """
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
                    pequeno e apenas reordenando quando a conexão é obtida.""",
            "deadlock", """
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
                    primeiro, nunca em ordens opostas."""
    );

    public String buscar(String laboratorioId) {
        return CONHECIMENTO.getOrDefault(laboratorioId,
                "Nenhum conhecimento específico disponível para este laboratório.");
    }
}
