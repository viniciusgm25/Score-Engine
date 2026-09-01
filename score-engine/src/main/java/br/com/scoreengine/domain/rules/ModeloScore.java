package br.com.scoreengine.domain.rules;

import br.com.scoreengine.domain.enums.ClassificacaoRisco;

/**
 * Interface que define os limites e regras de um modelo de score específico.
 * Permite a evolução e versionamento das políticas de risco de crédito.
 */
public interface ModeloScore {
    int SCORE_MINIMO = 0;
    int SCORE_MAXIMO = 1000;

    String getVersao();

    ClassificacaoRisco classificar(int score);

    int normalizar(int scoreCalculado);
}