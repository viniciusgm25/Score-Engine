package br.com.scoreengine.domain.repository;

import br.com.scoreengine.domain.model.common.CustomerType;
import br.com.scoreengine.domain.model.common.ScoreResult;

import java.time.Instant;
import java.util.Optional;

public interface ScoreEvaluationRepository {
    Optional<ScoreResult> findValidEvaluation(String clientId, CustomerType customerType, String payloadHash,
            Instant validSince, String modelVersion);

    void saveEvaluation(String clientId, CustomerType customerType, String payloadHash, ScoreResult result,
            String serializedComponents);
}