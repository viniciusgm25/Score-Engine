package br.com.scoreengine.domain.calculator;

import br.com.scoreengine.domain.enums.TipoPessoa;
import br.com.scoreengine.domain.exception.PerfilInvalidoException;
import br.com.scoreengine.domain.model.ClientePerfil;
import br.com.scoreengine.domain.model.PessoaFisicaPerfil;
import br.com.scoreengine.domain.model.ScoreComponentResult;
import br.com.scoreengine.domain.model.ScoreResultado;
import br.com.scoreengine.domain.rules.ModeloScore;
import br.com.scoreengine.domain.rules.PessoaFisicaRuleConfig;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PessoaFisicaScoreCalculator implements ScoreCalculator {

    private final ModeloScore modeloScore;
    private final PessoaFisicaRuleConfig config;

    public PessoaFisicaScoreCalculator(ModeloScore modeloScore, PessoaFisicaRuleConfig config) {
        this.modeloScore = modeloScore;
        this.config = config;
    }

    @Override
    public boolean isEligible(TipoPessoa tipoPessoa) {
        return TipoPessoa.PF == tipoPessoa;
    }

    @Override
    public ScoreResultado calcular(ClientePerfil perfil) {
        if (!(perfil instanceof PessoaFisicaPerfil pf)) {
            throw new PerfilInvalidoException("Perfil fornecido não é de Pessoa Física.");
        }

        List<ScoreComponentResult> componentes = new ArrayList<>();
        int scoreTotal = 0;

        // 1. Pagamentos (Ex: 290 pontos)
        int ptsPagamentos = pf.quantidadeAtrasos() == 0 ? config.pesoPagamentos()
                : Math.max(0, config.pesoPagamentos() - (pf.quantidadeAtrasos() * 50));
        componentes.add(new ScoreComponentResult("Hábitos de Pagamento", ptsPagamentos, config.pesoPagamentos(),
                "Análise do histórico de pagamentos e pontualidade."));
        scoreTotal += ptsPagamentos;

        // 2. Dívidas / Endividamento (Ex: 210 pontos)
        double endividamentoPerc = pf.endividamento().doubleValue();
        int ptsDividas = endividamentoPerc > 0.5 ? 0 : (int) (config.pesoDividas() * (1.0 - (endividamentoPerc * 2)));
        componentes.add(new ScoreComponentResult("Dívidas", ptsDividas, config.pesoDividas(),
                "Avaliação do comprometimento de renda."));
        scoreTotal += ptsDividas;

        // 3. Experiência e Relacionamento (Ex: 240 pontos)
        int ptsExperiencia = Math.min(config.pesoExperiencia(), pf.tempoRelacionamentoMeses() * 5);
        componentes.add(new ScoreComponentResult("Experiência no Mercado", ptsExperiencia, config.pesoExperiencia(),
                "Tempo de relacionamento com o mercado de crédito."));
        scoreTotal += ptsExperiencia;

        // Os demais pilares (Busca por Crédito, Informações Cadastrais e Contratos)
        // seriam calculados de forma análoga, injetando os limites da `config`.
        // Para simplificação técnica do exemplo, concederemos a pontuação cheia nestes
        // pilares ausentes no modelo de entrada.
        scoreTotal += config.pesoBuscaCredito() + config.pesoInformacoesCadastrais() + config.pesoContratos();
        componentes.add(new ScoreComponentResult("Outros Componentes",
                config.pesoBuscaCredito() + config.pesoInformacoesCadastrais() + config.pesoContratos(),
                config.pesoBuscaCredito() + config.pesoInformacoesCadastrais() + config.pesoContratos(),
                "Busca por crédito, cadastro e contratos."));

        int scoreFinalNormalizado = modeloScore.normalizar(scoreTotal);

        return new ScoreResultado(
                pf.clienteId(),
                pf.tipoPessoa(),
                scoreFinalNormalizado,
                modeloScore.classificar(scoreFinalNormalizado),
                componentes,
                modeloScore.getVersao(),
                LocalDateTime.now());
    }
}