package com.javaengineeringlab.backend.plataforma;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalogoLaboratoriosController.class)
@Import(ManipuladorGlobalDeExcecoes.class)
class CatalogoLaboratoriosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogoLaboratoriosService servico;

    @Test
    void deveListarLaboratoriosDoCatalogo() throws Exception {
        var n1 = new LaboratorioResumo("n1-queries", "N+1 Queries", "objetivo", StatusLaboratorio.PLANEJADO);
        given(servico.listar()).willReturn(List.of(n1));

        mockMvc.perform(get("/api/laboratorios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("n1-queries"))
                .andExpect(jsonPath("$[0].status").value("PLANEJADO"));
    }

    @Test
    void deveBuscarLaboratorioPorId() throws Exception {
        var n1 = new LaboratorioResumo("n1-queries", "N+1 Queries", "objetivo", StatusLaboratorio.PLANEJADO);
        given(servico.buscarPorId("n1-queries")).willReturn(n1);

        mockMvc.perform(get("/api/laboratorios/n1-queries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("N+1 Queries"));
    }

    @Test
    void deveRetornar404QuandoLaboratorioNaoExiste() throws Exception {
        given(servico.buscarPorId("inexistente")).willThrow(new LaboratorioNaoEncontradoException("inexistente"));

        mockMvc.perform(get("/api/laboratorios/inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404))
                .andExpect(jsonPath("$.caminho").value("/api/laboratorios/inexistente"));
    }
}
