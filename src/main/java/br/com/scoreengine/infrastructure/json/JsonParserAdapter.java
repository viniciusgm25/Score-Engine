package br.com.scoreengine.infrastructure.json;

import br.com.scoreengine.application.dto.ClientePerfilDto;
import br.com.scoreengine.domain.exception.ScoreException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Adaptador de Infraestrutura responsável por processar JSONs.
 */
public class JsonParserAdapter {

    private final ObjectMapper objectMapper;

    public JsonParserAdapter() {
        this.objectMapper = new ObjectMapper();
        // Segurança: Evitar falhas se o payload enviar campos não mapeados no DTO
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public ClientePerfilDto parse(String jsonPayload) {
        try {
            return objectMapper.readValue(jsonPayload, ClientePerfilDto.class);
        } catch (JsonProcessingException e) {
            // Empacotar a exceção técnica em uma exceção de negócio controlada para evitar
            // vazamento de stack trace
            throw new ScoreException(
                    "Falha na desserialização do payload JSON: Formato inválido ou atributos incompatíveis.");
        }
    }
}