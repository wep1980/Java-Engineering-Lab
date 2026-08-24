package com.javaengineeringlab.backend.laboratorios.indice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegistroBuscaRepository extends JpaRepository<RegistroBusca, Long> {

    /**
     * Inserção em lote via generate_series -- rápida mesmo em volume,
     * sem passar por entidade-por-entidade via JPA (ver SPEC-LAB-INDICE-001,
     * RNF-02 e seção de Riscos).
     */
    @Modifying
    @Query(value = """
            INSERT INTO registro_busca (email, nome)
            SELECT 'usuario' || gs || '@exemplo.com', 'Usuário ' || gs
            FROM generate_series(1, :quantidade) AS gs
            """, nativeQuery = true)
    void semearRegistros(@Param("quantidade") int quantidade);

    @Modifying
    @Query(value = "DROP INDEX IF EXISTS idx_registro_busca_email", nativeQuery = true)
    void removerIndice();

    @Modifying
    @Query(value = "CREATE INDEX IF NOT EXISTS idx_registro_busca_email ON registro_busca (email)", nativeQuery = true)
    void criarIndice();

    /**
     * ANALYZE atualiza as estatísticas que o otimizador usa para
     * decidir o plano -- sem isso, logo após popular ou indexar a
     * tabela, o otimizador pode escolher planos abaixo do ideal por
     * falta de estatísticas atualizadas.
     */
    @Modifying
    @Query(value = "ANALYZE registro_busca", nativeQuery = true)
    void atualizarEstatisticas();

    /**
     * Retorna o plano de execução real (JSON) da busca por email --
     * EXPLAIN ANALYZE executa a query de verdade e inclui o tempo real
     * (Actual Total Time) e o nó real escolhido pelo otimizador (Node
     * Type), direto do PostgreSQL (ver SPEC-LAB-INDICE-001, seção
     * "EXPLAIN (ANALYZE, FORMAT JSON), não medição só da aplicação").
     */
    @Query(value = "EXPLAIN (ANALYZE, FORMAT JSON) SELECT * FROM registro_busca WHERE email = :email", nativeQuery = true)
    String explicarBuscaPorEmail(@Param("email") String email);
}
