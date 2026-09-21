package br.com.scoreengine.interfaces.rest.controller;

import br.com.scoreengine.application.dto.UnifiedScoreOutput;
import br.com.scoreengine.application.usecase.EvaluateUnifiedScoreUseCase;
import br.com.scoreengine.domain.model.common.CustomerType;
import br.com.scoreengine.interfaces.rest.dto.request.UnifiedScoreRequestDTO;
import br.com.scoreengine.interfaces.rest.dto.response.UnifiedScoreResponseDTO;
import br.com.scoreengine.interfaces.rest.mapper.ScorePFDtoMapper;
import br.com.scoreengine.interfaces.rest.mapper.UnifiedScoreDtoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/score")
public class ScorePipelineController {

    private final EvaluateUnifiedScoreUseCase evaluateUseCase;
    private final ScorePFDtoMapper pfMapper;
    private final UnifiedScoreDtoMapper unifiedMapper;

    public ScorePipelineController(EvaluateUnifiedScoreUseCase evaluateUseCase,
            ScorePFDtoMapper pfMapper,
            UnifiedScoreDtoMapper unifiedMapper) {
        this.evaluateUseCase = evaluateUseCase;
        this.pfMapper = pfMapper;
        this.unifiedMapper = unifiedMapper;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<UnifiedScoreResponseDTO> evaluate(@Valid @RequestBody UnifiedScoreRequestDTO request) {
        UnifiedScoreOutput output;

        if (request.tipoPessoa() == CustomerType.PF) {
            if (request.perfilPF() == null) {
                throw new IllegalArgumentException("O bloco perfilPF é obrigatório para tipoPessoa = PF.");
            }
            output = evaluateUseCase.executePF(pfMapper.toDomain(request.perfilPF()), request.forcarRecalculo());
        } else {
            if (request.perfilPJ() == null) {
                throw new IllegalArgumentException("O bloco perfilPJ é obrigatório para tipoPessoa = PJ.");
            }
            output = evaluateUseCase.executePJ(unifiedMapper.toDomainPJ(request.perfilPJ()), request.forcarRecalculo());
        }

        return ResponseEntity.ok(unifiedMapper.toUnifiedResponse(output));
    }
}