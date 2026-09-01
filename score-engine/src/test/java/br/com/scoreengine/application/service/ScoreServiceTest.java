package br.com.scoreengine.application.service;

import br.com.scoreengine.application.port.out.ScoreAuditPort;
import br.com.scoreengine.domain.calculator.ScoreCalculator;
import br.com.scoreengine.domain.enums.ClassificacaoRisco;
import br.com.scoreengine.domain.enums.TipoPessoa;
import br.com.scoreengine.domain.model.PessoaFisicaPerfil;
import br.com.scoreengine.domain.model.ScoreResultado;
import br.com.scoreengine.domain.validator.PerfilValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ScoreServiceTest {

    private PerfilValidator validator;
    private ScoreCalculator calculator;
    private ScoreAuditPort auditPort;
    private ScoreService scoreService;

    @BeforeEach
    void setUp() {
        validator = mock(PerfilValidator.class);
        calculator = mock(ScoreCalculator.class);
        auditPort = mock(ScoreAuditPort.class);
        scoreService = new ScoreService(validator, List.of(calculator), auditPort);
    }

    @Test
    void deveProcessarScoreComSucessoParaClienteElegivel() {
        PessoaFisicaPerfil perfil = new PessoaFisicaPerfil(
                "CLI-001",
                new BigDecimal("5000.00"),
                0,
                new BigDecimal("1000.00"),
                24,
                30,
                "Solteiro",
                12,
                false);

        String correlationId = UUID.randomUUID().toString();

        when(calculator.isEligible(TipoPessoa.PF)).thenReturn(true);

        ScoreResultado mockResultado = new ScoreResultado(
                "CLI-001",
                TipoPessoa.PF,
                750,
                ClassificacaoRisco.BAIXO_RISCO,
                List.of(),
                "v1",
                LocalDateTime.now());
        when(calculator.calcular(perfil)).thenReturn(mockResultado);

        ScoreResultado resultado = scoreService.processarScore(perfil, correlationId);

        assertNotNull(resultado);
        assertEquals(750, resultado.scoreFinal());
        assertEquals(ClassificacaoRisco.BAIXO_RISCO, resultado.classificacao());

        verify(validator, times(1)).validar(perfil);
        verify(auditPort, times(1)).registrar(eq(mockResultado), eq(correlationId), anyLong());
    }
}