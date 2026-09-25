package br.com.scoreengine.infrastructure.registry;

import br.com.scoreengine.domain.calculator.ScoreCalculator;
import br.com.scoreengine.domain.model.ModelVersion;
import br.com.scoreengine.domain.registry.ModelRegistry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Implementação em memória do ModelRegistry para gerenciamento
 * e resolução de versões de modelos.
 */
@Component
public class InMemoryModelRegistry implements ModelRegistry {

    private final Map<ModelVersion, List<ScoreCalculator>> registry;

    public InMemoryModelRegistry(List<ScoreCalculator> calculators) {
        this.registry = Map.of(
                ModelVersion.MODELO_V1,
                List.copyOf(calculators));
    }

    @Override
    public List<ScoreCalculator> resolve(ModelVersion version) {

        if (version == null) {
            throw new IllegalArgumentException(
                    "Modelo de escore não encontrado para a versão: null");
        }

        List<ScoreCalculator> calculators = registry.get(version);

        if (calculators == null) {
            throw new IllegalArgumentException(
                    "Modelo de escore não encontrado para a versão: " + version);
        }

        return calculators;
    }

    @Override
    public ModelVersion getDefaultVersion() {
        return ModelVersion.MODELO_V1;
    }
}