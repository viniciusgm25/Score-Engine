package br.com.scoreengine.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidade JPA para registro individualizado de componentes de score de
 * crédito.
 * Suporta o nível granular da trilha de auditoria e explicabilidade regulatória
 * (Bacen / CDC).
 */
@Entity
@Table(name = "score_component_audit", indexes = {
        @Index(name = "idx_comp_audit_parent", columnList = "score_audit_id")
})
public class ScoreComponentAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "score_audit_id", nullable = false)
    private ScoreAuditEntity auditEntity;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "pontuacao", nullable = false, precision = 7, scale = 2)
    private BigDecimal pontuacao;

    @Column(name = "pontuacao_maxima", nullable = false, precision = 7, scale = 2)
    private BigDecimal pontuacaoMaxima;

    @Column(name = "peso_ponderado", nullable = false, precision = 5, scale = 4)
    private BigDecimal pesoPonderado;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    public ScoreComponentAuditEntity() {
    }

    public ScoreComponentAuditEntity(String nome, BigDecimal pontuacao, BigDecimal pontuacaoMaxima,
            BigDecimal pesoPonderado, String motivo) {
        this.nome = Objects.requireNonNull(nome, "O nome do componente não pode ser nulo.");
        this.pontuacao = Objects.requireNonNull(pontuacao, "A pontuação não pode ser nula.");
        this.pontuacaoMaxima = Objects.requireNonNull(pontuacaoMaxima, "A pontuação máxima não pode ser nula.");
        this.pesoPonderado = Objects.requireNonNull(pesoPonderado, "O peso ponderado não pode ser nulo.");
        this.motivo = motivo;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ScoreAuditEntity getAuditEntity() {
        return auditEntity;
    }

    public void setAuditEntity(ScoreAuditEntity auditEntity) {
        this.auditEntity = auditEntity;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(BigDecimal pontuacao) {
        this.pontuacao = pontuacao;
    }

    public BigDecimal getPontuacaoMaxima() {
        return pontuacaoMaxima;
    }

    public void setPontuacaoMaxima(BigDecimal pontuacaoMaxima) {
        this.pontuacaoMaxima = pontuacaoMaxima;
    }

    public BigDecimal getPesoPonderado() {
        return pesoPonderado;
    }

    public void setPesoPonderado(BigDecimal pesoPonderado) {
        this.pesoPonderado = pesoPonderado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}