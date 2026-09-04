package br.com.scoreengine.domain.validator;

import br.com.scoreengine.domain.exception.PerfilInvalidoException;
import br.com.scoreengine.domain.model.PessoaFisicaPerfil;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PerfilValidatorTest {

    private final PerfilValidator validator = new PerfilValidator();

    @Test
    void deveLancarExcecaoParaRendaNegativa() {
        PessoaFisicaPerfil pf = new PessoaFisicaPerfil(
                "CLI-01", new BigDecimal("-1500"), 0, BigDecimal.ZERO, 12, 30, "SOLTEIRO", 12, false);

        Exception exception = assertThrows(PerfilInvalidoException.class, () -> validator.validar(pf));
        assertTrue(exception.getMessage().contains("Renda mensal líquida não pode ser nula ou negativa"));
    }

    @Test
    void deveValidarPerfilCorreto() {
        PessoaFisicaPerfil pf = new PessoaFisicaPerfil(
                "CLI-02", new BigDecimal("5000"), 0, new BigDecimal("0.20"), 48, 35, "CASADO", 36, false);

        assertDoesNotThrow(() -> validator.validar(pf));
    }
}