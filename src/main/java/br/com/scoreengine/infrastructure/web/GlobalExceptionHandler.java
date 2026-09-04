package br.com.scoreengine.infrastructure.web;

import br.com.scoreengine.domain.exception.ScoreException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Evita o vazamento de stack traces e padroniza as respostas de erro da API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ScoreException.class)
    public ResponseEntity<Map<String, String>> handleScoreException(ScoreException ex) {
        return ResponseEntity.badRequest().body(Map.of("erro_negocio", ex.getMessage()));
    }
}