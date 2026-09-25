package br.com.scoreengine.domain.repository;

import br.com.scoreengine.domain.model.common.ScoreResult;

public interface ScoreAuditRepository {
    void saveExecution(String taxId, ScoreResult result);
}