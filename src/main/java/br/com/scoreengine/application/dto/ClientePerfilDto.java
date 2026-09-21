package br.com.scoreengine.application.dto;

import br.com.scoreengine.domain.model.common.CustomerType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Interface DTO raiz para requisições de avaliação de Score de Crédito.
 * Utiliza o discriminador polimórfico 'tipoPessoa' (PF / PJ) para permitir
 * a correta desserialização Jackson e viabilizar a segregação de regras
 * prudenciais
 * exigidas pelo Banco Central do Brasil e Acordos de Basileia.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tipoPessoa", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PessoaFisicaDto.class, name = "PF"),
        @JsonSubTypes.Type(value = PessoaJuridicaDto.class, name = "PJ")
})
public interface ClientePerfilDto {

    /**
     * Identificador fiscal unívoco do cliente (CPF para PF ou CNPJ para PJ).
     */
    String clienteId();

    /**
     * Tipo do proponente avaliado (PF ou PJ), garantindo aderência aos modelos
     * de mensuração de risco segregados de varejo e atacado.
     */
    default CustomerType tipoPessoa() {
        if (this instanceof PessoaFisicaDto) {
            return CustomerType.PF;
        } else if (this instanceof PessoaJuridicaDto) {
            return CustomerType.PJ;
        }
        return (clienteId() != null && clienteId().replaceAll("\\D", "").length() > 11)
                ? CustomerType.PJ
                : CustomerType.PF;
    }
}