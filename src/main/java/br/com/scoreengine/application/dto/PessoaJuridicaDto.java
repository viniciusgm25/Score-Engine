package br.com.scoreengine.application.dto;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * DTO de entrada para requisições de avaliação de Score de Pessoa Jurídica.
 * Fornece os dados cadastrais, capacidade de geração de caixa, estrutura
 * patrimonial
 * e pontualidade operacional em conformidade com as Resoluções CMN 2.682/1999 e
 * 4.557/2017.
 */
public record PessoaJuridicaDto(
                String clienteId,
                String razaoSocial,
                BigDecimal faturamentoMensal,
                BigDecimal despesasOperacionaisMensais,
                BigDecimal endividamento,
                BigDecimal faturamentoBrutoAnual,
                BigDecimal lucroMedioMensal,
                BigDecimal liquidez,
                int quantidadeAtrasos,
                int tempoAtividadeMeses,
                int operacoesCreditoAtivas,
                int tempoRelacionamentoMeses,
                String setorAtividade) implements ClientePerfilDto {

        public PessoaJuridicaDto {
                Objects.requireNonNull(clienteId, "O identificador do cliente (CNPJ) não pode ser nulo.");
                razaoSocial = (razaoSocial != null && !razaoSocial.isBlank()) ? razaoSocial : "Empresa " + clienteId;

                faturamentoMensal = faturamentoMensal != null ? faturamentoMensal : BigDecimal.ZERO;
                despesasOperacionaisMensais = despesasOperacionaisMensais != null ? despesasOperacionaisMensais
                                : BigDecimal.ZERO;
                endividamento = endividamento != null ? endividamento : BigDecimal.ZERO;
                lucroMedioMensal = lucroMedioMensal != null ? lucroMedioMensal : BigDecimal.ZERO;
                liquidez = liquidez != null ? liquidez : BigDecimal.ZERO;

                if (faturamentoBrutoAnual == null) {
                        faturamentoBrutoAnual = faturamentoMensal.multiply(new BigDecimal("12"));
                }

                setorAtividade = (setorAtividade != null && !setorAtividade.isBlank()) ? setorAtividade : "OUTROS";

                if (faturamentoMensal.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("O faturamento mensal não pode ser negativo.");
                }
                if (despesasOperacionaisMensais.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("As despesas operacionais não podem ser negativas.");
                }
                if (endividamento.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException(
                                        "O endividamento total (passivo bancário) não pode ser negativo.");
                }
                if (faturamentoBrutoAnual.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("O faturamento bruto anual não pode ser negativo.");
                }
                if (quantidadeAtrasos < 0) {
                        throw new IllegalArgumentException("A quantidade de atrasos não pode ser negativa.");
                }
                if (tempoAtividadeMeses < 0) {
                        throw new IllegalArgumentException("O tempo de atividade da empresa não pode ser negativo.");
                }
                if (operacoesCreditoAtivas < 0) {
                        throw new IllegalArgumentException(
                                        "A quantidade de operações de crédito ativas não pode ser negativa.");
                }
                if (tempoRelacionamentoMeses < 0) {
                        throw new IllegalArgumentException("O tempo de relacionamento bancário não pode ser negativo.");
                }
        }
}