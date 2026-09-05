package br.com.scoreengine.interfaces.rest.dto.response;

import java.math.BigDecimal;

public record ScoreComponentResponseDTO(
        String nome,
        int pontuacao,
        BigDecimal peso,
        String impacto,
        String motivo) {
}