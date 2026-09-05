package br.com.scoreengine.interfaces.rest.mapper;

import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.domain.model.pf.CustomerPFProfile;
import br.com.scoreengine.interfaces.rest.dto.request.ScorePFRequestDTO;
import br.com.scoreengine.interfaces.rest.dto.response.ScoreComponentResponseDTO;
import br.com.scoreengine.interfaces.rest.dto.response.ScoreResponseDTO;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ScorePFDtoMapper {

    public CustomerPFProfile toDomain(ScorePFRequestDTO dto) {
        CustomerPFProfile profile = new CustomerPFProfile();
        profile.setCpf(dto.cpf());
        profile.setRendaMensal(dto.rendaMensal());
        profile.setDividaTotal(dto.dividaTotal());
        profile.setIdade(dto.idade());
        profile.setEstadoCivil(dto.estadoCivil());
        profile.setNumeroDependentes(dto.numeroDependentes());
        profile.setDiasAtrasoUltimos12Meses(dto.diasAtrasoUltimos12Meses());
        profile.setLimiteRotativoUtilizado(dto.limiteRotativoUtilizado());
        profile.setLimiteRotativoTotal(dto.limiteRotativoTotal());
        profile.setMesesNoEmpregoAtual(dto.mesesNoEmpregoAtual());
        profile.setMesesRelacionamentoBanco(dto.mesesRelacionamentoBanco());
        return profile;
    }

    public ScoreResponseDTO toResponse(ScoreResult domain) {
        var componentesDTO = domain.componentes().stream()
                .map(c -> new ScoreComponentResponseDTO(
                        c.nome(),
                        c.pontuacao(),
                        c.peso(),
                        c.impacto().name(),
                        c.motivo()))
                .collect(Collectors.toList());

        return new ScoreResponseDTO(
                domain.scoreFinal(),
                domain.riskRating().name(),
                domain.riskRating().getDescricao(),
                domain.modelVersion(),
                componentesDTO,
                domain.timestamp());
    }
}