package br.com.scoreengine.infrastructure.messaging;

import br.com.scoreengine.application.dto.ClientePerfilDto;
import br.com.scoreengine.application.dto.ClientePerfilMapper;
import br.com.scoreengine.application.dto.PessoaFisicaDto;
import br.com.scoreengine.application.dto.PessoaJuridicaDto;
import br.com.scoreengine.application.service.ScoreService;
import br.com.scoreengine.domain.model.common.CustomerType;
import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.domain.model.pf.CustomerPFProfile;
import br.com.scoreengine.domain.model.pj.CustomerPJProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    @DisplayName("Deve consumir mensagem Kafka para proponente PF e direcionar para processarScorePF")
    void deveConsumirMensagemKafkaEProcessarScorePF() {
        // Arrange
        PessoaFisicaDto dto = new PessoaFisicaDto(
                "12345678901",
                new BigDecimal("5000.00"),
                0,
                new BigDecimal("500.00"),
                24,
                30,
                "CASADO",
                1,
                new BigDecimal("200.00"),
                new BigDecimal("1000.00"),
                36,
                false);

        CustomerPFProfile profilePF = new CustomerPFProfile();
        profilePF.setCpf("12345678901");

        ScoreResult resultadoPF = new ScoreResult(
                750,
                "BAIXO_RISCO",
                new BigDecimal("0.0150"),
                "V1.0",
                Instant.now(),
                Collections.emptyList(),
                Collections.emptyList());

        when(mapper.toDomainPF(dto)).thenReturn(profilePF);
        when(scoreService.processarScorePF(eq(profilePF), anyString())).thenReturn(resultadoPF);

        String correlationId = "corr-pf-12345";
        String messageKey = "key-pf-01";
        int partition = 0;
        long offset = 100L;

        // Act
        kafkaConsumer.consumirRequisicaoScore(dto, correlationId, messageKey, partition, offset);

        // Assert
        verify(mapper, times(1)).toDomainPF(dto);
        verify(scoreService, times(1)).processarScorePF(eq(profilePF), eq(correlationId));
        verify(scoreService, never()).processarScorePJ(any(), any());
    }

    @Test
    @DisplayName("Deve consumir mensagem Kafka para proponente PJ e direcionar para processarScorePJ")
    void deveConsumirMensagemKafkaEProcessarScorePJ() {
        // Arrange
        PessoaJuridicaDto dto = new PessoaJuridicaDto(
                "12345678000199",
                "Empresa Exemplo LTDA",
                new BigDecimal("50000.00"),
                new BigDecimal("30000.00"),
                new BigDecimal("15000.00"),
                new BigDecimal("600000.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("1.5"),
                0,
                48,
                2,
                36,
                "COMERCIO");

        CustomerPJProfile profilePJ = new CustomerPJProfile();
        profilePJ.setCnpj("12345678000199");

        ScoreResult resultadoPJ = new ScoreResult(
                820,
                "BAIXO_RISCO",
                new BigDecimal("0.0080"),
                "V1.0",
                Instant.now(),
                Collections.emptyList(),
                Collections.emptyList());

        when(mapper.toDomainPJ(dto)).thenReturn(profilePJ);
        when(scoreService.processarScorePJ(eq(profilePJ), anyString())).thenReturn(resultadoPJ);

        String correlationId = "corr-pj-67890";
        String messageKey = "key-pj-02";
        int partition = 1;
        long offset = 205L;

        // Act
        kafkaConsumer.consumirRequisicaoScore(dto, correlationId, messageKey, partition, offset);

        // Assert
        verify(mapper, times(1)).toDomainPJ(dto);
        verify(scoreService, times(1)).processarScorePJ(eq(profilePJ), eq(correlationId));
        verify(scoreService, never()).processarScorePF(any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção e propagar erro para DLQ quando o payload recebido for nulo")
    void deveLancarExcecaoQuandoPayloadForNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> kafkaConsumer.consumirRequisicaoScore(null, "trace-null", "key-null", 0, 1L));
    }
}