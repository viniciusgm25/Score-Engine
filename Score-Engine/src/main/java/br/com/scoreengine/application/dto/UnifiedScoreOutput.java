package br.com.scoreengine.application.dto;

import br.com.scoreengine.domain.model.common.CustomerType;
import br.com.scoreengine.domain.model.common.ScoreOrigin;
import br.com.scoreengine.domain.model.common.ScoreResult;

public record UnifiedScoreOutput(
        String clienteId,
        CustomerType tipoPessoa,
        ScoreResult scoreResult,
        ScoreOrigin origem) {
}