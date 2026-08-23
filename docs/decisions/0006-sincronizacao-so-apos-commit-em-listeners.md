# ADR-0006 — Sinalizar conclusão de um `@KafkaListener` somente após o commit

- **Status**: Aceita
- **Data**: 2026-08-22
- **Origem**: bug real encontrado durante a implementação de
  `SPEC-LAB-KAFKA-IDEMP-001-mensagem-duplicada-idempotencia.md`

## Contexto

O laboratório de Kafka/idempotência precisa saber, de forma síncrona, o
momento exato em que um evento publicado terminou de ser processado (para
então ler o estado final e responder à requisição HTTP). A primeira
implementação usava uma `CountDownLatch`, contada dentro do próprio
método anotado com `@KafkaListener` e `@Transactional`:

```java
@KafkaListener(topics = TOPICO, ...)
@Transactional
public void receber(EventoPagamentoConfirmado evento) {
    // ... credita a carteira, salva ...
    latch.countDown(); // <- dentro do metodo transacional
}
```

Isso causou uma corrida real, observada em execução manual contra o
ambiente Docker Compose (com Kafka de verdade): o saldo final lido pela
thread que aguardava o `latch` às vezes refletia apenas 1 dos 2 créditos
esperados, mesmo com `quantidadeEventosConsumidos: 2` reportado
corretamente.

**Causa raiz**: o proxy de `@Transactional` do Spring executa o commit
*depois* que o corpo do método retorna — e `latch.countDown()`, sendo a
última linha do corpo do método, executa *antes* desse commit. A thread
que aguarda o latch pode acordar e ler o banco antes da escrita da última
mensagem processada estar de fato durável.

Notavelmente, os testes de integração com Testcontainers passaram mesmo
com o bug presente — a janela de corrida é curta o bastante para raramente
se manifestar em execução local rápida, mas apareceu de forma consistente
no ambiente Docker Compose mais "carregado". Isso reforça que passar em
testes automatizados não é garantia de ausência de bugs de concorrência
sutis — a validação manual contra o ambiente real (parte do processo
deste projeto) encontrou o que o teste, sozinho, não encontrou de forma
confiável.

## Decisão

A lógica transacional de um listener deve viver em um **bean separado**
do listener, com seu próprio método `@Transactional`. O método do
`@KafkaListener` em si não é transacional — ele apenas chama o bean
transacional e, só depois que essa chamada retorna (o que só acontece
após o commit, pois a chamada atravessa o proxy do Spring), sinaliza a
conclusão (contador, `latch`, etc.).

```java
@KafkaListener(topics = TOPICO, ...)
public void receber(EventoPagamentoConfirmado evento) {
    creditoOperacao.creditar(...); // bean separado, @Transactional
    latch.countDown();             // só executa apos o commit
}
```

Chamar um método `@Transactional` na mesma classe do listener (auto-invocação)
não teria funcionado — não passaria pelo proxy do Spring, e a anotação
seria ignorada silenciosamente.

## Consequências

- Todo laboratório futuro baseado em `@KafkaListener` (ou qualquer
  consumidor assíncrono) que precise sinalizar conclusão para fora deve
  seguir este padrão: lógica transacional em um bean à parte, sinalização
  no método do listener, após a chamada retornar.
- Reforça, mais uma vez, a importância de validar contra infraestrutura
  real (não só contra testes automatizados) antes de considerar uma
  funcionalidade concluída — ver `specs/manifest/MANIFESTO.md`.
