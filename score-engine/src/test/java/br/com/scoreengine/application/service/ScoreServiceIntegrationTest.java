package br.com.scoreengine.application.service;

import br.com.scoreengine.application.dto.ClientePerfilDto;
import br.com.scoreengine.application.dto.ClientePerfilMapper;
import br.com.scoreengine.application.port.out.ScoreAuditPort;
import br.com.scoreengine.domain.calculator.PessoaFisicaScoreCalculator;
import br.com.scoreengine.domain.calculator.PessoaJuridicaScoreCalculator;
import br.com.scoreengine.domain.calculator.ScoreCalculator;
import br.com.scoreengine.domain.enums.TipoPessoa;
import br.com.scoreengine.domain.model.ClientePerfil;
import br.com.scoreengine.domain.model.ScoreResultado;
import br.com.scoreengine.domain.rules.ModeloScoreV1;
import br.com.scoreengine.domain.rules.PessoaFisicaRuleConfig;
import br.com.scoreengine.domain.rules.PessoaJuridicaRuleConfig;
import br.com.scoreengine.domain.validator.PerfilValidator;
import br.com.scoreengine.infrastructure.json.JsonParserAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScoreServiceIntegrationTest {

    private JsonParserAdapter jsonParser;
    private ClientePerfilMapper mapper;
    private ScoreService scoreService;
    private ScoreAuditPort auditPortMock;

    @BeforeEach
    void setUp() {
        // 1. Instanciando Infraestrutura de Entrada e DTO
        jsonParser = new JsonParserAdapter();
        mapper = new ClientePerfilMapper();

        // 2. Instanciando Domínio e Regras
        ModeloScoreV1 modeloScore = new ModeloScoreV1();
        ScoreCalculator pfCalculator = new PessoaFisicaScoreCalculator(modeloScore, new PessoaFisicaRuleConfig());
        ScoreCalculator pjCalculator = new PessoaJuridicaScoreCalculator(modeloScore, new PessoaJuridicaRuleConfig());

        // 3. Criando um Mock da Porta de Auditoria (Isola o teste do PostgreSQL)
        auditPortMock = Mockito.mock(ScoreAuditPort.class);

        // 4. Instanciando Orquestrador com o novo contrato
        scoreService = new ScoreService(new PerfilValidator(), List.of(pfCalculator, pjCalculator), auditPortMock);
    }

    private String carregarJson(String path) {
        InputStream is = getClass().getResourceAsStream(path);
        assertNotNull(is, "Arquivo JSON não encontrado no classpath: " + path);
        try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        }
    }

    @Test
    void deveProcessarFluxoCompletoParaPessoaFisica() {
        // Arrange
        String jsonPayload = carregarJson("/examples/cliente-pf.json");
        ClientePerfilDto dto = jsonParser.parse(jsonPayload);
        ClientePerfil perfil = mapper.toDomain(dto);
        String correlationId = UUID.randomUUID().toString();

        // Act
        ScoreResultado resultado = scoreService.processarScore(perfil, correlationId);

        // Assert: Garantir que o resultado é determinístico
        assertNotNull(resultado);
        assertEquals("CLI-PF-001", resultado.clienteId());
        assertEquals(TipoPessoa.PF, resultado.tipoPessoa());
        assertTrue(resultado.scoreFinal() > 0 && resultado.scoreFinal() <= 1000);

        // Assert: Garantir que a auditoria foi chamada exatamente 1 vez com o ID
        // correto
        Mockito.verify(auditPortMock, Mockito.times(1))
                .registrar(Mockito.any(ScoreResultado.class), Mockito.eq(correlationId), Mockito.anyLong());
    }

    @Test
    void deveProcessarFluxoCompletoParaPessoaJuridica() {
        // Arrange
        String jsonPayload = carregarJson("/examples/cliente-pj.json");
        ClientePerfilDto dto = jsonParser.parse(jsonPayload);
        ClientePerfil perfil = mapper.toDomain(dto);
        String correlationId = UUID.randomUUID().toString();

        // Act
        ScoreResultado resultado = scoreService.processarScore(perfil, correlationId);

        // Assert
        assertNotNull(resultado);
        assertEquals("CLI-PJ-999", resultado.clienteId());
        assertEquals(TipoPessoa.PJ, resultado.tipoPessoa());

        // Assert: Garantir que a auditoria foi acionada
        Mockito.verify(auditPortMock, Mockito.times(1))
                .registrar(Mockito.any(ScoreResultado.class), Mockito.eq(correlationId), Mockito.anyLong());
    }
}