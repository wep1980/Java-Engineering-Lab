package com.javaengineeringlab.backend.laboratorios.saga;

import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Orquestra as duas etapas da saga de criação de pedido e, na
 * variante corrigida, a ação de compensação real quando a etapa 2
 * falha -- ver specs/labs/SPEC-LAB-SAGA-001-saga.md.
 */
@Service
public class ExecucaoSagaService {

    static final int QUANTIDADE_PADRAO = 1;
    static final BigDecimal VALOR_PADRAO = BigDecimal.valueOf(199.90);

    private final EstoqueService estoqueService;
    private final ProcessadorPagamento processadorPagamento;
    private final ReservaEstoqueRepository reservaEstoqueRepository;

    public ExecucaoSagaService(
            EstoqueService estoqueService,
            ProcessadorPagamento processadorPagamento,
            ReservaEstoqueRepository reservaEstoqueRepository
    ) {
        this.estoqueService = estoqueService;
        this.processadorPagamento = processadorPagamento;
        this.reservaEstoqueRepository = reservaEstoqueRepository;
    }

    public ResultadoExecucaoLaboratorio executar(VarianteSaga variante) {
        Instant inicio = Instant.now();
        UUID pedidoId = UUID.randomUUID();

        // Etapa 1: reservar estoque -- transação local própria, já
        // commitada quando o método retorna.
        estoqueService.reservar(pedidoId, QUANTIDADE_PADRAO);
        boolean estoqueReservado = true;

        boolean pagamentoAprovado = false;
        boolean compensacaoExecutada = false;

        try {
            // Etapa 2: cobrar pagamento -- sempre falha nesta
            // demonstração (falha real e determinística).
            processadorPagamento.cobrar(pedidoId, VALOR_PADRAO);
            pagamentoAprovado = true;
        } catch (PagamentoRecusadoException falhaRealDoPagamento) {
            if (variante == VarianteSaga.COM_COMPENSACAO) {
                // Compensação real da etapa 1 -- desfaz a reserva.
                estoqueService.cancelarReserva(pedidoId);
                compensacaoExecutada = true;
            }
        }

        // Lê de volta o estado real da reserva no banco, depois de
        // toda a orquestração -- não é o valor em memória, é o que
        // realmente ficou persistido.
        ReservaEstoque estadoFinal = reservaEstoqueRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new IllegalStateException("Reserva não encontrada após a execução da saga"));
        boolean estoqueConsistente = estadoFinal.getStatus() == StatusReserva.CANCELADA;

        long duracaoMs = Duration.between(inicio, Instant.now()).toMillis();

        VarianteExecucao varianteExecucao = variante == VarianteSaga.SEM_COMPENSACAO
                ? VarianteExecucao.PROBLEMATICO
                : VarianteExecucao.CORRIGIDO;

        Map<String, Object> metricas = Map.of(
                "tecnica", variante.name(),
                "estoqueReservado", estoqueReservado,
                "pagamentoAprovado", pagamentoAprovado,
                "compensacaoExecutada", compensacaoExecutada,
                "estoqueConsistente", estoqueConsistente
        );

        return new ResultadoExecucaoLaboratorio(
                UUID.randomUUID(),
                "saga",
                varianteExecucao,
                OrigemDados.REAL,
                inicio,
                duracaoMs,
                metricas
        );
    }
}
