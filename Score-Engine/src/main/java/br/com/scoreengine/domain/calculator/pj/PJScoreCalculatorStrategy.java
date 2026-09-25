package br.com.scoreengine.domain.calculator.pj;

import br.com.scoreengine.domain.calculator.ScoreCalculatorStrategy;
import br.com.scoreengine.domain.model.common.ScoreComponent;
import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.domain.model.pj.CustomerPJProfile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Estratégia de cálculo de Score de Crédito para Pessoa Jurídica (PJ).
 * Analisa solvência, alavancagem, maturidade empresarial e histórico
 * operacional
 * em conformidade com as diretrizes regulatórias e prudenciais (Bacen /
 * Basileia).
 */
@Component("PJScoreCalculator")
public class PJScoreCalculatorStrategy implements ScoreCalculatorStrategy<CustomerPJProfile> {

    private static final String MODEL_VERSION = "v1.0.0";

    @Override
    public String getVersion() {
        return MODEL_VERSION;
    }

    @Override
    public ScoreResult calculate(CustomerPJProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("O perfil do cliente PJ não pode ser nulo.");
        }

        List<ScoreComponent> componentes = new ArrayList<>();

        // Componente 1: Histórico Operacional e Pontualidade Mercantil (Peso: 30%)
        componentes.add(avaliarHistoricoOperacional(profile.getDiasAtrasoUltimos12Meses()));

        // Componente 2: Liquidez e Fluxo de Caixa Livre (Peso: 25%)
        componentes.add(avaliarLiquidez(
                profile.getFaturamentoMensal(),
                profile.getDespesasOperacionaisMensais()));

        // Componente 3: Alavancagem e Endividamento Bancário (Peso: 25%)
        componentes.add(avaliarAlavancagem(
                profile.getPassivoTotalBancario(),
                profile.getFaturamentoBrutoAnual()));

        // Componente 4: Estabilidade Empresarial e Risco Setorial (Peso: 20%)
        componentes.add(avaliarEstabilidade(
                profile.getMesesConstituicao(),
                profile.getSetorAtuacao()));

        // Consolidação ponderada das parcelas
        BigDecimal scoreAgregado = BigDecimal.ZERO;
        for (ScoreComponent comp : componentes) {
            BigDecimal parcela = comp.pontuacao().multiply(comp.pesoPonderado());
            scoreAgregado = scoreAgregado.add(parcela);
        }

        int scoreFinal = Math.min(1000, Math.max(0, scoreAgregado.setScale(0, RoundingMode.HALF_EVEN).intValue()));

