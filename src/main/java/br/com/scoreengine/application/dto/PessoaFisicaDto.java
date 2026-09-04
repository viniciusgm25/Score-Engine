package br.com.scoreengine.application.dto;

import java.math.BigDecimal;

public record PessoaFisicaDto(
        String clienteId,
        BigDecimal rendaMensalLiquida,
        int quantidadeAtrasos,
        BigDecimal endividamento,
        int tempoRelacionamentoMeses,
        int idade,
        String estadoCivil,
        int tempoUltimoEmpregoMeses,
        boolean possuiAvalista) implements ClientePerfilDto {
}