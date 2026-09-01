package br.com.scoreengine.domain.calculator;

import br.com.scoreengine.domain.enums.TipoPessoa;
import br.com.scoreengine.domain.exception.PerfilInvalidoException;
import br.com.scoreengine.domain.model.ClientePerfil;
import br.com.scoreengine.domain.model.PessoaJuridicaPerfil;
import br.com.scoreengine.domain.model.ScoreComponentResult;
import br.com.scoreengine.domain.model.ScoreResultado;
import br.com.scoreengine.domain.rules.ModeloScore;
import br.com.scoreengine.domain.rules.PessoaJuridicaRuleConfig;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PessoaJuridicaScoreCalculator implements ScoreCalculator {

    private final ModeloScore modeloScore;
    private final PessoaJuridicaRuleConfig config;

    // Construtor atualizado para receber a configuração de pesos
    public PessoaJuridicaScoreCalculator(ModeloScore modeloScore, PessoaJuridicaRuleConfig config) {
        this.modeloScore = modeloScore;
        this.config = config;
    }

    @Override
    public boolean isEligible(TipoPessoa tipoPessoa) {
        return TipoPessoa.PJ == tipoPessoa;
    }

    @Override
    public ScoreResultado calcular(ClientePerfil perfil) {
        if (!(perfil instanceof PessoaJuridicaPerfil pj)) {
            throw new PerfilInvalidoException("Perfil fornecido não é de Pessoa Jurídica.");
        }

        List<ScoreComponentResult> componentes = new ArrayList<>();
        int scoreTotal = 0;

        // 1. Tempo de Atividade
        int ptsAtividade = Math.min(config.pesoTempoAtividade(), pj.tempoAtividadeMeses() * 5);
        componentes.add(new ScoreComponentResult("Tempo de Atividade", ptsAtividade, config.pesoTempoAtividade(),
                "Reflete a estabilidade e maturidade da empresa no mercado."));
        scoreTotal += ptsAtividade;

        // 2. Liquidez / Saúde Financeira
        int ptsLiquidez = pj.liquidez().doubleValue() >= 1.5 ? config.pesoLiquidez()
                : (int) (pj.liquidez().doubleValue() * (config.pesoLiquidez() / 2));
        ptsLiquidez = Math.min(config.pesoLiquidez(), ptsLiquidez);
        componentes.add(new ScoreComponentResult("Liquidez e Saúde Financeira", ptsLiquidez, config.pesoLiquidez(),
                "Avaliação da capacidade de cobrir obrigações de curto prazo."));
        scoreTotal += ptsLiquidez;

        // 3. Nível de Endividamento
        double endividamentoPerc = pj.endividamento().doubleValue();
        int ptsEndividamento = endividamentoPerc > 0.6 ? 0
                : (int) (config.pesoEndividamento() * (1.0 - (endividamentoPerc * 1.66)));
        ptsEndividamento = Math.max(0, ptsEndividamento);
        componentes.add(new ScoreComponentResult("Nível de Endividamento", ptsEndividamento, config.pesoEndividamento(),
                "Análise do passivo da empresa em relação ao patrimônio/lucro."));
        scoreTotal += ptsEndividamento;

        // 4. Relacionamento Bancário
        int ptsRelacionamento = Math.min(config.pesoRelacionamento(), pj.tempoRelacionamentoMeses() * 3);
        componentes
                .add(new ScoreComponentResult("Relacionamento Bancário", ptsRelacionamento, config.pesoRelacionamento(),
                        "Pontuação proporcional ao tempo de parceria."));
        scoreTotal += ptsRelacionamento;

        int scoreFinalNormalizado = modeloScore.normalizar(scoreTotal);

        return new ScoreResultado(
                pj.clienteId(),
                pj.tipoPessoa(),
                scoreFinalNormalizado,
                modeloScore.classificar(scoreFinalNormalizado),
                componentes,
                modeloScore.getVersao(),
                LocalDateTime.now());
    }
}