package br.com.scoreengine.application.service;

import br.com.scoreengine.application.port.out.ScoreAuditPort;
import br.com.scoreengine.domain.calculator.ScoreCalculatorStrategy;
import br.com.scoreengine.domain.exception.ScoreException;
import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.domain.model.pf.CustomerPFProfile;
import br.com.scoreengine.domain.model.pj.CustomerPJProfile;
import br.com.scoreengine.domain.validator.PerfilValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * Orquestrador da Aplicação para Execução de Políticas de Risco e Scoring.
 * Coordena validações cadastrais, seleção da estratégia de cálculo aplicável
 * (PF ou PJ) e envio para auditoria prudencial (Bacen / Basileia / CDC).
 */
public class ScoreService {

    private static final Logger log = LoggerFactory.getLogger(ScoreService.class);

    private final PerfilValidator validator;
    private final List<ScoreCalculatorStrategy<?>> calculatorStrategies;
    private final ScoreAuditPort auditPort;

    public ScoreService(PerfilValidator validator,
            List<ScoreCalculatorStrategy<?>> calculatorStrategies,
            ScoreAuditPort auditPort) {
        this.validator = Objects.requireNonNull(validator, "O validador de perfil não pode ser nulo.");
        this.calculatorStrategies = Objects.requireNonNull(calculatorStrategies,
                "A lista de calculadores não pode ser nula.");
        this.auditPort = Objects.requireNonNull(auditPort, "A porta de auditoria não pode ser nula.");
    }

    /**
     * Processa a avaliação de risco de crédito para clientes Pessoa Física.
     */
    public ScoreResult processarScorePF(CustomerPFProfile profile, String correlationId) {
        Objects.requireNonNull(profile, "O perfil PF não pode ser nulo.");
        log.info("[PF] Iniciando motor de scoring para CPF: [{}]. CorrelationID: [{}]", profile.getCpf(),
                correlationId);
        long start = System.currentTimeMillis();

        validator.validarPF(profile);

        @SuppressWarnings("unchecked")
        ScoreCalculatorStrategy<CustomerPFProfile> calculator = (ScoreCalculatorStrategy<CustomerPFProfile>) calculatorStrategies
                .stream()
                .filter(c -> "PFScoreCalculator".equalsIgnoreCase(c.getClass().getSimpleName()) || c.supports(profile))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Falha de domínio: Calculador PF não registrado no contexto.");
                    return new ScoreException("Calculador de Score PF não encontrado.");
                });

        ScoreResult resultado = calculator.calculate(profile);
        long tempoExecucaoMs = System.currentTimeMillis() - start;

        log.info("[PF] Scoring finalizado. CPF: [{}], Score: [{}], PD: [{}], Faixa: [{}] em {} ms",
                profile.getCpf(), resultado.scoreFinal(), resultado.probabilidadeDefault(), resultado.faixaRisco(),
                tempoExecucaoMs);

        auditPort.saveExecution(profile.getCpf(), resultado, correlationId, tempoExecucaoMs);

        return resultado;
    }

    /**
     * Processa a avaliação de risco de crédito para clientes Pessoa Jurídica.
     */
    public ScoreResult processarScorePJ(CustomerPJProfile profile, String correlationId) {
        Objects.requireNonNull(profile, "O perfil PJ não pode ser nulo.");
        log.info("[PJ] Iniciando motor de scoring para CNPJ: [{}]. CorrelationID: [{}]", profile.getCnpj(),
                correlationId);
        long start = System.currentTimeMillis();

        validator.validarPJ(profile);

        @SuppressWarnings("unchecked")
        ScoreCalculatorStrategy<CustomerPJProfile> calculator = (ScoreCalculatorStrategy<CustomerPJProfile>) calculatorStrategies
                .stream()
                .filter(c -> "PJScoreCalculator".equalsIgnoreCase(c.getClass().getSimpleName()) || c.supports(profile))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Falha de domínio: Calculador PJ não registrado no contexto.");
                    return new ScoreException("Calculador de Score PJ não encontrado.");
                });

        ScoreResult resultado = calculator.calculate(profile);
        long tempoExecucaoMs = System.currentTimeMillis() - start;

        log.info("[PJ] Scoring finalizado. CNPJ: [{}], Score: [{}], PD: [{}], Faixa: [{}] em {} ms",
                profile.getCnpj(), resultado.scoreFinal(), resultado.probabilidadeDefault(), resultado.faixaRisco(),
                tempoExecucaoMs);

        auditPort.saveExecution(profile.getCnpj(), resultado, correlationId, tempoExecucaoMs);

        return resultado;
    }
}