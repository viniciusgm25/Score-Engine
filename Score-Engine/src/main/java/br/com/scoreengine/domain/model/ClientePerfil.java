package br.com.scoreengine.domain.model;

import br.com.scoreengine.domain.enums.TipoPessoa;

/**
 * Interface base para padronizar o comportamento dos perfis de entrada.
 */
public interface ClientePerfil {
    String clienteId();

    TipoPessoa tipoPessoa();
}