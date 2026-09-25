package br.com.scoreengine.domain.model.common;

import java.math.BigDecimal;

public enum RiskRating {
    EXCELENTE("Risco Muito Baixo", new BigDecimal("0.005")),
    BOM("Risco Baixo", new BigDecimal("0.015")),
    REGULAR("Risco Médio", new BigDecimal("0.050")),
    CRITICO("Alto Risco de Inadimplência", new BigDecimal("0.150"));

    private final String descricao;
    private final BigDecimal probabilidadeDefault;

    RiskRating(String descricao, BigDecimal probabilidadeDefault) {
        this.descricao = descricao;
        this.probabilidadeDefault = probabilidadeDefault;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getProbabilidadeDefault() {
        return probabilidadeDefault;
    }

    public static RiskRating fromScore(int score) {
        if (score >= 701)
            return EXCELENTE;
        if (score >= 501)
            return BOM;
        if (score >= 301)
            return REGULAR;
        return CRITICO;
    }
}