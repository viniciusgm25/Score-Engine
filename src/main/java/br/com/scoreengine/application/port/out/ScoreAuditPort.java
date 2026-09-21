package br.com.scoreengine.application.port.out;

import br.com.scoreengine.domain.model.common.ScoreResult;

/**
 * Porta de Saída (Outbound Port) para Auditoria e Rastreabilidade do Score de
 * Crédito.
 * Atende às exigências de explicabilidade, governança e conformidade prudencial
 * (Bacen / Basileia / CDC).
 */
public interface ScoreAuditPort {

    /**
     * Registra a execução completa do cálculo de risco com parâmetros de
     * rastreabilidade distribuída.
     *
     * @param taxId                Documento fiscal do cliente (CPF ou CNPJ)
     * @param resultado            Resultado imutável consolidado do cálculo
     * @param correlationId        Identificador de correlação para rastreabilidade
     *                             de requisição
     * @param tempoProcessamentoMs Latência do cálculo em milissegundos
     */
    void saveExecution(String taxId, ScoreResult resultado, String correlationId, long tempoProcessamentoMs);

    /**
     * Sobrecarga de conveniência para persistência direta em fluxos simplificados
     * ou testes.
     *
     * @param taxId     Documento fiscal do cliente (CPF ou CNPJ)
     * @param resultado Resultado imutável consolidado do cálculo
     */
    default void saveExecution(String taxId, ScoreResult resultado) {
        saveExecution(taxId, resultado, "DEFAULT-CORRELATION", 0L);
    }
}