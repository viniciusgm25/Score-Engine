package br.com.scoreengine.application.service;

import br.com.scoreengine.application.port.out.ScoreAuditPort;
import br.com.scoreengine.domain.calculator.ScoreCalculatorStrategy;
import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.domain.model.pf.CustomerPFProfile;
import br.com.scoreengine.domain.validator.PerfilValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ScoreServiceTest {

    private PerfilValidator validator;
    private ScoreCalculatorStrategy<CustomerPFProfile> pfCalculator;
    private ScoreAuditPort auditPort;
    private ScoreService scoreService;

    @BeforeEach
    void setUp() {
        validator = mock(PerfilValidator.class);
        pfCalculator = mock(ScoreCalculatorStrategy.class);
        auditPort = mock(ScoreAuditPort.class);
        // List.of(pfCalculator) é inferido como List<ScoreCalculatorStrategy<?>> pelo
        // contexto do construtor (target typing) — é isso que faltava no teste antigo,
        // que usava o tipo ScoreCalculator (não relacionado a
        // ScoreCalculatorStrategy<?>).
        scoreService = new ScoreService(validator, List.of(pfCalculator), auditPort);
    }

    @Test
    void deveProcessarScorePFComSucessoParaClienteElegivel() {
        CustomerPFProfile perfil = new CustomerPFProfile();
        perfil.setCpf("12345678901");
        perfil.setRendaMensal(new BigDecimal("5000.00"));
        perfil.setDividaTotal(new BigDecimal("1000.00"));
        perfil.setIdade(30);
        perfil.setEstadoCivil("SOLTEIRO");
        perfil.setNumeroDependentes(0);
        perfil.setDiasAtrasoUltimos12Meses(0);
        perfil.setLimiteRotativoUtilizado(new BigDecimal("200.00"));
        perfil.setLimiteRotativoTotal(new BigDecimal("1000.00"));
        perfil.setMesesNoEmpregoAtual(24);
        perfil.setMesesRelacionamentoBanco(30);

        String correlationId = UUID.randomUUID().toString();

        // supports() precisa retornar true para que o ScoreService encontre essa
        // estratégia no stream().filter(...); o nome da classe do mock não bate com
        // "PFScoreCalculator", então supports() é o único caminho de seleção aqui.
        when(pfCalculator.supports(perfil)).thenReturn(true);

        ScoreResult mockResultado = new ScoreResult(750, "v1.1.0", List.of());
        when(pfCalculator.calculate(perfil)).thenReturn(mockResultado);

        ScoreResult resultado = scoreService.processarScorePF(perfil, correlationId);

        assertNotNull(resultado);
        assertEquals(750, resultado.scoreFinal());

        verify(validator, times(1)).validarPF(perfil);
        verify(auditPort, times(1)).saveExecution(eq("12345678901"), eq(mockResultado), eq(correlationId), anyLong());
    }
}