package br.com.scoreengine.domain.model.common;

import java.time.Instant;
import java.util.List;

public record ScoreResult(
        int scoreFinal,
        RiskRating riskRating,
        String modelVersion,
        List<ScoreComponent> componentes,
        Instant timestamp) {
}