package com.javaengineeringlab.backend.laboratorios.threadpool;

import com.javaengineeringlab.backend.plataforma.RequisicaoInvalidaException;

public class VarianteThreadPoolInvalidaException extends RequisicaoInvalidaException {

    public VarianteThreadPoolInvalidaException(String segmento) {
        super("Variante inválida para o laboratório de thread pool exhaustion: " + segmento
                + " (esperado: fila-ilimitada ou fila-limitada)");
    }
}
