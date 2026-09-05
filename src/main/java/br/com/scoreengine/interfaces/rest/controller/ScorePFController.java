package br.com.scoreengine.interfaces.rest.controller;

import br.com.scoreengine.application.usecase.CalculatePFScoreUseCase;
import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.interfaces.rest.dto.request.ScorePFRequestDTO;
import br.com.scoreengine.interfaces.rest.dto.response.ScoreResponseDTO;
import br.com.scoreengine.interfaces.rest.mapper.ScorePFDtoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/score/pf")
public class ScorePFController {

    private final CalculatePFScoreUseCase calculatePFScoreUseCase;
    private final ScorePFDtoMapper mapper;

    public ScorePFController(CalculatePFScoreUseCase calculatePFScoreUseCase, ScorePFDtoMapper mapper) {
        this.calculatePFScoreUseCase = calculatePFScoreUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<ScoreResponseDTO> calculate(@Valid @RequestBody ScorePFRequestDTO requestDTO) {
        ScoreResult result = calculatePFScoreUseCase.execute(mapper.toDomain(requestDTO));
        return ResponseEntity.ok(mapper.toResponse(result));
    }
}