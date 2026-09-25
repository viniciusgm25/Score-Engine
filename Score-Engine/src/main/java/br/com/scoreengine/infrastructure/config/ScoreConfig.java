package br.com.scoreengine.infrastructure.config;

import br.com.scoreengine.application.dto.ClientePerfilMapper;
import br.com.scoreengine.application.port.out.ScoreAuditPort;
import br.com.scoreengine.application.service.ScoreService;
import br.com.scoreengine.domain.calculator.ScoreCalculatorStrategy;
import br.com.scoreengine.domain.validator.PerfilValidator;
import br.com.scoreengine.infrastructure.persistence.mapper.AuditRepositoryAdapter;
import br.com.scoreengine.infrastructure.persistence.repository.ScoreAuditJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Raiz de Configuração da Arquitetura Hexagonal do Score Engine.
 * Responsável pela injeção de dependências das portas de saída de auditoria,
 * mappers de isolamento de contratos e orquestrador de aplicação
 * (ScoreService),
 * assegurando conformidade com as diretrizes prudenciais do Bacen e Basileia.
 */
@Configuration
public class ScoreConfig {

    @Bean
    public PerfilValidator perfilValidator() {
        return new PerfilValidator();
    }

    @Bean
    public ClientePerfilMapper clientePerfilMapper() {
        return new ClientePerfilMapper();
    }

    /**
     * Adaptador de persistência conectando o repositório JPA à porta de saída
     * de auditoria mandatória (trilha de explicabilidade regulatória e telemetria).
     */
    @Bean
    public ScoreAuditPort scoreAuditPort(ScoreAuditJpaRepository repository, ObjectMapper objectMapper) {
        return new AuditRepositoryAdapter(repository, objectMapper);
    }

    /**
     * Orquestrador de aplicação que recebe as estratégias de cálculo registradas
     * (PFScoreCalculatorStrategy e PJScoreCalculatorStrategy), validador cadastral
     * e a porta de auditoria.
     */
    @Bean
    public ScoreService scoreService(
            PerfilValidator validator,
            List<ScoreCalculatorStrategy<?>> calculatorStrategies,
            ScoreAuditPort auditPort) {
        return new ScoreService(validator, calculatorStrategies, auditPort);
    }
}