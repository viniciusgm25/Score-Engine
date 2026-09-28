package br.com.scoreengine.infrastructure.integration;

import br.com.scoreengine.application.port.out.ScoreToDecisionPort;
import br.com.scoreengine.interfaces.rest.dto.response.UnifiedScoreResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adaptador HTTP que envia o resultado do Score ao microsserviço de Decisão.
 *
 * A URL é configurável por ambiente. Em desenvolvimento local, o padrão aponta
 * para o serviço de Decisão executando na porta 8081 do mesmo computador.
 */
@Component
public class DecisionHttpClient implements ScoreToDecisionPort {

    private static final Logger log = LoggerFactory.getLogger(DecisionHttpClient.class);

    private final RestClient restClient;
    private final String decisionUrl;
    private final boolean enabled;

    public DecisionHttpClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.decision.url:http://localhost:8081}") String decisionUrl,
            @Value("${app.decision.enabled:true}") boolean enabled) {
        this.restClient = restClientBuilder.build();
        this.decisionUrl = decisionUrl;
        this.enabled = enabled;
    }

    @Override
    public void enviar(UnifiedScoreResponseDTO resultado) {
        if (!enabled) {
            log.info("Envio Score -> Decisão desabilitado pela configuração.");
            return;
        }

        try {
            restClient.post()
                    .uri(decisionUrl + "/api/v1/decisao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(resultado)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Resultado do Score enviado para a Decisão. Cliente: [{}], Score: [{}]",
                    resultado.clienteId(), resultado.scoreFinal());
        } catch (RestClientException ex) {
            log.error("Falha ao enviar o resultado do Score para a Decisão em [{}]. Cliente: [{}]",
                    decisionUrl, resultado.clienteId(), ex);
            throw ex;
        }
    }
}
