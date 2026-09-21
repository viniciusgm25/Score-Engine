package br.com.scoreengine.infrastructure.messaging;

import br.com.scoreengine.application.dto.ClientePerfilDto;
import br.com.scoreengine.application.dto.ClientePerfilMapper;
import br.com.scoreengine.application.service.ScoreService;
import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.domain.model.pf.CustomerPFProfile;
import br.com.scoreengine.domain.model.pj.CustomerPJProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador de Entrada (Inbound Adapter) Kafka para consumo assíncrono de
 * requisições de Score.
 * Gerencia contexto de rastreabilidade distribuída (MDC), identifica
 * dinamicamente o proponente (PF/PJ),
 * delega o processamento ao ScoreService e garante conformidade de auditoria
 * (Bacen / Basileia).
 */
@Component
public class ScoreKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(ScoreKafkaConsumer.class);
    private static final String MDC_CORRELATION_ID = "correlationId";
    private static final String MDC_CLIENTE_ID = "clienteId";

    private final ScoreService scoreService;
    private final ClientePerfilMapper mapper;

    public ScoreKafkaConsumer(ScoreService scoreService, ClientePerfilMapper mapper) {
        this.scoreService = Objects.requireNonNull(scoreService, "O ScoreService não pode ser nulo.");
        this.mapper = Objects.requireNonNull(mapper, "O ClientePerfilMapper não pode ser nulo.");
    }

    @KafkaListener(topics = "score-evaluation-requests", groupId = "score-engine-group")
    public void consumirRequisicaoScore(
            ClientePerfilDto dto,
            @Header(value = "X-Correlation-Id", required = false) String correlationId,
            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        String traceId = (correlationId != null && !correlationId.isBlank())
                ? correlationId
                : UUID.randomUUID().toString();

        MDC.put(MDC_CORRELATION_ID, traceId);
        MDC.put(MDC_CLIENTE_ID, dto != null ? dto.clienteId() : "DESCONHECIDO");

        try {
            log.info(
                    "Mensagem Kafka consumida [Topico: score-evaluation-requests, Particao: {}, Offset: {}, Key: {}]. Iniciando avaliacao...",
                    partition, offset, messageKey);

            if (dto == null) {
                throw new IllegalArgumentException("Payload da requisição de score (ClientePerfilDto) recebido nulo.");
            }

            ScoreResult resultado;

            // Roteamento determinístico de acordo com o tipo de proponente (PF ou PJ)
            if (isPessoaFisica(dto)) {
                CustomerPFProfile pfProfile = mapper.toDomainPF(dto);
                resultado = scoreService.processarScorePF(pfProfile, traceId);
            } else {
                CustomerPJProfile pjProfile = mapper.toDomainPJ(dto);
                resultado = scoreService.processarScorePJ(pjProfile, traceId);
            }

            log.info(
                    "Processamento assíncrono concluído com sucesso. Cliente: [{}], Score: [{}], Faixa: [{}], PD: [{}]",
                    dto.clienteId(), resultado.scoreFinal(), resultado.faixaRisco(), resultado.probabilidadeDefault());

        } catch (Exception e) {
            log.error(
                    "Falha crítica no processamento da mensagem de score via Kafka. Cliente ID: [{}], Chave: [{}]. Causa: {}",
                    dto != null ? dto.clienteId() : "N/A", messageKey, e.getMessage(), e);
            // Relança a exceção para acionar a política de Retry / Dead Letter Queue
            // configurada no contêiner do Spring Kafka
            throw e;
        } finally {
            MDC.remove(MDC_CORRELATION_ID);
            MDC.remove(MDC_CLIENTE_ID);
        }
    }

    private boolean isPessoaFisica(ClientePerfilDto dto) {
        if (dto.tipoPessoa() != null) {
            return "PF".equalsIgnoreCase(dto.tipoPessoa().name()) || "FISICA".equalsIgnoreCase(dto.tipoPessoa().name());
        }
        String documentoLimpo = dto.clienteId().replaceAll("\\D", "");
        return documentoLimpo.length() <= 11;
    }
}