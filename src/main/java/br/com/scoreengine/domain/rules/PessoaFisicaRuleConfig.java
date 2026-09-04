package br.com.scoreengine.domain.rules;

/**
 * HIPÓTESE TÉCNICA: Configuração de pesos para o cálculo de Score PF.
 * A soma dos pesos deve totalizar o SCORE_MAXIMO (1000).
 * Os percentuais de exemplo baseiam-se em modelos de mercado (29%, 24%, 21%,
 * 12%, 8%, 6%).
 */
public record PessoaFisicaRuleConfig(
        int pesoPagamentos,
        int pesoExperiencia,
        int pesoDividas,
        int pesoBuscaCredito,
        int pesoInformacoesCadastrais,
        int pesoContratos) {
    public PessoaFisicaRuleConfig() {
        this(290, 240, 210, 120, 80, 60);
    }
}