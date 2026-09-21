package br.com.scoreengine.interfaces.rest.mapper;

import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.domain.model.pf.CustomerPFProfile;
import br.com.scoreengine.interfaces.rest.dto.request.ScorePFRequestDTO;
import br.com.scoreengine.interfaces.rest.dto.response.ScoreComponentResponseDTO;
import br.com.scoreengine.interfaces.rest.dto.response.ScoreResponseDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Mapper de Fronteira REST para Perfil e Resposta de Score de Pessoa Física.
 * Garante a conversão correta dos objetos de transferência para o domínio
 * imutável
 * e projeta as métricas regulatórias de risco (Score, Faixa, PD e
 * Explicabilidade).
 */
@Component
public class ScorePFDtoMapper {

    public CustomerPFProfile toDomain(ScorePFRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        CustomerPFProfile profile = new CustomerPFProfile();
        profile.setCpf(dto.cpf());
        profile.setRendaMensal(dto.rendaMensal() != null ? dto.rendaMensal() : BigDecimal.ZERO);
        profile.setDividaTotal(dto.dividaTotal() != null ? dto.dividaTotal() : BigDecimal.ZERO);
        profile.setIdade(dto.idade());
        profile.setEstadoCivil(dto.estadoCivil());
        profile.setNumeroDependentes(dto.numeroDependentes());
        profile.setDiasAtrasoUltimos12Meses(dto.diasAtrasoUltimos12Meses());
        profile.setLimiteRotativoUtilizado(
                dto.limiteRotativoUtilizado() != null ? dto.limiteRotativoUtilizado() : BigDecimal.ZERO);
        profile.setLimiteRotativoTotal(dto.limiteRotativoTotal() != null ? dto.limiteRotativoTotal() : BigDecimal.ZERO);
        profile.setMesesNoEmpregoAtual(dto.mesesNoEmpregoAtual());
        profile.setMesesRelacionamentoBanco(dto.mesesRelacionamentoBanco());
        return profile;
    }

    public ScoreResponseDTO toResponse(ScoreResult domain) {
        Objects.requireNonNull(domain, "O resultado de domínio (ScoreResult) não pode ser nulo.");

        List<ScoreComponentResponseDTO> componentesDTO = domain.componentes().stream()
                .map(c -> new ScoreComponentResponseDTO(
                        c.nome(),
                        c.pontuacao(),
                        c.pontuacaoMaxima(),
                        c.pesoPonderado(),
                        c.motivo()))
                .toList();

        return new ScoreResponseDTO(
                domain.scoreFinal(),
                domain.faixaRisco(),
                domain.probabilidadeDefault(),
                domain.modelVersion(),
                componentesDTO,
                domain.fatoresImpacto(),
                domain.calculatedAt());
    }
}