package br.com.scoreengine.domain.exception;

/**
 * Lançada quando os dados de entrada do cliente violam regras de negócio
 * estruturais.
 */
public class PerfilInvalidoException extends ScoreException {
    public PerfilInvalidoException(String message) {
        super(message);
    }
}