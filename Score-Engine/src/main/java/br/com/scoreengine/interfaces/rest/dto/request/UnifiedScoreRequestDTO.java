package br.com.scoreengine.interfaces.rest.dto.request;

import br.com.scoreengine.domain.model.common.CustomerType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UnifiedScoreRequestDTO(
                @NotNull(message = "O tipo de pessoa (PF ou PJ) é obrigatório.") @JsonProperty("tipoPessoa") CustomerType tipoPessoa,

                @JsonProperty("forcarRecalculo") boolean forcarRecalculo,

                @Valid @JsonProperty("perfilPF") ScorePFRequestDTO perfilPF,

                @Valid @JsonProperty("perfilPJ") ScorePJRequestDTO perfilPJ) {
}