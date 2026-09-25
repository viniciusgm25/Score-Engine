package br.com.scoreengine.interfaces.rest.mapper;

import br.com.scoreengine.application.dto.UnifiedScoreOutput;
import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.domain.model.pj.CustomerPJProfile;
import br.com.scoreengine.interfaces.rest.dto.request.ScorePJRequestDTO;
import br.com.scoreengine.interfaces.rest.dto.response.ModelInfoDTO;
import br.com.scoreengine.interfaces.rest.dto.response.ScoreComponentResponseDTO;
import br.com.scoreengine.interfaces.rest.dto.response.UnifiedScoreResponseDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Mapper REST para projeção unificada de scores (PF e PJ).
 * Garante a conversão íntegra de requisições corporativas e expõe
 * os componentes de explicabilidade, probabilidade de default (PD)
 * e métricas regulatórias de governança bancária.
 */
@Component
public class UnifiedScoreDtoMapper {

    public CustomerPJProfile toDomainPJ(ScorePJRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        CustomerPJProfile profile = new CustomerPJProfile();
        profile.setCnpj(dto.cnpj());
        profile.setRazaoSocial(dto.razaoSocial());
        profile.setFaturamentoMensal(dto.faturamentoMensal() != null ? dto.faturamentoMensal() : BigDecimal.ZERO);
        profile.setDespesasOperacionaisMensais(
                dto.despesasOperacionaisMensais() != null ? dto.despesasOperacionaisMensais() : BigDecimal.ZERO);
        profile.setPassivoTotalBancario(
                dto.passivoTotalBancario() != null ? dto.passivoTotalBancario() : BigDecimal.ZERO);
        profile.setFaturamentoBrutoAnual(
                dto.faturamentoBrutoAnual() != null ? dto.faturamentoBrutoAnual() : BigDecimal.ZERO);
        profile.setDiasAtrasoUltimos12Meses(dto.diasAtrasoUltimos12Meses());
        profile.setMesesConstituicao(dto.mesesConstituicao());
        profile.setSetorAtuacao(dto.setorAtuacao());
        return profile;
    }

    public UnifiedScoreResponseDTO toUnifiedResponse(UnifiedScoreOutput output) {
        Objects.requireNonNull(output, "O objeto UnifiedScoreOutput não pode ser nulo.");
        ScoreResult res = Objects.requireNonNull(output.scoreResult(), "O ScoreResult consolidado não pode ser nulo.");

        List<ScoreComponentResponseDTO> componentesDTO = res.componentes().stream()
                .map(c -> new ScoreComponentResponseDTO(
                        c.nome(),
                        c.pontuacao(),
                        c.pontuacaoMaxima(),
                        c.pesoPonderado(),
                        c.motivo()))
                .toList();

        String modelCode = output.tipoPessoa().name().equalsIgnoreCase("PF") ? "SCORE_PF" : "SCORE_PJ";

        return new UnifiedScoreResponseDTO(
                output.clienteId(),
                output.tipoPessoa(),
                res.scoreFinal(),
                res.faixaRisco(),
                res.probabilidadeDefault(),
                new ModelInfoDTO(modelCode, res.modelVersion()),
                res.calculatedAt(),
                output.origem(),
                componentesDTO,
                res.fatoresImpacto());
    }
}