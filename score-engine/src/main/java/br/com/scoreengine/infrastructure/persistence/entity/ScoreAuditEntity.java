package br.com.scoreengine.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "score_audit")
@Getter
@Setter
@NoArgsConstructor
public class ScoreAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private String clienteId;

    @Column(nullable = false, updatable = false)
    private String tipoPessoa;

    @Column(nullable = false, updatable = false)
    private int scoreFinal;

    @Column(nullable = false, updatable = false)
    private String classificacao;

    @Column(nullable = false, updatable = false)
    private String versaoModelo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCalculo;

    @Column(nullable = false, updatable = false)
    private String correlationId;

    @Column(nullable = false, updatable = false)
    private long tempoProcessamentoMs;

    @OneToMany(mappedBy = "auditEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScoreComponentAuditEntity> componentes = new ArrayList<>();

    public void addComponente(ScoreComponentAuditEntity componente) {
        componentes.add(componente);
        componente.setAuditEntity(this);
    }
}