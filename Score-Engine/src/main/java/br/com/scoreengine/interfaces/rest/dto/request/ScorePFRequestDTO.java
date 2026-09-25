package br.com.scoreengine.interfaces.rest.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ScorePFRequestDTO(
        @NotBlank(message = "O documento CPF é obrigatório.") @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 dígitos numéricos.") String cpf,

        @NotNull(message = "A renda mensal é obrigatória.") @PositiveOrZero(message = "A renda mensal deve ser igual ou superior a zero.") BigDecimal rendaMensal,

        @NotNull(message = "A dívida total é obrigatória.") @PositiveOrZero(message = "A dívida total deve ser igual ou superior a zero.") BigDecimal dividaTotal,

        @Min(value = 18, message = "Idade mínima regulamentar permitida é 18 anos.") @Max(value = 120, message = "Idade máxima fora do intervalo de avaliação.") int idade,

        @NotBlank(message = "O estado civil é obrigatório.") String estadoCivil,

        @Min(value = 0, message = "Número de dependentes não pode ser negativo.") int numeroDependentes,

        @Min(value = 0, message = "Dias de atraso não podem ser negativos.") int diasAtrasoUltimos12Meses,

        @NotNull(message = "O limite utilizado é obrigatório.") @PositiveOrZero(message = "O limite utilizado deve ser igual ou superior a zero.") BigDecimal limiteRotativoUtilizado,

        @NotNull(message = "O limite total é obrigatório.") @PositiveOrZero(message = "O limite total deve ser igual ou superior a zero.") BigDecimal limiteRotativoTotal,

        @Min(value = 0, message = "Tempo de emprego deve ser igual ou superior a zero.") int mesesNoEmpregoAtual,

        @Min(value = 0, message = "Tempo de relacionamento deve ser igual ou superior a zero.") int mesesRelacionamentoBanco) {
}