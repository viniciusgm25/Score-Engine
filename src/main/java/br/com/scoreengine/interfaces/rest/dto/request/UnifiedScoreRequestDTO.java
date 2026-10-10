package br.com.scoreengine.interfaces.rest.dto.request;

import br.com.scoreengine.domain.model.common.CustomerType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UnifiedScoreRequestDTO(
                @NotNull(message = "O tipo de pessoa (PF ou PJ) é obrigatório.") @JsonProperty("tipoPessoa") CustomerType tipoPessoa,

                @JsonProperty("forcarRecalculo") boolean forcarRecalculo,

                @Valid @JsonProperty("perfilPF") ScorePFRequestDTO perfilPF,

                @Valid @JsonProperty("perfilPJ") ScorePJRequestDTO perfilPJ,

                // ---- CAMPOS PROVISÓRIOS (dados da solicitação) ----
                // Pertencem ao grupo de Financiamento. Todos opcionais: o Score não os usa
                // no cálculo, apenas repassa para a Decisão. Remover quando o Financiamento integrar.
                @JsonProperty("identificador") String identificador,
                @JsonProperty("valor") BigDecimal valor,
                @Pattern(regexp = "CONSIGNADO_INSS|CONSIGNADO_PRIVADO|CONSIGNADO_PUBLICO|CREDITO_PESSOAL|OUTROS_BENS|VEICULOS", message = "Modalidade inválida. Use: CONSIGNADO_INSS, CONSIGNADO_PRIVADO, CONSIGNADO_PUBLICO, CREDITO_PESSOAL, OUTROS_BENS ou VEICULOS.") @JsonProperty("modalidade") String modalidade,
                @JsonProperty("prazoMeses") Integer prazoMeses,
                @JsonProperty("dataLiberacao") LocalDate dataLiberacao,
                @JsonProperty("primeiroRelacionamento") Boolean primeiroRelacionamento) {
}