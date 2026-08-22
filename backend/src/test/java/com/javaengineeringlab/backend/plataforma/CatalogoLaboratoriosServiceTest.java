package com.javaengineeringlab.backend.plataforma;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogoLaboratoriosServiceTest {

    private final CatalogoLaboratoriosService servico = new CatalogoLaboratoriosService();

    @Test
    void deveListarAoMenosOLaboratorioDeNMaisUm() {
        assertThat(servico.listar())
                .extracting(LaboratorioResumo::id)
                .contains("n1-queries");
    }

    @Test
    void deveLancarExcecaoParaIdInexistente() {
        assertThatThrownBy(() -> servico.buscarPorId("nao-existe"))
                .isInstanceOf(LaboratorioNaoEncontradoException.class);
    }
}
