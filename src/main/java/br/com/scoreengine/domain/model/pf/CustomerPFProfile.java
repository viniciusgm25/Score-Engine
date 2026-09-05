package br.com.scoreengine.domain.model.pf;

import java.math.BigDecimal;

public class CustomerPFProfile {

    private String cpf;
    private BigDecimal rendaMensal;
    private BigDecimal dividaTotal;
    private int idade;
    private String estadoCivil;
    private int numeroDependentes;
    private int diasAtrasoUltimos12Meses;
    private BigDecimal limiteRotativoUtilizado;
    private BigDecimal limiteRotativoTotal;
    private int mesesNoEmpregoAtual;
    private int mesesRelacionamentoBanco;

    public CustomerPFProfile() {
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public BigDecimal getRendaMensal() {
        return rendaMensal;
    }

    public void setRendaMensal(BigDecimal rendaMensal) {
        this.rendaMensal = rendaMensal;
    }

    public BigDecimal getDividaTotal() {
        return dividaTotal;
    }

    public void setDividaTotal(BigDecimal dividaTotal) {
        this.dividaTotal = dividaTotal;
    }

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        this.idade = idade;
    }

    public String getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(String estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public int getNumeroDependentes() {
        return numeroDependentes;
    }

    public void setNumeroDependentes(int numeroDependentes) {
        this.numeroDependentes = numeroDependentes;
    }

    public int getDiasAtrasoUltimos12Meses() {
        return diasAtrasoUltimos12Meses;
    }

    public void setDiasAtrasoUltimos12Meses(int diasAtrasoUltimos12Meses) {
        this.diasAtrasoUltimos12Meses = diasAtrasoUltimos12Meses;
    }

    public BigDecimal getLimiteRotativoUtilizado() {
        return limiteRotativoUtilizado;
    }

    public void setLimiteRotativoUtilizado(BigDecimal limiteRotativoUtilizado) {
        this.limiteRotativoUtilizado = limiteRotativoUtilizado;
    }

    public BigDecimal getLimiteRotativoTotal() {
        return limiteRotativoTotal;
    }

    public void setLimiteRotativoTotal(BigDecimal limiteRotativoTotal) {
        this.limiteRotativoTotal = limiteRotativoTotal;
    }

    public int getMesesNoEmpregoAtual() {
        return mesesNoEmpregoAtual;
    }

    public void setMesesNoEmpregoAtual(int mesesNoEmpregoAtual) {
        this.mesesNoEmpregoAtual = mesesNoEmpregoAtual;
    }

    public int getMesesRelacionamentoBanco() {
        return mesesRelacionamentoBanco;
    }

    public void setMesesRelacionamentoBanco(int mesesRelacionamentoBanco) {
        this.mesesRelacionamentoBanco = mesesRelacionamentoBanco;
    }
}