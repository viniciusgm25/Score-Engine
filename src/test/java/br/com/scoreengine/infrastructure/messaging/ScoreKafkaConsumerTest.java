package br.com.scoreengine.infrastructure.messaging;

import br.com.scoreengine.application.dto.ClientePerfilDto;
import br.com.scoreengine.application.dto.ClientePerfilMapper;
import br.com.scoreengine.application.service.ScoreService;
import br.com.scoreengine.domain.model.ClientePerfil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ScoreKafkaConsumerTest {

    private ScoreService scoreService;
    private ClientePerfilMapper mapper;
    private ScoreKafkaConsumer kafkaConsumer;

    @BeforeEach
    void setUp() {
        scoreService = mock(ScoreService.class);
        mapper = mock(ClientePerfilMapper.class);
        kafkaConsumer = new ScoreKafkaConsumer(scoreService, mapper);
    }

    @Test
    void deveConsumirMensagemKafkaEProcessarScore() {
        // Arrange - Mock the DTO interface since it cannot be instantiated directly
        ClientePerfilDto dto = mock(ClientePerfilDto.class);

        ClientePerfil perfilDom = mock(ClientePerfil.class);
        when(mapper.toDomain(dto)).thenReturn(perfilDom);

        String correlationId = "corr-12345";
        String messageKey = "key-01";

        // Act
        kafkaConsumer.consumirRequisicaoScore(dto, correlationId, messageKey);

        // Assert
        verify(mapper, times(1)).toDomain(dto);
        verify(scoreService, times(1)).processarScore(eq(perfilDom), eq(correlationId));
    }
}