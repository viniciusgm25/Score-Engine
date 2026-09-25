package br.com.scoreengine.domain.calculator;

import br.com.scoreengine.domain.model.common.ScoreResult;

/**
 * Contrato Strategy para Execução de Políticas de Score de Crédito.
 * Provê tipagem estrita para perfis PF/PJ, verificação polimórfica de suporte
 * e identificação de versão do modelo para conformidade e auditoria (Bacen /
 * Basileia).
 *
 * @param <T> Tipo do perfil cadastral e financeiro suportado (PF ou PJ)
 */
public interface ScoreCalculatorStrategy<T> {

    /**
     * Executa o cálculo determinístico de score, faixa de risco, probabilidade de
     * default (PD)
     * e os componentes explicativos analíticos.
     *
     * @param profile Dados cadastrais e financeiros do proponente
     * @return Resultado imutável consolidado (ScoreResult)
     */
    ScoreResult calculate(T profile);

    /**
     * Retorna o identificador semântico de versão do modelo estatístico/regras
     * vigente.
     *
     * @return String contendo a versão para trilha de auditoria
     */
    String getVersion();

    /**
     * Verifica se a estratégia atual é compatível com o perfil informado.
     *
     * @param profile Objeto de perfil a ser verificado
     * @return true se o calculador suporta este tipo de entrada
     */
    default boolean supports(Object profile) {
        return profile != null;
    }
}