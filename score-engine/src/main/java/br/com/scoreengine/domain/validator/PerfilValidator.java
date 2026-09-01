package br.com.scoreengine.domain.validator;

import br.com.scoreengine.domain.model.ClientePerfil;
import br.com.scoreengine.domain.model.PessoaFisicaPerfil;
import br.com.scoreengine.domain.model.PessoaJuridicaPerfil;
import br.com.scoreengine.domain.exception.PerfilInvalidoException;
import java.math.BigDecimal;

public class PerfilValidator {

    public void validar(ClientePerfil perfil) {
        if (perfil == null) {
            throw new PerfilInvalidoException("O perfil do cliente não pode ser nulo.");
        }
        if (perfil.clienteId() == null || perfil.clienteId().isBlank()) {
            throw new PerfilInvalidoException("O identificador do cliente é obrigatório.");
        }

        // Utilização de Pattern Matching (Java 21) para roteamento limpo
        switch (perfil) {
            case PessoaFisicaPerfil pf -> validarPessoaFisica(pf);
            case PessoaJuridicaPerfil pj -> validarPessoaJuridica(pj);
            default -> throw new PerfilInvalidoException("Tipo de perfil não suportado.");
        }
    }

    private void validarPessoaFisica(PessoaFisicaPerfil pf) {
        validarMonetario(pf.rendaMensalLiquida(), "Renda mensal líquida");
        validarPercentual(pf.endividamento(), "Endividamento");

        if (pf.idade() < 18) {
            throw new PerfilInvalidoException("Cliente Pessoa Física deve ter 18 anos ou mais.");
        }
        if (pf.quantidadeAtrasos() < 0) {
            throw new PerfilInvalidoException("A quantidade de atrasos não pode ser negativa.");
        }
        if (pf.tempoRelacionamentoMeses() < 0 || pf.tempoUltimoEmpregoMeses() < 0) {
            throw new PerfilInvalidoException("Prazos em meses não podem ser negativos.");
        }
    }

    private void validarPessoaJuridica(PessoaJuridicaPerfil pj) {
        validarMonetario(pj.faturamentoMensal(), "Faturamento mensal");
        validarMonetario(pj.lucroMedioMensal(), "Lucro médio mensal");
        validarMonetario(pj.liquidez(), "Liquidez");
        validarPercentual(pj.endividamento(), "Endividamento");

        if (pj.tempoAtividadeMeses() < 0) {
            throw new PerfilInvalidoException("O tempo de atividade não pode ser negativo.");
        }
        if (pj.operacoesCreditoAtivas() < 0) {
            throw new PerfilInvalidoException("A quantidade de operações de crédito ativas não pode ser negativa.");
        }
    }

    private void validarMonetario(BigDecimal valor, String campo) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new PerfilInvalidoException(campo + " não pode ser nula ou negativa.");
        }
    }

    private void validarPercentual(BigDecimal percentual, String campo) {
        if (percentual == null || percentual.compareTo(BigDecimal.ZERO) < 0
                || percentual.compareTo(BigDecimal.ONE) > 0) {
            throw new PerfilInvalidoException(campo + " deve ser um percentual válido entre 0.0 e 1.0.");
        }
    }
}