package br.com.scoreengine.infrastructure.config;

import br.com.scoreengine.application.dto.ClientePerfilMapper;
import br.com.scoreengine.application.port.out.ScoreAuditPort;
import br.com.scoreengine.application.service.ScoreService;
import br.com.scoreengine.domain.calculator.PessoaFisicaScoreCalculator;
import br.com.scoreengine.domain.calculator.PessoaJuridicaScoreCalculator;
import br.com.scoreengine.domain.calculator.ScoreCalculator;
import br.com.scoreengine.domain.rules.ModeloScore;
import br.com.scoreengine.domain.rules.ModeloScoreV1;
import br.com.scoreengine.domain.rules.PessoaFisicaRuleConfig;
import br.com.scoreengine.domain.rules.PessoaJuridicaRuleConfig;
import br.com.scoreengine.domain.validator.PerfilValidator;
import br.com.scoreengine.infrastructure.persistence.mapper.AuditRepositoryAdapter;
import br.com.scoreengine.infrastructure.persistence.repository.ScoreAuditJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * [RETOMADA DOS TRABALHOS DAQUI]
 * Configuration Root: Ensina o ecossistema Spring a instanciar nossas classes
 * de domínio puras.
 * Consolidado com a injeção correta do ScoreAuditJpaRepository para a Fase 12.
 */
@Configuration
public class ScoreConfig {

    @Bean
    public PerfilValidator perfilValidator() {
        return new PerfilValidator();
    }

    @Bean
    public ModeloScore modeloScore() {
        return new ModeloScoreV1();
    }

    @Bean
    public PessoaFisicaRuleConfig pessoaFisicaRuleConfig() {
        return new PessoaFisicaRuleConfig();
    }

    @Bean
    public PessoaJuridicaRuleConfig pessoaJuridicaRuleConfig() {
        return new PessoaJuridicaRuleConfig();
    }

    @Bean
    public ScoreCalculator pessoaFisicaScoreCalculator(ModeloScore modeloScore, PessoaFisicaRuleConfig config) {
        return new PessoaFisicaScoreCalculator(modeloScore, config);
    }

    @Bean
    public ScoreCalculator pessoaJuridicaScoreCalculator(ModeloScore modeloScore, PessoaJuridicaRuleConfig config) {
        return new PessoaJuridicaScoreCalculator(modeloScore, config);
    }

    // Adaptador de Infraestrutura implementando a Porta de Saída (Outbound Port)
    @Bean
    public ScoreAuditPort scoreAuditPort(ScoreAuditJpaRepository repository) {
        return new AuditRepositoryAdapter(repository);
    }

    // Serviço de Aplicação orquestrando o Domínio e a Auditoria
    @Bean
    public ScoreService scoreService(PerfilValidator validator, List<ScoreCalculator> calculators,
            ScoreAuditPort auditPort) {
        return new ScoreService(validator, calculators, auditPort);
    }

    @Bean
    public ClientePerfilMapper clientePerfilMapper() {
        return new ClientePerfilMapper();
    }
}