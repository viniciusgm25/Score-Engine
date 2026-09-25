package br.com.scoreengine.interfaces.rest.controller;

import br.com.scoreengine.application.dto.UnifiedScoreOutput;
import br.com.scoreengine.application.port.out.ScoreToDecisionPort;
import br.com.scoreengine.application.usecase.EvaluateUnifiedScoreUseCase;
import br.com.scoreengine.domain.model.common.CustomerType;
import br.com.scoreengine.interfaces.rest.dto.request.UnifiedScoreRequestDTO;
import br.com.scoreengine.interfaces.rest.dto.response.UnifiedScoreResponseDTO;
import br.com.scoreengine.interfaces.rest.mapper.ScorePFDtoMapper;
import br.com.scoreengine.interfaces.rest.mapper.UnifiedScoreDtoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name = "Score Pipeline",
    description = "Pipeline de avaliação de score de crédito"
)

@RestController
@RequestMapping("/api/v1/score")
public class ScorePipelineController {

    private final EvaluateUnifiedScoreUseCase evaluateUseCase;
    private final ScorePFDtoMapper pfMapper;
    private final UnifiedScoreDtoMapper unifiedMapper;
    private final ScoreToDecisionPort scoreToDecisionPort;

    public ScorePipelineController(
            EvaluateUnifiedScoreUseCase evaluateUseCase,
            ScorePFDtoMapper pfMapper,
            UnifiedScoreDtoMapper unifiedMapper,
            ScoreToDecisionPort scoreToDecisionPort) {
        this.evaluateUseCase = evaluateUseCase;
        this.pfMapper = pfMapper;
        this.unifiedMapper = unifiedMapper;
        this.scoreToDecisionPort = scoreToDecisionPort;
    }

    /**
     * Calcula o Score e, na sequência, envia o resultado consolidado ao
     * microsserviço de Decisão.
     */

    @Operation(
        summary = "Avalia o score de um cliente",
        description = "Recebe os dados do cliente, processa a avaliação de score e retorna o resultado calculado."
    )
    @PostMapping("/evaluate")
    public ResponseEntity<UnifiedScoreResponseDTO> evaluate(
            @Valid @RequestBody UnifiedScoreRequestDTO request) {

        UnifiedScoreOutput output;

        if (request.tipoPessoa() == CustomerType.PF) {
            if (request.perfilPF() == null) {
                throw new IllegalArgumentException("O bloco perfilPF é obrigatório para tipoPessoa = PF.");
            }
            output = evaluateUseCase.executePF(
                    pfMapper.toDomain(request.perfilPF()),
                    request.forcarRecalculo());
        } else {
            if (request.perfilPJ() == null) {
                throw new IllegalArgumentException("O bloco perfilPJ é obrigatório para tipoPessoa = PJ.");
            }
            output = evaluateUseCase.executePJ(
                    unifiedMapper.toDomainPJ(request.perfilPJ()),
                    request.forcarRecalculo());
        }

        UnifiedScoreResponseDTO response = unifiedMapper.toUnifiedResponse(output);

        // O Score calcula. A Decisão recebe esse resultado e decide.
        scoreToDecisionPort.enviar(response);

        return ResponseEntity.ok(response);
    }
}
