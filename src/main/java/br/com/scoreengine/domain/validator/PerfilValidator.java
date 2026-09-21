package br.com.scoreengine.domain.validator;

import br.com.scoreengine.domain.exception.PerfilInvalidoException;
import br.com.scoreengine.domain.model.pf.CustomerPFProfile;
import br.com.scoreengine.domain.model.pj.CustomerPJProfile;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Validador de Domínio para Perfis Cadastrais e Financeiros de Risco (PF e PJ).
 * Assegura integridade documental, saneamento de variáveis financeiras em
 * BigDecimal
 * e conformidade com os normativos prudenciais do Bacen (CMN 4.557/2017) e CDC.
 */
public class PerfilValidator {

    /**
     * Validação prudencial estrita para proponentes Pessoa Física.
     */
    public void validarPF(CustomerPFProfile pf) {
        if (pf == null) {
            throw new PerfilInvalidoException("O perfil do cliente Pessoa Física não pode ser nulo.");
        }

        validarDocumento(pf.getCpf(), 11, "CPF");

        validarMonetario(pf.getRendaMensal(), "Renda mensal");
        validarMonetario(pf.getDividaTotal(), "Dívida total");
        validarMonetario(pf.getLimiteRotativoUtilizado(), "Limite rotativo utilizado");
        validarMonetario(pf.getLimiteRotativoTotal(), "Limite rotativo total");

        if (pf.getIdade() < 18) {
            throw new PerfilInvalidoException(
                    "O proponente Pessoa Física deve possuir idade igual ou superior a 18 anos.");
        }

        if (pf.getNumeroDependentes() < 0) {
            throw new PerfilInvalidoException("O número de dependentes não pode ser negativo.");
        }

        if (pf.getDiasAtrasoUltimos12Meses() < 0) {
            throw new PerfilInvalidoException("A quantidade de dias de atraso não pode ser negativa.");
        }

        if (pf.getMesesNoEmpregoAtual() < 0 || pf.getMesesRelacionamentoBanco() < 0) {
            throw new PerfilInvalidoException("Os prazos temporais em meses não podem ser negativos.");
        }
    }

    /**
     * Validação prudencial estrita para proponentes Pessoa Jurídica.
     */
    public void validarPJ(CustomerPJProfile pj) {
        if (pj == null) {
            throw new PerfilInvalidoException("O perfil do cliente Pessoa Jurídica não pode ser nulo.");
        }

        validarDocumento(pj.getCnpj(), 14, "CNPJ");

        validarMonetario(pj.getFaturamentoMensal(), "Faturamento mensal");
        validarMonetario(pj.getDespesasOperacionaisMensais(), "Despesas operacionais mensais");
        validarMonetario(pj.getPassivoTotalBancario(), "Passivo total bancário");
        validarMonetario(pj.getFaturamentoBrutoAnual(), "Faturamento bruto anual");

        if (pj.getDiasAtrasoUltimos12Meses() < 0) {
            throw new PerfilInvalidoException(
                    "A quantidade de dias de atraso comercial/bancário não pode ser negativa.");
        }

        if (pj.getMesesConstituicao() < 0) {
            throw new PerfilInvalidoException("O tempo de constituição da empresa em meses não pode ser negativo.");
        }

        if (pj.getRazaoSocial() == null || pj.getRazaoSocial().isBlank()) {
            throw new PerfilInvalidoException("A razão social da empresa é de preenchimento obrigatório.");
        }
    }

    private void validarDocumento(String documento, int tamanhoEsperado, String tipo) {
        if (documento == null || documento.isBlank()) {
            throw new PerfilInvalidoException("O " + tipo + " do cliente é obrigatório.");
        }
        String digitos = documento.replaceAll("\\D", "");
        if (digitos.length() != tamanhoEsperado) {
            throw new PerfilInvalidoException("O " + tipo + " informado [" + documento + "] é inválido. Esperados "
                    + tamanhoEsperado + " dígitos numéricos.");
        }
    }

    private void validarMonetario(BigDecimal valor, String campo) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new PerfilInvalidoException(campo + " não pode ser nulo ou negativo.");
        }
    }
}