package br.com.scoreengine.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "score_audit_trail")
public class ScoreAuditTrailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tax_id", nullable = false, length = 14)
    private String taxId;

    @Column(name = "score_final", nullable = false)
    private Integer scoreFinal;

    @Column(name = "risk_rating", nullable = false, length = 10)
    private String riskRating;

    @Column(name = "model_version", nullable = false, length = 20)
    private String modelVersion;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    public ScoreAuditTrailEntity() {
    }

    public ScoreAuditTrailEntity(String taxId, Integer scoreFinal, String riskRating, String modelVersion,
            Instant calculatedAt) {
        this.taxId = taxId;
        this.scoreFinal = scoreFinal;
        this.riskRating = riskRating;
        this.modelVersion = modelVersion;
        this.calculatedAt = calculatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTaxId() {
        return taxId;
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

    public Instant getCalculatedAt() {
        return calculatedAt;
    }
}