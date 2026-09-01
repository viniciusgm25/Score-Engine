package br.com.scoreengine.domain.model;

/**
 * Representa a identidade imutável e versionamento dos modelos de escore de
 * crédito.
 * Garante rastreabilidade e reprodutibilidade histórica das decisões do motor.
 */
public enum ModelVersion {
    MODELO_V1("v1");

    private final String codigo;

    ModelVersion(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public static ModelVersion fromString(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return MODELO_V1; // Versão padrão/ativa atual
        }
        for (ModelVersion version : values()) {
            if (version.codigo.equalsIgnoreCase(codigo)) {
                return version;
            }
        }
        throw new IllegalArgumentException("Versão de modelo inválida ou não suportada: " + codigo);
    }
}