package br.com.scoreengine.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "score_evaluations", indexes = {
        @Index(name = "idx_eval_client_lookup", columnList = "client_id, customer_type, payload_hash, calculated_at DESC")
})
public class ScoreEvaluationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false, length = 20)
    private String clientId;

    @Column(name = "customer_type", nullable = false, length = 5)
    private String customerType;

    @Column(name = "score_final", nullable = false)
    private Integer scoreFinal;

    @Column(name = "risk_rating", nullable = false, length = 20)
    private String riskRating;

    @Column(name = "model_version", nullable = false, length = 20)
    private String modelVersion;

    @Column(name = "payload_hash", nullable = false, length = 64)
    private String payloadHash;

    @Column(name = "components_payload", columnDefinition = "TEXT")
    private String componentsPayload;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    public ScoreEvaluationEntity() {
    }

    public ScoreEvaluationEntity(String clientId, String customerType, Integer scoreFinal,
            String riskRating, String modelVersion, String payloadHash,
            String componentsPayload, Instant calculatedAt) {
        this.clientId = clientId;
        this.customerType = customerType;
        this.scoreFinal = scoreFinal;
        this.riskRating = riskRating;
        this.modelVersion = modelVersion;
        this.payloadHash = payloadHash;
        this.componentsPayload = componentsPayload;
        this.calculatedAt = calculatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getClientId() {
        return clientId;
    }

    public String getCustomerType() {
        return customerType;
    }

    public Integer getScoreFinal() {
        return scoreFinal;
    }

    public String getRiskRating() {
        return riskRating;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public String getPayloadHash() {
        return payloadHash;
    }

    public String getComponentsPayload() {
        return componentsPayload;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }
}