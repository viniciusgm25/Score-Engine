package br.com.scoreengine.domain.rules;

import br.com.scoreengine.domain.enums.ClassificacaoRisco;

/**
 * HIPÓTESE TÉCNICA: Implementação inicial do modelo de classificação.
 * As faixas estabelecidas aqui devem ser validadas e substituídas
 * pelas definições estatísticas oficiais da área de risco.
 */
public class ModeloScoreV1 implements ModeloScore {

    @Override
    public String getVersao() {
        return "1.0";
    }

    @Override
    public int normalizar(int scoreCalculado) {
        if (scoreCalculado < SCORE_MINIMO) {
            return SCORE_MINIMO;
        }
        if (scoreCalculado > SCORE_MAXIMO) {
            return SCORE_MAXIMO;
        }
        return scoreCalculado;
    }

    @Override
    public ClassificacaoRisco classificar(int score) {
        int scoreNormalizado = normalizar(score);

        if (scoreNormalizado <= 199)
            return ClassificacaoRisco.MUITO_ALTO_RISCO;
        if (scoreNormalizado <= 399)
            return ClassificacaoRisco.ALTO_RISCO;
        if (scoreNormalizado <= 599)
            return ClassificacaoRisco.MEDIO_RISCO;
        if (scoreNormalizado <= 799)
            return ClassificacaoRisco.BAIXO_RISCO;

        return ClassificacaoRisco.MUITO_BAIXO_RISCO;
    }
}