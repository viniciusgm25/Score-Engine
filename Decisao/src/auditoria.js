const SCORE_MINIMO_ANALISE = 400;
const SCORE_MINIMO_APROVACAO = 500;

function validarResultadoScore(scoreResult) {
  if (!scoreResult || typeof scoreResult !== "object") {
    throw new Error("Payload do Score ausente ou inválido.");
  }

  if (typeof scoreResult.clienteId !== "string" || !scoreResult.clienteId.trim()) {
    throw new Error("clienteId é obrigatório no resultado do Score.");
  }

  if (!Number.isInteger(scoreResult.scoreFinal) || scoreResult.scoreFinal < 0 || scoreResult.scoreFinal > 1000) {
    throw new Error("scoreFinal deve ser um inteiro entre 0 e 1000.");
  }

  if (typeof scoreResult.faixaRisco !== "string" || !scoreResult.faixaRisco.trim()) {
    throw new Error("faixaRisco é obrigatório no resultado do Score.");
  }
}

/**
 * Aplica a política de decisão atualmente existente no módulo de Decisão.
 * Nenhum dado cadastral adicional é solicitado: a decisão utiliza apenas o
 * resultado entregue pelo Score.
 */
function decidirScore(scoreResult) {
  validarResultadoScore(scoreResult);

  const score = scoreResult.scoreFinal;

  if (score < SCORE_MINIMO_ANALISE) {
    return {
      decisao: "REPROVADO",
      motivo: `Score ${score} abaixo do mínimo aceitável (${SCORE_MINIMO_ANALISE}).`,
    };
  }

  if (score < SCORE_MINIMO_APROVACAO) {
    return {
      decisao: "ANALISE_MANUAL",
      motivo: `Score ${score} exige análise manual (mínimo para aprovação automática: ${SCORE_MINIMO_APROVACAO}).`,
    };
  }

  return {
    decisao: "APROVADO",
    motivo: `Score ${score} atende ao mínimo para aprovação automática.`,
  };
}

function processarResultadoScore(scoreResult) {
  const resultado = decidirScore(scoreResult);

  const fatoresImpacto = Array.isArray(scoreResult.fatoresImpacto)
    ? scoreResult.fatoresImpacto
    : [];

  return {
    clienteId: scoreResult.clienteId,
    tipoPessoa: scoreResult.tipoPessoa ?? null,
    decisao: resultado.decisao,
    motivo: resultado.motivo,
    scoreFinal: scoreResult.scoreFinal,
    faixaRisco: scoreResult.faixaRisco,
    probabilidadeDefault: scoreResult.probabilidadeDefault ?? null,
    modelo: scoreResult.modelo ?? null,
    calculatedAt: scoreResult.calculatedAt ?? null,
    origem: scoreResult.origem ?? null,
    componentes: Array.isArray(scoreResult.componentes) ? scoreResult.componentes : [],
    fatoresImpacto,
    recebidoEm: new Date().toISOString(),
  };
}

module.exports = {
  SCORE_MINIMO_ANALISE,
  SCORE_MINIMO_APROVACAO,
  validarResultadoScore,
  decidirScore,
  processarResultadoScore,
};
