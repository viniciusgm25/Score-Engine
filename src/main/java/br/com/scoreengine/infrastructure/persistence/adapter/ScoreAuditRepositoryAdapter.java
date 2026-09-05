package br.com.scoreengine.infrastructure.persistence.adapter;

import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.domain.repository.ScoreAuditRepository;
import br.com.scoreengine.infrastructure.persistence.entity.ScoreAuditTrailEntity;
import br.com.scoreengine.infrastructure.persistence.repository.SpringDataScoreAuditRepository;
import org.springframework.stereotype.Component;

@Component
public class ScoreAuditRepositoryAdapter implements ScoreAuditRepository {

    private final SpringDataScoreAuditRepository springDataRepository;

    public ScoreAuditRepositoryAdapter(SpringDataScoreAuditRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public void saveExecution(String taxId, ScoreResult result) {
        ScoreAuditTrailEntity entity = new ScoreAuditTrailEntity(
                taxId,
                result.scoreFinal(),
                result.riskRating().name(),
                result.modelVersion(),
                result.timestamp());
        springDataRepository.save(entity);
    }
}