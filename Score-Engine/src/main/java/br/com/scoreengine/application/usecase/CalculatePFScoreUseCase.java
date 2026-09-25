package br.com.scoreengine.application.usecase;

import br.com.scoreengine.domain.calculator.ScoreCalculatorStrategy;
import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.domain.model.pf.CustomerPFProfile;
import br.com.scoreengine.domain.repository.ScoreAuditRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CalculatePFScoreUseCase {

    private final ScoreCalculatorStrategy<CustomerPFProfile> calculatorStrategy;
    private final ScoreAuditRepository auditRepository;

    public CalculatePFScoreUseCase(
            @Qualifier("PFScoreCalculator") ScoreCalculatorStrategy<CustomerPFProfile> calculatorStrategy,
            ScoreAuditRepository auditRepository) {
        this.calculatorStrategy = calculatorStrategy;
        this.auditRepository = auditRepository;
    }

    @Transactional
    public ScoreResult execute(CustomerPFProfile profile) {
        ScoreResult result = calculatorStrategy.calculate(profile);
        auditRepository.saveExecution(profile.getCpf(), result);
        return result;
    }
}