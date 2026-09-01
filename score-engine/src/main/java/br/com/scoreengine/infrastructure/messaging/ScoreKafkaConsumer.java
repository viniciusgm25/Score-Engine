package br.com.scoreengine.infrastructure.messaging;

import br.com.scoreengine.application.dto.ClientePerfilDto;
import br.com.scoreengine.application.dto.ClientePerfilMapper;
import br.com.scoreengine.application.service.ScoreService;
import br.com.scoreengine.domain.model.ClientePerfil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adaptador de Entrada (Inbound Adapter) para consumo assíncrono de requisições
 * de cálculo de Score.
 */
@Component
public class ScoreKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(ScoreKafkaConsumer.class);
    private static final String MDC_KEY = "correlationId";

    private final ScoreService scoreService;
    private final ClientePerfilMapper mapper;

    public ScoreKafkaConsumer(ScoreService scoreService, ClientePerfilMapper mapper) {
        this.scoreService = scoreService;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "score-evaluation-requests", groupId = "score-engine-group")
    public void consumirRequisicaoScore(
            ClientePerfilDto dto,
            @Header(value = "X-Correlation-Id", required = false) String correlationId,
            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {

        // Garante a rastreabilidade da mensagem ingerida
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_KEY, correlationId);

        try {
            log.info("Recebida mensagem do Kafka [Key: {}] para recalibração de score do cliente.", messageKey);

            // Converte DTO para a Entidade de Domínio Puro
            ClientePerfil perfil = mapper.toDomain(dto);

            // O serviço de aplicação orquestra o cálculo e a gravação na trilha de
            // auditoria
            scoreService.processarScore(perfil, correlationId);

            log.info("Processamento assíncrono concluído via Kafka com sucesso.");
        } catch (Exception e) {
            // Utiliza o método acessor correto compatível com o DTO
            log.error("Erro ao processar mensagem do Kafka para o cliente ID: {}. Motivo: {}", dto.clienteId(),
                    e.getMessage());
        } finally {
            MDC.remove(MDC_KEY); // Limpa o contexto da thread
        }
    }
}