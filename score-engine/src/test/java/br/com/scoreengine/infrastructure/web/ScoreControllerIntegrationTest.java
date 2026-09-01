package br.com.scoreengine.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ScoreControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveNegarAcessoSemTokenJwt() throws Exception {
        String payloadJson = "{\"clienteId\":\"CLI-999\",\"tipoPessoa\":\"PF\"}";

        mockMvc.perform(post("/api/v1/score")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirAcessoComTokenJwtValido() throws Exception {
        String payloadJson = "{\"clienteId\":\"CLI-999\",\"tipoPessoa\":\"PF\",\"rendaMensalLiquida\":5000.00,\"quantidadeAtrasos\":0,\"endividamento\":0.15,\"tempoRelacionamentoMeses\":12,\"idade\":30,\"estadoCivil\":\"Solteiro\",\"tempoUltimoEmpregoMeses\":12,\"possuiAvalista\":false}";

        mockMvc.perform(post("/api/v1/score")
                .with(jwt()) // Simula um token JWT válido injetado pelo Spring Security Resource Server
                .header("X-Correlation-Id", "test-corr-id-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                .andExpect(status().isOk());
    }
}