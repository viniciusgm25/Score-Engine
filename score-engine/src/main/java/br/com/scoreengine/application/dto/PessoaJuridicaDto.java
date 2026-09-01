package br.com.scoreengine.application.dto;

import java.math.BigDecimal;

public record PessoaJuridicaDto(
        String clienteId,
        BigDecimal faturamentoMensal,
        int tempoAtividadeMeses,
        BigDecimal endividamento,
        BigDecimal lucroMedioMensal,
        BigDecimal liquidez,
        int operacoesCreditoAtivas,
        int tempoRelacionamentoMeses,
        String setorAtividade) implements ClientePerfilDto {
}