        return new ScoreResult(scoreFinal, MODEL_VERSION, componentes);
    }

    private ScoreComponent avaliarHistoricoOperacional(int diasAtraso) {
        BigDecimal pontuacaoMaxima = new BigDecimal("1000.00");
        BigDecimal peso = new BigDecimal("0.30");

        BigDecimal pontos;
        String motivo;

        if (diasAtraso > 60) {
            pontos = new BigDecimal("200.00");
            motivo = "Inadimplência mercantil severa registrada nos últimos 12 meses (>60 dias).";
        } else if (diasAtraso > 15) {
            pontos = new BigDecimal("500.00");
            motivo = "Ocorrência de atrasos operacionais recorrentes em linhas de capital de giro.";
        } else if (diasAtraso > 0) {
            pontos = new BigDecimal("700.00");
            motivo = "Apontamentos eventuais de liquidação com atraso inferior a 15 dias.";
        } else {
            pontos = new BigDecimal("900.00");
            motivo = "Ausência de apontamentos restritivos ou protestos comerciais no período avaliado.";
        }

        return new ScoreComponent("Histórico Operacional", pontos, pontuacaoMaxima, peso, motivo);
    }

    private ScoreComponent avaliarLiquidez(BigDecimal faturamento, BigDecimal despesas) {
        BigDecimal pontuacaoMaxima = new BigDecimal("1000.00");
        BigDecimal peso = new BigDecimal("0.25");

        if (faturamento == null || faturamento.compareTo(BigDecimal.ZERO) <= 0) {
            return new ScoreComponent(
                    "Liquidez e Fluxo de Caixa",
                    new BigDecimal("100.00"),
                    pontuacaoMaxima,
                    peso,
                    "Faturamento mensal nulo ou não comprovado, impossibilitando apuração de liquidez.");
        }

        BigDecimal despesasEfetivas = despesas != null ? despesas : BigDecimal.ZERO;
        BigDecimal margemOperacional = faturamento.subtract(despesasEfetivas)
                .divide(faturamento, 4, RoundingMode.HALF_EVEN);

        BigDecimal pontos;
        String motivo;

        if (margemOperacional.compareTo(new BigDecimal("0.30")) >= 0) {
            pontos = new BigDecimal("850.00");
            motivo = "Forte geração de caixa operacional líquido, superior a 30% da receita corrente.";
        } else if (margemOperacional.compareTo(new BigDecimal("0.10")) >= 0) {
            pontos = new BigDecimal("650.00");
            motivo = "Margem operacional líquida equilibrada frente aos custos correntes da empresa.";
        } else {
            pontos = new BigDecimal("300.00");
            motivo = "Margem de caixa estreita, elevando a vulnerabilidade a choques operacionais e de mercado.";
        }

        return new ScoreComponent("Liquidez e Fluxo de Caixa", pontos, pontuacaoMaxima, peso, motivo);
    }

    private ScoreComponent avaliarAlavancagem(BigDecimal passivoTotal, BigDecimal faturamentoAnual) {
        BigDecimal pontuacaoMaxima = new BigDecimal("1000.00");
        BigDecimal peso = new BigDecimal("0.25");

        if (faturamentoAnual == null || faturamentoAnual.compareTo(BigDecimal.ZERO) <= 0) {
            return new ScoreComponent(
                    "Alavancagem Empresarial",
                    new BigDecimal("200.00"),
                    pontuacaoMaxima,
                    peso,
                    "Ausência de faturamento anual auditado reportado para apuração de solvência.");
        }

        BigDecimal passivo = passivoTotal != null ? passivoTotal : BigDecimal.ZERO;
        BigDecimal taxaAlavancagem = passivo.divide(faturamentoAnual, 4, RoundingMode.HALF_EVEN);

        BigDecimal pontos;
        String motivo;

        if (taxaAlavancagem.compareTo(new BigDecimal("0.25")) <= 0) {
            pontos = new BigDecimal("850.00");
            motivo = "Baixo endividamento bancário, representando menos de 25% do faturamento bruto anual.";
        } else if (taxaAlavancagem.compareTo(new BigDecimal("0.50")) <= 0) {
            pontos = new BigDecimal("600.00");
            motivo = "Alavancagem financeira equilibrada e proporcional à capacidade de receita corporativa.";
        } else {
            pontos = new BigDecimal("250.00");
            motivo = "Endividamento elevado em relação à capacidade anual comprovada de geração de receita.";
        }

        return new ScoreComponent("Alavancagem Empresarial", pontos, pontuacaoMaxima, peso, motivo);
    }

    private ScoreComponent avaliarEstabilidade(int mesesConstituicao, String setor) {
        BigDecimal pontuacaoMaxima = new BigDecimal("1000.00");
        BigDecimal peso = new BigDecimal("0.20");

        BigDecimal pontos = new BigDecimal("400.00");
        if (mesesConstituicao >= 36) {
            pontos = new BigDecimal("850.00");
        } else if (mesesConstituicao >= 12) {
            pontos = new BigDecimal("600.00");
        }

        String motivo = String.format("Maturidade empresarial de %d meses de constituição cadastral no setor %s.",
                mesesConstituicao, (setor != null && !setor.isBlank()) ? setor : "GERAL");

        return new ScoreComponent("Estabilidade e Risco Setorial", pontos, pontuacaoMaxima, peso, motivo);
    }
}