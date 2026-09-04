package br.com.scoreengine.infrastructure.web;

import br.com.scoreengine.application.service.ScoreValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/score/validation")
public class ScoreValidationController {

    private final ScoreValidationService validationService;

    public ScoreValidationController(ScoreValidationService validationService) {
        this.validationService = validationService;
    }

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