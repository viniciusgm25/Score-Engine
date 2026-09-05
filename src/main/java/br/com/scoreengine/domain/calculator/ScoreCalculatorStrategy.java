package br.com.scoreengine.domain.calculator;

import br.com.scoreengine.domain.model.common.ScoreResult;

public interface ScoreCalculatorStrategy<T> {
    ScoreResult calculate(T profile);

    String getVersion();
}