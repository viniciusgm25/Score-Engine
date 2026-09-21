package br.com.scoreengine.infrastructure.persistence.adapter;

import br.com.scoreengine.domain.model.common.CustomerType;
import br.com.scoreengine.domain.model.common.ScoreComponent;
import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.domain.repository.ScoreEvaluationRepository;
import br.com.scoreengine.infrastructure.persistence.entity.ScoreEvaluationEntity;
import br.com.scoreengine.infrastructure.persistence.repository.SpringDataScoreEvaluationRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador de Infraestrutura para consulta e persistência de avaliações de
 * risco.
 * Garante idempotência por hash de payload e rastreabilidade integral dos
 * componentes.
 */
@Component
public class ScoreEvaluationRepositoryAdapter implements ScoreEvaluationRepository {

    private static final Logger log = LoggerFactory.getLogger(ScoreEvaluationRepositoryAdapter.class);

    private final SpringDataScoreEvaluationRepository repository;
    private final ObjectMapper objectMapper;

    public ScoreEvaluationRepositoryAdapter(SpringDataScoreEvaluationRepository repository, ObjectMapper objectMapper) {
        this.repository = Objects.requireNonNull(repository, "O repositório JPA de avaliação não pode ser nulo.");
        this.objectMapper = Objects.requireNonNull(objectMapper, "O ObjectMapper não pode ser nulo.");
    }

    @Override
    public Optional<ScoreResult> findValidEvaluation(String clientId, CustomerType customerType, String payloadHash,
            Instant validSince, String modelVersion) {
        Objects.requireNonNull(clientId, "O identificador do cliente não pode ser nulo.");
        Objects.requireNonNull(customerType, "O tipo de cliente não pode ser nulo.");
        Objects.requireNonNull(payloadHash, "O hash do payload não pode ser nulo.");
        Objects.requireNonNull(validSince, "A data de validade não pode ser nula.");
        Objects.requireNonNull(modelVersion, "A versão do modelo não pode ser nula.");

        return repository
                .findLatestMatchingEvaluation(clientId, customerType.name(), payloadHash, validSince, modelVersion)
                .map(entity -> {
                    List<ScoreComponent> componentes = Collections.emptyList();
                    if (entity.getComponentsPayload() != null && !entity.getComponentsPayload().isBlank()) {
                        try {
                            componentes = objectMapper.readValue(entity.getComponentsPayload(), new TypeReference<>() {
                            });
                        } catch (Exception e) {
                            log.error("Erro ao desserializar componentes de risco da avaliação [ID: {}] do cliente: {}",
                                    entity.getId(), clientId, e);
                        }
                    }

                    return new ScoreResult(
                            entity.getScoreFinal(),
                            entity.getModelVersion(),
                            componentes);
                });
    }

    @Override
    public void saveEvaluation(String clientId, CustomerType customerType, String payloadHash, ScoreResult result,
            String serializedComponents) {
        Objects.requireNonNull(clientId, "O identificador do cliente não pode ser nulo.");
        Objects.requireNonNull(customerType, "O tipo de cliente não pode ser nulo.");
        Objects.requireNonNull(payloadHash, "O hash do payload não pode ser nulo.");
        Objects.requireNonNull(result, "O resultado do score não pode ser nulo.");

        ScoreEvaluationEntity entity = new ScoreEvaluationEntity(
                clientId,
                customerType.name(),
                result.scoreFinal(),
                result.faixaRisco(),
                result.modelVersion(),
                payloadHash,
                serializedComponents,
                result.calculatedAt());

        repository.save(entity);
        log.info("Avaliação de crédito persistida. Cliente: [{}], Tipo: [{}], Score: [{}], Versão: [{}]",
                clientId, customerType, result.scoreFinal(), result.modelVersion());
    }
}