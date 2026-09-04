package br.com.scoreengine.domain.model;

import br.com.scoreengine.domain.enums.TipoPessoa;
import java.math.BigDecimal;

/**
 * Representa os dados demográficos e financeiros para cálculo de risco PF.
 */
public record PessoaFisicaPerfil(
        String clienteId,
        BigDecimal rendaMensalLiquida,
        int quantidadeAtrasos,
        BigDecimal endividamento,
        int tempoRelacionamentoMeses,
        int idade,
        String estadoCivil,
        int tempoUltimoEmpregoMeses,
        boolean possuiAvalista) implements ClientePerfil {

    @Override
    public TipoPessoa tipoPessoa() {
        return TipoPessoa.PF;
    }
}