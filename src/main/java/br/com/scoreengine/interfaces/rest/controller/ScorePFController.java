package br.com.scoreengine.interfaces.rest.controller;

import br.com.scoreengine.application.usecase.CalculatePFScoreUseCase;
import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.interfaces.rest.dto.request.ScorePFRequestDTO;
import br.com.scoreengine.interfaces.rest.dto.response.ScoreResponseDTO;
import br.com.scoreengine.interfaces.rest.mapper.ScorePFDtoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name = "Score PF",
    description = "Operações de cálculo e avaliação de score para pessoa física"
)
@RestController
@RequestMapping("/api/v1/score/pf")
public class ScorePFController {

    private final CalculatePFScoreUseCase calculatePFScoreUseCase;
    private final ScorePFDtoMapper mapper;

    public ScorePFController(CalculatePFScoreUseCase calculatePFScoreUseCase, ScorePFDtoMapper mapper) {
        this.calculatePFScoreUseCase = calculatePFScoreUseCase;
        this.mapper = mapper;
    }
    @Operation(
        summary = "Calcula score de pessoa física",
        description = "Processa uma avaliação de score específica para clientes pessoa física."
    )
    @PostMapping
    public ResponseEntity<ScoreResponseDTO> calculate(@Valid @RequestBody ScorePFRequestDTO requestDTO) {
        ScoreResult result = calculatePFScoreUseCase.execute(mapper.toDomain(requestDTO));
        return ResponseEntity.ok(mapper.toResponse(result));
    }
}