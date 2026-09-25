package br.com.scoreengine.domain.model.common;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object imutável que representa uma fração analítica do escore de
 * crédito.
 * Atende aos requisitos de explicabilidade e rastreabilidade de risco (Bacen /
 * CDC).
 */
public record ScoreComponent(
        String nome,
        BigDecimal pontuacao,
        BigDecimal pontuacaoMaxima,
        BigDecimal pesoPonderado,
        String motivo) {
    public ScoreComponent {
        Objects.requireNonNull(nome, "O nome do componente não pode ser nulo.");
        Objects.requireNonNull(pontuacao, "A pontuação atribuída não pode ser nula.");
        Objects.requireNonNull(pontuacaoMaxima, "A pontuação máxima não pode ser nula.");
        Objects.requireNonNull(pesoPonderado, "O peso ponderado não pode ser nulo.");
        Objects.requireNonNull(motivo, "O motivo/justificativa não pode ser nulo.");

        if (pontuacao.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("A pontuação não pode ser negativa.");
        }
        if (pontuacao.compareTo(pontuacaoMaxima) > 0) {
            throw new IllegalArgumentException("A pontuação atribuída não pode exceder a pontuação máxima permitida.");
        }
    }

    /**
     * Construtor de conveniência para manter compatibilidade com chamadas prévias
     * assumindo peso unitário (1.00) caso não informado.
     */
    public ScoreComponent(String nome, BigDecimal pontuacao, BigDecimal pontuacaoMaxima, String motivo) {
        this(nome, pontuacao, pontuacaoMaxima, BigDecimal.ONE, motivo);
    }
}