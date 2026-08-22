# Fluxo padrão de um laboratório

> Status: proposta inicial. Referência: seção 17 do prompt mestre do
> projeto, registrado em `docs/conversation-history.md`.

```mermaid
flowchart TD
    A[Introducao e objetivo] --> B[Arquitetura visual]
    B --> C[Executar cenario problematico]
    C --> D[Observar: metricas, logs, queries, traces]
    D --> E[Diagnosticar causa raiz]
    E --> F[Ver codigo problematico]
    F --> G[Conhecer solucoes e trade-offs]
    G --> H[Aplicar solucao]
    H --> I[Executar cenario corrigido]
    I --> J[Comparar antes x depois]
    J --> K[Entender trade-offs]
    K --> L[Explicar em entrevista]
```

## Diagrama de sequência — laboratório de N+1 (variante problemática)

Referência de implementação: `specs/labs/SPEC-LAB-N1-001-n-mais-um-queries.md`.

```mermaid
sequenceDiagram
    participant U as Usuário (frontend)
    participant API as Backend (Controller)
    participant SRV as Service
    participant DB as PostgreSQL

    U->>API: GET /api/laboratorios/n1-queries/execucoes?variante=problematico
    API->>SRV: listarPedidosComItens()
    SRV->>DB: SELECT * FROM pedido
    DB-->>SRV: N pedidos
    loop para cada um dos N pedidos
        SRV->>DB: SELECT * FROM item_pedido WHERE pedido_id = ?
        DB-->>SRV: itens do pedido
    end
    SRV-->>API: lista de pedidos com itens + métricas (1 + N queries)
    API-->>U: resposta com dados e métricas REAL
```
