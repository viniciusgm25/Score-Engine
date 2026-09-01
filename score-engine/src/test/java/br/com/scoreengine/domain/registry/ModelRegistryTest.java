package br.com.scoreengine.domain.registry;

import br.com.scoreengine.domain.calculator.ScoreCalculator;
import br.com.scoreengine.domain.model.ModelVersion;
import br.com.scoreengine.infrastructure.registry.InMemoryModelRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ModelRegistryTest {

    private ModelRegistry modelRegistry;
    private ScoreCalculator mockCalculator;

    @BeforeEach
    void setUp() {
        mockCalculator = mock(ScoreCalculator.class);
        modelRegistry = new InMemoryModelRegistry(List.of(mockCalculator));
    }

    @Test
    void deveResolverVersaoValidaComSucesso() {
        List<ScoreCalculator> calculators = modelRegistry.resolve(ModelVersion.MODELO_V1);

        assertNotNull(calculators);
        assertFalse(calculators.isEmpty());
        assertEquals(1, calculators.size());
        assertSame(mockCalculator, calculators.get(0));
    }

    @Test
    void deveLancarExcecaoParaVersaoInexistente() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            modelRegistry.resolve(null);
        });
        assertTrue(exception.getMessage().contains("Modelo de escore não encontrado"));
    }

    @Test
    void deveRetornarVersaoPadraoCorreta() {
        assertEquals(ModelVersion.MODELO_V1, modelRegistry.getDefaultVersion());
    }
}