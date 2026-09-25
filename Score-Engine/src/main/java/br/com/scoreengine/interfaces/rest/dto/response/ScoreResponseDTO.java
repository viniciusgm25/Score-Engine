package br.com.scoreengine.interfaces.rest.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * DTO de Projeção REST para o Resultado da Avaliação de Score de Crédito PF.
 * Totalmente alinhado aos padrões regulatórios prudenciais (Bacen / Basileia /
 * CDC),
 * disponibilizando o Score numérico (0 a 1000), Faixa de Risco, Probabilidade
 * de Default (PD),
 * versionamento do modelo e componentes analíticos explicáveis.
 */
public record ScoreResponseDTO(
                int scoreFinal,
                String faixaRisco,
                BigDecimal probabilidadeDefault,
                String modelVersion,
                List<ScoreComponentResponseDTO> componentes,
                List<String> fatoresImpacto,
                Instant calculatedAt) {
        public ScoreResponseDTO {
                Objects.requireNonNull(faixaRisco, "A faixa de risco não pode ser nula.");
                Objects.requireNonNull(probabilidadeDefault, "A probabilidade de default (PD) não pode ser nula.");
                Objects.requireNonNull(modelVersion, "A versão do modelo não pode ser nula.");
                Objects.requireNonNull(calculatedAt, "O timestamp de cálculo não pode ser nulo.");

                componentes = componentes != null ? List.copyOf(componentes) : List.of();
                fatoresImpacto = fatoresImpacto != null ? List.copyOf(fatoresImpacto) : List.of();

                if (scoreFinal < 0 || scoreFinal > 1000) {
                        throw new IllegalArgumentException("O score final deve estar compreendido entre 0 e 1000.");
                }
        }
}