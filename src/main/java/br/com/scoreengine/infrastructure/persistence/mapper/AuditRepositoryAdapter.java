package br.com.scoreengine.infrastructure.persistence.mapper;

import br.com.scoreengine.application.port.out.ScoreAuditPort;
import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.infrastructure.persistence.entity.ScoreAuditEntity;
import br.com.scoreengine.infrastructure.persistence.repository.ScoreAuditJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Adaptador de Infraestrutura que implementa a Porta de Saída de Auditoria
 * (ScoreAuditPort).
 * Persiste imutavelmente o resultado do cálculo, telemetria, correlação
 * distribuída,
 * parâmetros prudenciais de Basileia (PD) e decomposição analítica dos fatores
 * de decisão.
 */
public class AuditRepositoryAdapter implements ScoreAuditPort {

    private static final Logger log = LoggerFactory.getLogger(AuditRepositoryAdapter.class);

    private final ScoreAuditJpaRepository repository;
    private final ObjectMapper objectMapper;

    public AuditRepositoryAdapter(ScoreAuditJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = Objects.requireNonNull(repository,
                "O repositório ScoreAuditJpaRepository não pode ser nulo.");
        this.objectMapper = Objects.requireNonNull(objectMapper, "O ObjectMapper não pode ser nulo.");
    }

    public AuditRepositoryAdapter(ScoreAuditJpaRepository repository) {
        this(repository, new ObjectMapper());
    }

    @Override
    public void saveExecution(String taxId, ScoreResult resultado, String correlationId, long tempoProcessamentoMs) {
        Objects.requireNonNull(taxId, "O documento fiscal (taxId) não pode ser nulo.");
        Objects.requireNonNull(resultado, "O ScoreResult não pode ser nulo.");

        String tipoPessoa = (taxId.replaceAll("\\D", "").length() > 11) ? "PJ" : "PF";
        String componentesJson = serializarComponentes(resultado);

        ScoreAuditEntity entity = toEntity(
                taxId,
                tipoPessoa,
                resultado.scoreFinal(),
                resultado.faixaRisco(),
                resultado.modelVersion(),
                correlationId,
                tempoProcessamentoMs);

        entity.setProbabilidadeDefault(resultado.probabilidadeDefault());
        entity.setPayloadComponentesJson(componentesJson);

        repository.save(entity);

        log.info(
                "Auditoria regulatória persistida com sucesso. Doc: [{}], Tipo: [{}], Score: [{}], PD: [{}], Latência: [{} ms]",
                taxId, tipoPessoa, resultado.scoreFinal(), resultado.probabilidadeDefault(), tempoProcessamentoMs);
    }

    public ScoreAuditEntity toEntity(
            String clienteId,
            String tipoPessoa,
            int scoreFinal,
            String classificacao,
            String versaoModelo,
            String correlationId,
            long tempoProcessamentoMs) {
        ScoreAuditEntity entity = new ScoreAuditEntity();
        entity.setClienteId(clienteId);
        entity.setTipoPessoa(tipoPessoa);
        entity.setScoreFinal(scoreFinal);
        entity.setClassificacao(classificacao);
        entity.setVersaoModelo(versaoModelo);
        entity.setDataCalculo(LocalDateTime.now());
        entity.setCorrelationId(correlationId);
        entity.setTempoProcessamentoMs(tempoProcessamentoMs);
        return entity;
    }

    private String serializarComponentes(ScoreResult resultado) {
        try {
            return objectMapper.writeValueAsString(resultado.componentes());
        } catch (JsonProcessingException e) {
            log.error("Falha na serialização dos componentes analíticos de risco para auditoria. Doc: {}",
                    resultado.faixaRisco(), e);
            return "[]";
        }
    }
}