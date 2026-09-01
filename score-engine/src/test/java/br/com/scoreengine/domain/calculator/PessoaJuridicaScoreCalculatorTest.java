package br.com.scoreengine.domain.calculator;

import br.com.scoreengine.domain.enums.ClassificacaoRisco;
import br.com.scoreengine.domain.enums.TipoPessoa;
import br.com.scoreengine.domain.exception.PerfilInvalidoException;
import br.com.scoreengine.domain.model.PessoaFisicaPerfil;
import br.com.scoreengine.domain.model.PessoaJuridicaPerfil;
import br.com.scoreengine.domain.model.ScoreResultado;
import br.com.scoreengine.domain.rules.ModeloScoreV1;
import br.com.scoreengine.domain.rules.PessoaJuridicaRuleConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PessoaJuridicaScoreCalculatorTest {

    private PessoaJuridicaScoreCalculator calculator;

    @BeforeEach
    void setUp() {
        ModeloScoreV1 modelo = new ModeloScoreV1();
        PessoaJuridicaRuleConfig config = new PessoaJuridicaRuleConfig(); // Pesos padrão totalizando 1000
        calculator = new PessoaJuridicaScoreCalculator(modelo, config);
    }

    @Test
    void deveSerElegivelApenasParaPessoaJuridica() {
        assertTrue(calculator.isEligible(TipoPessoa.PJ));
        assertFalse(calculator.isEligible(TipoPessoa.PF));
    }

    @Test
    void deveLancarExcecaoSePerfilNaoForPessoaJuridica() {
        PessoaFisicaPerfil pf = new PessoaFisicaPerfil(
                "CLI-888", new BigDecimal("5000"), 0, BigDecimal.ZERO,
                12, 30, "CASADO", 12, false);

        Exception exception = assertThrows(PerfilInvalidoException.class, () -> calculator.calcular(pf));
        assertEquals("Perfil fornecido não é de Pessoa Jurídica.", exception.getMessage());
    }

    @Test
    void deveCalcularScoreMaximoParaPerfilExcelente() {
        PessoaJuridicaPerfil pj = new PessoaJuridicaPerfil(
                "CLI-PJ-001", new BigDecimal("500000"), 120, BigDecimal.ZERO,
                new BigDecimal("100000"), new BigDecimal("2.5"), 1, 120, "INDUSTRIA");

        ScoreResultado resultado = calculator.calcular(pj);

        assertEquals(1000, resultado.scoreFinal());
        assertEquals(ClassificacaoRisco.MUITO_BAIXO_RISCO, resultado.classificacao());
    }

    @Test
    void deveCalcularScoreParaPerfilComBaixaLiquidezEAltoEndividamento() {
        PessoaJuridicaPerfil pj = new PessoaJuridicaPerfil(
                "CLI-PJ-002", new BigDecimal("50000"), 24, new BigDecimal("0.70"),
                new BigDecimal("2000"), new BigDecimal("0.8"), 3, 12, "COMERCIO");

        ScoreResultado resultado = calculator.calcular(pj);

        assertTrue(resultado.scoreFinal() < 600, "A baixa liquidez e alto endividamento devem derrubar a nota");
        assertNotEquals(ClassificacaoRisco.MUITO_BAIXO_RISCO, resultado.classificacao());
    }
}