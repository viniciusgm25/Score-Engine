package br.com.scoreengine.interfaces.rest.dto.response;

import java.time.Instant;
import java.util.List;

public record ScoreResponseDTO(
        int scoreFinal,
        String classificacao,
        String ratingDescricao,
        String modelVersion,
        List<ScoreComponentResponseDTO> componentes,
        Instant timestamp) {
}