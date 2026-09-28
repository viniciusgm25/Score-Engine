package br.com.scoreengine.application.port.out;

import br.com.scoreengine.interfaces.rest.dto.response.UnifiedScoreResponseDTO;

/**
 * Porta de saída responsável por entregar o resultado calculado pelo Score ao
 * microsserviço de Decisão.
 */
public interface ScoreToDecisionPort {
    void enviar(UnifiedScoreResponseDTO resultado);
}
