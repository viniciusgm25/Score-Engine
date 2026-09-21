package br.com.scoreengine.interfaces.rest.dto.response;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * DTO de Projeção REST para cada componente analítico do Score de Crédito.
 * Provê transparência e explicabilidade regulatória (Bacen / CDC),
 * discriminando a pontuação obtida, teto máximo da regra, peso na matriz de
 * risco
 * e justificativa textual padronizada.
 */
public record ScoreComponentResponseDTO(
                String nome,
                BigDecimal pontuacao,
                BigDecimal pontuacaoMaxima,
                BigDecimal pesoPonderado,
                String motivo) {
        public ScoreComponentResponseDTO {
                Objects.requireNonNull(nome, "O nome do componente não pode ser nulo.");
                Objects.requireNonNull(pontuacao, "A pontuação atribuída não pode ser nula.");
                Objects.requireNonNull(pontuacaoMaxima, "A pontuação máxima não pode ser nula.");
                Objects.requireNonNull(pesoPonderado, "O peso ponderado não pode ser nulo.");
                Objects.requireNonNull(motivo, "O motivo explicativo não pode ser nulo.");
        }
}