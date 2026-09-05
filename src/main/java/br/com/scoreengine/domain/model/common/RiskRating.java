package br.com.scoreengine.domain.model.common;

public enum RiskRating {
    A("Risco Muito Baixo"),
    B("Risco Baixo"),
    C("Risco Médio"),
    D("Risco Alto"),
    E("Risco Muito Alto");

    private final String descricao;

    RiskRating(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static RiskRating fromScore(int score) {
        if (score >= 800)
            return A;
        if (score >= 650)
            return B;
        if (score >= 500)
            return C;
        if (score >= 350)
            return D;
        return E;
    }
}