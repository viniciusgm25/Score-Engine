package br.com.scoreengine.application.dto;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * DTO de entrada para requisições de avaliação de Score de Pessoa Física.
 * Contempla as variáveis cadastrais, financeiras e de exposição a linhas
 * rotativas
 * em conformidade com as diretrizes prudenciais do Bacen, Basileia e CDC.
 */
public record PessoaFisicaDto(
                String clienteId,
                BigDecimal rendaMensalLiquida,
                int quantidadeAtrasos,
                BigDecimal endividamento,
                int tempoRelacionamentoMeses,
                int idade,
                String estadoCivil,
                int numeroDependentes,
                BigDecimal limiteRotativoUtilizado,
                BigDecimal limiteRotativoTotal,
                int tempoUltimoEmpregoMeses,
                boolean possuiAvalista) implements ClientePerfilDto {

        public PessoaFisicaDto {
                Objects.requireNonNull(clienteId, "O identificador do cliente (CPF) não pode ser nulo.");

                rendaMensalLiquida = rendaMensalLiquida != null ? rendaMensalLiquida : BigDecimal.ZERO;
                endividamento = endividamento != null ? endividamento : BigDecimal.ZERO;
                limiteRotativoUtilizado = limiteRotativoUtilizado != null ? limiteRotativoUtilizado : BigDecimal.ZERO;
                limiteRotativoTotal = limiteRotativoTotal != null ? limiteRotativoTotal : BigDecimal.ZERO;
                estadoCivil = (estadoCivil != null && !estadoCivil.isBlank()) ? estadoCivil : "SOLTEIRO";

                if (rendaMensalLiquida.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("A renda mensal líquida não pode ser negativa.");
                }
                if (endividamento.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("O endividamento total não pode ser negativo.");
                }
                if (limiteRotativoUtilizado.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("O limite rotativo utilizado não pode ser negativo.");
                }
                if (limiteRotativoTotal.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("O limite rotativo total não pode ser negativo.");
                }
                if (quantidadeAtrasos < 0) {
                        throw new IllegalArgumentException("A quantidade de atrasos não pode ser negativa.");
                }
                if (idade < 0) {
                        throw new IllegalArgumentException("A idade não pode ser negativa.");
                }
                if (numeroDependentes < 0) {
                        throw new IllegalArgumentException("O número de dependentes não pode ser negativo.");
                }
        }
}