package br.com.scoreengine.application.dto;

import br.com.scoreengine.domain.exception.ScoreException;
import br.com.scoreengine.domain.model.ClientePerfil;
import br.com.scoreengine.domain.model.PessoaFisicaPerfil;
import br.com.scoreengine.domain.model.PessoaJuridicaPerfil;

/**
 * Isolador arquitetural: Converte representações externas (DTO) em Entidades de
 * Domínio puras.
 */
public class ClientePerfilMapper {

    public ClientePerfil toDomain(ClientePerfilDto dto) {
        if (dto == null) {
            throw new ScoreException("DTO de entrada não pode ser nulo para mapeamento.");
        }

        return switch (dto) {
            case PessoaFisicaDto pf -> new PessoaFisicaPerfil(
                    pf.clienteId(),
                    pf.rendaMensalLiquida(),
                    pf.quantidadeAtrasos(),
                    pf.endividamento(),
                    pf.tempoRelacionamentoMeses(),
                    pf.idade(),
                    pf.estadoCivil(),
                    pf.tempoUltimoEmpregoMeses(),
                    pf.possuiAvalista());
            case PessoaJuridicaDto pj -> new PessoaJuridicaPerfil(
                    pj.clienteId(),
                    pj.faturamentoMensal(),
                    pj.tempoAtividadeMeses(),
                    pj.endividamento(),
                    pj.lucroMedioMensal(),
                    pj.liquidez(),
                    pj.operacoesCreditoAtivas(),
                    pj.tempoRelacionamentoMeses(),
                    pj.setorAtividade());
            default -> throw new ScoreException(
                    "Tipo de DTO não suportado para mapeamento: " + dto.getClass().getSimpleName());
        };
    }
}