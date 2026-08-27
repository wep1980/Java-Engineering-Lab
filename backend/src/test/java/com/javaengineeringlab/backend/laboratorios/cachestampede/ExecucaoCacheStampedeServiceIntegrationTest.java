package com.javaengineeringlab.backend.laboratorios.cachestampede;

import com.javaengineeringlab.backend.plataforma.OrigemDados;
import com.javaengineeringlab.backend.plataforma.ResultadoExecucaoLaboratorio;
import com.javaengineeringlab.backend.plataforma.VarianteExecucao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valida, contra Redis real (Testcontainers -- sem módulo dedicado do
 * Testcontainers para Redis, usa {@link GenericContainer} diretamente,
 * abordagem padrão da comunidade) e PostgreSQL real (a aplicação
 * inteira sobe em qualquer {@code @SpringBootTest}, JPA incluído,
 * mesmo que este laboratório não use o banco -- achado real durante a
 * implementação), o comportamento das duas variantes do laboratório de
 * Cache Stampede -- RNF-02 de
 * SPEC-LAB-CACHESTAMPEDE-001-cache-stampede.md.
 */
@Testcontainers
@SpringBootTest
class ExecucaoCacheStampedeServiceIntegrationTest {

    private static final int PORTA_REDIS = 6379;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(PORTA_REDIS);

    @DynamicPropertySource
    static void propriedadesRedis(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(PORTA_REDIS));
    }

    @Autowired
    private ExecucaoCacheStampedeService servico;

    @Test
    void semProtecaoDeveAcessarORecursoLentoDezVezes() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteCacheStampede.SEM_PROTECAO);

        assertThat(resultado.metricas().get("quantidadeAcessosAoRecursoLentoReal"))
                .isEqualTo(ExecucaoCacheStampedeService.QUANTIDADE_REQUISICOES_CONCORRENTES);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.PROBLEMATICO);
        assertThat(resultado.origemDados()).isEqualTo(OrigemDados.REAL);
    }

    @Test
    void comProtecaoDeveAcessarORecursoLentoApenasUmaVez() {
        ResultadoExecucaoLaboratorio resultado = servico.executar(VarianteCacheStampede.COM_PROTECAO);

        assertThat(resultado.metricas().get("quantidadeAcessosAoRecursoLentoReal")).isEqualTo(1);
        assertThat(resultado.variante()).isEqualTo(VarianteExecucao.CORRIGIDO);
    }
}
