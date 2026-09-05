package br.com.scoreengine.domain.calculator.pf;

import br.com.scoreengine.domain.calculator.ScoreCalculatorStrategy;
import br.com.scoreengine.domain.model.common.ImpactType;
import br.com.scoreengine.domain.model.common.RiskRating;
import br.com.scoreengine.domain.model.common.ScoreComponent;
import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.domain.model.pf.CustomerPFProfile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component("PFScoreCalculator")
public class PFScoreCalculatorStrategy implements ScoreCalculatorStrategy<CustomerPFProfile> {

    private static final String MODEL_VERSION = "v1.1.0";

    @Override
    public String getVersion() {
        return MODEL_VERSION;
    }

    @Override
    public ScoreResult calculate(CustomerPFProfile profile) {
        List<ScoreComponent> componentes = new ArrayList<>();

        // Componente 1: Histórico de Pagamentos (Peso: 35%)
        componentes.add(avaliarHistorico(profile.getDiasAtrasoUltimos12Meses()));

        // Componente 2: Capacidade Financeira e Margem Disponível (Peso: 30%)
        componentes.add(
                avaliarCapacidade(profile.getRendaMensal(), profile.getDividaTotal(), profile.getNumeroDependentes()));

        // Componente 3: Nível de Endividamento e Uso de Limites (Peso: 20%)
        componentes.add(avaliarEndividamento(profile.getLimiteRotativoUtilizado(), profile.getLimiteRotativoTotal()));

        // Componente 4: Estabilidade Cadastral e Relacionamento (Peso: 15%)
        componentes.add(avaliarEstabilidade(profile.getMesesNoEmpregoAtual(), profile.getMesesRelacionamentoBanco()));

        BigDecimal ponderacaoTotal = BigDecimal.ZERO;
        for (ScoreComponent comp : componentes) {
            BigDecimal parcela = BigDecimal.valueOf(comp.pontuacao()).multiply(comp.peso());
            ponderacaoTotal = ponderacaoTotal.add(parcela);
        }

        int scoreFinal = Math.min(1000, Math.max(0, ponderacaoTotal.setScale(0, RoundingMode.HALF_UP).intValue()));
        RiskRating rating = RiskRating.fromScore(scoreFinal);

        return new ScoreResult(scoreFinal, rating, MODEL_VERSION, componentes, Instant.now());
    }

    private ScoreComponent avaliarHistorico(int diasAtraso) {
        int pontos = 850;
        ImpactType impacto = ImpactType.POSITIVO;
        String motivo;

        if (diasAtraso > 60) {
            pontos = 150;
            impacto = ImpactType.NEGATIVO;
            motivo = "Inadimplência severa registrada no histórico recente (>60 dias).";
        } else if (diasAtraso > 15) {
            pontos = 400;
            impacto = ImpactType.NEGATIVO;
            motivo = "Atrasos operacionais recorrentes identificados nos últimos 12 meses.";
        } else if (diasAtraso > 0) {
            pontos = 650;
            impacto = ImpactType.NEUTRO;
            motivo = "Ocorrência de apontamentos de pequeno porte sem reincidência severa.";
        } else {
            motivo = "Pontualidade integral na liquidação dos compromissos financeiros.";
        }

        return new ScoreComponent("Histórico de Pagamento", pontos, new BigDecimal("0.35"), impacto, motivo);
    }

    private ScoreComponent avaliarCapacidade(BigDecimal renda, BigDecimal divida, int dependentes) {
        BigDecimal deducoesFamiliares = BigDecimal.valueOf(Math.max(0, dependentes)).multiply(new BigDecimal("600.00"));
        BigDecimal rendaLiquidaAjustada = (renda != null ? renda : BigDecimal.ZERO).subtract(deducoesFamiliares);

        int pontos;
        ImpactType impacto;
        String motivo;

        if (rendaLiquidaAjustada.compareTo(BigDecimal.ZERO) <= 0) {
            pontos = 200;
            impacto = ImpactType.NEGATIVO;
            motivo = "Renda líquida familiar exaurida pela estrutura de subsistência de dependentes.";
        } else {
            BigDecimal totalDivida = divida != null ? divida : BigDecimal.ZERO;
            BigDecimal comprometimento = totalDivida.divide(renda, 4, RoundingMode.HALF_UP);

            if (comprometimento.compareTo(new BigDecimal("0.20")) <= 0) {
                pontos = 900;
                impacto = ImpactType.POSITIVO;
                motivo = "Excelente capacidade de pagamento com margem de renda livre superior a 80%.";
            } else if (comprometimento.compareTo(new BigDecimal("0.50")) <= 0) {
                pontos = 650;
                impacto = ImpactType.NEUTRO;
                motivo = "Comprometimento financeiro compatível com a estabilidade de renda declarada.";
            } else {
                pontos = 300;
                impacto = ImpactType.NEGATIVO;
                motivo = "Comprometimento crítico da renda com passivos pré-existentes (>50%).";
            }
        }

        return new ScoreComponent("Capacidade Financeira", pontos, new BigDecimal("0.30"), impacto, motivo);
    }

    private ScoreComponent avaliarEndividamento(BigDecimal utilizado, BigDecimal total) {
        int pontos;
        ImpactType impacto;
        String motivo;

        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            pontos = 500;
            impacto = ImpactType.NEUTRO;
            motivo = "Inexistência de linhas ativas de crédito rotativo concedidas.";
        } else {
            BigDecimal uso = utilizado != null ? utilizado : BigDecimal.ZERO;
            BigDecimal taxaUso = uso.divide(total, 4, RoundingMode.HALF_UP);

            if (taxaUso.compareTo(new BigDecimal("0.30")) <= 0) {
                pontos = 850;
                impacto = ImpactType.POSITIVO;
                motivo = "Gestão prudencial de linhas emergenciais, com uso inferior a 30% do teto.";
            } else if (taxaUso.compareTo(new BigDecimal("0.70")) <= 0) {
                pontos = 600;
                impacto = ImpactType.NEUTRO;
                motivo = "Utilização regular das linhas concedidas de cartão e cheque especial.";
            } else {
                pontos = 250;
                impacto = ImpactType.NEGATIVO;
                motivo = "Exposição elevada em instrumentos rotativos de alta taxa de juros.";
            }
        }

        return new ScoreComponent("Endividamento e Limites", pontos, new BigDecimal("0.20"), impacto, motivo);
    }

    private ScoreComponent avaliarEstabilidade(int mesesEmprego, int mesesConta) {
        int pontos = 400;
        if (mesesEmprego >= 24 && mesesConta >= 24) {
            pontos = 900;
        } else if (mesesEmprego >= 12 || mesesConta >= 12) {
            pontos = 700;
        }

        ImpactType impacto = pontos >= 700 ? ImpactType.POSITIVO : ImpactType.NEUTRO;
        String motivo = String.format("Vínculo empregatício (%d meses) e relacionamento bancário (%d meses).",
                mesesEmprego, mesesConta);

        return new ScoreComponent("Estabilidade e Relacionamento", pontos, new BigDecimal("0.15"), impacto, motivo);
    }
}