package br.com.scoreengine.domain.model.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Representa o resultado consolidado e imutável da avaliação de crédito.
 * Modelo alinhado aos pilares de explicabilidade, tratabilidade e métricas
 * prudenciais (Bacen / Basileia / CDC).
 */
public record ScoreResult(
        int scoreFinal,
        String faixaRisco,
        BigDecimal probabilidadeDefault,
        String modelVersion,
        Instant calculatedAt,
        List<ScoreComponent> componentes,
        List<String> fatoresImpacto) {

    public ScoreResult {
        Objects.requireNonNull(faixaRisco, "A faixa de risco não pode ser nula.");
        Objects.requireNonNull(probabilidadeDefault, "A probabilidade de default não pode ser nula.");
        Objects.requireNonNull(modelVersion, "A versão do modelo não pode ser nula.");
        Objects.requireNonNull(calculatedAt, "A data de cálculo não pode ser nula.");

        componentes = componentes != null ? List.copyOf(componentes) : List.of();
        fatoresImpacto = fatoresImpacto != null ? List.copyOf(fatoresImpacto) : List.of();

        if (scoreFinal < 0 || scoreFinal > 1000) {
            throw new IllegalArgumentException("O score final deve estar compreendido entre 0 e 1000.");
        }
    }

    /**
     * Construtor principal para montagem analítica completa do score a partir dos
     * componentes.
     */
    public ScoreResult(
            int scoreFinal,
            String modelVersion,
            List<ScoreComponent> componentes) {
        this(
                scoreFinal,
                definirFaixaRiscoPadrao(scoreFinal),
                calcularPdPadrao(scoreFinal),
                modelVersion,
                Instant.now(),
                componentes,
                extrairMotivosDosComponentes(componentes));
    }

    /**
     * Construtor de conveniência/retrocompatibilidade para scorecards simplificados
     * e testes unitários.
     */
    public ScoreResult(int scoreFinal, String modelVersion) {
        this(
                scoreFinal,
                definirFaixaRiscoPadrao(scoreFinal),
                calcularPdPadrao(scoreFinal),
                modelVersion,
                Instant.now(),
                List.of(),
                List.of("Avaliação processada com base no scorecard padrão vigente."));
    }

    public static String definirFaixaRiscoPadrao(int score) {
        if (score >= 701) {
            return "EXCELENTE";
        }
        if (score >= 501) {
            return "BOM";
        }
        if (score >= 301) {
            return "REGULAR";
        }
        return "CRITICO";
    }

    public static BigDecimal calcularPdPadrao(int score) {
        BigDecimal pontuacaoMaxima = new BigDecimal("1000.00");
        BigDecimal divisorEscala = new BigDecimal("10000.00");
        BigDecimal pdMinima = new BigDecimal("0.0010");

        BigDecimal scoreBd = BigDecimal.valueOf(score);
        BigDecimal diferenca = pontuacaoMaxima.subtract(scoreBd);
        BigDecimal pdLinear = diferenca.divide(divisorEscala, 4, RoundingMode.HALF_EVEN);

        return pdLinear.max(pdMinima);
    }

    private static List<String> extrairMotivosDosComponentes(List<ScoreComponent> componentes) {
        if (componentes == null || componentes.isEmpty()) {
            return List.of("Avaliação processada sem detalhamento analítico de componentes.");
        }
        return componentes.stream()
                .map(ScoreComponent::motivo)
                .toList();
    }
}