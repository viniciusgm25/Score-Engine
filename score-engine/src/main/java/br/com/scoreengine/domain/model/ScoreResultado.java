package br.com.scoreengine.domain.model;

import br.com.scoreengine.domain.enums.ClassificacaoRisco;
import br.com.scoreengine.domain.enums.TipoPessoa;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidade final que encapsula o resultado do motor de decisão.
 * Imutável para garantir integridade de auditoria.
 */
public record ScoreResultado(
        String clienteId,
        TipoPessoa tipoPessoa,
        int scoreFinal,
        ClassificacaoRisco classificacao,
        List<ScoreComponentResult> componentes,
        String versaoModelo,
        LocalDateTime dataCalculo) {
}