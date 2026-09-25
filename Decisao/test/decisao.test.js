const {
  SCORE_MINIMO_ANALISE,
  SCORE_MINIMO_APROVACAO,
  decidirScore,
  processarResultadoScore,
} = require("../src/auditoria");

function criarScore(overrides = {}) {
  return {
    clienteId: "12345678901",
    tipoPessoa: "PF",
    scoreFinal: 650,
    faixaRisco: "BOM",
    probabilidadeDefault: 0.035,
    modelo: { codigo: "SCORE_PF", versao: "v1.1.0" },
    calculatedAt: "2026-09-21T23:26:49.133948700Z",
    origem: "CALCULO",
    componentes: [],
    fatoresImpacto: ["Pontualidade integral na liquidação dos compromissos financeiros."],
    ...overrides,
  };
}

describe("decidirScore", () => {
  test("reprova score abaixo do mínimo", () => {
    const resultado = decidirScore(criarScore({ scoreFinal: SCORE_MINIMO_ANALISE - 1 }));
    expect(resultado.decisao).toBe("REPROVADO");
  });

  test("encaminha faixa intermediária para análise manual", () => {
    const resultado = decidirScore(criarScore({ scoreFinal: SCORE_MINIMO_ANALISE }));
    expect(resultado.decisao).toBe("ANALISE_MANUAL");
  });

  test("aprova a partir do mínimo definido", () => {
    const resultado = decidirScore(criarScore({ scoreFinal: SCORE_MINIMO_APROVACAO }));
    expect(resultado.decisao).toBe("APROVADO");
  });

  test("rejeita payload sem score", () => {
    expect(() => decidirScore(criarScore({ scoreFinal: undefined }))).toThrow(/scoreFinal/);
  });
});

describe("processarResultadoScore", () => {
  test("mantém os dados recebidos do Score na resposta", () => {
    const score = criarScore({ scoreFinal: 802, faixaRisco: "EXCELENTE" });
    const resultado = processarResultadoScore(score);

    expect(resultado.clienteId).toBe(score.clienteId);
    expect(resultado.scoreFinal).toBe(802);
    expect(resultado.faixaRisco).toBe("EXCELENTE");
    expect(resultado.decisao).toBe("APROVADO");
    expect(resultado.fatoresImpacto).toEqual(score.fatoresImpacto);
    expect(resultado.componentes).toEqual(score.componentes);
  });
});
