package br.com.scoreengine.domain.calculator.pf;

import br.com.scoreengine.domain.model.common.ImpactType;
import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.domain.model.pf.CustomerPFProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PFScoreCalculatorSensitivityTest {

    private PFScoreCalculatorStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PFScoreCalculatorStrategy();
    }

    private CustomerPFProfile criarPerfilPadrao() {
        CustomerPFProfile p = new CustomerPFProfile();
        p.setCpf("12345678900");
        p.setRendaMensal(new BigDecimal("6000.00"));
        p.setDividaTotal(new BigDecimal("1000.00"));
        p.setIdade(32);
        p.setEstadoCivil("CASADO");
        p.setNumeroDependentes(0);
        p.setDiasAtrasoUltimos12Meses(0);
        p.setLimiteRotativoUtilizado(new BigDecimal("300.00"));
        p.setLimiteRotativoTotal(new BigDecimal("3000.00"));
        p.setMesesNoEmpregoAtual(36);
        p.setMesesRelacionamentoBanco(36);
        return p;
    }

    @Test
    @DisplayName("Sensibilidade 1: Histórico de atraso reduz o score e gera componente com impacto negativo")
    void testSensibilidadeAtraso() {
        CustomerPFProfile pontual = criarPerfilPadrao();
        CustomerPFProfile inadimplente = criarPerfilPadrao();
        inadimplente.setDiasAtrasoUltimos12Meses(70);

        ScoreResult resPontual = strategy.calculate(pontual);
        ScoreResult resInadimplente = strategy.calculate(inadimplente);

        assertTrue(resPontual.scoreFinal() > resInadimplente.scoreFinal(),
                "Cliente inadimplente deve possuir score final inferior ao pontual.");
        assertEquals(ImpactType.POSITIVO, resPontual.componentes().get(0).impacto());
        assertEquals(ImpactType.NEGATIVO, resInadimplente.componentes().get(0).impacto());
    }

    @Test
    @DisplayName("Sensibilidade 2: Dependentes impactam a margem de subsistência e a capacidade financeira")
    void testSensibilidadeDependentesECapacidade() {
        CustomerPFProfile semDependentes = criarPerfilPadrao();
        CustomerPFProfile comMuitosDependentes = criarPerfilPadrao();
        comMuitosDependentes.setNumeroDependentes(10); // 10 * 600 = 6000 (absorve toda a renda)

        ScoreResult resSem = strategy.calculate(semDependentes);
        ScoreResult resCom = strategy.calculate(comMuitosDependentes);

        assertTrue(resSem.scoreFinal() > resCom.scoreFinal(),
                "Renda residual absorvida por dependentes deve mitigar a pontuação da capacidade financeira.");
        assertEquals(200, resCom.componentes().get(1).pontuacao());
    }

    @Test
    @DisplayName("Sensibilidade 3: Alavancagem rotativa excessiva penaliza o componente de endividamento")
    void testSensibilidadeUsoRotativo() {
        CustomerPFProfile usoModerado = criarPerfilPadrao();
        CustomerPFProfile usoCritico = criarPerfilPadrao();
        usoCritico.setLimiteRotativoUtilizado(new BigDecimal("2900.00")); // >90%

        ScoreResult resMod = strategy.calculate(usoModerado);
        ScoreResult resCrit = strategy.calculate(usoCritico);

        assertTrue(resMod.scoreFinal() > resCrit.scoreFinal());
        assertEquals(850, resMod.componentes().get(2).pontuacao());
        assertEquals(250, resCrit.componentes().get(2).pontuacao());
    }

    @Test
    @DisplayName("Sensibilidade 4: Variação etária isolada preserva a neutralidade sem deduções arbitrárias")
    void testNeutralidadeEtaria() {
        CustomerPFProfile jovem = criarPerfilPadrao();
        jovem.setIdade(20);

        CustomerPFProfile maduro = criarPerfilPadrao();
        maduro.setIdade(50);

        ScoreResult resJovem = strategy.calculate(jovem);
        ScoreResult resMaduro = strategy.calculate(maduro);

        assertEquals(resJovem.scoreFinal(), resMaduro.scoreFinal(),
                "A idade isolada não deve introduzir variações espúrias na ausência de outros fatores.");
    }
}