package br.com.scoreengine.interfaces.rest.controller;

import br.com.scoreengine.application.usecase.CalculatePFScoreUseCase;
import br.com.scoreengine.domain.model.common.ScoreComponent;
import br.com.scoreengine.domain.model.common.ScoreResult;
import br.com.scoreengine.interfaces.rest.mapper.ScorePFDtoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração do controlador REST de Score de Crédito PF.
 * Valida os contratos HTTP, deserialização de payload, conformidade
 * com parâmetros de risco prudencial e explicabilidade regulatória (Bacen /
 * Basileia / CDC).
 */
@WebMvcTest(ScorePFController.class)
@AutoConfigureMockMvc(addFilters = false)
class ScorePFControllerIT {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private CalculatePFScoreUseCase calculatePFScoreUseCase;

        @MockBean
        private ScorePFDtoMapper mapper;

        @Test
        @DisplayName("Deve processar requisição válida com sucesso retornando componentes de score e métricas prudenciais")
        void testCalculoSucesso() throws Exception {
                List<ScoreComponent> componentes = List.of(
                                new ScoreComponent(
                                                "Histórico de Pagamento",
                                                new BigDecimal("850.00"),
                                                new BigDecimal("1000.00"),
                                                new BigDecimal("0.35"),
                                                "Pontualidade integral na liquidação dos compromissos financeiros."));

                ScoreResult mockResult = new ScoreResult(785, "v1.1.0", componentes);

                when(mapper.toDomain(any())).thenCallRealMethod();
                when(calculatePFScoreUseCase.execute(any())).thenReturn(mockResult);
                when(mapper.toResponse(any())).thenCallRealMethod();

                String payload = """
                                {
                                    "cpf": "12345678901",
                                    "rendaMensal": 5000.00,
                                    "dividaTotal": 1000.00,
                                    "idade": 30,
                                    "estadoCivil": "SOLTEIRO",
                                    "numeroDependentes": 0,
                                    "diasAtrasoUltimos12Meses": 0,
                                    "limiteRotativoUtilizado": 200.00,
                                    "limiteRotativoTotal": 2000.00,
                                    "mesesNoEmpregoAtual": 24,
                                    "mesesRelacionamentoBanco": 36
                                }
                                """;

                mockMvc.perform(post("/api/v1/score/pf")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.scoreFinal").value(785))
                                .andExpect(jsonPath("$.faixaRisco").value("EXCELENTE"))
                                .andExpect(jsonPath("$.probabilidadeDefault").exists())
                                .andExpect(jsonPath("$.modelVersion").value("v1.1.0"))
                                .andExpect(jsonPath("$.componentes[0].nome").value("Histórico de Pagamento"))
                                .andExpect(jsonPath("$.componentes[0].pontuacao").value(850.00))
                                .andExpect(jsonPath("$.componentes[0].pontuacaoMaxima").value(1000.00))
                                .andExpect(jsonPath("$.componentes[0].pesoPonderado").value(0.35))
                                .andExpect(jsonPath("$.componentes[0].motivo").exists());
        }

        @Test
        @DisplayName("Deve rejeitar payload com parâmetros inválidos (HTTP 400)")
        void testValidacaoPayloadInvalido() throws Exception {
                String payloadInvalido = """
                                {
                                    "cpf": "123",
                                    "rendaMensal": -50.00
                                }
                                """;

                mockMvc.perform(post("/api/v1/score/pf")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadInvalido))
                                .andExpect(status().isBadRequest());
        }
}