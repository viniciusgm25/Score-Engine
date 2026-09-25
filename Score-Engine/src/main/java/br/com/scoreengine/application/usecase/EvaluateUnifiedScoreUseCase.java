package br.com.scoreengine.application.usecase;

import br.com.scoreengine.application.dto.UnifiedScoreOutput;
import br.com.scoreengine.domain.calculator.ScoreCalculatorStrategy;
import br.com.scoreengine.domain.model.common.CustomerType;
import br.com.scoreengine.domain.model.common.ScoreOrigin;
import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.domain.model.pf.CustomerPFProfile;
import br.com.scoreengine.domain.model.pj.CustomerPJProfile;
import br.com.scoreengine.domain.repository.ScoreEvaluationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class EvaluateUnifiedScoreUseCase {

    private static final Duration SCORE_VALIDITY_TTL = Duration.ofDays(30);

    private final ScoreCalculatorStrategy<CustomerPFProfile> pfStrategy;
    private final ScoreCalculatorStrategy<CustomerPJProfile> pjStrategy;
    private final ScoreEvaluationRepository evaluationRepository;
    private final ObjectMapper objectMapper;

    public EvaluateUnifiedScoreUseCase(
            @Qualifier("PFScoreCalculator") ScoreCalculatorStrategy<CustomerPFProfile> pfStrategy,
            @Qualifier("PJScoreCalculator") ScoreCalculatorStrategy<CustomerPJProfile> pjStrategy,
            ScoreEvaluationRepository evaluationRepository,
            ObjectMapper objectMapper) {
        this.pfStrategy = pfStrategy;
        this.pjStrategy = pjStrategy;
        this.evaluationRepository = evaluationRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UnifiedScoreOutput executePF(CustomerPFProfile profile, boolean forcarRecalculo) {
        String clientId = profile.getCpf();
        String currentVersion = pfStrategy.getVersion();
        String payloadHash = profile.generateFingerprint();
        Instant validThreshold = Instant.now().minus(SCORE_VALIDITY_TTL);

        if (!forcarRecalculo) {
            Optional<ScoreResult> existing = evaluationRepository.findValidEvaluation(
                    clientId, CustomerType.PF, payloadHash, validThreshold, currentVersion);

            if (existing.isPresent()) {
                return new UnifiedScoreOutput(clientId, CustomerType.PF, existing.get(), ScoreOrigin.BANCO);
            }
        }

        ScoreResult freshResult = pfStrategy.calculate(profile);
        persistEvaluation(clientId, CustomerType.PF, payloadHash, freshResult);

        return new UnifiedScoreOutput(clientId, CustomerType.PF, freshResult, ScoreOrigin.CALCULO);
    }

    @Transactional
    public UnifiedScoreOutput executePJ(CustomerPJProfile profile, boolean forcarRecalculo) {
        String clientId = profile.getCnpj();
        String currentVersion = pjStrategy.getVersion();
        Instant validThreshold = Instant.now().minus(SCORE_VALIDITY_TTL);

        // Se forcarRecalculo for true, pula a consulta de banco
        if (!forcarRecalculo) {
            Optional<ScoreResult> existing = evaluationRepository.findValidEvaluation(
                    clientId, CustomerType.PJ, profile.generateFingerprint(), validThreshold, currentVersion);

            if (existing.isPresent()) {
                return new UnifiedScoreOutput(clientId, CustomerType.PJ, existing.get(), ScoreOrigin.BANCO);
            }
        }

        // Executa novo cálculo
        ScoreResult freshResult = pjStrategy.calculate(profile);
        persistEvaluation(clientId, CustomerType.PJ, profile.generateFingerprint(), freshResult);

        return new UnifiedScoreOutput(clientId, CustomerType.PJ, freshResult, ScoreOrigin.CALCULO);
    }

    private void persistEvaluation(String clientId, CustomerType type, String payloadHash, ScoreResult result) {
        String serialized = null;
        try {
            serialized = objectMapper.writeValueAsString(result.componentes());
        } catch (Exception ignored) {
        }
        evaluationRepository.saveEvaluation(clientId, type, payloadHash, result, serialized);
    }
}