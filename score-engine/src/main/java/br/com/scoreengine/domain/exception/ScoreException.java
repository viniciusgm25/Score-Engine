package br.com.scoreengine.domain.exception;

/**
 * Exceção base de negócio para o Motor de Score.
 */
public class ScoreException extends RuntimeException {
    public ScoreException(String message) {
        super(message);
    }
}