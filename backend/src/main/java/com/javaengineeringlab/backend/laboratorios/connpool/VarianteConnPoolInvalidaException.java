package com.javaengineeringlab.backend.laboratorios.connpool;

import com.javaengineeringlab.backend.plataforma.RequisicaoInvalidaException;

public class VarianteConnPoolInvalidaException extends RequisicaoInvalidaException {

    public VarianteConnPoolInvalidaException(String segmento) {
        super("Variante inválida para o laboratório de connection pool exhaustion: " + segmento
                + " (esperado: pool-pequeno, pool-redimensionado ou conexao-curta)");
    }
}
