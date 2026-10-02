package br.com.scoreengine.infrastructure.web;

import br.com.scoreengine.application.service.ScoreValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name = "Score Validation",
    description = "Operações de validação e métricas do Score"
)

@RestController
@RequestMapping("/api/v1/score/validation")
public class ScoreValidationController {

    private final ScoreValidationService validationService;

    public ScoreValidationController(ScoreValidationService validationService) {
        this.validationService = validationService;
    }
    @Operation(
        summary = "Executa métricas de validação",
        description = "Executa as métricas disponíveis para validação do modelo de Score."
    )
    @PostMapping("/metrics")
    public ResponseEntity<ScoreValidationService.ValidationResult> avaliarPoderDiscriminatorio(
            @RequestBody ValidationRequest request) {

        ScoreValidationService.ValidationResult result = validationService.calcularMetricasAcuracia(
                request.scores(), request.defaults());

        return ResponseEntity.ok(result);
    }

    public record ValidationRequest(List<Integer> scores, List<Boolean> defaults) {
    }
}