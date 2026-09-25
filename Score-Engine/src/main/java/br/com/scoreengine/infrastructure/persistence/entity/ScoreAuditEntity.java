package br.com.scoreengine.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade JPA para Trilha de Auditoria do Score de Crédito.
 * Registra a pontuação de risco, PD prudencial de Basileia e os componentes
 * analíticos de explicabilidade em conformidade com Bacen e CDC.
 */
@Entity
@Table(name = "score_audit", indexes = {
        @Index(name = "idx_audit_cliente_id", columnList = "clienteId"),
        @Index(name = "idx_audit_data_calculo", columnList = "dataCalculo")
})
public class ScoreAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false, length = 20)
    private String clienteId;

    @Column(name = "tipo_pessoa", nullable = false, length = 10)
    private String tipoPessoa;

    @Column(name = "score_final", nullable = false)
    private int scoreFinal;

    @Column(name = "probabilidade_default", nullable = false, precision = 6, scale = 4)
    private BigDecimal probabilidadeDefault;

    @Column(name = "classificacao", nullable = false, length = 30)
    private String classificacao;

    @Column(name = "versao_modelo", nullable = false, length = 20)
    private String versaoModelo;

    @Column(name = "payload_componentes_json", columnDefinition = "TEXT")
    private String payloadComponentesJson;

    @Column(name = "data_calculo", nullable = false)
    private LocalDateTime dataCalculo;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "tempo_processamento_ms")
    private long tempoProcessamentoMs;

    @OneToMany(mappedBy = "auditEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScoreComponentAuditEntity> componentes = new ArrayList<>();

    public ScoreAuditEntity() {
    }

    public void addComponente(ScoreComponentAuditEntity componente) {
        if (componentes == null) {
            componentes = new ArrayList<>();
        }
        componentes.add(componente);
        componente.setAuditEntity(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getTipoPessoa() {
        return tipoPessoa;
    }

    public void setTipoPessoa(String tipoPessoa) {
        this.tipoPessoa = tipoPessoa;
    }

    public int getScoreFinal() {
        return scoreFinal;
    }

    public void setScoreFinal(int scoreFinal) {
        this.scoreFinal = scoreFinal;
    }

    public BigDecimal getProbabilidadeDefault() {
        return probabilidadeDefault;
    }

    public void setProbabilidadeDefault(BigDecimal probabilidadeDefault) {
        this.probabilidadeDefault = probabilidadeDefault;
    }

    public String getClassificacao() {
        return classificacao;
    }

    public void setClassificacao(String classificacao) {
        this.classificacao = classificacao;
    }

    public String getVersaoModelo() {
        return versaoModelo;
    }

    public void setVersaoModelo(String versaoModelo) {
        this.versaoModelo = versaoModelo;
    }

    public String getPayloadComponentesJson() {
        return payloadComponentesJson;
    }

    public void setPayloadComponentesJson(String payloadComponentesJson) {
        this.payloadComponentesJson = payloadComponentesJson;
    }

    public LocalDateTime getDataCalculo() {
        return dataCalculo;
    }

    public void setDataCalculo(LocalDateTime dataCalculo) {
        this.dataCalculo = dataCalculo;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public long getTempoProcessamentoMs() {
        return tempoProcessamentoMs;
    }

    public void setTempoProcessamentoMs(long tempoProcessamentoMs) {
        this.tempoProcessamentoMs = tempoProcessamentoMs;
    }

    public List<ScoreComponentAuditEntity> getComponentes() {
        return componentes;
    }

    public void setComponentes(List<ScoreComponentAuditEntity> componentes) {
        this.componentes = componentes;
    }
}