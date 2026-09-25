package br.com.scoreengine.domain.strategy;

import br.com.scoreengine.domain.model.common.ScoreResult;

public interface ScoringPolicy<T> {
    ScoreResult calculate(T profile);
}