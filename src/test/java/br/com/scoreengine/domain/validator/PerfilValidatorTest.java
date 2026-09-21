package br.com.scoreengine.domain.validator;

import br.com.scoreengine.domain.exception.PerfilInvalidoException;
import br.com.scoreengine.domain.model.pf.CustomerPFProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PerfilValidatorTest {

    private PerfilValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PerfilValidator();
    }

    private CustomerPFProfile criarPerfilPFValido() {
        CustomerPFProfile pf = new CustomerPFProfile();
        pf.setCpf("12345678901");
        pf.setRendaMensal(new BigDecimal("5000.00"));
        pf.setDividaTotal(new BigDecimal("1000.00"));
        pf.setLimiteRotativoUtilizado(new BigDecimal("200.00"));
        pf.setLimiteRotativoTotal(new BigDecimal("1500.00"));
        pf.setIdade(30);
        pf.setEstadoCivil("CASADO");
        pf.setNumeroDependentes(1);
        pf.setDiasAtrasoUltimos12Meses(0);
        pf.setMesesNoEmpregoAtual(24);
        pf.setMesesRelacionamentoBanco(36);
        return pf;
    }

    @Test
    @DisplayName("Deve validar com sucesso quando o perfil PF contiver dados corretos e saneados")
    void deveValidarPerfilPFCorreto() {
        CustomerPFProfile pf = criarPerfilPFValido();
        assertDoesNotThrow(() -> validator.validarPF(pf));
    }

    @Test
    @DisplayName("Deve lançar exceção quando a renda mensal for negativa")
    void deveLancarExcecaoParaRendaNegativa() {
        CustomerPFProfile pf = criarPerfilPFValido();
        pf.setRendaMensal(new BigDecimal("-1500.00"));

        PerfilInvalidoException ex = assertThrows(PerfilInvalidoException.class, () -> validator.validarPF(pf));
        assertTrue(ex.getMessage().contains("Renda mensal não pode ser nulo ou negativo"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o CPF possuir tamanho diferente de 11 dígitos")
    void deveLancarExcecaoParaCpfInvalido() {
        CustomerPFProfile pf = criarPerfilPFValido();
        pf.setCpf("12345");

        PerfilInvalidoException ex = assertThrows(PerfilInvalidoException.class, () -> validator.validarPF(pf));
        assertTrue(ex.getMessage().contains("Esperados 11 dígitos numéricos"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o cliente for menor de 18 anos")
    void deveLancarExcecaoParaMenorDeIdade() {
        CustomerPFProfile pf = criarPerfilPFValido();
        pf.setIdade(17);

        PerfilInvalidoException ex = assertThrows(PerfilInvalidoException.class, () -> validator.validarPF(pf));
        assertTrue(ex.getMessage().contains("idade igual ou superior a 18 anos"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o limite rotativo utilizado for negativo")
    void deveLancarExcecaoParaLimiteRotativoUtilizadoNegativo() {
        CustomerPFProfile pf = criarPerfilPFValido();
        pf.setLimiteRotativoUtilizado(new BigDecimal("-50.00"));

        PerfilInvalidoException ex = assertThrows(PerfilInvalidoException.class, () -> validator.validarPF(pf));
        assertTrue(ex.getMessage().contains("Limite rotativo utilizado não pode ser nulo ou negativo"));
    }
}