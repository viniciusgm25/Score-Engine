package br.com.scoreengine.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Entidade JPA para Trilha de Auditoria do Score de Crédito.
 * Registra imutavelmente a pontuação, faixa de risco, probabilidade de default
 * (PD),
 * versão do modelo e a decomposição analítica dos fatores de decisão (Bacen /
 * Basileia / CDC).
 */
@Entity
@Table(name = "score_audit_trail", indexes = {
        @Index(name = "idx_audit_tax_id", columnList = "tax_id"),
        @Index(name = "idx_audit_calculated_at", columnList = "calculated_at")
})
public class ScoreAuditTrailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tax_id", nullable = false, length = 14)
    private String taxId;

    @Column(name = "score_final", nullable = false)
    private Integer scoreFinal;

    @Column(name = "risk_rating", nullable = false, length = 30)
    private String riskRating;

    @Column(name = "probabilidade_default", nullable = false, precision = 6, scale = 4)
    private BigDecimal probabilidadeDefault;

    @Column(name = "model_version", nullable = false, length = 20)
    private String modelVersion;

    @Column(name = "components_payload", nullable = false, columnDefinition = "TEXT")
    private String componentsPayload;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    public ScoreAuditTrailEntity() {
    }

    public ScoreAuditTrailEntity(String taxId, Integer scoreFinal, String riskRating,
            BigDecimal probabilidadeDefault, String modelVersion,
            String componentsPayload, Instant calculatedAt) {
        this.taxId = Objects.requireNonNull(taxId, "O documento (taxId) não pode ser nulo.");
        this.scoreFinal = Objects.requireNonNull(scoreFinal, "O scoreFinal não pode ser nulo.");
        this.riskRating = Objects.requireNonNull(riskRating, "O riskRating/faixa de risco não pode ser nulo.");
        this.probabilidadeDefault = Objects.requireNonNull(probabilidadeDefault,
                "A probabilidade de default não pode ser nula.");
        this.modelVersion = Objects.requireNonNull(modelVersion, "A versão do modelo não pode ser nula.");
        this.componentsPayload = componentsPayload != null ? componentsPayload : "[]";
        this.calculatedAt = Objects.requireNonNull(calculatedAt, "O timestamp de cálculo não pode ser nulo.");
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

    public BigDecimal getProbabilidadeDefault() {
        return probabilidadeDefault;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public String getComponentsPayload() {
        return componentsPayload;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }
}