package br.com.scoreengine.application.port.out;

import br.com.scoreengine.domain.model.ScoreResultado;

public interface ScoreAuditPort {
    void registrar(ScoreResultado resultado, String correlationId, long tempoProcessamentoMs);
}