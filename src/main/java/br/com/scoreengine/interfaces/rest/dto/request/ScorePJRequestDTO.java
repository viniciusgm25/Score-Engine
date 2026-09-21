package br.com.scoreengine.interfaces.rest.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ScorePJRequestDTO(
        @NotBlank(message = "O CNPJ é obrigatório.") @Pattern(regexp = "\\d{14}", message = "O CNPJ deve conter exatamente 14 dígitos numéricos.") String cnpj,

        @NotBlank(message = "A razão social é obrigatória.") String razaoSocial,

        @NotNull(message = "O faturamento mensal é obrigatório.") @PositiveOrZero(message = "O faturamento mensal deve ser maior ou igual a zero.") BigDecimal faturamentoMensal,

        @NotNull(message = "As despesas operacionais são obrigatórias.") @PositiveOrZero(message = "As despesas operacionais devem ser maiores ou iguais a zero.") BigDecimal despesasOperacionaisMensais,

        @NotNull(message = "O passivo bancário é obrigatório.") @PositiveOrZero(message = "O passivo bancário deve ser maior ou igual a zero.") BigDecimal passivoTotalBancario,

        @NotNull(message = "O faturamento anual é obrigatório.") @PositiveOrZero(message = "O faturamento anual deve ser maior ou igual a zero.") BigDecimal faturamentoBrutoAnual,

        @Min(value = 0, message = "Dias de atraso não podem ser negativos.") int diasAtrasoUltimos12Meses,

        @Min(value = 0, message = "Meses de constituição devem ser maiores ou iguais a zero.") int mesesConstituicao,

        @NotBlank(message = "O setor de atuação é obrigatório.") String setorAtuacao) {
}