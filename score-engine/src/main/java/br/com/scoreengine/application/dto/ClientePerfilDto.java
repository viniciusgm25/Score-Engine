package br.com.scoreengine.application.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * Interface DTO raiz. Utiliza o campo 'tipoPessoa' como discriminador
 * polimórfico
 * para determinar qual subclasse o Jackson deve instanciar no momento do parse.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tipoPessoa")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PessoaFisicaDto.class, name = "PF"),
        @JsonSubTypes.Type(value = PessoaJuridicaDto.class, name = "PJ")
})
public interface ClientePerfilDto {
    String clienteId();
}