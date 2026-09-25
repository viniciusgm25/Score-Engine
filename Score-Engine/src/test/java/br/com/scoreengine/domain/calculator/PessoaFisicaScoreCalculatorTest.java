package br.com.scoreengine.domain.calculator;

import br.com.scoreengine.domain.enums.ClassificacaoRisco;
import br.com.scoreengine.domain.enums.TipoPessoa;
import br.com.scoreengine.domain.exception.PerfilInvalidoException;
import br.com.scoreengine.domain.model.PessoaFisicaPerfil;
import br.com.scoreengine.domain.model.PessoaJuridicaPerfil;
import br.com.scoreengine.domain.model.ScoreResultado;
import br.com.scoreengine.domain.rules.ModeloScoreV1;
import br.com.scoreengine.domain.rules.PessoaFisicaRuleConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PessoaFisicaScoreCalculatorTest {

    private PessoaFisicaScoreCalculator calculator;

    @BeforeEach
    void setUp() {
        ModeloScoreV1 modelo = new ModeloScoreV1();
        PessoaFisicaRuleConfig config = new PessoaFisicaRuleConfig(); // Pesos padrão totalizando 1000
        calculator = new PessoaFisicaScoreCalculator(modelo, config);
    }

    @Test
    void deveSerElegivelApenasParaPessoaFisica() {
        assertTrue(calculator.isEligible(TipoPessoa.PF));
        assertFalse(calculator.isEligible(TipoPessoa.PJ));
    }

    @Test
    void deveLancarExcecaoSePerfilNaoForPessoaFisica() {
        PessoaJuridicaPerfil pj = new PessoaJuridicaPerfil(
                "CLI-999", new BigDecimal("10000"), 12, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ONE, 0, 12, "TI");

        Exception exception = assertThrows(PerfilInvalidoException.class, () -> calculator.calcular(pj));
        assertEquals("Perfil fornecido não é de Pessoa Física.", exception.getMessage());
    }

    @Test
    void deveCalcularScoreMaximoParaPerfilExcelente() {
        // CORREÇÃO: Endividamento ajustado para BigDecimal.ZERO para garantir os 1000
        // pontos.
        PessoaFisicaPerfil pf = new PessoaFisicaPerfil(
                "CLI-001", new BigDecimal("15000"), 0, BigDecimal.ZERO,
                60, 40, "CASADO", 60, false);

        ScoreResultado resultado = calculator.calcular(pf);

        assertEquals(1000, resultado.scoreFinal());
        assertEquals(ClassificacaoRisco.MUITO_BAIXO_RISCO, resultado.classificacao());
        assertEquals(4, resultado.componentes().size());
    }

    @Test
    void devePenalizarScoreParaPerfilComAtrasosEAltoEndividamento() {
        PessoaFisicaPerfil pf = new PessoaFisicaPerfil(
                "CLI-002", new BigDecimal("3000"), 3, new BigDecimal("0.60"),
                5, 25, "SOLTEIRO", 6, false);

        ScoreResultado resultado = calculator.calcular(pf);

        assertTrue(resultado.scoreFinal() < 500, "Score deveria ser fortemente penalizado");
        assertNotEquals(ClassificacaoRisco.MUITO_BAIXO_RISCO, resultado.classificacao());
    }
}