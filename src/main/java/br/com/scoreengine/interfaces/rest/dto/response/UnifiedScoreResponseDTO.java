package br.com.scoreengine.interfaces.rest.dto.response;

import br.com.scoreengine.domain.model.common.CustomerType;
import br.com.scoreengine.domain.model.common.ScoreOrigin;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * DTO de Projeção REST Unificada de Avaliação de Risco e Score de Crédito (PF e
 * PJ).
 * Consolida as métricas prudenciais de Basileia (Score, Faixa de Risco, PD)
 * e atende às exigências de explicabilidade do Bacen e do CDC.
 */
public record UnifiedScoreResponseDTO(
                String clienteId,
                CustomerType tipoPessoa,
                int scoreFinal,
                String faixaRisco,
                BigDecimal probabilidadeDefault,
                ModelInfoDTO modelo,
                Instant calculatedAt,
                ScoreOrigin origem,
                List<ScoreComponentResponseDTO> componentes,
                List<String> fatoresImpacto) {
        public UnifiedScoreResponseDTO {
                Objects.requireNonNull(clienteId, "O identificador do cliente não pode ser nulo.");
                Objects.requireNonNull(tipoPessoa, "O tipo de pessoa (PF/PJ) não pode ser nulo.");
                Objects.requireNonNull(faixaRisco, "A faixa de risco não pode ser nula.");
                Objects.requireNonNull(probabilidadeDefault, "A probabilidade de default (PD) não pode ser nula.");
                Objects.requireNonNull(modelo, "As informações do modelo não podem ser nulas.");
                Objects.requireNonNull(calculatedAt, "O timestamp de cálculo não pode ser nulo.");
                Objects.requireNonNull(origem, "A origem da requisição não pode ser nula.");

                componentes = componentes != null ? List.copyOf(componentes) : List.of();
                fatoresImpacto = fatoresImpacto != null ? List.copyOf(fatoresImpacto) : List.of();

                if (scoreFinal < 0 || scoreFinal > 1000) {
                        throw new IllegalArgumentException("O score final deve estar compreendido entre 0 e 1000.");
                }
        }
}