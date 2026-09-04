package br.com.scoreengine.infrastructure.persistence.mapper;

import br.com.scoreengine.application.port.out.ScoreAuditPort;
import br.com.scoreengine.domain.model.ScoreResultado;
import br.com.scoreengine.infrastructure.persistence.entity.ScoreAuditEntity;
import br.com.scoreengine.infrastructure.persistence.entity.ScoreComponentAuditEntity;
import br.com.scoreengine.infrastructure.persistence.repository.ScoreAuditJpaRepository;

public class AuditRepositoryAdapter implements ScoreAuditPort {

    private final ScoreAuditJpaRepository repository;

    // Construtor atualizado para receber o JpaRepository correto
    public AuditRepositoryAdapter(ScoreAuditJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void registrar(ScoreResultado resultado, String correlationId, long tempoProcessamentoMs) {
        ScoreAuditEntity entity = new ScoreAuditEntity();
        entity.setClienteId(resultado.clienteId());
        entity.setTipoPessoa(resultado.tipoPessoa().name());
        entity.setScoreFinal(resultado.scoreFinal());
        entity.setClassificacao(resultado.classificacao().name());
        entity.setVersaoModelo(resultado.versaoModelo());
        entity.setDataCalculo(resultado.dataCalculo());
        entity.setCorrelationId(correlationId);
        entity.setTempoProcessamentoMs(tempoProcessamentoMs);

        resultado.componentes().forEach(comp -> {
            ScoreComponentAuditEntity compEntity = new ScoreComponentAuditEntity();
            compEntity.setNome(comp.nome());
            compEntity.setPontuacao(comp.pontuacao());
            compEntity.setPontuacaoMaxima(comp.pontuacaoMaxima());
            compEntity.setMotivo(comp.motivo());
            entity.addComponente(compEntity);
        });

        repository.save(entity);
    }
}