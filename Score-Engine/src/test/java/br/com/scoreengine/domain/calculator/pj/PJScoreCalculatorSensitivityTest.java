package br.com.scoreengine.domain.calculator.pj;

import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.domain.model.pj.CustomerPJProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PJScoreCalculatorSensitivityTest {

    private PJScoreCalculatorStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PJScoreCalculatorStrategy();
    }

    private CustomerPJProfile criarPerfilPJBase() {
        CustomerPJProfile p = new CustomerPJProfile();
        p.setCnpj("12345678000195");
        p.setRazaoSocial("Alpha Tech Ltda");
        p.setFaturamentoMensal(new BigDecimal("100000.00"));
        p.setDespesasOperacionaisMensais(new BigDecimal("60000.00"));
        p.setPassivoTotalBancario(new BigDecimal("200000.00"));
        p.setFaturamentoBrutoAnual(new BigDecimal("1200000.00"));
        p.setDiasAtrasoUltimos12Meses(0);
        p.setMesesConstituicao(48);
        p.setSetorAtuacao("SERVICOS");
        return p;
    }

    @Test
    @DisplayName("PJ Sensibilidade 1: Atraso operacional severo degrada histórico e eleva a PD")
    void testSensibilidadeAtrasosPJ() {
        CustomerPJProfile pontual = criarPerfilPJBase();
        CustomerPJProfile atrasada = criarPerfilPJBase();
        atrasada.setDiasAtrasoUltimos12Meses(65);

        ScoreResult resPontual = strategy.calculate(pontual);
        ScoreResult resAtrasada = strategy.calculate(atrasada);

        assertTrue(resPontual.scoreFinal() > resAtrasada.scoreFinal(),
                "A pontuação final da empresa pontual deve ser superior à da empresa inadimplente.");
        assertTrue(resAtrasada.probabilidadeDefault().compareTo(resPontual.probabilidadeDefault()) > 0,
                "A probabilidade de default (PD) da empresa em atraso deve ser superior.");

        assertEquals(0, resPontual.componentes().get(0).pontuacao().compareTo(new BigDecimal("900.00")));
        assertEquals(0, resAtrasada.componentes().get(0).pontuacao().compareTo(new BigDecimal("200.00")));
        assertTrue(resAtrasada.componentes().get(0).motivo().contains("Inadimplência mercantil severa"));
    }

    @Test
    @DisplayName("PJ Sensibilidade 2: Alavancagem bancária excessiva penaliza o componente explicável")
    void testSensibilidadeAlavancagem() {
        CustomerPJProfile equilibrada = criarPerfilPJBase();
        CustomerPJProfile endividada = criarPerfilPJBase();
        endividada.setPassivoTotalBancario(new BigDecimal("900000.00")); // 75% do faturamento anual

        ScoreResult resEq = strategy.calculate(equilibrada);
        ScoreResult resEnd = strategy.calculate(endividada);

        assertTrue(resEq.scoreFinal() > resEnd.scoreFinal(),
                "Empresa com endividamento equilibrado deve obter pontuação final superior.");
        assertEquals(0, resEq.componentes().get(2).pontuacao().compareTo(new BigDecimal("850.00")));
        assertEquals(0, resEnd.componentes().get(2).pontuacao().compareTo(new BigDecimal("250.00")));
        assertTrue(resEnd.componentes().get(2).motivo().contains("Endividamento elevado"));
    }
}