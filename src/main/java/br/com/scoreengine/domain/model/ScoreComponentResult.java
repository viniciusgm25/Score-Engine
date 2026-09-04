package br.com.scoreengine.domain.model;

/**
 * Representa a avaliação individual de um componente específico do Score
 * (ex: Histórico de Pagamentos, Endividamento), garantindo explicabilidade.
 */
public record ScoreComponentResult(
        String nome,
        int pontuacao,
        int pontuacaoMaxima,
        String motivo) {
}