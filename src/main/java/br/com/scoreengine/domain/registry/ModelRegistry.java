package br.com.scoreengine.domain.registry;

import br.com.scoreengine.domain.calculator.ScoreCalculator;
import br.com.scoreengine.domain.model.ModelVersion;

import java.util.List;

/**
 * Contrato (Port) do Registro de Modelos.
 * Responsável estritamente por localizar e disponibilizar os
 * calculadores/regras
 * associados a uma versão específica do modelo de escore.
 */
public interface ModelRegistry {
    List<ScoreCalculator> resolve(ModelVersion version);

    ModelVersion getDefaultVersion();
}