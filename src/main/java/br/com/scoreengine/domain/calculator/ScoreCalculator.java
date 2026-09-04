package br.com.scoreengine.domain.calculator;

import br.com.scoreengine.domain.enums.TipoPessoa;
import br.com.scoreengine.domain.model.ClientePerfil;
import br.com.scoreengine.domain.model.ScoreResultado;

/**
 * Contrato para o padrão Strategy de cálculo de score.
 */
public interface ScoreCalculator {
    boolean isEligible(TipoPessoa tipoPessoa);

    ScoreResultado calcular(ClientePerfil perfil);
}