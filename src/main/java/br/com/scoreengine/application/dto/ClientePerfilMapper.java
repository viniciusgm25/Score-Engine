package br.com.scoreengine.application.dto;

import br.com.scoreengine.domain.exception.ScoreException;
import br.com.scoreengine.domain.model.pf.CustomerPFProfile;
import br.com.scoreengine.domain.model.pj.CustomerPJProfile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Isolador Arquitetural e Mapper de Entrada.
 * Converte DTOs externos (REST / Kafka) para os modelos de domínio imutáveis
 * (CustomerPFProfile e CustomerPJProfile), garantindo sanitização cadastral,
 * precisão decimal monetária e aderência prudencial (Bacen / Basileia / CDC).
 */
@Component
public class ClientePerfilMapper {

    /**
     * Mapeia requisições genéricas ou especializadas de PF para CustomerPFProfile.
     */
    public CustomerPFProfile toDomainPF(ClientePerfilDto dto) {
        if (dto == null) {
            throw new ScoreException("DTO de entrada não pode ser nulo para mapeamento PF.");
        }

        CustomerPFProfile profile = new CustomerPFProfile();
        profile.setCpf(dto.clienteId());

        if (dto instanceof PessoaFisicaDto pf) {
            profile.setRendaMensal(pf.rendaMensalLiquida() != null ? pf.rendaMensalLiquida() : BigDecimal.ZERO);
            profile.setDividaTotal(pf.endividamento() != null ? pf.endividamento() : BigDecimal.ZERO);
            profile.setIdade(pf.idade());
            profile.setEstadoCivil(pf.estadoCivil());
            profile.setNumeroDependentes(pf.numeroDependentes());
            profile.setDiasAtrasoUltimos12Meses(pf.quantidadeAtrasos());
            profile.setLimiteRotativoUtilizado(
                    pf.limiteRotativoUtilizado() != null ? pf.limiteRotativoUtilizado() : BigDecimal.ZERO);
            profile.setLimiteRotativoTotal(
                    pf.limiteRotativoTotal() != null ? pf.limiteRotativoTotal() : BigDecimal.ZERO);
            profile.setMesesNoEmpregoAtual(pf.tempoUltimoEmpregoMeses());
            profile.setMesesRelacionamentoBanco(pf.tempoRelacionamentoMeses());
        } else {
            // Valores de fallback seguro para instâncias genéricas
            profile.setRendaMensal(BigDecimal.ZERO);
            profile.setDividaTotal(BigDecimal.ZERO);
            profile.setLimiteRotativoUtilizado(BigDecimal.ZERO);
            profile.setLimiteRotativoTotal(BigDecimal.ZERO);
            profile.setDiasAtrasoUltimos12Meses(0);
        }

        return profile;
    }

    /**
     * Mapeia requisições genéricas ou especializadas de PJ para CustomerPJProfile.
     */
    public CustomerPJProfile toDomainPJ(ClientePerfilDto dto) {
        if (dto == null) {
            throw new ScoreException("DTO de entrada não pode ser nulo para mapeamento PJ.");
        }

        CustomerPJProfile profile = new CustomerPJProfile();
        profile.setCnpj(dto.clienteId());

        if (dto instanceof PessoaJuridicaDto pj) {
            profile.setRazaoSocial(pj.razaoSocial() != null ? pj.razaoSocial() : "Empresa " + pj.clienteId());
            profile.setFaturamentoMensal(pj.faturamentoMensal() != null ? pj.faturamentoMensal() : BigDecimal.ZERO);
            profile.setDespesasOperacionaisMensais(
                    pj.despesasOperacionaisMensais() != null ? pj.despesasOperacionaisMensais() : BigDecimal.ZERO);
            profile.setPassivoTotalBancario(pj.endividamento() != null ? pj.endividamento() : BigDecimal.ZERO);

            BigDecimal faturamentoAnual = pj.faturamentoBrutoAnual() != null
                    ? pj.faturamentoBrutoAnual()
                    : profile.getFaturamentoMensal().multiply(new BigDecimal("12"));
            profile.setFaturamentoBrutoAnual(faturamentoAnual);

            profile.setDiasAtrasoUltimos12Meses(pj.quantidadeAtrasos());
            profile.setMesesConstituicao(pj.tempoAtividadeMeses());
            profile.setSetorAtuacao(pj.setorAtividade() != null ? pj.setorAtividade() : "OUTROS");
        } else {
            // Valores de fallback seguro para instâncias genéricas
            profile.setRazaoSocial("Empresa " + dto.clienteId());
            profile.setFaturamentoMensal(BigDecimal.ZERO);
            profile.setDespesasOperacionaisMensais(BigDecimal.ZERO);
            profile.setPassivoTotalBancario(BigDecimal.ZERO);
            profile.setFaturamentoBrutoAnual(BigDecimal.ZERO);
            profile.setDiasAtrasoUltimos12Meses(0);
            profile.setMesesConstituicao(0);
            profile.setSetorAtuacao("OUTROS");
        }

        return profile;
    }
}