package br.com.scoreengine.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "score_component_audit")
@Getter
@Setter
@NoArgsConstructor
public class ScoreComponentAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String nome;

    @Column(nullable = false, updatable = false)
    private int pontuacao;

    @Column(nullable = false, updatable = false)
    private int pontuacaoMaxima;

    @Column(length = 500, updatable = false)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_id", nullable = false)
    private ScoreAuditEntity auditEntity;
}