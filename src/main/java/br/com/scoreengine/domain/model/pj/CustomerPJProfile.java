package br.com.scoreengine.domain.model.pj;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class CustomerPJProfile {

    private String cnpj;
    private String razaoSocial;
    private BigDecimal faturamentoMensal;
    private BigDecimal despesasOperacionaisMensais;
    private BigDecimal passivoTotalBancario;
    private BigDecimal faturamentoBrutoAnual;
    private int diasAtrasoUltimos12Meses;
    private int mesesConstituicao;
    private String setorAtuacao;

    public CustomerPJProfile() {
    }

    /**
     * Gera um fingerprint determinístico dos dados que impactam o cálculo.
     */
    public String generateFingerprint() {
        String raw = String.join("|",
                cnpj != null ? cnpj : "",
                faturamentoMensal != null ? faturamentoMensal.toPlainString() : "0",
                despesasOperacionaisMensais != null ? despesasOperacionaisMensais.toPlainString() : "0",
                passivoTotalBancario != null ? passivoTotalBancario.toPlainString() : "0",
                faturamentoBrutoAnual != null ? faturamentoBrutoAnual.toPlainString() : "0",
                String.valueOf(diasAtrasoUltimos12Meses),
                String.valueOf(mesesConstituicao),
                setorAtuacao != null ? setorAtuacao : "");

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponível no ambiente runtime", e);
        }
    }

    // Getters e Setters...
    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public BigDecimal getFaturamentoMensal() {
        return faturamentoMensal;
    }

    public void setFaturamentoMensal(BigDecimal faturamentoMensal) {
        this.faturamentoMensal = faturamentoMensal;
    }

    public BigDecimal getDespesasOperacionaisMensais() {
        return despesasOperacionaisMensais;
    }

    public void setDespesasOperacionaisMensais(BigDecimal despesasOperacionaisMensais) {
        this.despesasOperacionaisMensais = despesasOperacionaisMensais;
    }

    public BigDecimal getPassivoTotalBancario() {
        return passivoTotalBancario;
    }

    public void setPassivoTotalBancario(BigDecimal passivoTotalBancario) {
        this.passivoTotalBancario = passivoTotalBancario;
    }

    public BigDecimal getFaturamentoBrutoAnual() {
        return faturamentoBrutoAnual;
    }

    public void setFaturamentoBrutoAnual(BigDecimal faturamentoBrutoAnual) {
        this.faturamentoBrutoAnual = faturamentoBrutoAnual;
    }

    public int getDiasAtrasoUltimos12Meses() {
        return diasAtrasoUltimos12Meses;
    }

    public void setDiasAtrasoUltimos12Meses(int diasAtrasoUltimos12Meses) {
        this.diasAtrasoUltimos12Meses = diasAtrasoUltimos12Meses;
    }

    public int getMesesConstituicao() {
        return mesesConstituicao;
    }

    public void setMesesConstituicao(int mesesConstituicao) {
        this.mesesConstituicao = mesesConstituicao;
    }

    public String getSetorAtuacao() {
        return setorAtuacao;
    }

    public void setSetorAtuacao(String setorAtuacao) {
        this.setorAtuacao = setorAtuacao;
    }
}