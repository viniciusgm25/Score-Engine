package br.com.scoreengine.domain.calculator.pf;

import br.com.scoreengine.domain.calculator.ScoreCalculatorStrategy;
import br.com.scoreengine.domain.model.common.ScoreComponent;
import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.domain.model.pf.CustomerPFProfile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Estratégia de cálculo de Score de Crédito para Pessoa Física (PF).
 * Atende às diretrizes de governança e explicabilidade regulatória (Bacen /
 * CDC).
 */
@Component("PFScoreCalculator")
public class PFScoreCalculatorStrategy implements ScoreCalculatorStrategy<CustomerPFProfile> {

    private static final String MODEL_VERSION = "v1.1.0";

    @Override
    public String getVersion() {
        return MODEL_VERSION;
    }

    @Override
    public ScoreResult calculate(CustomerPFProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("O perfil do cliente PF não pode ser nulo.");
        }

        List<ScoreComponent> componentes = new ArrayList<>();

        // Componente 1: Histórico de Pagamentos (Peso: 35%)
        componentes.add(avaliarHistorico(profile.getDiasAtrasoUltimos12Meses()));

        // Componente 2: Capacidade Financeira e Margem Disponível (Peso: 30%)
        componentes.add(avaliarCapacidade(
                profile.getRendaMensal(),
                profile.getDividaTotal(),
                profile.getNumeroDependentes()));

        // Componente 3: Nível de Endividamento e Uso de Limites (Peso: 20%)
        componentes.add(avaliarEndividamento(
                profile.getLimiteRotativoUtilizado(),
                profile.getLimiteRotativoTotal()));

        // Componente 4: Estabilidade Cadastral e Relacionamento (Peso: 15%)
        componentes.add(avaliarEstabilidade(
                profile.getMesesNoEmpregoAtual(),
                profile.getMesesRelacionamentoBanco()));

        // Consolidação ponderada das parcelas
        BigDecimal scoreAgregado = BigDecimal.ZERO;
        for (ScoreComponent comp : componentes) {
            BigDecimal parcela = comp.pontuacao().multiply(comp.pesoPonderado());
            scoreAgregado = scoreAgregado.add(parcela);
        }

        int scoreFinal = Math.min(1000, Math.max(0, scoreAgregado.setScale(0, RoundingMode.HALF_EVEN).intValue()));

        return new ScoreResult(scoreFinal, MODEL_VERSION, componentes);
    }

    private ScoreComponent avaliarHistorico(int diasAtraso) {
        BigDecimal pontos;
        String motivo;
        BigDecimal pontuacaoMaxima = new BigDecimal("1000.00");
        BigDecimal peso = new BigDecimal("0.35");

        if (diasAtraso > 60) {
            pontos = new BigDecimal("150.00");
            motivo = "Inadimplência severa registrada no histórico recente (>60 dias).";
        } else if (diasAtraso > 15) {
            pontos = new BigDecimal("400.00");
            motivo = "Atrasos operacionais recorrentes identificados nos últimos 12 meses.";
        } else if (diasAtraso > 0) {
            pontos = new BigDecimal("650.00");
            motivo = "Ocorrência de apontamentos de pequeno porte sem reincidência severa.";
        } else {
            pontos = new BigDecimal("850.00");
            motivo = "Pontualidade integral na liquidação dos compromissos financeiros.";
        }

        return new ScoreComponent("Histórico de Pagamento", pontos, pontuacaoMaxima, peso, motivo);
    }

    private ScoreComponent avaliarCapacidade(BigDecimal renda, BigDecimal divida, int dependentes) {
        BigDecimal pontuacaoMaxima = new BigDecimal("1000.00");
        BigDecimal peso = new BigDecimal("0.30");

        BigDecimal rendaInformada = renda != null ? renda : BigDecimal.ZERO;
        BigDecimal deducoesDependentes = BigDecimal.valueOf(Math.max(0, dependentes))
                .multiply(new BigDecimal("600.00"));
        BigDecimal rendaLiquidaAjustada = rendaInformada.subtract(deducoesDependentes);

        BigDecimal pontos;
        String motivo;

        if (rendaLiquidaAjustada.compareTo(BigDecimal.ZERO) <= 0) {
            pontos = new BigDecimal("200.00");
            motivo = "Renda líquida familiar exaurida pela estrutura de subsistência de dependentes.";
        } else {
            BigDecimal totalDivida = divida != null ? divida : BigDecimal.ZERO;
            BigDecimal comprometimento = rendaInformada.compareTo(BigDecimal.ZERO) > 0
                    ? totalDivida.divide(rendaInformada, 4, RoundingMode.HALF_EVEN)
                    : BigDecimal.ONE;

            if (comprometimento.compareTo(new BigDecimal("0.20")) <= 0) {
                pontos = new BigDecimal("900.00");
                motivo = "Excelente capacidade de pagamento com margem de renda livre superior a 80%.";
            } else if (comprometimento.compareTo(new BigDecimal("0.50")) <= 0) {
                pontos = new BigDecimal("650.00");
                motivo = "Comprometimento financeiro compatível com a estabilidade de renda declarada.";
            } else {
                pontos = new BigDecimal("300.00");
                motivo = "Comprometimento crítico da renda com passivos pré-existentes (>50%).";
            }
        }

        return new ScoreComponent("Capacidade Financeira", pontos, pontuacaoMaxima, peso, motivo);
    }

    private ScoreComponent avaliarEndividamento(BigDecimal utilizado, BigDecimal total) {
        BigDecimal pontuacaoMaxima = new BigDecimal("1000.00");
        BigDecimal peso = new BigDecimal("0.20");

        BigDecimal pontos;
        String motivo;

        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            pontos = new BigDecimal("500.00");
            motivo = "Inexistência de linhas ativas de crédito rotativo concedidas.";
        } else {
            BigDecimal uso = utilizado != null ? utilizado : BigDecimal.ZERO;
            BigDecimal taxaUso = uso.divide(total, 4, RoundingMode.HALF_EVEN);

            if (taxaUso.compareTo(new BigDecimal("0.30")) <= 0) {
                pontos = new BigDecimal("850.00");
                motivo = "Gestão prudencial de linhas emergenciais, com uso inferior a 30% do teto.";
            } else if (taxaUso.compareTo(new BigDecimal("0.70")) <= 0) {
                pontos = new BigDecimal("600.00");
                motivo = "Utilização regular das linhas concedidas de cartão e cheque especial.";
            } else {
                pontos = new BigDecimal("250.00");
                motivo = "Exposição elevada em instrumentos rotativos de alta taxa de juros.";
            }
        }

        return new ScoreComponent("Endividamento e Limites", pontos, pontuacaoMaxima, peso, motivo);
    }

    private ScoreComponent avaliarEstabilidade(int mesesEmprego, int mesesConta) {
        BigDecimal pontuacaoMaxima = new BigDecimal("1000.00");
        BigDecimal peso = new BigDecimal("0.15");

        BigDecimal pontos = new BigDecimal("400.00");
        if (mesesEmprego >= 24 && mesesConta >= 24) {
            pontos = new BigDecimal("900.00");
        } else if (mesesEmprego >= 12 || mesesConta >= 12) {
            pontos = new BigDecimal("700.00");
        }

        String motivo = String.format("Vínculo empregatício (%d meses) e relacionamento bancário (%d meses).",
                mesesEmprego, mesesConta);

        return new ScoreComponent("Estabilidade e Relacionamento", pontos, pontuacaoMaxima, peso, motivo);
    }
}