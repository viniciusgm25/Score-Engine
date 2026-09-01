package br.com.scoreengine.domain.model;

import br.com.scoreengine.domain.enums.TipoPessoa;
import java.math.BigDecimal;

/**
 * Representa os dados financeiros e sistêmicos para cálculo de risco PJ.
 */
public record PessoaJuridicaPerfil(
        String clienteId,
        BigDecimal faturamentoMensal,
        int tempoAtividadeMeses,
        BigDecimal endividamento,
        BigDecimal lucroMedioMensal,
        BigDecimal liquidez,
        int operacoesCreditoAtivas,
        int tempoRelacionamentoMeses,
        String setorAtividade) implements ClientePerfil {

    @Override
    public TipoPessoa tipoPessoa() {
        return TipoPessoa.PJ;
    }
}