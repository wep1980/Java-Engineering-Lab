package com.javaengineeringlab.backend.plataforma;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/laboratorios")
@Tag(name = "Laboratórios", description = "Catálogo dos laboratórios do Java Engineering Lab")
public class CatalogoLaboratoriosController {

    private final CatalogoLaboratoriosService servico;

    public CatalogoLaboratoriosController(CatalogoLaboratoriosService servico) {
        this.servico = servico;
    }

    @GetMapping
    @Operation(summary = "Lista os laboratórios do catálogo")
    public List<LaboratorioResumo> listar() {
        return servico.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um laboratório pelo id")
    public LaboratorioResumo buscarPorId(@PathVariable String id) {
        return servico.buscarPorId(id);
    }
}
