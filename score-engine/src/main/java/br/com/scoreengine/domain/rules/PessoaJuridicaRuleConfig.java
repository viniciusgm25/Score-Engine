package br.com.scoreengine.domain.rules;

/**
 * HIPÓTESE TÉCNICA: Configuração de pesos para o cálculo de Score PJ.
 */
public record PessoaJuridicaRuleConfig(
        int pesoTempoAtividade,
        int pesoLiquidez,
        int pesoEndividamento,
        int pesoRelacionamento) {
    public PessoaJuridicaRuleConfig() {
        this(300, 300, 250, 150);
    }
}