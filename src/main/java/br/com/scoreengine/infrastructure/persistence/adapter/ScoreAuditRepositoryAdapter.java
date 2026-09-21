package br.com.scoreengine.infrastructure.persistence.adapter;

import br.com.scoreengine.domain.model.common.ScoreComponent;
import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.domain.repository.ScoreAuditRepository;
import br.com.scoreengine.infrastructure.persistence.entity.ScoreAuditTrailEntity;
import br.com.scoreengine.infrastructure.persistence.repository.SpringDataScoreAuditRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Adaptador de Infraestrutura para Trilha de Auditoria de Score de Crédito.
 * Registra a decisão de risco, probabilidade de default e a decomposição
 * analítica
 * de regras (explicabilidade regulatória Bacen / Basileia / CDC).
 */
@Component
public class ScoreAuditRepositoryAdapter implements ScoreAuditRepository {

    private static final Logger log = LoggerFactory.getLogger(ScoreAuditRepositoryAdapter.class);

    private final SpringDataScoreAuditRepository springDataRepository;
    private final ObjectMapper objectMapper;

    public ScoreAuditRepositoryAdapter(SpringDataScoreAuditRepository springDataRepository, ObjectMapper objectMapper) {
        this.springDataRepository = Objects.requireNonNull(springDataRepository,
                "O repositório JPA não pode ser nulo.");
        this.objectMapper = Objects.requireNonNull(objectMapper, "O ObjectMapper não pode ser nulo.");
    }

    @Override
    public void saveExecution(String taxId, ScoreResult result) {
        Objects.requireNonNull(taxId, "O identificador fiscal (taxId) não pode ser nulo.");
        Objects.requireNonNull(result, "O resultado de score não pode ser nulo.");

        String componentesJson = serializarComponentes(result);

        ScoreAuditTrailEntity entity = new ScoreAuditTrailEntity(
                taxId,
                result.scoreFinal(),
                result.faixaRisco(),
                result.probabilidadeDefault(),
                result.modelVersion(),
                componentesJson,
                result.calculatedAt());

        springDataRepository.save(entity);
        log.info("Auditoria de risco persistida com sucesso. Documento: [{}], Score: [{}], PD: [{}]",
                taxId, result.scoreFinal(), result.probabilidadeDefault());
    }

    private String serializarComponentes(ScoreResult result) {
        try {
            return objectMapper.writeValueAsString(result.componentes());
        } catch (JsonProcessingException e) {
            log.error("Falha ao serializar componentes analíticos para auditoria do documento: {}", result.faixaRisco(),
                    e);
            return "[]";
        }
    }
}