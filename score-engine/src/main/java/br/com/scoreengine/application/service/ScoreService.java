package br.com.scoreengine.application.service;

import br.com.scoreengine.application.port.out.ScoreAuditPort;
import br.com.scoreengine.domain.calculator.ScoreCalculator;
import br.com.scoreengine.domain.exception.ScoreException;
import br.com.scoreengine.domain.model.ClientePerfil;
import br.com.scoreengine.domain.model.ScoreResultado;
import br.com.scoreengine.domain.validator.PerfilValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ScoreService {

    private static final Logger log = LoggerFactory.getLogger(ScoreService.class);

    private final PerfilValidator validator;
    private final List<ScoreCalculator> calculators;
    private final ScoreAuditPort auditPort;

    public ScoreService(PerfilValidator validator, List<ScoreCalculator> calculators, ScoreAuditPort auditPort) {
        this.validator = validator;
        this.calculators = calculators;
        this.auditPort = auditPort;
    }

    public ScoreResultado processarScore(ClientePerfil perfil, String correlationId) {
        log.info("Iniciando cálculo de score para o cliente: {} - Tipo: {}", perfil.clienteId(), perfil.tipoPessoa());
        long start = System.currentTimeMillis();

        validator.validar(perfil);

        ScoreCalculator calculator = calculators.stream()
                .filter(c -> c.isEligible(perfil.tipoPessoa()))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Falha de domínio: Nenhum calculador elegível encontrado para o tipo {}",
                            perfil.tipoPessoa());
                    return new ScoreException("Calculador não encontrado.");
                });

        ScoreResultado resultado = calculator.calcular(perfil);

        long tempoProcessamentoMs = System.currentTimeMillis() - start;

        log.info("Score calculado com sucesso: {} pontos. Registrando trilha de auditoria...", resultado.scoreFinal());
        auditPort.registrar(resultado, correlationId, tempoProcessamentoMs);

        log.info("Processamento finalizado em {} ms", tempoProcessamentoMs);

        return resultado;
    }
}