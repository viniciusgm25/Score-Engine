package br.com.scoreengine.domain.model.common;

import java.math.BigDecimal;

public record ScoreComponent(
        String nome,
        int pontuacao,
        BigDecimal peso,
        ImpactType impacto,
        String motivo) {
    public ScoreComponent {
        if (pontuacao < 0 || pontuacao > 1000) {
            throw new IllegalArgumentException("A pontuação do componente deve situar-se entre 0 e 1000.");
        }
        if (peso == null || peso.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O peso do componente não pode ser nulo ou negativo.");
        }
    }
}